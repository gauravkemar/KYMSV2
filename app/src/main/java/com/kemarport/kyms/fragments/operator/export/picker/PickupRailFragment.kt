package com.kemarport.kyms.fragments.operator.export.picker

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.kemarport.kyms.databinding.FragmentPickupRailBinding
import com.kemarport.kyms.helper.Constants
import com.kemarport.kyms.helper.Resource
import com.kemarport.kyms.models.export.CoilData.CoilData
import com.kemarport.kyms.models.export.EDI.Coil
import com.kemarport.kyms.models.export.EDI.EdiConfirmationRequest
import com.kemarport.kyms.repository.KYMSRepository
import com.kemarport.kyms.viewmodel.export.PickupRailVMProviderFactory
import com.kemarport.kyms.viewmodel.export.PickupRailViewModel
import es.dmoral.toasty.Toasty

class PickupRailFragment : Fragment(), View.OnClickListener {
    private var _binding: FragmentPickupRailBinding? = null
    private val TAG = "PickupRailFragment"
    private lateinit var arrayAdapter: ArrayAdapter<String>
    private val locationNames = mutableListOf(Constants.DEFAULT_LOCATION_RAIL)
    private lateinit var viewModel: PickupRailViewModel
    private var scannedBarcode: String? = ""
    private lateinit var ediDetailsAdapter: EdiDetailsAdapter
    private val barcodes = mutableListOf<String?>()
    private val scannedCoilDataList = mutableListOf<CoilData>()
    private val coilList = mutableListOf<Coil?>()
    private var locationID: Int? = 1
    private lateinit var ediConfirmationRequest: EdiConfirmationRequest

    // This property is only valid between onCreateView and
// onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_pickup_rail, container, false)
        val view = binding.root
        binding.listener = this

        val toolbar =
            (activity as FragmentHostActivity).findViewById<Toolbar>(R.id.tbFragmentHostActivity)
        toolbar?.let {
            val titleTextView = it.findViewById<TextView>(R.id.tvToolbarTitle)
            titleTextView.text = "Pickup from Rail"
            it.setNavigationIcon(R.drawable.ic_back_icon)
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Toasty.Config.getInstance()
            .setGravity(Gravity.CENTER)
            .apply() // required

        val kymsRepository = KYMSRepository()
        val application = (activity as FragmentHostActivity).application
        val viewModelProviderFactory =
            PickupRailVMProviderFactory(application, kymsRepository)
        viewModel =
            ViewModelProvider(this, viewModelProviderFactory)[PickupRailViewModel::class.java]

        arrayAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, locationNames)
        arrayAdapter.setDropDownViewResource(R.layout.item_ddm_location)
        binding.spinnerLoc.adapter = arrayAdapter
        binding.spinnerLoc.isClickable = false

        setupRecyclerView()

        viewModel.validatePickerMutableLiveData.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    var coil: Coil?
                    response.data?.let { resultResponse ->
                        hideProgressBar()
                        Log.d(
                            TAG,
                            "setItemScanningStatus: Success Message ${resultResponse.statusMessage}"
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
                                    binding.btnPickFromRail.visibility = View.VISIBLE

                                    coil = Coil(scannedBarcode)
                                    coilList.add(coil)
                                    ediConfirmationRequest =
                                        EdiConfirmationRequest(
                                            locationID,
                                            coilList
                                        )
                                    Log.d(TAG, "setBarcode: $ediConfirmationRequest")
                                }
                            }
                            else->
                            {
                                (requireActivity() as FragmentHostActivity).setScannerAudio("error")
                                (activity as FragmentHostActivity).showCustomDialog(
                                    resultResponse.batchNumber,
                                    resultResponse.statusMessage
                                )
                            }
                            /*"Not Found" -> {
                                (activity as FragmentHostActivity).showCustomDialog(
                                    resultResponse.batchNumber+" "+resultResponse.statusMessage,
                                    ""
                                )
                            }
                            "Already Exist" -> {
                                (activity as FragmentHostActivity).showCustomDialog(
                                    resultResponse.batchNumber+" "+resultResponse.statusMessage,
                                    ""
                                )
                            }
                            "Invalid" -> {
                                (activity as FragmentHostActivity).showCustomDialog(
                                    "${resultResponse.batchNumber} not found on location: $DEFAULT_LOCATION_RAIL",
                                    ""
                                )
                            }*/
                        }
                    }
                }
                is Resource.Error -> {
                    response.message?.let { errorMessage ->
                        hideProgressBar()
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

            ediDetailsAdapter.updateCoilDataList(scannedCoilDataList)
        })

        viewModel.pickCoilMutableLiveData.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    response.data?.let { resultResponse ->
                        hideProgressBar()
                        when (resultResponse.responseMessage) {
                            "All Product Picked Successfully" -> {
                                binding.btnPickFromRail.visibility = View.GONE
                                Toasty.success(
                                    activity as FragmentHostActivity,
                                    resultResponse.responseMessage,
                                    Toast.LENGTH_SHORT
                                ).show()
                                barcodes.clear()
                                coilList.clear()
                                scannedCoilDataList.clear()
                                ediDetailsAdapter.updateCoilDataList(scannedCoilDataList)
                                binding.tvEmptyList.visibility = View.VISIBLE
                                binding.btnPickFromRail.visibility = View.GONE
                            }
                        }
                    }
                }
                is Resource.Error -> {
                    response.message?.let { errorMessage ->
                        hideProgressBar()
                        Log.e(TAG, "onViewCreated: ${response.message}")
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
    }

    override fun onClick(view: View?) {
        view?.let {
            when (it.id) {
                binding.btnPickFromRail.id -> {
//                    viewModel.pickCoil(ediConfirmationRequest)
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
        (activity as FragmentHostActivity).runOnUiThread {
//            viewModel.validatePicker(scannedBarcode)
        }
    }

    private fun setupRecyclerView() {
        Log.d(TAG, "setupRecyclerView: called")
        ediDetailsAdapter = EdiDetailsAdapter()
        binding.rvPickupRail.apply {
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