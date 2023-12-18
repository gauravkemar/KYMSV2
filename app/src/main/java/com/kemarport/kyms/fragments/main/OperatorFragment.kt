package com.kemarport.kyms.fragments.main

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.kemarport.kyms.R
import com.kemarport.kyms.activities.FragmentHostActivity
import com.kemarport.kyms.activities.LoginActivity
import com.kemarport.kyms.databinding.FragmentOperatorBinding
import com.kemarport.kyms.helper.SessionManager

class OperatorFragment : Fragment(), View.OnClickListener {
    private var _binding: FragmentOperatorBinding? = null
    private val TAG = "OperatorFragment"

    // This property is only valid between onCreateView and
// onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_operator, container, false)
        val view = binding.root
        binding.listener = this

        val toolbar =
            (activity as FragmentHostActivity).findViewById<Toolbar>(R.id.tbFragmentHostActivity)
        toolbar?.let {
            val titleTextView = it.findViewById<TextView>(R.id.tvToolbarTitle)
            titleTextView.text = getString(R.string.app_name)
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val menuHost: MenuHost = requireActivity()
        setupMenu(menuHost)
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
                    val action = OperatorFragmentDirections.actionOperatorFragmentToPickerFragment()
                    findNavController().navigate(action)
                }
                binding.cvDropper.id -> {
                    val action =
                        OperatorFragmentDirections.actionOperatorFragmentToDropperFragment()
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