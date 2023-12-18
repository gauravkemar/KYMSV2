package com.kemarport.kyms.fragments.supervisor

import android.content.pm.PackageManager
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
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.kemarport.kyms.R
import com.kemarport.kyms.activities.FragmentHostActivity
import com.kemarport.kyms.adapters.EdiDetailsAdapter
import com.kemarport.kyms.databinding.FragmentBttBinding
import com.kemarport.kyms.helper.Constants
import com.kemarport.kyms.helper.Resource
import com.kemarport.kyms.helper.SessionManager
import com.kemarport.kyms.models.CoilData.CoilData
import com.kemarport.kyms.repository.KYMSRepository
import com.kemarport.kyms.viewmodel.BTTViewModel
import com.kemarport.kyms.viewmodel.BTTViewModelProviderFactory
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import es.dmoral.toasty.Toasty

class BttFragment : Fragment(), View.OnClickListener {
    private var _binding: FragmentBttBinding? = null
    private val TAG = "BttFragment"
    private lateinit var ediDetailsAdapter: EdiDetailsAdapter
    private var serverIpSharedPrefText: String? = null
    private var jwtToken: String? = null
    private var portSharedPrefText: Int? = 0
    private lateinit var adminUser: HashMap<String, String?>
    private lateinit var user: HashMap<String, String?>
    private lateinit var session: SessionManager
    private lateinit var baseUrl: String
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var viewModel: BTTViewModel
    private val locationNames = mutableListOf("PLEASE SCAN / SELECT A LOCATION")
    private val locationIds = hashMapOf<String, Int>()
    private val locationBarcodes = mutableListOf<String>()
    private lateinit var arrayAdapter: ArrayAdapter<String>
    private var locationID: Int? = -1
    private val barcodes = mutableListOf<String?>()
    private val scannedCoilDataList = mutableListOf<CoilData>()
    private var scannedBarcode: String? = ""

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
                R.layout.fragment_btt,
                container,
                false
            )
        val view = binding.root
        binding.listener = this

        val toolbar =
            (activity as FragmentHostActivity).findViewById<Toolbar>(R.id.tbFragmentHostActivity)
        toolbar?.let {
            val titleTextView = it.findViewById<TextView>(R.id.tvToolbarTitle)
            titleTextView.text = "Back to Town"
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
            BTTViewModelProviderFactory(application, kymsRepository)
        viewModel =
            ViewModelProvider(this, viewModelProviderFactory)[BTTViewModel::class.java]

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

        viewModel.getLocations(baseUrl, jwtToken)
        viewModel.locationsMutableLiveData.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    response.data?.let { resultResponse ->
                        hideProgressBar()
                        Log.d(TAG, "onViewCreated: $resultResponse")
                        if (resultResponse.isNotEmpty()) {
                            for (i in resultResponse.indices) {
                                val locationId = resultResponse[i].locationId
                                val locationName = resultResponse[i].locationName
                                val locationBarcode = resultResponse[i].barcode
                                if (!locationName.equals("Berth.No 10")) {
                                    locationNames.add(locationName)
                                    locationIds[locationName] = locationId
                                    locationBarcodes.add(locationBarcode)
                                }
                            }
                            arrayAdapter.notifyDataSetChanged()
                        } else {
                            (activity as FragmentHostActivity).showCustomDialog(
                                "NO LOCATION DATA!",
                                ""
                            )
                        }
                    }
                }
                is Resource.Error -> {
                    response.message?.let { errorMessage ->
                        hideProgressBar()
                        (activity as FragmentHostActivity).showCustomDialog(
                            "",
                            "errorMessage"
                        )
                    }
                }
                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        })

        arrayAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, locationNames)
        arrayAdapter.setDropDownViewResource(R.layout.item_ddm_location)
        binding.spinnerLoc.adapter = arrayAdapter
        binding.spinnerLoc.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if (position != 0) {
                        val spinnerLocationName =
                            binding.spinnerLoc.getItemAtPosition(position).toString()
                        locationID = locationIds[spinnerLocationName]
                        Log.d(TAG, "onItemSelected: SpinnerSelectedLocation $spinnerLocationName")
                        Log.d(TAG, "onItemSelected: SpinnerSelectedLocationId $locationID")
                        barcodes.clear()
//                        coilList.clear()
                        scannedCoilDataList.clear()
                        ediDetailsAdapter.updateCoilDataList(scannedCoilDataList)
                        binding.tvEmptyList.visibility = View.VISIBLE
                    } else {
                        barcodes.clear()
//                        coilList.clear()
                        scannedCoilDataList.clear()
                        ediDetailsAdapter.updateCoilDataList(scannedCoilDataList)
                        binding.tvEmptyList.visibility = View.VISIBLE
                    }
//                    binding.btnAddToStock.visibility = View.GONE
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    TODO("Not yet implemented")
                }

            }

        viewModel.markedBTTMutableLiveData.observe(viewLifecycleOwner, Observer { response ->
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
                                        resultResponse.batchNumber+" "+resultResponse.productMessage,
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
                                    binding.etBTTRemark.setText("")
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
                            /*"Not Found" -> {
                                (requireActivity() as FragmentHostActivity).setScannerAudio("error")
                                (activity as FragmentHostActivity).showCustomDialog(
                                    "Invalid Batch Number ${resultResponse.batchNumber}",
                                    ""
                                )
                            }
                            "Found" -> {
                                (requireActivity() as FragmentHostActivity).setScannerAudio("error")
                                (activity as FragmentHostActivity).showCustomDialog(
                                    "${resultResponse.batchNumber} Already marked for BTT",
                                    ""
                                )
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

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())

        binding.floatingActionButton.setOnClickListener {
            (requireActivity() as FragmentHostActivity).showSteelSlabDialog(this)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onClick(p0: View?) {
        TODO("Not yet implemented")
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

//            fetchLocation()
            if (binding.spinnerLoc.selectedItemPosition == 0) {
                if (data in locationBarcodes) {
                    (activity as FragmentHostActivity).setScannerAudio("success")
                    val locationNamePosition = locationBarcodes.indexOf(data) + 1
                    val locationName = locationNames[locationNamePosition]
                    locationID = locationIds[locationName]
                    binding.spinnerLoc.setSelection(locationNamePosition)
                    Log.d(TAG, "setBarcode: locationId: $locationID")
                } else {
                    (activity as FragmentHostActivity).setScannerAudio("error")
                    Toasty.error(
                        activity as FragmentHostActivity,
                        "Please Scan Valid Location Barcode",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                val bttRemark = binding.etBTTRemark.text.toString()
                viewModel.markedBTT(jwtToken, scannedBarcode, locationID, bttRemark, baseUrl)
            }

            binding.rvBTT.smoothScrollToPosition(scannedCoilDataList.size)
        }
    }

    private fun setupRecyclerView() {
        Log.d(TAG, "setupRecyclerView: called")
        ediDetailsAdapter = EdiDetailsAdapter()
        binding.rvBTT.apply {
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

    private fun fetchLocation() {
        val task = fusedLocationProviderClient.lastLocation

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                101
            )
            return
        }
        task.addOnSuccessListener {
            if (it != null) {
                Log.d("MainActivity", "fetchLocation: Latitude: ${it.latitude}")
                Log.d("MainActivity", "fetchLocation: Longitude: ${it.longitude}")
            }
        }
    }
}