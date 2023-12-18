package com.kemarport.kyms.fragments.operator.picker

import android.os.Bundle
import android.util.Log
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
import com.kemarport.kyms.R
import com.kemarport.kyms.activities.FragmentHostActivity
import com.kemarport.kyms.adapters.EdiAdapter
import com.kemarport.kyms.databinding.FragmentStockTallyBinding
import com.kemarport.kyms.helper.Constants
import com.kemarport.kyms.helper.Resource
import com.kemarport.kyms.helper.SessionManager
import com.kemarport.kyms.repository.KYMSRepository
import com.kemarport.kyms.viewmodel.StockTallyVMProviderFactory
import com.kemarport.kyms.viewmodel.StockTallyViewModel
import es.dmoral.toasty.Toasty

class StockTallyFragment : Fragment(), View.OnClickListener {
    private var _binding: FragmentStockTallyBinding? = null

    // This property is only valid between onCreateView and
// onDestroyView.
    private val binding get() = _binding!!

    private lateinit var viewModel: StockTallyViewModel
    private lateinit var ediAdapter: EdiAdapter
    private val TAG = "StockTallyFragment"

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_stock_tally,
                container,
                false
            )
        val view = binding.root
        binding.listener = this

        val toolbar =
            (activity as FragmentHostActivity).findViewById<Toolbar>(R.id.tbFragmentHostActivity)
        toolbar?.let {
            val titleTextView = it.findViewById<TextView>(R.id.tvToolbarTitle)
            titleTextView.text = "List of EDI"
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
            StockTallyVMProviderFactory(application, kymsRepository)
        viewModel =
            ViewModelProvider(this, viewModelProviderFactory)[StockTallyViewModel::class.java]

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
        //baseUrl=Constants.BASE_URL_NEW
        Log.d(TAG, "onCreate: $baseUrl")

        setupRecyclerView()

        ediAdapter.setOnItemClickListener {
            val action =
                StockTallyFragmentDirections.actionStockTallyFragmentToEdiDetailsFragment(
                    it.jobId,
                    it.rakeRefNo,
                    it.createdDate,
                    it.transportMode,
                    it.pendingUnloadingRecord
                )
            findNavController().navigate(action)
        }
    }

    override fun onResume() {
        super.onResume()

        val jwtToken = user["jwtToken"]
        viewModel.getEdiData(baseUrl, jwtToken)
        viewModel.ediMutableLiveData.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let { resultResponse ->
                        Log.d(TAG, "onViewCreated: $resultResponse")
                        if (resultResponse.isNotEmpty()) {
                            /*jobId = resultResponse[0].jobId
                            rakeRefNo = resultResponse[0].rakeRefNo
                            createdDate = resultResponse[0].createdDate
                            transportationMode = resultResponse[0].transportMode
                            totalCoils = resultResponse[0].pendingUnloadingRecord*/
                            ediAdapter.differ.submitList(resultResponse)
                        } else {
                            (activity as FragmentHostActivity).showCustomDialog("NO DATA", "")
                        }
                    }
                }
                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let { errorMessage ->
                        Log.e(TAG, "onViewCreated: $errorMessage")
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

    override fun onClick(view: View?) {

    }

    private fun setupRecyclerView() {
        ediAdapter = EdiAdapter()
        binding.rvUnloading.apply {
            adapter = ediAdapter
            layoutManager = LinearLayoutManager(activity)
        }
    }

    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        binding.progressBar.visibility = View.INVISIBLE
    }
}