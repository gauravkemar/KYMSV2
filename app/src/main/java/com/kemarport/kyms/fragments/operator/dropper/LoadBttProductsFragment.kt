package com.kemarport.kyms.fragments.operator.dropper

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.kemarport.kyms.databinding.FragmentLoadBttProductsBinding
import com.kemarport.kyms.helper.Constants
import com.kemarport.kyms.helper.Resource
import com.kemarport.kyms.helper.SessionManager
import com.kemarport.kyms.models.CoilData.CoilData
import com.kemarport.kyms.repository.KYMSRepository
import com.kemarport.kyms.viewmodel.LoadCoilsBTTVMProviderFactory
import com.kemarport.kyms.viewmodel.LoadCoilsBTTViewModel
import es.dmoral.toasty.Toasty

class LoadBttProductsFragment : Fragment(), View.OnClickListener {
    private var _binding: FragmentLoadBttProductsBinding? = null
    private val TAG = "LoadBttProductsFragment"
    private lateinit var viewModel: LoadCoilsBTTViewModel
    private var scannedBarcode: String? = ""
    private lateinit var ediDetailsAdapter: EdiDetailsAdapter
    private var serverIpSharedPrefText: String? = null
    private var portSharedPrefText: Int? = 0
    private lateinit var user: HashMap<String, String?>
    private lateinit var session: SessionManager
    private lateinit var baseUrl: String
    private val barcodes = mutableListOf<String?>()
    private val scannedCoilDataList = mutableListOf<CoilData>()

    // This property is only valid between onCreateView and
// onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_load_btt_products, container, false)
        val view = binding.root
        binding.listener = this

        val toolbar =
            (activity as FragmentHostActivity).findViewById<Toolbar>(R.id.tbFragmentHostActivity)
        toolbar?.let {
            val titleTextView = it.findViewById<TextView>(R.id.tvToolbarTitle)
            titleTextView.text = "Load BTT Products"
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
            LoadCoilsBTTVMProviderFactory(application, kymsRepository)
        viewModel =
            ViewModelProvider(this, viewModelProviderFactory)[LoadCoilsBTTViewModel::class.java]

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

        setupRecyclerView()

        viewModel.loadCoilsBttMutableLiveData.observe(viewLifecycleOwner, Observer { response ->
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
                                        "${resultResponse.batchNumber} marked for BTT Successfully",
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
//                                    binding.etLoadBTTVehicleNo.setText("")
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
                                    "${resultResponse.batchNumber} marking for BTT is Mandatory",
                                    ""
                                )
                            }
                            "Found" -> {
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
            val loadBttRemark = binding.etBTTRemark.text.toString()
            val loadBttVehicleNo = binding.etLoadBTTVehicleNo.text.toString()
            if (loadBttVehicleNo.isNotEmpty()) {
                viewModel.loadCoilBTT(
                    jwtToken,
                    loadBttVehicleNo,
                    scannedBarcode,
                    loadBttRemark,
                    baseUrl
                )
            } else {
                (activity as FragmentHostActivity).showCustomDialog(
                    "",
                    "Train Number is mandatory!"
                )
            }
        }
    }

    private fun setupRecyclerView() {
        Log.d(TAG, "setupRecyclerView: called")
        ediDetailsAdapter = EdiDetailsAdapter()
        binding.rvBTS.apply {
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