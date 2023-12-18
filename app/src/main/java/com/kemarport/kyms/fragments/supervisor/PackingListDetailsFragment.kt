package com.kemarport.kyms.fragments.supervisor

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.kemarport.kyms.R
import com.kemarport.kyms.activities.FragmentHostActivity
import com.kemarport.kyms.adapters.PackingListDetailsAdapter
import com.kemarport.kyms.databinding.FragmentPackingListDetailsBinding
import com.kemarport.kyms.helper.Constants
import com.kemarport.kyms.helper.Resource
import com.kemarport.kyms.helper.SessionManager
import com.kemarport.kyms.models.CoilData.CoilData
import com.kemarport.kyms.models.vesselandintercarting.VesselAndIntercartingRequest
import com.kemarport.kyms.repository.KYMSRepository
import com.kemarport.kyms.viewmodel.VesselLoadingVMProviderFactory
import com.kemarport.kyms.viewmodel.VesselLoadingViewModel
import es.dmoral.toasty.Toasty
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class PackingListDetailsFragment : Fragment(), View.OnClickListener {
    private var _binding: FragmentPackingListDetailsBinding? = null
    private val TAG = "PackingListDetailFrag"
    private lateinit var packingListDetailsAdapter: PackingListDetailsAdapter
    private lateinit var viewModel: VesselLoadingViewModel
    private val barcodes = mutableListOf<String?>()
    private val scannedCoilDataList = mutableListOf<CoilData>()
    private var barcodeFoundCount = 0
    private var barcodePendingCount = 0
    private var barcodeTotalCount = 0
    private var scannedBarcode: String? = ""
    private val operationType = "New"
    private val args: PackingListDetailsFragmentArgs by navArgs()
    private var packingId: Int = 0
    private var totalCoils: Int = 0
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

    private val hatchLocations = mutableListOf(
        "1",
        "2",
        "3",
        "4",
        "5",
        "6",
        "7",
        "8"
    )

    var dischargePortList : ArrayList<String> = ArrayList()

    private var hatchNumber: Int = 0
    private lateinit var arrayAdapter: ArrayAdapter<String>
    private lateinit var dischargePortAdapter: ArrayAdapter<String>

    // This property is only valid between onCreateView and
// onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_packing_list_details,
                container,
                false
            )
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

        packingId = args.packingId
        totalCoils = args.totalRecord
        binding.tvFoundValue.text = "$barcodeFoundCount/$totalCoils"
        Log.d(TAG, "onViewCreated: $packingId")

        toolbar?.let {
            val titleTextView = it.findViewById<TextView>(R.id.tvToolbarTitle)
            //val dateAndShift = parseDateAndShift(receivedCreatedDate)
            titleTextView.text = "Packing List Details"
        }

        /*val berthNumbers = resources.getStringArray(R.array.berth_array)
        val arrayAdapter = ArrayAdapter(requireContext(), R.layout.item_ddm_berthno, berthNumbers)
        binding.autoCompleteTextView.setAdapter(arrayAdapter)*/

        binding.tvPendingValue.text = "$barcodePendingCount"
        binding.tvTotalValue.text = "$barcodeTotalCount"

        val kymsRepository = KYMSRepository()
        val application = (activity as FragmentHostActivity).application
        val viewModelProviderFactory =
            VesselLoadingVMProviderFactory(application, kymsRepository)
        viewModel =
            ViewModelProvider(this, viewModelProviderFactory)[VesselLoadingViewModel::class.java]

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
       // baseUrl=Constants.BASE_URL_NEW
        Log.d(TAG, "onCreate: $baseUrl")

        arrayAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, hatchLocations)
        arrayAdapter.setDropDownViewResource(R.layout.item_ddm_location)
        binding.spinnerLoc.adapter = arrayAdapter
        binding.spinnerLoc.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                hatchNumber = hatchLocations.indexOf(binding.spinnerLoc.selectedItem) + 1
                /*barcodes.clear()
                scannedCoilDataList.clear()
                packingListDetailsAdapter.updateCoilDataList(scannedCoilDataList)
                binding.tvEmptyList.visibility = View.VISIBLE*/
                Log.d(TAG, "onItemSelected: $hatchNumber")
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

        }

        dischargePortAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, dischargePortList)
        dischargePortAdapter.setDropDownViewResource(R.layout.item_ddm_location)
        binding.spinnerPort.adapter = dischargePortAdapter

        setupRecyclerView()

        viewModel.packingListDetails(jwtToken, baseUrl, packingId)
        viewModel.dischargePortList(jwtToken, baseUrl, packingId)
        viewModel.packingListDetailsMutableLiveData.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    response.data?.let { resultResponse ->
                        hideProgressBar()
                        Log.d(TAG, "currentScanningListResponse: $resultResponse")
                        if (scannedBarcode !in barcodes) {
                            if (resultResponse.isNotEmpty()) {
                                barcodes.add(resultResponse[i].batchNo)
                                scannedCoilDataList.clear()
                                for (i in resultResponse.indices) {
                                    scannedCoilDataList.add(
                                        CoilData(
                                            resultResponse[i].shipToPartyName,
                                            resultResponse[i].batchNo,
                                            resultResponse[i].portOfDischarge,
                                            resultResponse[i].productStatus,
                                            resultResponse[i].rakeRefNo
                                        )
                                    )
                                    unloadedBarcodesFromAPI.add(resultResponse[i].batchNo)
                                }
                                binding.tvEmptyList.visibility = View.GONE
                                packingListDetailsAdapter.updateCoilDataList(scannedCoilDataList)
                                barcodeFoundCount = scannedCoilDataList.size
                                binding.tvFoundValue.text = "$barcodeFoundCount/$totalCoils"
                                barcodeTotalCount =
                                    barcodeFoundCount + barcodePendingCount
                                binding.tvTotalValue.text = "$barcodeTotalCount"
                            }
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
        }

        viewModel.loadingOnVesselMutableLiveData.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    response.data?.let { resultResponse ->
                        hideProgressBar()
                        Log.d(
                            TAG,
                            "onViewCreated: ${resultResponse.statusMessage}"
                        )
                        when (resultResponse.statusMessage) {
                            "Success" -> {
                                (requireActivity() as FragmentHostActivity).setScannerAudio("success")
                                if (scannedBarcode !in barcodes) {
                                    barcodes.add(scannedBarcode)
                                    Toasty.success(
                                        activity as FragmentHostActivity,
                                        "${resultResponse.batchNumber} loaded in vessel successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    scannedCoilDataList.add(
                                        CoilData(
                                            resultResponse.currentASNScaningListResponses.shipToPartyName,
                                            scannedBarcode,
                                            resultResponse.currentASNScaningListResponses.portOfDischarge,
                                            resultResponse.productMessage,
                                            resultResponse.currentASNScaningListResponses.rakeRefNo
                                        )
                                    )
                                    packingListDetailsAdapter.updateCoilDataList(scannedCoilDataList)
                                    binding.tvEmptyList.visibility = View.GONE
                                    barcodeFoundCount = scannedCoilDataList.size
                                    binding.tvFoundValue.text = "$barcodeFoundCount/$totalCoils"
                                    barcodeTotalCount =
                                        barcodeFoundCount + barcodePendingCount
                                    binding.tvTotalValue.text = "$barcodeTotalCount"
                                }
                            }
                            else -> {
                                (requireActivity() as FragmentHostActivity).setScannerAudio("error")
                                (activity as FragmentHostActivity).showCustomDialog(
                                    resultResponse.batchNumber,
                                    resultResponse.productMessage
                                )
                            }
                        }
                    }
                }
                is Resource.Error -> {
                    response.message?.let { errorMessage ->
                        hideProgressBar()
                        Log.e(
                            TAG,
                            "Error : $errorMessage"
                        )
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

        }

        viewModel.dischargePortListMutableLiveData.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    response.data?.let { resultResponse ->
                        hideProgressBar()
                        if (resultResponse.size>0) {
                            dischargePortList.addAll(resultResponse)
                            dischargePortAdapter.notifyDataSetChanged()
                        }
                        else{
                            (requireActivity() as FragmentHostActivity).showCustomDialog(
                                "Please Try Again!",
                                "No Port of discharge found!"
                            )
                        }
                    }
                }
                is Resource.Error -> {
                    response.message?.let { message ->
                        hideProgressBar()
                        Log.e(TAG, "dischargePortListResponse: $message")
                    }
                }
                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        }


        if (Build.MANUFACTURER.contains("Zebra Technologies") || Build.MANUFACTURER.contains("Motorola Solutions")) {
            binding.btnSetBarcode.visibility = View.GONE
        }

        binding.floatingActionButton.setOnClickListener {
            (requireActivity() as FragmentHostActivity).showSteelSlabDialog(this)
        }
    }

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

    override fun onResume() {
        super.onResume()

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
        hatchNumber = hatchLocations.indexOf(binding.spinnerLoc.selectedItem) + 1
        val user = session.getUserDetails()
        val firstName = user["firstName"]
        val lastName = user["lastName"]
        val jwtToken = user["jwtToken"]
        val refreshToken = user["refreshToken"]
        val email = user["email"]
        Log.d(TAG, "setBarcode: $firstName & $lastName & $jwtToken & $refreshToken & $email")
        Log.d(TAG, "setBarcode: location after scanning - $hatchNumber")

        var dischargePort=binding.spinnerPort.selectedItem

        (activity as FragmentHostActivity).runOnUiThread {
            val vesselAndIntercartingRequest =
                VesselAndIntercartingRequest(scannedBarcode, hatchNumber.toString(), operationType,packingId,dischargePort.toString())
            viewModel.loadingOnVessel(jwtToken, vesselAndIntercartingRequest, baseUrl)
        }
    }

    private fun setupRecyclerView() {
        Log.d(TAG, "setupRecyclerView: called")
        packingListDetailsAdapter = PackingListDetailsAdapter()
        binding.rvEdiDetails.apply {
            adapter = packingListDetailsAdapter
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
}