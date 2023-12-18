package com.kemarport.kyms.fragments.supervisor

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
import com.kemarport.kyms.adapters.EdiDetailsAdapter
import com.kemarport.kyms.databinding.FragmentIntercartingBinding
import com.kemarport.kyms.helper.Constants
import com.kemarport.kyms.helper.Resource
import com.kemarport.kyms.helper.SessionManager
import com.kemarport.kyms.models.CoilData.CoilData
import com.kemarport.kyms.models.upload.Coil
import com.kemarport.kyms.models.vesselandintercarting.VesselAndIntercartingRequest
import com.kemarport.kyms.repository.KYMSRepository
import com.kemarport.kyms.viewmodel.IntercartingVMProviderFactory
import com.kemarport.kyms.viewmodel.IntercartingViewModel
import es.dmoral.toasty.Toasty


class IntercartingFragment : Fragment(), View.OnClickListener {
    private var _binding: FragmentIntercartingBinding? = null
    private lateinit var arrayAdapter: ArrayAdapter<String>
    private var hatchNumber: Int = 0
    private val TAG = "IntercartingFragment"
    private var scannedBarcode: String? = ""
    private val operationType = "Update"
    private lateinit var viewModel: IntercartingViewModel
    private lateinit var ediDetailsAdapter: EdiDetailsAdapter
    private val barcodes = mutableListOf<String?>()
    private val scannedCoilDataList = mutableListOf<CoilData>()
    private val coilList = mutableListOf<Coil?>()
    private lateinit var vesselAndIntercartingRequest: VesselAndIntercartingRequest
    private var serverIpSharedPrefText: String? = null
    private var portSharedPrefText: Int? = 0
    private lateinit var user: HashMap<String, String?>
    private lateinit var session: SessionManager
    private lateinit var baseUrl: String
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
                R.layout.fragment_intercarting,
                container,
                false
            )
        val view = binding.root
        binding.listener = this

        val toolbar =
            (activity as FragmentHostActivity).findViewById<Toolbar>(R.id.tbFragmentHostActivity)
        toolbar?.let {
            val titleTextView = it.findViewById<TextView>(R.id.tvToolbarTitle)
            titleTextView.text = "Intercarting"
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
            IntercartingVMProviderFactory(application, kymsRepository)
        viewModel =
            ViewModelProvider(this, viewModelProviderFactory)[IntercartingViewModel::class.java]

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
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, hatchLocations)
        arrayAdapter.setDropDownViewResource(R.layout.item_ddm_location)
        binding.spinnerLoc.adapter = arrayAdapter
        binding.spinnerLoc.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                hatchNumber = hatchLocations.indexOf(binding.spinnerLoc.selectedItem) + 1
                barcodes.clear()
                coilList.clear()
                scannedCoilDataList.clear()
                ediDetailsAdapter.updateCoilDataList(scannedCoilDataList)
                binding.tvEmptyList.visibility = View.VISIBLE
//                binding.btnUpdateHatch.visibility = View.GONE
                Log.d(TAG, "onItemSelected: $hatchNumber")
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

        }

        setupRecyclerView()

        /*viewModel.validationBerthToVesselMutableLiveData.observe(
            viewLifecycleOwner,
            Observer { response ->
                when (response) {
                    is Resource.Success -> {
                        var coil: Coil?
                        response.data?.let { resultResponse ->
                            hideProgressBar()
                            Log.d(
                                TAG,
                                "onViewCreated: Intercarting Status Success ${resultResponse.statusMessage}"
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
                                        binding.tvEmptyList.visibility = View.INVISIBLE
                                        binding.btnUpdateHatch.visibility = View.VISIBLE

                                        coil = Coil(scannedBarcode)
                                        coilList.add(coil)
                                        vesselAndIntercartingRequest =
                                            VesselAndIntercartingRequest(
                                                hatchNumber.toString(),
                                                operationType,
                                                coilList
                                            )
                                        Log.d(TAG, "setBarcode: $vesselAndIntercartingRequest")
                                    }
                                }
                                "Not Found" -> {
                                    Toasty.error(
                                        activity as FragmentHostActivity,
                                        resultResponse.statusMessage
                                    )
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
                                "onViewCreated: Intercarting Status Error $errorMessage"
                            )
                        }
                    }
                    is Resource.Loading -> {
                        showProgressBar()
                    }
                }

                ediDetailsAdapter.updateCoilDataList(scannedCoilDataList)
            })*/

        viewModel.interCartingMutableLiveData.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    response.data?.let { resultResponse ->
                        hideProgressBar()
                        Log.d(
                            TAG,
                            "onViewCreated: ${resultResponse.statusMessage}"
                        )

                        /*when (resultResponse.responseMessage) {
                            "All Product loaded at Vessel Sucessfully" -> {
                                binding.btnUpdateHatch.visibility = View.GONE
                                Toasty.success(
                                    activity as FragmentHostActivity,
                                    "Hatch updated successfully"
                                ).show()
                                barcodes.clear()
                                coilList.clear()
                                scannedCoilDataList.clear()
                                ediDetailsAdapter.updateCoilDataList(scannedCoilDataList)
                                binding.tvEmptyList.visibility = View.VISIBLE
                                binding.btnUpdateHatch.visibility = View.GONE
                            }
                            "Invalid" -> {

                            }
                        }*/

                        when (resultResponse.statusMessage) {
                            "Success" -> {
                                (requireActivity() as FragmentHostActivity).setScannerAudio("success")
                                if (scannedBarcode !in barcodes) {
                                    barcodes.add(scannedBarcode)
                                    Toasty.success(
                                        activity as FragmentHostActivity,
                                        "Hatch updated successfully for ${resultResponse.batchNumber}",
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
                                } else {

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
                                    "Not Found In Packing List" -> {
                                        (activity as FragmentHostActivity).showCustomDialog(
                                            "",
                                            "${resultResponse.batchNumber} not found in packing list"
                                        )
                                    }
                                    "Already Exist in Same Hatch" -> {
                                        (activity as FragmentHostActivity).showCustomDialog(
                                            "",
                                            "${resultResponse.batchNumber} already exists in the same hatch"
                                        )
                                    }
                                    "Something Went wrong" -> {
                                        (activity as FragmentHostActivity).showCustomDialog(
                                            "",
                                            "${resultResponse.batchNumber} already exists in some other hatch"
                                        )
                                    }
                                    else -> {
                                        (activity as FragmentHostActivity).showCustomDialog(
                                            "",
                                            resultResponse.batchNumber+" "+resultResponse.productMessage
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
                        Log.e(
                            TAG,
                            "onViewCreated: Stock to berth success response $errorMessage"
                        )
                        if (errorMessage =="Unauthorized" || errorMessage == "Authentication token expired" || errorMessage == Constants.CONFIG_ERROR) {
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
                /*binding.btnUpdateHatch.id -> {
                    viewModel.interCarting(vesselAndIntercartingRequest)
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
        hatchNumber = hatchLocations.indexOf(binding.spinnerLoc.selectedItem) + 1
        val user = session.getUserDetails()
        val firstName = user["firstName"]
        val lastName = user["lastName"]
        val jwtToken = user["jwtToken"]
        val refreshToken = user["refreshToken"]
        val email = user["email"]
        Log.d(TAG, "setBarcode: $firstName & $lastName & $jwtToken & $refreshToken & $email")
        Log.d(TAG, "setBarcode: location after scanning - $hatchNumber")
        (activity as FragmentHostActivity).runOnUiThread {
            val vesselAndIntercartingRequest =
                VesselAndIntercartingRequest(scannedBarcode, hatchNumber.toString(), operationType,0,"")
            viewModel.interCarting(jwtToken, vesselAndIntercartingRequest, baseUrl)
        }
    }

    private fun setupRecyclerView() {
        Log.d(TAG, "setupRecyclerView: called")
        ediDetailsAdapter = EdiDetailsAdapter()
        binding.rvIntercarting.apply {
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