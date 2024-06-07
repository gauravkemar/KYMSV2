package com.kemarport.kyms.fragments.operator.importupdate.importpicker

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.kemarport.kyms.R
import com.kemarport.kyms.activities.FragmentHostActivity
import com.kemarport.kyms.adapters.importupdate.ImportEdiDetailsAdapter
import com.kemarport.kyms.databinding.FragmentImportEditDetailsBinding
import com.kemarport.kyms.helper.Constants
import com.kemarport.kyms.helper.Gps
import com.kemarport.kyms.helper.Resource
import com.kemarport.kyms.helper.SessionManager
import com.kemarport.kyms.models.importupdate.importstocktally.GetReceivedProductResponse
import com.kemarport.kyms.models.importupdate.importstocktally.importjobmaster.GetJobDetailsListItemResponse
import com.kemarport.kyms.repository.KYMSRepository
import com.kemarport.kyms.viewmodel.importupdate.ImportStockTallyViewModel
import com.kemarport.kyms.viewmodel.importupdate.ImportStockTallyViewModelFactory
import es.dmoral.toasty.Toasty
import java.util.Timer
import java.util.TimerTask
import kotlin.math.roundToInt

class ImportEditDetailsFragment : Fragment() {

    lateinit var binding:FragmentImportEditDetailsBinding
    lateinit var viewModel:ImportStockTallyViewModel
    private val args: ImportEditDetailsFragmentArgs by navArgs()
    private var importJobMasterId: Int = 0
    private lateinit var adminUser: HashMap<String, String?>
    private var serverIpSharedPrefText: String? = null
    private var portSharedPrefText: Int? = 0
    private lateinit var user: HashMap<String, String?>
    private lateinit var session: SessionManager
    private lateinit var baseUrl: String
    private var scannedBarcode: String? = ""
    private lateinit var ediAdapter: ImportEdiDetailsAdapter
    private var getJobDetailsListMobileResponse=ArrayList<GetJobDetailsListItemResponse>()
    var jwtToken:String=""
    private var currentLocation: Location? = null
    var newLat = 0.0
    var newLng = 0.0
    var t = Timer()
    var tt: TimerTask? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

        binding=DataBindingUtil.inflate(inflater,R.layout.fragment_import_edit_details, container, false)
        setupRecyclerView()
        return binding.root
    }

    private fun setupRecyclerView() {
        ediAdapter = ImportEdiDetailsAdapter()
        binding.ImportRvEdiDetails.apply {
            adapter = ediAdapter
            layoutManager = LinearLayoutManager(activity).apply {
                stackFromEnd = true
                reverseLayout = true
            }
            addOnScrollListener(scrollListener)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Toasty.Config.getInstance()
            .setGravity(Gravity.CENTER)
            .apply() // required

        importJobMasterId = args.importJobMasterId

        val kymsRepository = KYMSRepository()
        val application = (activity as FragmentHostActivity).application
        val viewModelProviderFactory =
            ImportStockTallyViewModelFactory(application, kymsRepository)
        viewModel =
            ViewModelProvider(this, viewModelProviderFactory)[ImportStockTallyViewModel::class.java]

        session = SessionManager(requireContext())
        adminUser = session.getAdminDetails()
        user = session.getUserDetails()
        serverIpSharedPrefText = adminUser["serverIp"]
        portSharedPrefText = adminUser["port"]?.toInt()
        baseUrl = if (serverIpSharedPrefText == "192.168.1.52") {
            "http://$serverIpSharedPrefText:$portSharedPrefText/api/"
        } else {
            "http://$serverIpSharedPrefText:$portSharedPrefText/service/api/"
        }
        // baseUrl="http://192.168.1.10:5200/api/"
        jwtToken = user["jwtToken"].toString()
        tt = object : TimerTask() {
            override fun run() {
                getLocationNew()
            }
        }

        viewModel.getReceivedProductMutable .observe(viewLifecycleOwner, Observer
        { response ->
            when (response) {
                is Resource.Success -> {
                    getJobDetailsListMobileResponse.clear()
                    response.data?.let { resultResponse ->
                        val resultFirst = resultResponse.responseObject as? GetReceivedProductResponse
                        if(resultFirst!=null)
                        {
                            /*val result = resultFirst.map { map ->
                                GetJobDetailsListItemResponse(
                                    arrivalDateTime = map["arrivalDateTime"] as? String ?: "",
                                    imoNumber = map["imoNumber"] as? String ?: "",
                                    importJobDetailId = map["importJobDetailId"] as Int,
                                    importJobMasterId = map["importJobMasterId"] as Int,
                                    jobFileName = map["jobFileName"] as? String ?: "",
                                    vesselName = map["vesselName"] as? String ?: "",
                                )
                            }*/
                            var getJobDetailsListItemResponse=GetJobDetailsListItemResponse(resultFirst.batchno,0,0,"")
                            getJobDetailsListMobileResponse.add( getJobDetailsListItemResponse)
                            ediAdapter.updateImportDataList(getJobDetailsListMobileResponse)
                        }
                        else {
                             //   (activity as FragmentHostActivity).showCustomDialog("No Data", resultResponse.responseMessage)
                        }
                    }
                }
                is Resource.Error -> {

                    response.message?.let { errorMessage ->
                        (activity as FragmentHostActivity).showCustomDialog("Error", errorMessage)
                        //Log.e(TAG, "onViewCreated: $errorMessage")
                        if (errorMessage == "Unauthorized" || errorMessage == "Authentication token expired" || errorMessage == Constants.CONFIG_ERROR) {
                            (requireActivity() as FragmentHostActivity).showCustomDialog(
                                "Session Expired",
                                "Please re-login to continue"
                            )
                        }
                    }
                }
                is Resource.Loading -> {

                }
            }
        })

        t.scheduleAtFixedRate(tt, 1000, 1000)
    }
    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

            if (dy > 0 && (visibleItemCount + firstVisibleItemPosition) >= totalItemCount) {
                // Scrolling down, load next page
                viewModel.jobDetailsLoadNextPage(baseUrl, jwtToken,importJobMasterId)
            } else if (dy < 0 && firstVisibleItemPosition == 0) {
                // Scrolling up and reached the top, load previous page
                viewModel.jobDetailsLoadPreviousPage(baseUrl, jwtToken,importJobMasterId)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (t != null) {
            t.cancel()
            tt!!.cancel()
        }
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
            if (isAdded) { // Check if the fragment is still attached to the activity
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

    override fun onResume() {
        super.onResume()
        viewModel.jobDetailsLoadNextPage(baseUrl, jwtToken,importJobMasterId)

        viewModel.getJobDetailsMobileLiveData.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    getJobDetailsListMobileResponse.clear()
                    response.data?.let { resultResponse ->
                        //Log.d(TAG, "onViewCreated: $resultResponse")
                        // var result=resultResponse.responseList as ArrayList<GetJobMasterListMobileResponse>
                        //if (result.isNotEmpty()) {
                        /*     jobId = result.
                             rakeRefNo = resultResponse[0].rakeRefNo
                             createdDate = resultResponse[0].createdDate
                             transportationMode = resultResponse[0].transportMode
                             totalCoils = resultResponse[0].pendingUnloadingRecord*/
                        // getJobMasterListMobileResponse.addAll(result as ArrayList<GetJobMasterListMobileResponse>)
                        //   ediAdapter.differ.submitList(getJobMasterListMobileResponse)
                        // } else {
                        //     (activity as FragmentHostActivity).showCustomDialog("NO DATA", "")
                        //}
                       /* val resultList = resultResponse.responseList as? List<Map<String, Any>>
                        if (resultList?.isNotEmpty() == true) {
                            val result = resultList.map { map ->
                                GetJobDetailsListItemResponse(
                                    batchNumber = map["batchNumber"] as? String ?: "",
                                    importJobDetailId = map["importJobDetailId"] as? Int ?: 0,
                                    productId = map["productId"] as? Int ?: 0,
                                    status = map["status"] as? String ?: "",
                                )
                            }
                            binding.ImportTvEmptyList.visibility = View.GONE
                            getJobDetailsListMobileResponse.addAll(result)
                            ediAdapter.updateCoilDataList(getJobDetailsListMobileResponse)
                        } else {
                            (activity as FragmentHostActivity).showCustomDialog("NO DATA", "")
                        }*/

                       val responseObject = resultResponse.responseObject as? Map<*, *>
                        binding.apply {
                            val stockTallyCount =
                                responseObject?.get("stockTallyCount") as? Double?:0.0
                            val pendingCount =
                                responseObject?.get("pendingCount")as? Double?:0.0

                            val totalCount =
                                responseObject?.get("totalCount") as? Double?:0.0

                            val instockCount =
                                responseObject?.get("instockCount") as? Double?:0.0

                            ImportTvFoundValue.setText("${stockTallyCount.roundToInt()}")
                            ImportTvPendingValue.setText("${pendingCount.roundToInt()}")
                            ImportTvTotalValue.setText("${totalCount.roundToInt()}")

                        }

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
                           }
                           binding.ImportTvEmptyList.visibility = View.GONE
                           getJobDetailsListMobileResponse.addAll(result)
                           ediAdapter.updateImportDataList(getJobDetailsListMobileResponse)
                       }
                       else {
                           (activity as FragmentHostActivity).showCustomDialog("NO DATA", "")
                       }
                    }
                }
                is Resource.Error -> {

                    response.message?.let { errorMessage ->
                        //Log.e(TAG, "onViewCreated: $errorMessage")
                        if (errorMessage == "Unauthorized" || errorMessage == "Authentication token expired" || errorMessage == Constants.CONFIG_ERROR) {
                            (requireActivity() as FragmentHostActivity).showCustomDialog(
                                "Session Expired",
                                "Please re-login to continue"
                            )
                        }
                    }
                }
                is Resource.Loading -> {

                }
            }
        })

    }
    fun setBarcode(data: String?) {
        data?.let {
            scannedBarcode = it.split(" ")[0].trim()
        }

    /*    val user = session.getUserDetails()
        val firstName = user["firstName"]
        val lastName = user["lastName"]
        val jwtToken = user["jwtToken"]
        val refreshToken = user["refreshToken"]
        val email = user["email"]
*/

        (activity as FragmentHostActivity).runOnUiThread {
            viewModel.getReceivedProduct(jwtToken!!,baseUrl,scannedBarcode,1, "${currentLocation?.latitude},${currentLocation?.longitude}")        }
    }
   /* private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        binding.progressBar.visibility = View.INVISIBLE
    }*/
}