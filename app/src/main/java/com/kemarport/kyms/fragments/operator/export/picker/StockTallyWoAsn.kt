package com.kemarport.kyms.fragments.operator.export.picker

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
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
import com.kemarport.kyms.adapters.export.DelayedEdiAdapter
import com.kemarport.kyms.databinding.FragmentStockTallyWoAsnBinding
import com.kemarport.kyms.helper.Constants
import com.kemarport.kyms.helper.Resource
import com.kemarport.kyms.helper.SessionManager
import com.kemarport.kyms.repository.KYMSRepository
import com.kemarport.kyms.viewmodel.export.StockTallyWoAsnVM
import com.kemarport.kyms.viewmodel.export.StockTallyWoAsnVMProviderFactory
import com.google.android.material.textfield.TextInputEditText
import com.kemarport.kyms.models.export.withoutasn.CreateJobMasterRequest
import es.dmoral.toasty.Toasty
import java.util.*

class StockTallyWoAsn : Fragment(), View.OnClickListener {
    private var _binding: FragmentStockTallyWoAsnBinding? = null
    private lateinit var toolbar: Toolbar
    private lateinit var viewModel: StockTallyWoAsnVM
    private var serverIpSharedPrefText: String? = null
    private var portSharedPrefText: Int? = 0
    private lateinit var admin: HashMap<String, String?>
    private lateinit var session: SessionManager
    private lateinit var baseUrl: String
    private val TAG = "StockTallyWoAsn"

    // This property is only valid between onCreateView and
// onDestroyView.
    private val binding get() = _binding!!

    private lateinit var delayedEdiAdapter: DelayedEdiAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_stock_tally_wo_asn,
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

        val kymsRepository = KYMSRepository()
        val application = (activity as FragmentHostActivity).application
        val viewModelProviderFactory =
            StockTallyWoAsnVMProviderFactory(application, kymsRepository)
        viewModel =
            ViewModelProvider(this, viewModelProviderFactory)[StockTallyWoAsnVM::class.java]

        session = SessionManager(requireContext())
        admin = session.getAdminDetails()
        serverIpSharedPrefText = admin["serverIp"]
        portSharedPrefText = admin["port"]?.toInt()
        baseUrl = if (serverIpSharedPrefText == "192.168.1.52") {
            "http://$serverIpSharedPrefText:$portSharedPrefText/api/"
        } else {
            "http://$serverIpSharedPrefText:$portSharedPrefText/service/api/"
        }
        //baseUrl=Constants.BASE_URL_NEW
        Log.d(TAG, "onCreate: $baseUrl")
        val user = session.getUserDetails()
        val jwtToken = user["jwtToken"]

        viewModel.createJobMasterMutableLiveData.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    response.data?.let { resultResponse ->
                        Toasty.success(requireContext(), resultResponse.responseMessage).show()
                        if (resultResponse.statusCode == 201) {
                            viewModel.getDelayedEdiData(baseUrl, jwtToken)
                        } else if (resultResponse.statusCode == 409) {
                            (requireActivity() as FragmentHostActivity).showCustomDialog(
                                "",
                                "Delayed ASN Already Added"
                            )
                        } else {
                            (requireActivity() as FragmentHostActivity).showCustomDialog(
                                "",
                                "Error has occured, please try again"
                            )
                        }
                    }
                }
                is Resource.Error -> {
                    response.message?.let { errorMessage ->
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
        setupRecyclerView()

        delayedEdiAdapter.setOnItemClickListener {
            val action =
                StockTallyWoAsnDirections.actionStockTallyWoAsnToEdiDetailsFragment(
                    it.jobId,
                    it.rakeRefNo,
                    it.createdDate,
                    it.transportMode,
                    it.pendingUnloadingRecord
                )
            findNavController().navigate(action)
        }

        viewModel.getDelayedEdiData(baseUrl, jwtToken)
        viewModel.delayedEdiMutableLiveData.observe(viewLifecycleOwner, Observer { response ->
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
                            delayedEdiAdapter.differ.submitList(resultResponse)
                            if (binding.tvEmptyList.visibility == View.VISIBLE)
                                binding.tvEmptyList.visibility = View.INVISIBLE
                            if (dialog != null)
                                if (dialog!!.isShowing)
                                    dialog!!.dismiss()
                        } else {
                            binding.tvEmptyList.visibility = View.VISIBLE
                            (activity as FragmentHostActivity).showCustomDialog("NO DATA FOUND", "")
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

        binding.floatingActionButton.setOnClickListener {
            showAddRakeRefNoDialog()
        }
    }

    override fun onClick(view: View?) {

    }

    var builder: AlertDialog.Builder? = null
    var dialog: AlertDialog? = null

    private fun showAddRakeRefNoDialog() {
        builder = AlertDialog.Builder(requireActivity())
        val customLayout: View = layoutInflater.inflate(R.layout.dialog_add_rake_ref_no, null)
        val etRakeRefNo = customLayout.findViewById<TextInputEditText>(R.id.etRakeRefNo)
        val btnSubmit = customLayout.findViewById<Button>(R.id.btnSubmit)
        val btnCancel = customLayout.findViewById<Button>(R.id.btnCancel)
        builder!!.setView(customLayout)
        dialog = builder!!.create()
        btnSubmit.setOnClickListener { view: View? ->
            if (etRakeRefNo.getText().toString().trim { it <= ' ' } != "" || etRakeRefNo.getText()
                    .toString()
                    .trim { it <= ' ' }.isNotEmpty()) {
                val rakeRefNo: String =
                    etRakeRefNo.getText().toString().trim { it <= ' ' }
                        .uppercase(Locale.getDefault())

                val user = session.getUserDetails()
                val jwtToken = user["jwtToken"]
                val createJobMasterRequest =
                    CreateJobMasterRequest(rakeRefNo)
                viewModel.createJobMaster(jwtToken, createJobMasterRequest, baseUrl)

            } else {
                etRakeRefNo.setError("Please Add Number!")
            }
        }
        btnCancel.setOnClickListener { view: View? -> dialog!!.dismiss() }
        dialog!!.setCancelable(false)
        dialog!!.show()
        val window: Window? = dialog!!.getWindow()
        window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun setupRecyclerView() {
        delayedEdiAdapter = DelayedEdiAdapter()
        binding.rvDelayedEdiDetails.apply {
            adapter = delayedEdiAdapter
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