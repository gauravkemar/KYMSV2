package com.kemarport.kyms.fragments.operator.export.dropper

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
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
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.kemarport.kyms.R
import com.kemarport.kyms.activities.FragmentHostActivity
import com.kemarport.kyms.adapters.export.EdiDetailsAdapter
import com.kemarport.kyms.databinding.FragmentDropStorageBinding
import com.kemarport.kyms.helper.Constants
import com.kemarport.kyms.helper.Resource
import com.kemarport.kyms.helper.SessionManager
import com.kemarport.kyms.repository.KYMSRepository
import com.kemarport.kyms.viewmodel.export.DropStorageViewModel
import com.kemarport.kyms.viewmodel.export.DropStorageViewModelFactory
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.kemarport.kyms.helper.Gps
import com.kemarport.kyms.models.export.CoilData.CoilData
import es.dmoral.toasty.Toasty
import java.util.Timer
import java.util.TimerTask

class DropStorageFragment : Fragment(), View.OnClickListener {
    private var _binding: FragmentDropStorageBinding? = null
    private val TAG = "DropStorageFragment"
    private lateinit var viewModel: DropStorageViewModel
    private var locationID: Int? = -1
    private val locationNames = mutableListOf("PLEASE SCAN / SELECT A LOCATION")
    private val dropOperations = mutableListOf("NEW DROP OPERATION", "BACK TO STOCK")
    private val locationIds = hashMapOf<String, Int>()
    private val locationBarcodes = mutableListOf<String>()
    private lateinit var arrayAdapter: ArrayAdapter<String>
    private var scannedBarcode: String? = ""
    private lateinit var ediDetailsAdapter: EdiDetailsAdapter
    private val barcodes = mutableListOf<String?>()
    private val scannedCoilDataList = mutableListOf<CoilData>()
    private var i = 0
    private var j = 0
    private var serverIpSharedPrefText: String? = null
    private var jwtToken: String? = null
    private var portSharedPrefText: Int? = 0
    private lateinit var adminUser: HashMap<String, String?>
    private lateinit var user: HashMap<String, String?>
    private lateinit var session: SessionManager
    private lateinit var baseUrl: String
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var demoBarcodes: MutableList<String?> = mutableListOf(
        "22AS30020",
        "M967990000",
        "M968210000",
        "M968250000",
        "M968821000"
    )
    private var demoLocationBarcodes: MutableList<String?> = mutableListOf(
        "000AACB",
        "0100CCB"
    )

    // This property is only valid between onCreateView and
// onDestroyView.
    private val binding get() = _binding!!

    private var currentLocation: Location? = null
    var newLat = 0.0
    var newLng = 0.0
    var t = Timer()
    var tt: TimerTask? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_drop_storage, container, false)
        val view = binding.root
        binding.listener = this

        val toolbar =
            (activity as FragmentHostActivity).findViewById<Toolbar>(R.id.tbFragmentHostActivity)
        toolbar?.let {
            val titleTextView = it.findViewById<TextView>(R.id.tvToolbarTitle)
            titleTextView.text = "Drop at Storage"
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
            DropStorageViewModelFactory(application, kymsRepository)
        viewModel =
            ViewModelProvider(this, viewModelProviderFactory)[DropStorageViewModel::class.java]

        session = SessionManager(requireContext())
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
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
                            errorMessage,
                            ""
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

        arrayAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, dropOperations)
        arrayAdapter.setDropDownViewResource(R.layout.item_ddm_location)
        binding.spinnerLoc2.adapter = arrayAdapter

        /*viewModel.coilStatusDroppingMutableLiveData.observe(
            viewLifecycleOwner,
            Observer { response ->
                when (response) {
                    is Resource.Success -> {
                        var coil: Coil?
                        response.data?.let { resultResponse ->
                            hideProgressBar()
                            Log.d(
                                TAG,
                                "onViewCreated: Stock Entry Barcode Status Success ${resultResponse.statusMessage}"
                            )

                            when (resultResponse.statusMessage) {
                                "Success" -> {
                                    if (scannedBarcode !in barcodes) {
                                        barcodes.add(scannedBarcode)
                                        when (resultResponse.productMessage) {
                                            "Found" -> {
                                                Log.d(
                                                    TAG,
                                                    "productMessage: ${resultResponse.productMessage}"
                                                )
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
                                                binding.btnAddToStock.visibility = View.VISIBLE

                                                coil = Coil(scannedBarcode)
                                                coilList.add(coil)
                                                ediConfirmationRequest =
                                                    EdiConfirmationRequest(locationID, coilList)
                                                Log.d(TAG, "setBarcode: $ediConfirmationRequest")
                                            }
                                            "Unloaded Not Completed Yet" -> {
                                                Log.d(
                                                    TAG,
                                                    "productMessage: ${resultResponse.productMessage}"
                                                )
                                                Toasty.warning(
                                                    activity as FragmentHostActivity,
                                                    "Unloading not done yet"
                                                ).show()
                                                barcodes.clear()
                                            }
                                            "Already Exist" -> {
                                                Log.d(
                                                    TAG,
                                                    "productMessage: ${resultResponse.productMessage}"
                                                )
                                                Toasty.warning(
                                                    activity as FragmentHostActivity,
                                                    "Product already in stock for this location"
                                                ).show()
                                                barcodes.clear()
                                            }
                                        }
                                    }
                                    scannedBarcode = ""
                                }
                                "Not Found" -> {
                                    Toasty.error(
                                        activity as FragmentHostActivity,
                                        "Invalid Batch Number",
                                        Toast.LENGTH_SHORT
                                    ).show()
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
                            Log.d(TAG, "onViewCreated: Stock Entry Barcode Error $errorMessage")
                        }
                    }
                    is Resource.Loading -> {
                        showProgressBar()
                    }
                }

                ediDetailsAdapter.updateCoilDataList(scannedCoilDataList)
            })*/

        /*viewModel.addProductInStockMutableLiveData.observe(
            viewLifecycleOwner,
            Observer { response ->
                when (response) {
                    is Resource.Success -> {
                        response.data?.let { resultResponse ->
                            hideProgressBar()
                            when (resultResponse.responseMessage) {
                                "Products Store Sucessfully" -> {
//                                    binding.btnAddToStock.visibility = View.GONE
                                    Toasty.success(
                                        activity as FragmentHostActivity,
                                        "Products Stored Successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    barcodes.clear()
                                    coilList.clear()
                                    scannedCoilDataList.clear()
                                    ediDetailsAdapter.updateCoilDataList(scannedCoilDataList)
                                    binding.tvEmptyList.visibility = View.VISIBLE
//                                    binding.btnAddToStock.visibility = View.GONE
                                }
                                "All Products are Invalid" -> {
                                    *//*for (i in resultResponse.partiallySave.indices) {
                                        Toasty.error(
                                            activity as FragmentHostActivity,
                                            "Batch No: ${resultResponse.partiallySave[i].batchNo} ${resultResponse.partiallySave[i].message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }*//*
                                }
                            }
                        }
                    }
                    is Resource.Error -> {
                        response.message?.let {
                            hideProgressBar()
                            Log.e(TAG, "onViewCreated: ${response.message}")
                        }
                    }
                    is Resource.Loading -> {
                        showProgressBar()
                    }
                }
            })*/

        viewModel.addProductInStockMutableLiveData.observe(
            viewLifecycleOwner,
            Observer { response ->
                when (response) {
                    is Resource.Success -> {
                        response.data?.let { resultResponse ->
                            hideProgressBar()
                            when (resultResponse.statusMessage) {
                                "Products Store Successfully" -> {
                                    (requireActivity() as FragmentHostActivity).setScannerAudio("success")
                                    if (scannedBarcode !in barcodes) {
                                        barcodes.add(scannedBarcode)
                                        Toasty.success(
                                            activity as FragmentHostActivity,
                                            "${resultResponse.batchNumber} stored successfully",
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
                                /*"Already Exist" -> {
                                    (requireActivity() as FragmentHostActivity).setScannerAudio("error")
                                    *//*Toasty.warning(
                                        activity as FragmentHostActivity,
                                        "${resultResponse.batchNumber} already exists in same location.",
                                        Toast.LENGTH_SHORT
                                    ).show()*//*
                                    (activity as FragmentHostActivity).showCustomDialog(
                                        "${resultResponse.batchNumber} already exists in same location.",
                                        ""
                                    )
                                }
                                "Already Exist In Same Location Id." -> {
                                    (requireActivity() as FragmentHostActivity).setScannerAudio("error")
                                    *//*Toasty.warning(
                                        activity as FragmentHostActivity,
                                        "${resultResponse.batchNumber} already exists in same location.",
                                        Toast.LENGTH_SHORT
                                    ).show()*//*
                                    (activity as FragmentHostActivity).showCustomDialog(
                                        "${resultResponse.batchNumber} already exists in same location.",
                                        ""
                                    )
                                }
                                "Not Found" -> {
                                    (requireActivity() as FragmentHostActivity).setScannerAudio("error")
                                    (activity as FragmentHostActivity).showCustomDialog(
                                        "Invalid Batch Number ${resultResponse.batchNumber}",
                                        ""
                                    )
                                }
                                "Found" -> {
                                    *//*(requireActivity() as FragmentHostActivity).setScannerAudio("error")
                                    (activity as FragmentHostActivity).showCustomDialog(
                                        "",
                                        "${resultResponse.batchNumber} is assigned for Rake to Vessel Operation"
                                    )*//*
                                    (requireActivity() as FragmentHostActivity).setScannerAudio("error")
                                    (activity as FragmentHostActivity).showCustomDialog(
                                        resultResponse.batchNumber+" - "+resultResponse.productMessage,
                                        ""
                                    )
                                }*/
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

        viewModel.backToStockMutableLiveData.observe(
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
                                            "${resultResponse.batchNumber} stored successfully",
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
                                else -> {
                                    (requireActivity() as FragmentHostActivity).setScannerAudio("error")
                                    (activity as FragmentHostActivity).showCustomDialog(
                                        resultResponse.batchNumber,
                                        resultResponse.productMessage
                                    )
                                }
                                /*"Found" -> {
                                    (requireActivity() as FragmentHostActivity).setScannerAudio("error")
                                    (activity as FragmentHostActivity).showCustomDialog(
                                        resultResponse.batchNumber+" "+resultResponse.productMessage,
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

        if (Build.MANUFACTURER.contains("Zebra Technologies") || Build.MANUFACTURER.contains("Motorola Solutions")) {
            binding.btnSetBarcode.visibility = View.GONE
            binding.btnSetLocationBarcode.visibility = View.GONE
        }

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())

        binding.floatingActionButton.setOnClickListener {
            (requireActivity() as FragmentHostActivity).showSteelSlabDialog(this)
        }

        tt = object : TimerTask() {
            override fun run() {
                getLocationNew()
            }
        }

        t.scheduleAtFixedRate(tt, 1000, 1000)
    }
    fun getLocationNew() {
        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        requireActivity().runOnUiThread(Runnable {
            val gps = Gps(requireActivity())
            if (gps.canGetLocation()) {
                newLat = gps.getLatitude()
                newLng = gps.getLongitude()
                if (gps != null) {
                    currentLocation = gps.location
                    Log.e("currentLoc",currentLocation.toString())
                } else {
                    requestLocation()
                }
            }
        })
    }


    private fun requestLocation() {
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 10000 // 10 seconds

        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            object : LocationCallback() {
                override fun onLocationResult(locationResult: com.google.android.gms.location.LocationResult) {
                    val location = locationResult.lastLocation
                    // Handle the location update here
                    if (location != null) {
                        updateLocation(location)
                    }
                }
            },
            null
        )
    }
    private fun updateLocation(location: Location) {
        currentLocation=location
    }

    override fun onPause() {
        super.onPause()
        if (t != null) {
            t.cancel()
            tt!!.cancel()
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onClick(view: View?) {
        view?.let {
            when (it.id) {
                /*binding.btnAddToStock.id -> {
                    Log.d(TAG, "onClick: $ediConfirmationRequest")
                    viewModel.addProductInStock(ediConfirmationRequest)
                }*/
                binding.btnSetBarcode.id -> {
                    if (i == demoBarcodes.size) {
                        i = 0
                    }
                    setBarcode(demoBarcodes[i])
                    i++
                }
                binding.btnSetLocationBarcode.id -> {
                    if (j == demoLocationBarcodes.size) {
                        j = 0
                    }
                    setBarcode(demoLocationBarcodes[j])
                    j++
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
                if (binding.spinnerLoc2.selectedItemPosition == 0) {
                    val generalRequestLocationId =
                        com.kemarport.kyms.models.export.generalrequestandresponse.GeneralRequestLocationId(
                            scannedBarcode,
                            locationID
                        )
                    Log.d(TAG, "setBarcode: generalRequest: $generalRequestLocationId")
                    viewModel.addProductInStock(jwtToken, generalRequestLocationId, baseUrl)
                } else {
                    Log.d(TAG, "setBarcode: BTS Operation")
                    val generalRequestLocationId =
                        com.kemarport.kyms.models.export.generalrequestandresponse.GeneralRequestLocationId(
                            scannedBarcode,
                            locationID
                        )
                    viewModel.backToStock(jwtToken, generalRequestLocationId, baseUrl)
                }
            }

            binding.rvDropStorage.smoothScrollToPosition(scannedCoilDataList.size)
        }
    }

    private fun setupRecyclerView() {
        Log.d(TAG, "setupRecyclerView: called")
        ediDetailsAdapter = EdiDetailsAdapter()
        binding.rvDropStorage.apply {
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