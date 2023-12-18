package com.kemarport.kyms.fragments.operator.dropper

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.kemarport.kyms.R
import com.kemarport.kyms.activities.FragmentHostActivity
import com.kemarport.kyms.adapters.EdiDetailsAdapter
import com.kemarport.kyms.databinding.FragmentDropBerthBinding
import com.kemarport.kyms.helper.Constants.Companion.BERTH_LOCATION_1
import com.kemarport.kyms.helper.Constants.Companion.BERTH_LOCATION_2
import com.kemarport.kyms.helper.Resource
import com.kemarport.kyms.models.CoilData.CoilData
import com.kemarport.kyms.models.upload.Coil
import com.kemarport.kyms.models.upload.CoilRequest
import com.kemarport.kyms.repository.KYMSRepository
import com.kemarport.kyms.viewmodel.DropBerthVMProviderFactory
import com.kemarport.kyms.viewmodel.DropBerthViewModel
import es.dmoral.toasty.Toasty

class DropBerthFragment : Fragment(), View.OnClickListener {
    private var _binding: FragmentDropBerthBinding? = null
    private val TAG = "DropBerthFragment"
    private lateinit var arrayAdapter: ArrayAdapter<String>
    private val locationNames = mutableListOf(BERTH_LOCATION_1, BERTH_LOCATION_2)
    private lateinit var viewModel: DropBerthViewModel
    private var scannedBarcode: String? = ""
    private lateinit var ediDetailsAdapter: EdiDetailsAdapter
    private lateinit var berthLocation: String
    private val operationType = "Droped"
    private val barcodes = mutableListOf<String?>()
    private val scannedCoilDataList = mutableListOf<CoilData>()
    private val coilList = mutableListOf<Coil?>()
    private lateinit var coilRequest: CoilRequest

    // This property is only valid between onCreateView and
// onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_drop_berth, container, false)
        val view = binding.root
        binding.listener = this

        val toolbar =
            (activity as FragmentHostActivity).findViewById<Toolbar>(R.id.tbFragmentHostActivity)
        toolbar?.let {
            val titleTextView = it.findViewById<TextView>(R.id.tvToolbarTitle)
            titleTextView.text = "Drop at Berth"
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
            DropBerthVMProviderFactory(application, kymsRepository)
        viewModel =
            ViewModelProvider(this, viewModelProviderFactory)[DropBerthViewModel::class.java]

        arrayAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, locationNames)
        arrayAdapter.setDropDownViewResource(R.layout.item_ddm_location)
        binding.spinnerLoc.adapter = arrayAdapter
        binding.spinnerLoc.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                berthLocation = binding.spinnerLoc.selectedItem.toString()
                barcodes.clear()
                coilList.clear()
                scannedCoilDataList.clear()
                ediDetailsAdapter.updateCoilDataList(scannedCoilDataList)
                binding.tvEmptyList.visibility = View.VISIBLE
                binding.btnDropToBerth.visibility = View.GONE
                Log.d(TAG, "onItemSelected: $berthLocation")
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

        }

        setupRecyclerView()

        viewModel.validationStockToBerthMutableLiveData.observe(
            viewLifecycleOwner,
            Observer { response ->
                when (response) {
                    is Resource.Success -> {
                        var coil: Coil?
                        response.data?.let { resultResponse ->
                            hideProgressBar()
                            Log.d(
                                TAG,
                                "onViewCreated: Pickup Storage Barcode Status Success ${resultResponse.statusMessage}"
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
                                        binding.btnDropToBerth.visibility = View.VISIBLE

                                        coil = Coil(scannedBarcode)
                                        coilList.add(coil)
                                        coilRequest =
                                            CoilRequest(berthLocation, operationType, coilList)
                                        Log.d(TAG, "setBarcode: $coilRequest")
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
                                }*/
                            }
                        }
                    }
                    is Resource.Error -> {
                        response.message?.let { errorMessage ->
                            hideProgressBar()
                            Log.e(
                                TAG,
                                "onViewCreated: Pickup Storage Barcode Status Error $errorMessage"
                            )
                        }
                    }
                    is Resource.Loading -> {
                        showProgressBar()
                    }
                }

                ediDetailsAdapter.updateCoilDataList(scannedCoilDataList)
            })

        viewModel.stockToBerthMutableLiveData.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    response.data?.let { resultResponse ->
                        hideProgressBar()
                        Log.d(
                            TAG,
                            "onViewCreated: ${resultResponse.responseMessage}"
                        )

                        when (resultResponse.responseMessage) {
                            "All Product loaded at Birth Sucessfully" -> {
                                binding.btnDropToBerth.visibility = View.GONE
                                Toasty.success(
                                    activity as FragmentHostActivity,
                                    "All products dropped at berth successfully"
                                ).show()
                                barcodes.clear()
                                coilList.clear()
                                scannedCoilDataList.clear()
                                ediDetailsAdapter.updateCoilDataList(scannedCoilDataList)
                                binding.tvEmptyList.visibility = View.VISIBLE
                                binding.btnDropToBerth.visibility = View.GONE
                            }
                            else -> {

                            }
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
                binding.btnDropToBerth.id -> {
//                    viewModel.stockToBerth(coilRequest)
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
        berthLocation = binding.spinnerLoc.selectedItem.toString()
        Log.d(TAG, "setBarcode: location after scanning - $berthLocation")
        (activity as FragmentHostActivity).runOnUiThread {
//            viewModel.validationStockToBerth(berthLocation, scannedBarcode, operationType)
        }
    }

    private fun setupRecyclerView() {
        Log.d(TAG, "setupRecyclerView: called")
        ediDetailsAdapter = EdiDetailsAdapter()
        binding.rvDropBerth.apply {
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