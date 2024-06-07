package com.kemarport.kyms.fragments.operator.importupdate.importpicker

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kemarport.kyms.R
import com.kemarport.kyms.activities.FragmentHostActivity
import com.kemarport.kyms.adapters.importupdate.ImportEdiAdapter
import com.kemarport.kyms.databinding.FragmentImportStockTallyBinding
import com.kemarport.kyms.helper.Constants
import com.kemarport.kyms.helper.Resource
import com.kemarport.kyms.helper.SessionManager
import com.kemarport.kyms.models.importupdate.importstocktally.importjobmaster.ImportJobMasterResponseList

import com.kemarport.kyms.repository.KYMSRepository
import com.kemarport.kyms.viewmodel.importupdate.ImportStockTallyViewModel
import com.kemarport.kyms.viewmodel.importupdate.ImportStockTallyViewModelFactory
import es.dmoral.toasty.Toasty
import kotlin.math.roundToInt


class ImportStockTallyFragment : Fragment(){

    private var _binding: FragmentImportStockTallyBinding? = null

    // This property is only valid between onCreateView and
// onDestroyView.
    private val binding get() = _binding!!

    private lateinit var viewModel: ImportStockTallyViewModel
    private lateinit var ediAdapter: ImportEdiAdapter
    private val TAG = "ImportStockTallyFragment"
    var jwtToken:String=""

    /*private var jobId: Int = 0
    private lateinit var rakeRefNo: String
    private lateinit var createdDate: String
    private lateinit var transportationMode: String
    private var totalCoils: Int = 0*/
    private var serverIpSharedPrefText: String? = null
    private var portSharedPrefText: Int? = 0
    private lateinit var adminUser: HashMap<String, String?>
    private lateinit var user: HashMap<String, String?>
    private lateinit var session: SessionManager
    private lateinit var baseUrl: String
    private var getJobMasterListMobileResponse=ArrayList<ImportJobMasterResponseList>()
    private var skipRows = 0
    private val rowSize = 15
    private var isLoading = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        _binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_import_stock_tally,
                container,
                false
            )
        val view = binding.root


        val toolbar =
            (activity as FragmentHostActivity).findViewById<Toolbar>(R.id.tbFragmentHostActivity)
        toolbar?.let {
            val titleTextView = it.findViewById<TextView>(R.id.tvToolbarTitle)
            titleTextView.text = "Import List of EDI"
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


        //baseUrl=Constants.BASE_URL_NEW
       // Log.d(TAG, "onCreate: $baseUrl")


        setupRecyclerView()
       /* ediAdapter.setOnItemClickListener {
            val action =
                StockTallyFragmentDirections.actionStockTallyFragmentToEdiDetailsFragment(
                    it.jobId,
                    it.rakeRefNo,
                    it.createdDate,
                    it.transportMode,
                    it.pendingUnloadingRecord
                )
            findNavController().navigate(action)
        }*/
        ediAdapter.setOnItemClickListener {
            val action =
                ImportStockTallyFragmentDirections.actionImportStockTallyFragmentToImportEditDetailsFragment(
                    it.importJobMasterId.roundToInt(),
                )
            findNavController().navigate(action)
        }
    }
    override fun onResume() {
        super.onResume()

        viewModel.getJobMasterMobile(baseUrl, jwtToken,0,15)
        hideProgressBar()
        viewModel.getJobMasterMobileLiveData.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    getJobMasterListMobileResponse.clear()
                    response.data?.let { resultResponse ->
                        val resultList = resultResponse.responseList as? List<Map<String, Any>>
                        if (resultList?.isNotEmpty() == true) {
                            val result = resultList.map { map ->
                                ImportJobMasterResponseList(
                                    arrivalDateTime = map["arrivalDateTime"] as? String ?: "",
                                    imoNumber = map["imoNumber"] as? String ?: "",
                                    importJobDetailId = map["importJobDetailId"] as? Double ?: 0.0,
                                    importJobMasterId = map["importJobMasterId"] as? Double ?: 0.0,
                                    instockCount = map["instockCount"] as? Double ?: 0.0,
                                    jobFileName = map["jobFileName"] as? String ?: "",
                                    pendingCount = map["pendingCount: "] as? Double ?: 0.0,
                                    stockTallyCount = map["stockTallyCount"] as? Double ?: 0.0,
                                    totalCount = map["totalCount"] as? Double ?: 0.0,
                                    vesselName = map["vesselName"] as? String ?: "",
                                )
                            }

                            getJobMasterListMobileResponse.addAll(result)
                            ediAdapter.differ.submitList(getJobMasterListMobileResponse)
                        }
                    /*    val responseObject = resultResponse.responseObject as? Map<*, *>
                        val importJobMasterMobileResponseList =
                            responseObject?.get("importJobMasterMobileResponse") as? List<*>
                        if (importJobMasterMobileResponseList?.isNotEmpty() == true) {
                            val result = importJobMasterMobileResponseList.mapNotNull { item ->
                                if (item is Map<*, *>) {
                                    ImportJobMasterMobileResponse(
                                        arrivalDateTime = item["arrivalDateTime"] as? String ?: "",
                                        imoNumber = item["imoNumber"] as? String ?: "",
                                        importJobDetailId = (item["importJobDetailId"] as? Double)?.toInt()
                                            ?: 0,
                                        importJobMasterId = (item["importJobMasterId"] as? Double)?.toInt()
                                            ?: 0,
                                        jobFileName = item["jobFileName"] as? String ?: "",
                                        vesselName = item["vesselName"] as? String ?: ""
                                    )
                                } else {
                                    null
                                }

                            }
                            getJobMasterListMobileResponse.addAll(result)
                            ediAdapter.differ.submitList(getJobMasterListMobileResponse)
                        }*/


                    }
                }
                is Resource.Error -> {
                    hideProgressBar()
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
                    showProgressBar()
                }
            }
        })


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



    private fun setupRecyclerView() {
        ediAdapter = ImportEdiAdapter()
        binding.rvUnloading.apply {
            adapter = ediAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(scrollListener)
        }


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
                viewModel.loadNextPage(baseUrl, jwtToken)
            } else if (dy < 0 && firstVisibleItemPosition == 0) {
                // Scrolling up and reached the top, load previous page
                viewModel.loadPreviousPage(baseUrl, jwtToken)
            }
        }
    }
    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        binding.progressBar.visibility = View.GONE
    }

}




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
/*  val resultFirst = resultResponse.responseObject as? GetJobMasterResponseObject
val resultList = resultFirst?.importJobMasterMobileResponse
if (resultFirst != null) {
 *//*   val result = resultList.map { map ->
                                ImportJobMasterMobileResponse(
                                    arrivalDateTime = map["arrivalDateTime"] as? String ?: "",
                                    imoNumber = map["imoNumber"] as? String ?: "",
                                    importJobDetailId = map["importJobDetailId"] as Int,
                                    importJobMasterId = map["importJobMasterId"] as Int,
                                    jobFileName = map["jobFileName"] as? String ?: "",
                                    vesselName = map["vesselName"] as? String ?: "",
                                )
                            }*//*
                            if(resultList?.isNotEmpty() == true)
                            {
                                getJobMasterListMobileResponse.addAll(resultList)
                                ediAdapter.differ.submitList(getJobMasterListMobileResponse)
                            }

                        } else {
                            (activity as FragmentHostActivity).showCustomDialog("NO DATA", "")
                        }*/

/*  val resultList = when (resultResponse.responseObject) {
    is List<*> -> resultResponse.responseObject as? List<Map<String, Any>>
    is Map<*, *> -> listOf(resultResponse.responseObject as Map<String, Any>)
    else -> null
}

if (resultList?.isNotEmpty() == true) {
    val result = resultList.map { map ->
        // Implement your parsing logic here
        ImportJobMasterMobileResponse(
            arrivalDateTime = map["arrivalDateTime"] as? String ?: "",
            imoNumber = map["imoNumber"] as? String ?: "",
            importJobDetailId = (map["importJobDetailId"] as? Double)?.toInt() ?: 0,
            importJobMasterId = (map["importJobMasterId"] as? Double)?.toInt() ?: 0,
            jobFileName = map["jobFileName"] as? String ?: "",
            vesselName = map["vesselName"] as? String ?: ""
        )
    }

    getJobMasterListMobileResponse.addAll(result)
    ediAdapter.differ.submitList(getJobMasterListMobileResponse)
} else {
    (activity as FragmentHostActivity).showCustomDialog("NO DATA", "")
}*/
