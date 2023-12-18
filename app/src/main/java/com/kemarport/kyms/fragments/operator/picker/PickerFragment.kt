package com.kemarport.kyms.fragments.operator.picker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.kemarport.kyms.R
import com.kemarport.kyms.activities.FragmentHostActivity
import com.kemarport.kyms.databinding.FragmentPickerBinding

class PickerFragment : Fragment(), View.OnClickListener {
    private var _binding: FragmentPickerBinding? = null
    private val TAG = "PickerFragment"

    // This property is only valid between onCreateView and
// onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_picker, container, false)
        val view = binding.root
        binding.listener = this

        val toolbar =
            (activity as FragmentHostActivity).findViewById<Toolbar>(R.id.tbFragmentHostActivity)
        toolbar?.let {
            val titleTextView = it.findViewById<TextView>(R.id.tvToolbarTitle)
            titleTextView.text = "Picker"
            it.setNavigationIcon(R.drawable.ic_back_icon)
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onClick(view: View?) {
        view?.let {
            when (it.id) {
                binding.cvStockTally.id -> {
                    val action =
                        PickerFragmentDirections.actionPickerFragmentToStockTallyFragment()
                    findNavController().navigate(action)
                }
                binding.cvPickFromStorage.id -> {
                    val action =
                        PickerFragmentDirections.actionPickerFragmentToPickupStorageFragment()
                    findNavController().navigate(action)
                }
                binding.cvStockTallyWoAsn.id -> {
                    val action =
                        PickerFragmentDirections.actionPickerFragmentToStockTallyWoAsnFragment()
                    findNavController().navigate(action)
                }
            }
        }
    }
}