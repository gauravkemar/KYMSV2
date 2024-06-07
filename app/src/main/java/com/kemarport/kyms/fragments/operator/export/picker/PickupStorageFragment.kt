package com.kemarport.kyms.fragments.operator.export.picker

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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.kemarport.kyms.R
import com.kemarport.kyms.activities.FragmentHostActivity
import com.kemarport.kyms.adapters.export.EdiDetailsAdapter
import com.kemarport.kyms.databinding.FragmentPickupStorageBinding
import com.kemarport.kyms.helper.Constants
 import com.kemarport.kyms.helper.Resource
import com.kemarport.kyms.helper.SessionManager
import com.kemarport.kyms.models.export.CoilData.CoilData
import com.kemarport.kyms.models.export.generalrequestandresponse.GeneralRequestBerthLocation
import com.kemarport.kyms.models.export.upload.Coil
import com.kemarport.kyms.models.export.upload.CoilRequest
import com.kemarport.kyms.repository.KYMSRepository
import com.kemarport.kyms.viewmodel.export.PickupStorageVMProviderFactory
import com.kemarport.kyms.viewmodel.export.PickupStorageViewModel
import es.dmoral.toasty.Toasty

class PickupStorageFragment : Fragment(), View.OnClickListener {
    private var _binding: FragmentPickupStorageBinding? = null
    private val TAG = "PickupStorageFragment"
    private lateinit var viewModel: PickupStorageViewModel
    private lateinit var arrayAdapter: ArrayAdapter<String>
    private val berthLocations = mutableListOf(Constants.BERTH_LOCATION_1)
    private var scannedBarcode: String? = ""
    private lateinit var ediDetailsAdapter: EdiDetailsAdapter
    private lateinit var berthLocation: String
    private val operationType = "Picked"
    private val barcodes = mutableListOf<String?>()
    private val scannedCoilDataList = mutableListOf<CoilData>()
    private val coilList = mutableListOf<Coil?>()
    private lateinit var coilRequest: CoilRequest
    private var serverIpSharedPrefText: String? = null
    private var portSharedPrefText: Int? = 0
    private lateinit var user: HashMap<String, String?>
    private lateinit var session: SessionManager
    private lateinit var baseUrl: String

    // This property is only valid between onCreateView and
// onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_pickup_storage, container, false)
        val view = binding.root
        binding.listener = this

        val toolbar =
            (activity as FragmentHostActivity).findViewById<Toolbar>(R.id.tbFragmentHostActivity)
        toolbar?.let {
            val titleTextView = it.findViewById<TextView>(R.id.tvToolbarTitle)
            titleTextView.text = "Pickup from Storage"
            it.setNavigationIcon(R.drawable.ic_back_icon)
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Toasty.Config.getInstance()
            .setGravity(Gravity.CENTER)
            .apply() // required

        val kymsRepository = KYMSRepository()
        val application = (activity as FragmentHostActivity).application
        val viewModelProviderFactory =
            PickupStorageVMProviderFactory(application, kymsRepository)
        viewModel =
            ViewModelProvider(this, viewModelProviderFactory)[PickupStorageViewModel::class.java]

        session = SessionManager(requireContext())
        user = session.getAdminDetails()
        serverIpSharedPrefText = user["serverIp"]
        portSharedPrefText = user["port"]?.toInt()
        baseUrl = if (serverIpSharedPrefText == "192.168.1.52") {
            "http://$serverIpSharedPrefText:$portSharedPrefText/api/"
        } else {
            "http://$serverIpSharedPrefText:$portSharedPrefText/service/api/"
        }
        //baseUrl=Constants.BASE_URL_NEW
        Log.d(TAG, "onCreate: $baseUrl")

        arrayAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, berthLocations)
        arrayAdapter.setDropDownViewResource(R.layout.item_ddm_location)
        binding.spinnerLoc.adapter = arrayAdapter
        binding.spinnerLoc.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                berthLocation = binding.spinnerLoc.selectedItem.toString()
                barcodes.clear()
//                coilList.clear()
                scannedCoilDataList.clear()
                ediDetailsAdapter.updateCoilDataList(scannedCoilDataList)
                binding.tvEmptyList.visibility = View.VISIBLE
//                binding.btnPickFromStorage.visibility = View.GONE
                Log.d(TAG, "onItemSelected: $berthLocation")
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

        }

        setupRecyclerView()

        /*viewModel.validationStockToBerthMutableLiveData.observe(
            viewLifecycleOwner,
            Observer { response ->
                when (response) {
                    is Resource.Success -> {
                        var coil: Coil?
                        response.data?.let { resultResponse ->
                            hideProgressBar()
                            Log.d(
                                TAG,
                                "onViewCreated: Pickup Storage Barcode Status Success ${resultResponse.statusMessage}"
                            )

                            when (resultResponse.statusMessage) {
                                "Success" -> {
                                    if (scannedBarcode !in barcodes) {
                                        barcodes.add(scannedBarcode)
                                        scannedCoilDataList.add(
                                            CoilData(
                                                resultResponse.currentASNScaningListResponses.shipToPartyName,
                                                scannedBarcode,
                                                resultResponse.currentASNScaningListResponses.jswGrade,
                                                resultResponse.productMessage,
                                                resultResponse.currentASNScaningListResponses.rakeRefNo
                                            )
                                        )
                                        binding.tvEmptyList.visibility = View.GONE
                                        binding.btnPickFromStorage.visibility = View.VISIBLE

                                        coil = Coil(scannedBarcode)
                                        coilList.add(coil)
                                        coilRequest =
                                            CoilRequest(berthLocation, operationType, coilList)
                                        Log.d(TAG, "setBarcode: $coilRequest")
                                    }
                                }
                                "Not Added In Packing List" -> {
                                    Toasty.warning(
                                        activity as FragmentHostActivity,
                                        resultResponse.statusMessage
                                    ).show()
                                }
                                "Not Found" -> {
                                    Toasty.error(requireContext(), resultResponse.statusMessage)
                                        .show()
                                }
                                "Already Exist" -> {
                                    Toasty.warning(requireContext(), resultResponse.statusMessage)
                                        .show()
                                }
                            }
                        }
                    }
                    is Resource.Error -> {
                        response.message?.let { errorMessage ->
                            hideProgressBar()
                            Log.e(
                                TAG,
                                "onViewCreated: Pickup Storage Barcode Status Error $errorMessage"
                            )
                        }
                    }
                    is Resource.Loading -> {
                        showProgressBar()
                    }
                }

                ediDetailsAdapter.updateCoilDataList(scannedCoilDataList)
            })

        viewModel.stockToBerthMutableLiveData.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    response.data?.let { resultResponse ->
                        hideProgressBar()
                        Log.d(
                            TAG,
                            "onViewCreated: ${resultResponse.responseMessage}"
                        )

                        when (resultResponse.responseMessage) {
                            "All Product loaded at Birth Sucessfully" -> {
                                binding.btnPickFromStorage.visibility = View.GONE
                                Toasty.success(
                                    activity as FragmentHostActivity,
                                    "All products picked from storage successfully"
                                ).show()
                                barcodes.clear()
                                coilList.clear()
                                scannedCoilDataList.clear()
                                ediDetailsAdapter.updateCoilDataList(scannedCoilDataList)
                                binding.tvEmptyList.visibility = View.VISIBLE
                                binding.btnPickFromStorage.visibility = View.GONE
                            }
                            else -> {

                            }
                        }
                    }
                }
                is Resource.Error -> {
                    response.message?.let { errorMessage ->
                        hideProgressBar()
                        Log.e(
                            TAG,
                            "onViewCreated: Stock to berth success response $errorMessage"
                        )
                    }
                }
                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        })*/

        viewModel.pickCoilFromStockMutableLiveData.observe(
            viewLifecycleOwner,
            Observer { response ->
                when (response) {
                    is Resource.Success -> {
                        response.data?.let { resultResponse ->
                            hideProgressBar()
                            when (resultResponse.statusMessage) {
                                "Success" -> {
                                    (requireActivity() as FragmentHostActivity).setScannerAudio("success")
                                    if (scannedBarcode !in barcodes) {
                                        barcodes.add(scannedBarcode)
                                        Toasty.success(
                                            activity as FragmentHostActivity,
                                            "${resultResponse.batchNumber} picked from stock successfully",
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
                                        ediDetailsAdapter.updateCoilDataList(scannedCoilDataList)
                                        binding.tvEmptyList.visibility = View.GONE
                                    }
                                }
                                else->
                                {
                                    (requireActivity() as FragmentHostActivity).setScannerAudio("error")
                                    (activity as FragmentHostActivity).showCustomDialog(
                                        resultResponse.batchNumber,
                                        resultResponse.productMessage
                                    )
                                }
                                /*"Invalid" -> {
                                    (requireActivity() as FragmentHostActivity).setScannerAudio("error")
                                    when (resultResponse.productMessage) {
                                        "Not Found" -> {
                                            (activity as FragmentHostActivity).showCustomDialog(
                                                "Invalid Batch Number ${resultResponse.batchNumber}",
                                                ""
                                            )
                                        }
                                        "Not Found Packing List" -> {
                                            (activity as FragmentHostActivity).showCustomDialog(
                                                "${resultResponse.batchNumber} is not in packing list yet",
                                                ""
                                            )
                                        }
                                        "Already Picked from stock." -> {
                                            (activity as FragmentHostActivity).showCustomDialog(
                                                "${resultResponse.batchNumber} already picked from stock",
                                                ""
                                            )
                                        }
                                        "It is an intransit product and cannot be picked from stock" -> {
                                            (activity as FragmentHostActivity).showCustomDialog(
                                                "",
                                                "${resultResponse.batchNumber} is an intransit product and cannot be picked from stock"
                                            )
                                        }
                                        "Marked For BTT." -> {
                                            (activity as FragmentHostActivity).showCustomDialog(
                                                "",
                                                "${resultResponse.batchNumber} is Marked For BTT."
                                            )
                                        }
                                    }
                                }*/
                            }
                        }
                    }
                    is Resource.Error -> {
                        response.message?.let { errorMessage ->
                            hideProgressBar()
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
            })

        binding.floatingActionButton.setOnClickListener {
            (requireActivity() as FragmentHostActivity).showSteelSlabDialog(this)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onClick(view: View?) {
        view?.let {
            when (it.id) {
                /*binding.btnPickFromStorage.id -> {
                    viewModel.stockToBerth(coilRequest)
                }*/
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
        berthLocation = binding.spinnerLoc.selectedItem.toString()
        val user = session.getUserDetails()
        val firstName = user["firstName"]
        val lastName = user["lastName"]
        val jwtToken = user["jwtToken"]
        val refreshToken = user["refreshToken"]
        val email = user["email"]
        Log.d(TAG, "setBarcode: $firstName & $lastName & $jwtToken & $refreshToken & $email")
        Log.d(TAG, "setBarcode: berth location - $berthLocation")
        (activity as FragmentHostActivity).runOnUiThread {
            val generalRequestBerthLocation =
                GeneralRequestBerthLocation(
                    berthLocation,
                    scannedBarcode
                )
            viewModel.pickCoilFromStock(jwtToken, generalRequestBerthLocation, baseUrl)
        }
    }

    private fun setupRecyclerView() {
        Log.d(TAG, "setupRecyclerView: called")
        ediDetailsAdapter = EdiDetailsAdapter()
        binding.rvPickStorage.apply {
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
}