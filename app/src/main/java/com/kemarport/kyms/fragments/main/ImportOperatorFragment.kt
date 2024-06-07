package com.kemarport.kyms.fragments.main

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.kemarport.kyms.R
import com.kemarport.kyms.activities.FragmentHostActivity
import com.kemarport.kyms.activities.LoginActivity
import com.kemarport.kyms.databinding.FragmentImportOperatorBinding
import com.kemarport.kyms.databinding.FragmentOperatorBinding
import com.kemarport.kyms.helper.SessionManager


class ImportOperatorFragment : Fragment(),View.OnClickListener {
    private var _binding: FragmentImportOperatorBinding? = null
    private val TAG = "OperatorFragment"

    // This property is only valid between onCreateView and
// onDestroyView.
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_import_operator, container, false)
        val view = binding.root
        binding.listener = this

        val toolbar =
            (activity as FragmentHostActivity).findViewById<Toolbar>(R.id.tbFragmentHostActivity)
        toolbar?.let {
            val titleTextView = it.findViewById<TextView>(R.id.tvToolbarTitle)
            titleTextView.text = "Import Operator"
        }

        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onClick(view: View?) {
        view?.let {
            when (it.id) {
                binding.cvPicker.
                id -> {
                    val action = ImportOperatorFragmentDirections.actionImportOperatorFragmentToImportPickerFragment()
                    findNavController().navigate(action)
                }
                binding.cvDropper.id -> {
                    val action =
                        ImportOperatorFragmentDirections.actionImportOperatorFragmentToImportDropperFragment()
                    findNavController().navigate(action)
                }
            }
        }
    }





}