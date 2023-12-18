package com.kemarport.kyms.fragments.main

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.kemarport.kyms.R
import com.kemarport.kyms.activities.FragmentHostActivity
import com.kemarport.kyms.activities.LoginActivity
import com.kemarport.kyms.databinding.FragmentAdminBinding
import com.kemarport.kyms.helper.SessionManager

class AdminFragment : Fragment(), View.OnClickListener {
    private var _binding: FragmentAdminBinding? = null
    private var builder: AlertDialog.Builder? = null
    private var alert: AlertDialog? = null
    private var serverIpSharedPrefText: String? = null
    private var portSharedPrefText: Int? = 0
    private lateinit var session: SessionManager
    private lateinit var user: HashMap<String, String?>

    // This property is only valid between onCreateView and
// onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_admin, container, false)
        val view = binding.root
        binding.listener = this

        session = SessionManager(requireActivity())
        user = session.getAdminDetails()
        serverIpSharedPrefText = user["serverIp"]
        portSharedPrefText = user["port"]?.toInt()
        initFilter()
        binding.edServerIp.setText(serverIpSharedPrefText)
        if (portSharedPrefText.toString() == "null") {
            binding.edPort.setText("")
        } else {
            binding.edPort.setText(portSharedPrefText.toString())
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val menuHost: MenuHost = requireActivity()
        setupMenu(menuHost)
    }

    override fun onClick(view: View?) {
        view?.let {
            when (it.id) {
                R.id.btnSave -> {
                    val serverIp = binding.edServerIp.text.toString().trim()
                    var port = binding.edPort.text.toString().trim()
                    if (port.isEmpty()) {
                        port = "0"
                    }
                    val portNumber = port.toInt()
                    val serverIpSharedPref = user["serverIp"]
                    val portSharedPrefText = user["port"]?.toInt()
                    if (serverIp == "" || portNumber == 0) {
                        if (serverIp == "" && portNumber == 0) {
                            Toast.makeText(
                                activity,
                                "Please enter all values!!",
                                Toast.LENGTH_SHORT
                            ).show()
                            binding.edServerIp.error = "Please enter ip address"
                            binding.edPort.error = "Please enter value"
                        } else if (serverIp == "") {
                            Toast.makeText(activity, "Please Enter ServerIP!!", Toast.LENGTH_SHORT)
                                .show()
                            binding.edServerIp.error = "Please enter ip address"
                        } else if (portNumber == 0) {
                            Toast.makeText(
                                activity,
                                "Please Enter Port Number!!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        showDialog(serverIp, portNumber)
                        //if (serverIp != serverIpSharedPref || portNumber != portSharedPrefText) {

                        //}
                    }
                }
            }
        }
    }

    private fun showDialog(
        serverIp: String?,
        portNumber: Int
    ) {
        builder = AlertDialog.Builder(requireActivity())
        builder!!.setMessage("Changes will take effect after Re-Login!")
            .setCancelable(false)
            .setPositiveButton("Yes") { dialog: DialogInterface?, id: Int ->
                session.saveAdminDetails(serverIp, portNumber.toString())
                startActivity(Intent(activity, LoginActivity::class.java))
                activity?.finishAffinity()
            }
            .setNegativeButton("No, Continue") { dialog: DialogInterface, id: Int ->
                dialog.cancel()
                binding.edServerIp.setText(serverIpSharedPrefText)
                if (portSharedPrefText.toString() == "null") {
                    binding.edPort.setText("")
                } else {
                    binding.edPort.setText(portSharedPrefText.toString())
                }
            }
        alert = builder!!.create()
        alert!!.show()
    }

    private fun initFilter() {
        val filters = arrayOfNulls<InputFilter>(1)
        filters[0] = InputFilter { source, start, end, dest, dstart, dend ->
            if (end > start) {
                val destTxt = dest.toString()
                val resultingTxt = (destTxt.substring(0, dstart)
                        + source.subSequence(start, end)
                        + destTxt.substring(dend))
                if (!resultingTxt
                        .matches(Regex("^\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3})?)?)?)?)?)?"))
                ) {
                    return@InputFilter ""
                } else {
                    val splits = resultingTxt.split(".").toTypedArray()
                    for (i in splits.indices) {
                        if (splits[i].isNotEmpty())
                            if (Integer.valueOf(splits[i]) > 255) {
                                return@InputFilter ""
                            }
                    }
                }
            }
            null
        }
        binding.edServerIp.filters = filters
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