package com.kemarport.kyms.fragments.operator.importupdate.importdropper

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.kemarport.kyms.R
import com.kemarport.kyms.activities.FragmentHostActivity
import com.kemarport.kyms.adapters.importupdate.ImportEdiDetailsAdapter
import com.kemarport.kyms.databinding.FragmentImportDropStorageBinding
import com.kemarport.kyms.helper.Constants
import com.kemarport.kyms.helper.Gps
import com.kemarport.kyms.helper.Resource
import com.kemarport.kyms.helper.SessionManager
import com.kemarport.kyms.models.importupdate.importstocktally.GetReceivedProductResponse
import com.kemarport.kyms.models.importupdate.importstocktally.importjobmaster.GetJobDetailsListItemResponse
import com.kemarport.kyms.repository.KYMSRepository
import com.kemarport.kyms.viewmodel.importupdate.ImportDropperViewModel
import com.kemarport.kyms.viewmodel.importupdate.ImportDropperViewModelFactory
import es.dmoral.toasty.Toasty
import java.util.Timer
import java.util.TimerTask


class ImportDropStorageFragment : Fragment(), View.OnClickListener {

    private var _binding: FragmentImportDropStorageBinding? = null
    private val TAG = "DropStorageFragment"
    private lateinit var viewModel: ImportDropperViewModel
    private var locationID: Int? = -1
    private val locationNames = mutableListOf("PLEASE SCAN / SELECT A LOCATION")
    private val dropOperations = mutableListOf("NEW DROP OPERATION")
   // private val dropOperations = mutableListOf("NEW DROP OPERATION", "BACK TO STOCK")
    private val locationIds = hashMapOf<String, Int>()
    private val locationBarcodes = mutableListOf<String>()
    private lateinit var arrayAdapter: ArrayAdapter<String>
    private var scannedBarcode: String? = ""
    private lateinit var ediDetailsAdapter: ImportEdiDetailsAdapter
    private val barcodes = mutableListOf<String?>()
    private val scannedImportStoredProductDataList = mutableListOf<GetJobDetailsListItemResponse>()
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
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        _binding =
            DataBindingUtil.inflate(inflater,R.layout.fragment_import_drop_storage, container, false)
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
            ImportDropperViewModelFactory(application, kymsRepository)
        viewModel =
            ViewModelProvider(this, viewModelProviderFactory)[ImportDropperViewModel::class.java]

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
                        scannedImportStoredProductDataList.clear()
                        ediDetailsAdapter.updateImportDataList(scannedImportStoredProductDataList)
                        binding.tvEmptyList.visibility = View.VISIBLE
                    } else {
                        barcodes.clear()
//                        coilList.clear()
                        scannedImportStoredProductDataList.clear()
                        ediDetailsAdapter.updateImportDataList(scannedImportStoredProductDataList)
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



        viewModel.importAddProductInStockMutableLiveData.observe(
            viewLifecycleOwner,
            Observer { response ->
                when (response) {
                    is Resource.Success -> {
                        response.data?.let { resultResponse ->
                            hideProgressBar()
                            when (resultResponse.responseMessage) {
                                "Product Successfully Store in Stock" -> {


                                 /*   (requireActivity() as FragmentHostActivity).setScannerAudio("success")
                                    if (scannedBarcode !in barcodes) {
                                        barcodes.add(scannedBarcode)
                                        Toasty.success(
                                            activity as FragmentHostActivity,
                                            "${resultResponse.batchNumber} stored successfully",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        scannedImportStoredProductDataList.add(
                                            CoilData(
                                                resultResponse.currentASNScaningListResponses.shipToPartyName,
                                                scannedBarcode,
                                                resultResponse.currentASNScaningListResponses.portOfDischarge,
                                                resultResponse.productMessage,
                                                resultResponse.currentASNScaningListResponses.rakeRefNo
                                            )
                                        )
                                        ediDetailsAdapter.updateCoilDataList(scannedImportStoredProductDataList)
                                        binding.tvEmptyList.visibility = View.GONE
                                    }*/

                                 /*   val responseObject = resultResponse.responseObject as? Map<*, *>
                                    val importJobMasterMobileResponseList =
                                        responseObject?.get("importJobMasterMobileResponse") as? List<*>
                                    if (importJobMasterMobileResponseList?.isNotEmpty() == true) {
                                        val result = importJobMasterMobileResponseList.mapNotNull { map ->
                                            if (map is Map<*, *>) {
                                                GetJobDetailsListItemResponse(
                                                    batchNumber = map["batchNumber"] as? String ?: "",
                                                    importJobDetailId = map["importJobDetailId"] as? Int ?: 0,
                                                    productId = map["productId"] as? Int ?: 0,
                                                    status = map["status"] as? String ?: "",
                                                )
                                            } else {
                                                null
                                            }
                                        }*/

                                    val resultFirst = resultResponse.responseObject as? GetReceivedProductResponse
                                    var getJobDetailsListItemResponse= resultFirst?.let {
                                        GetJobDetailsListItemResponse(
                                            it.batchno,0,0,"")
                                    }
                                    getJobDetailsListItemResponse?.let {
                                        scannedImportStoredProductDataList.add(
                                            it
                                        )
                                    }
                                        ediDetailsAdapter.updateImportDataList(scannedImportStoredProductDataList)
                                        binding.tvEmptyList.visibility = View.GONE

                                }
                                else -> {
                                    (requireActivity() as FragmentHostActivity).setScannerAudio("error")
                                    (activity as FragmentHostActivity).showCustomDialog(
                                        "Error",
                                        resultResponse.errorMessage
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

        /*        viewModel.backToStockMutableLiveData.observe(
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
                                                scannedImportStoredProductDataList.add(
                                                    CoilData(
                                                        resultResponse.currentASNScaningListResponses.shipToPartyName,
                                                        scannedBarcode,
                                                        resultResponse.currentASNScaningListResponses.portOfDischarge,
                                                        resultResponse.productMessage,
                                                        resultResponse.currentASNScaningListResponses.rakeRefNo
                                                    )
                                                )
                                                ediDetailsAdapter.updateCoilDataList(scannedImportStoredProductDataList)
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
                                        *//*"Found" -> {
                                            (requireActivity() as FragmentHostActivity).setScannerAudio("error")
                                            (activity as FragmentHostActivity).showCustomDialog(
                                                resultResponse.batchNumber+" "+resultResponse.productMessage,
                                                ""
                                            )
                                        }*//*
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
                    })*/

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
                override fun onLocationResult(locationResult: LocationResult) {
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
      /*  val user = session.getUserDetails()
        val firstName = user["firstName"]
        val lastName = user["lastName"]
        val jwtToken = user["jwtToken"]
        val refreshToken = user["refreshToken"]
        val email = user["email"]
        Log.d(TAG, "setBarcode: $firstName & $lastName & $jwtToken & $refreshToken & $email")*/
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
                    /*val generalRequestLocationId =
                        GeneralRequestLocationId(
                            scannedBarcode,
                            locationID
                        )*/
                    //Log.d(TAG, "setBarcode: generalRequest: $generalRequestLocationId")
                    viewModel.addProductInStock(jwtToken, baseUrl,scannedBarcode,locationID,"${currentLocation?.latitude}/${currentLocation?.longitude}")
                } else {
             /*       Log.d(TAG, "setBarcode: BTS Operation")
                    val generalRequestLocationId =
                        GeneralRequestLocationId(
                            scannedBarcode,
                            locationID
                        )
                    viewModel.backToStock(jwtToken, generalRequestLocationId, baseUrl)*/
                }
            }

            binding.rvDropStorage.smoothScrollToPosition(scannedImportStoredProductDataList.size)
        }
    }

    private fun setupRecyclerView() {
        Log.d(TAG, "setupRecyclerView: called")
        ediDetailsAdapter = ImportEdiDetailsAdapter()
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