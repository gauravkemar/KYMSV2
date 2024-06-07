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
import com.kemarport.kyms.databinding.FragmentOperatorBinding
import com.kemarport.kyms.databinding.FragmentOperatorMainOptionsBinding
import com.kemarport.kyms.helper.SessionManager

class OperatorMainOptionsFragment : Fragment() , View.OnClickListener {
    lateinit var binding:FragmentOperatorMainOptionsBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_operator_main_options, container, false)
        binding.listener = this
        val toolbar =
            (activity as FragmentHostActivity).findViewById<Toolbar>(R.id.tbFragmentHostActivity)
        toolbar?.let {
            val titleTextView = it.findViewById<TextView>(R.id.tvToolbarTitle)
            titleTextView.text = "KYMS Operator Login"
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val menuHost: MenuHost = requireActivity()
        setupMenu(menuHost)
    }

    override fun onClick(p0: View?) {
        p0?.let {
            when (it.id) {
                binding.mcvOperatorImport .id -> {
                    val action = OperatorMainOptionsFragmentDirections.actionOperatorMainOptionsFragmentToImportOperatorFragment()

                    findNavController().navigate(action)
                }
                binding.mcvOperatorExport .id -> {
                    val action = OperatorMainOptionsFragmentDirections.actionOperatorMainOptionsFragmentToOperatorFragment()
                    findNavController().navigate(action)
                }
            }
        }
    }

    private fun setupMenu(menuHost: MenuHost) {
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onPrepareMenu(menu: Menu) {
                // Handle for example visibility of menu items
            }

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_logout, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Validate and handle the selected menu item
                when (menuItem.itemId) {
                    R.id.miLogout -> {
                        showDialog()
                    }
                    android.R.id.home -> {
                        (activity as FragmentHostActivity).onBackPressed()
                    }
                }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun showDialog() {
        var alertDialog: AlertDialog? = null
        val builder = AlertDialog.Builder(activity as FragmentHostActivity)
            .setTitle("Log Out")
            .setMessage("Are you sure?")
            .setIcon(R.drawable.ic_logout)
            .setPositiveButton("Yes") { dialogInterface, which ->
                logUserOut()
            }
            .setNegativeButton("No") { dialogInterface, which ->
                alertDialog?.dismiss()
            }
        alertDialog = builder.create()
        alertDialog.setCancelable(true)
        alertDialog.show()
    }

    private fun logUserOut() {
        val sessionManager = SessionManager(activity as FragmentHostActivity)
        sessionManager.logoutUser()
        Intent(activity, LoginActivity::class.java).apply {
            startActivity(this)
        }
        activity?.finish()
    }
}