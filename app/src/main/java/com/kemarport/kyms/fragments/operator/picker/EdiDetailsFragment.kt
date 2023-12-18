package com.kemarport.kyms.fragments.operator.picker

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.kemarport.kyms.R
import com.kemarport.kyms.activities.FragmentHostActivity
import com.kemarport.kyms.adapters.EdiDetailsAdapter
import com.kemarport.kyms.databinding.FragmentEdiDetailsBinding
import com.kemarport.kyms.helper.Constants
import com.kemarport.kyms.helper.Constants.Companion.MODE_TRAIN
import com.kemarport.kyms.helper.Constants.Companion.MODE_TRUCK
import com.kemarport.kyms.helper.Resource
import com.kemarport.kyms.helper.SessionManager
import com.kemarport.kyms.models.CoilData.CoilData
import com.kemarport.kyms.repository.KYMSRepository
import com.kemarport.kyms.viewmodel.EdiDetailsVMProviderFactory
import com.kemarport.kyms.viewmodel.EdiDetailsViewModel
import es.dmoral.toasty.Toasty
import java.text.SimpleDateFormat
import java.util.*


class EdiDetailsFragment : Fragment(), View.OnClickListener {
    private var _binding: FragmentEdiDetailsBinding? = null
    private val TAG = "EdiDetailsFragment"
    private lateinit var ediDetailsAdapter: EdiDetailsAdapter
    lateinit var viewModel: EdiDetailsViewModel
    private val barcodes = mutableListOf<String?>()
    private val scannedCoilDataList = mutableListOf<CoilData>()
    private var barcodeFoundCount = 0
    private var barcodePendingCount = 0
    private var barcodeTotalCount = 0
    private var scannedBarcode: String? = ""
    private val args: EdiDetailsFragmentArgs by navArgs()
    private var jobId: Int = 0
    private lateinit var rakeRefNo: String
    private lateinit var receivedCreatedDate: String
    private var totalCoils: Int = 0
    private lateinit var createdDate: String
    private lateinit var transportationMode: String
    private var i = 0
    private val unloadedBarcodesFromAPI = mutableListOf<String>()
    private lateinit var toolbar: Toolbar
    private var serverIpSharedPrefText: String? = null
    private var jwtToken: String? = null
    private var portSharedPrefText: Int? = 0
    private lateinit var adminUser: HashMap<String, String?>
    private lateinit var user: HashMap<String, String?>
    private lateinit var session: SessionManager
    private lateinit var baseUrl: String
    private var demoBarcodes: MutableList<String?> = mutableListOf(
        "22AS30020",
        "M967990000",
        "M968210000",
        "M968250000",
        "M968821000"
    )

    // This property is only valid between onCreateView and
// onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_edi_details, container, false)
        val view = binding.root
        binding.listener = this

        toolbar =
            (activity as FragmentHostActivity).findViewById<Toolbar>(R.id.tbFragmentHostActivity)
        toolbar.let {
            val titleTextView = it.findViewById<TextView>(R.id.tvToolbarTitle)
            titleTextView.text = getString(R.string.app_name)
            it.setNavigationIcon(R.drawable.ic_back_icon)
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Toasty.Config.getInstance()
            .setGravity(Gravity.CENTER)
            .apply() // required

        jobId = args.jobId
        rakeRefNo = args.rakeCode
        receivedCreatedDate = args.createdDate
        transportationMode = args.source
        totalCoils = args.totalCoils
        binding.tvFoundValue.text = "$barcodeFoundCount/$totalCoils"
        Log.d(TAG, "onViewCreated: $jobId and $rakeRefNo")

        toolbar?.let {
            val titleTextView = it.findViewById<TextView>(R.id.tvToolbarTitle)
            val dateAndShift = parseDateAndShift(receivedCreatedDate)
            titleTextView.text = "$dateAndShift"
        }

        /*val berthNumbers = resources.getStringArray(R.array.berth_array)
        val arrayAdapter = ArrayAdapter(requireContext(), R.layout.item_ddm_berthno, berthNumbers)
        binding.autoCompleteTextView.setAdapter(arrayAdapter)*/

        binding.tvPendingValue.text = "$barcodePendingCount"
        binding.tvTotalValue.text = "$barcodeTotalCount"

        val kymsRepository = KYMSRepository()
        val application = (activity as FragmentHostActivity).application
        val viewModelProviderFactory =
            EdiDetailsVMProviderFactory(application, kymsRepository)
        viewModel =
            ViewModelProvider(this, viewModelProviderFactory)[EdiDetailsViewModel::class.java]

        session = SessionManager(requireContext())
        adminUser = session.getAdminDetails()
        user = session.getUserDetails()
        serverIpSharedPrefText = adminUser["serverIp"]
        portSharedPrefText = adminUser["port"]?.toInt()
        jwtToken = user["jwtToken"]
        baseUrl = if (serverIpSharedPrefText == "192.168.1.52") {
            "http://$serverIpSharedPrefText:$portSharedPrefText/api/"
        } else {
            "http://$serverIpSharedPrefText:$portSharedPrefText/service/api/"
        }
        //baseUrl=Constants.BASE_URL_NEW
        Log.d(TAG, "onCreate: $baseUrl")

        setupRecyclerView()

        /*viewModel.getEdiDetailsData(jobId, 0, 1, "")
        viewModel.ediDetailsMutableLiveData.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let { resultResponse ->
                        Log.d(TAG, "$resultResponse")
                        val item = resultResponse[0]
                        totalCoils = item.jobDetailsCount
                        createdDate = item.createdDate
                    }
                }
                is Resource.Error -> {
                    response.message.let { message ->
                        Log.e(TAG, "onViewCreated: $message")
                    }
                }
                is Resource.Loading -> {

                }
            }
        })*/

        viewModel.getCurrentScanningList(jobId, baseUrl, jwtToken)
        viewModel.currentScanningMutableLiveData.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    response.data?.let { resultResponse ->
                        hideProgressBar()
                        Log.d(TAG, "currentScanningListResponse: $resultResponse")
                        if (resultResponse.isNotEmpty()) {
                            scannedCoilDataList.clear()
                            for (i in resultResponse.indices) {
                                scannedCoilDataList.add(
                                    CoilData(
                                        resultResponse[i].shipToPartyName,
                                        resultResponse[i].batchNo,
                                        resultResponse[i].portOfDischarge,
                                        resultResponse[i].productStatus,
                                        rakeRefNo
                                    )
                                )
                                unloadedBarcodesFromAPI.add(resultResponse[i].batchNo)
                            }
                            binding.tvEmptyList.visibility = View.GONE
                            ediDetailsAdapter.updateCoilDataList(scannedCoilDataList)
                            barcodeFoundCount = scannedCoilDataList.size
                            binding.tvFoundValue.text = "$barcodeFoundCount/$totalCoils"
                            barcodeTotalCount =
                                barcodeFoundCount + barcodePendingCount
                            binding.tvTotalValue.text = "$barcodeTotalCount"
                        }
                    }
                }
                is Resource.Error -> {
                    response.message?.let { message ->
                        hideProgressBar()
                        Log.e(TAG, "currentScanningListResponse: $message")
                    }
                }
                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        })

        viewModel.unloadingCoilMutableLiveData.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let { resultResponse ->
                        Log.d(
                            TAG,
                            "setItemScanningStatus: Success Message ${resultResponse.statusMessage}"
                        )

                        when (resultResponse.statusMessage) {
                            "Active" -> {
                                (requireActivity() as FragmentHostActivity).setScannerAudio("success")
                                if (scannedBarcode !in barcodes && scannedBarcode !in unloadedBarcodesFromAPI) {
                                    barcodes.add(scannedBarcode)
                                    binding.tvEmptyList.visibility = View.GONE
                                    scannedCoilDataList.add(
                                        CoilData(
                                            resultResponse.currentASNScaningListResponses.shipToPartyName,
                                            resultResponse.currentASNScaningListResponses.batchNo,
                                            resultResponse.currentASNScaningListResponses.jswGrade,
                                            resultResponse.productMessage,
                                            rakeRefNo
                                        )
                                    )
                                    Toasty.success(
                                        activity as FragmentHostActivity,
                                        "${resultResponse.batchNumber} unloaded successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    if (resultResponse.productMessage == "Pending") {
                                        barcodePendingCount++
                                        binding.tvPendingValue.text = "$barcodePendingCount"
                                    } else {
                                        barcodeFoundCount++
                                        binding.tvFoundValue.text = "$barcodeFoundCount/$totalCoils"
                                    }
                                    Log.d(
                                        TAG,
                                        "setItemScanningStatus: scannedCoilDataListSize = ${scannedCoilDataList.size}"
                                    )
                                } else {

                                }
                                scannedBarcode = ""
                            }
                            else->
                            {
                                (requireActivity() as FragmentHostActivity).setScannerAudio("error")
                                (activity as FragmentHostActivity).showCustomDialog(
                                    resultResponse.batchNumber,
                                    resultResponse.statusMessage
                                )
                            }
                            /*"Already Exist" -> {
                                (requireActivity() as FragmentHostActivity).setScannerAudio("error")
                                (activity as FragmentHostActivity).showCustomDialog(
                                    "",
                                    resultResponse.batchNumber + " " +resultResponse.statusMessage
                                )
                            }
                            "Not Found" -> {
                                (requireActivity() as FragmentHostActivity).setScannerAudio("error")
                                (activity as FragmentHostActivity).showCustomDialog(
                                    "",
                                    "<b>$scannedBarcode</b> does not exist in current EDI"
                                )
                            }*/
                        }
                    }
                }
                is Resource.Error -> {
                    response.message?.let { errorMessage ->
                        hideProgressBar()
                        Log.e(TAG, "setItemScanningStatus: Error Message $errorMessage")
                        /*if (this.data !in barcodes && this.data !in unloadedBarcodesFromAPI) {
                            barcodes.add(this.data)
                            binding.tvEmptyList.visibility = View.GONE
                            scannedCoilDataList.add(
                                CoilData(
                                    "Invalid",
                                    this.data,
                                    "Invalid",
                                    errorMessage,
                                    "Invalid"
                                )
                            )
                            barcodeNotFoundCount++
                            binding.tvNotFoundValue.text = "$barcodeNotFoundCount"
                            Log.d(
                                TAG,
                                "setItemScanningStatus: scannedCoilDataListSize = ${scannedCoilDataList.size}"
                            )
                        } else {*/
                        /*}
                        this.data = ""*/
                        if (errorMessage == "Unauthorized" || errorMessage == "Authentication token expired" || errorMessage == Constants.CONFIG_ERROR) {
                            (requireActivity() as FragmentHostActivity).showCustomDialog(
                                "Session Expired",
                                "Please re-login to continue"
                            )
                        }
                    }
                }
                is Resource.Loading -> {
                    showProgressBar()
                }
            }

            barcodeTotalCount = barcodeFoundCount + barcodePendingCount
            binding.tvTotalValue.text = "$barcodeTotalCount"
            ediDetailsAdapter.updateCoilDataList(scannedCoilDataList)
        })

        if (Build.MANUFACTURER.contains("Zebra Technologies") || Build.MANUFACTURER.contains("Motorola Solutions")) {
            binding.btnSetBarcode.visibility = View.GONE
        }

        binding.floatingActionButton.setOnClickListener {
            (requireActivity() as FragmentHostActivity).showSteelSlabDialog(this)
        }
    }

//    private fun showDialog() {
//        var alertDialog: AlertDialog? = null
//        val builder = AlertDialog.Builder(activity as FragmentHostActivity)
//            .setTitle("Invalid Batch No")
//            .setMessage("Scanned barcode does not exist in current EDI")
//            .setIcon(android.R.drawable.ic_dialog_alert)
//            .setPositiveButton("Okay") { dialogInterface, which ->
//                alertDialog?.dismiss()
//            }
//        alertDialog = builder.create()
//        alertDialog.setCancelable(true)
//        alertDialog.show()
//    }

    private fun parseDateAndShift(receivedCreatedDate: String): String {
        Log.d(TAG, "parseDate: $receivedCreatedDate")
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val formatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

        val timeArray = receivedCreatedDate.split("T")
        val time = timeArray[1]
        val timeInMinutesArray = time.split(":")
        val timeInMinutesString = timeInMinutesArray[0]
        var shift = ""
        shift = if (timeInMinutesString.toInt() in 7..12 || timeInMinutesString.toInt() in 1..2) {
            "A"
        } else if (timeInMinutesString.toInt() in 15..22) {
            "B"
        } else {
            "C"
        }
        Log.d(TAG, "parseDate: $shift")

        return "${
            parser.parse(receivedCreatedDate)?.let {
                formatter.format(it)
            }
        } ($shift)"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onClick(view: View?) {
        view?.let {
            when (it.id) {
                binding.btnSetBarcode.id -> {
                    if (i == demoBarcodes.size) {
                        i = 0
                    }
                    setBarcode(demoBarcodes[i])
                    i++
                }
                else -> {

                }
            }
        }
    }

    fun setBarcode(data: String?) {
        Log.e(TAG, "$data")
        data?.let {
            scannedBarcode = it.split(" ")[0].trim()
            Log.e(TAG, scannedBarcode!!)
        }
        val user = session.getUserDetails()
        val firstName = user["firstName"]
        val lastName = user["lastName"]
        val jwtToken = user["jwtToken"]
        val refreshToken = user["refreshToken"]
        val email = user["email"]
        Log.d(TAG, "setBarcode: $firstName & $lastName & $jwtToken & $refreshToken & $email")

        (activity as FragmentHostActivity).runOnUiThread {
            if (transportationMode == MODE_TRAIN) {
                viewModel.unloadingCoil(jwtToken, scannedBarcode, rakeRefNo, jobId, baseUrl)
            } else if (transportationMode == MODE_TRUCK) {

            }

            binding.rvEdiDetails.smoothScrollToPosition(scannedCoilDataList.size)
        }
    }

    private fun setupRecyclerView() {
        Log.d(TAG, "setupRecyclerView: called")
        ediDetailsAdapter = EdiDetailsAdapter()
        binding.rvEdiDetails.apply {
            adapter = ediDetailsAdapter
            layoutManager = LinearLayoutManager(activity).apply {
                stackFromEnd = true
                reverseLayout = true
            }
        }
    }

    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        binding.progressBar.visibility = View.INVISIBLE
    }

    /*private fun setItemScanningStatus(data: String?) {

    }*/

    /*private fun setItemScanningStatus(data: String?) {
        var statusMessage = ""
        val coilData = CoilData(1, "Rajesh", statusMessage, data, R.color.grey)
        viewModel.itemScanningMutableLiveData.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    response.data?.let { resultResponse ->
                        statusMessage = resultResponse.statusMessage
                        Log.d(TAG, "getItemScanningStatus: $statusMessage")
                        coilData.status = statusMessage
                        coilData.statusColor = R.color.white
                        if (!scannedCoilDataList.contains(coilData)) {
                            barcodeFoundCount++
                            binding.tvFoundValue.text = "$barcodeFoundCount/${barcodesFromApi.size}"
                            scannedCoilDataList.add(coilData)
                        }
                    }
                }
                is Resource.Error -> {
                    response.message?.let { errorMessage ->
                        statusMessage = errorMessage
                        Log.d(TAG, "getItemScanningStatus: $statusMessage")
                        coilData.status = statusMessage
                        coilData.statusColor = R.color.md_theme_errorContainer
                        if (!scannedCoilDataList.contains(coilData)) {
                            barcodeNotFoundCount++
                            binding.tvNotFoundValue.text = "$barcodeNotFoundCount"
                            scannedCoilDataList.add(coilData)
                        }
                    }
                }
                is Resource.Loading -> {

                }
            }

            barcodeTotalCount = barcodeFoundCount + barcodeNotFoundCount + barcodePendingCount
            binding.tvTotalValue.text = "$barcodeTotalCount"
            ediDetailsAdapter.updateCoilDataList(scannedCoilDataList)
        })
    }*/

    /*private fun showSteelSlabDialog() {
        val alertDialogBuilder = AlertDialog.Builder(requireContext())

        val params = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(20)

        val tv = TextView(requireContext())
        tv.text = "Enter Steel Slab Module Number"
        tv.setPadding(40, 40, 40, 40)
        tv.gravity = Gravity.CENTER
        tv.textSize = 20f
        tv.setTextColor(resources.getColor(R.color.black))

        val et = EditText(requireContext())
        et.layoutParams = params

        alertDialogBuilder.setView(et)
        alertDialogBuilder.setTitle(tv.text)
        alertDialogBuilder.setMessage("Input Student ID")
        alertDialogBuilder.setCustomTitle(tv)
        // alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setCancelable(false)

        // Setting Negative "Cancel" Button
        alertDialogBuilder.setNegativeButton("CANCEL",
            DialogInterface.OnClickListener { dialog, whichButton -> dialog.cancel() })

        // Setting Positive "Yes" Button
        alertDialogBuilder.setPositiveButton("ADD",
            DialogInterface.OnClickListener { dialog, which ->
                setBarcode(et.text.toString())
            })

        val alertDialog: AlertDialog = alertDialogBuilder.create()

        try {
            alertDialog.show()
        } catch (e: Exception) {
            // WindowManager$BadTokenException will be caught and the app would
            // not display the 'Force Close' message
            e.printStackTrace()
        }
    }*/
}