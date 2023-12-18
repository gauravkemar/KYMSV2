package com.kemarport.kyms.activities

import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.kemarport.kyms.R
import com.kemarport.kyms.databinding.ActivityFragmentHostBinding
import com.kemarport.kyms.fragments.main.SupervisorFragment
import com.kemarport.kyms.fragments.operator.dropper.DropBerthFragment
import com.kemarport.kyms.fragments.operator.dropper.DropStorageFragment
import com.kemarport.kyms.fragments.operator.dropper.LoadBttProductsFragment
import com.kemarport.kyms.fragments.operator.picker.EdiDetailsFragment
import com.kemarport.kyms.fragments.operator.picker.PickupRailFragment
import com.kemarport.kyms.fragments.operator.picker.PickupStorageFragment
import com.kemarport.kyms.fragments.supervisor.*
import com.kemarport.kyms.helper.Constants
import com.kemarport.kyms.helper.Resource
import com.kemarport.kyms.helper.SessionManager
import com.kemarport.kyms.repository.KYMSRepository
import com.kemarport.kyms.viewmodel.FragmentHostVMProviderFactory
import com.kemarport.kyms.viewmodel.FragmentHostViewModel
import com.symbol.emdk.EMDKManager
import com.symbol.emdk.EMDKResults
import com.symbol.emdk.barcode.*
import es.dmoral.toasty.Toasty

class FragmentHostActivity : AppCompatActivity(), EMDKManager.EMDKListener, Scanner.StatusListener,
    Scanner.DataListener {
    private lateinit var binding: ActivityFragmentHostBinding
    private lateinit var session: SessionManager
    private val TAG = "FragmentHostActivity"
    private lateinit var navController: NavController
    lateinit var viewModel: FragmentHostViewModel
    private var serverIpSharedPrefText: String? = null
    private var portSharedPrefText: Int? = 0
    private lateinit var adminDetails: HashMap<String, String?>
    private lateinit var baseUrl: String
    private var jwtToken: String? = null
    private lateinit var suggestions: List<String>

    var emdkManager: EMDKManager? = null
    var barcodeManager: BarcodeManager? = null
    var scanner: Scanner? = null
    var resumeFlag = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_fragment_host)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        Toasty.Config.getInstance()
            .setGravity(Gravity.TOP)
            .apply() // required
        session = SessionManager(this@FragmentHostActivity)
        val userDetails = session.getUserDetails()
        var roleName = userDetails["userRole"]
        val toolbar = binding.tbFragmentHostActivity
        toolbar.let {
            val titleTextView = it.findViewById<TextView>(R.id.tvToolbarTitle)
            if (roleName?.isEmpty() == true || roleName == null)
                roleName = "App"
            titleTextView.text = ("$roleName  Login").uppercase()
        }

        val kymsRepository = KYMSRepository()
        val viewModelProviderFactory =
            FragmentHostVMProviderFactory(application, kymsRepository)
        viewModel =
            ViewModelProvider(this, viewModelProviderFactory)[FragmentHostViewModel::class.java]

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val inflater = navHostFragment.navController.navInflater
        val navGraph = inflater.inflate(R.navigation.kyms_nav_graph)

        adminDetails = session.getAdminDetails()
        serverIpSharedPrefText = adminDetails["serverIp"]
        portSharedPrefText = adminDetails["port"]?.toInt()
        baseUrl = if (serverIpSharedPrefText == "192.168.1.52") {
            "http://$serverIpSharedPrefText:$portSharedPrefText/api/"
        } else {
            "http://$serverIpSharedPrefText:$portSharedPrefText/service/api/"
        }
        //baseUrl= Constants.BASE_URL_NEW
        Log.d(TAG, "onCreate: $baseUrl")

        jwtToken = userDetails["jwtToken"]
        when (roleName) {
            "Supervisor" -> {
                navGraph.setStartDestination(R.id.supervisorFragment)
            }
            "Operator" -> {
                navGraph.setStartDestination(R.id.operatorFragment)
            }
            "Admin" -> {
                navGraph.setStartDestination(R.id.adminFragment)
            }
            "SuperAdmin" -> {
                navGraph.setStartDestination(R.id.adminFragment)
            }
            else -> {

            }
        }
        navHostFragment.navController.graph = navGraph

        navController = navHostFragment.findNavController()
        setSupportActionBar(binding.tbFragmentHostActivity)
        setupActionBarWithNavController(navController)

        if (Build.MANUFACTURER.contains("Zebra Technologies") || Build.MANUFACTURER.contains("Motorola Solutions")) {
            val results = EMDKManager.getEMDKManager(this@FragmentHostActivity, this)
            if (results.statusCode != EMDKResults.STATUS_CODE.SUCCESS) {
                Log.e("MyTag", "EMDKManager object request failed!")
            } else {
                Log.e("MyTag", "EMDKManager object initialization is   in   progress.......")
            }
        } else {
            Log.e("MyTag", "Normal Phone")
        }

        viewModel.getBatchNoList(jwtToken, baseUrl)
        viewModel.allBatchNoMutableLiveData.observe(this, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    response.data?.let { resultResponse ->
                        Log.d(TAG, "onCreate: ${resultResponse.batchNo}")
                        suggestions = resultResponse.batchNo
                    }
                }
                is Resource.Error -> {
                    response.message?.let { errorMessage ->
                        Log.e(TAG, "onCreate: API error")
                    }
                }
                is Resource.Loading -> {

                }
                else -> {}
            }
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() or super.onSupportNavigateUp()
    }

    override fun onOpened(emdkManager: EMDKManager?) {
        if (android.os.Build.MANUFACTURER.contains("Zebra Technologies") || android.os.Build.MANUFACTURER.contains(
                "Motorola Solutions"
            )
        ) {
            this.emdkManager = emdkManager;
            initBarcodeManager();
            initScanner();
        }
    }

    override fun onClosed() {
        if (emdkManager != null) {
            emdkManager!!.release();
            emdkManager = null;
        }
    }

    override fun onResume() {
        super.onResume()

        if (resumeFlag) {
            resumeFlag = false;
            if (android.os.Build.MANUFACTURER.contains("Zebra Technologies") || android.os.Build.MANUFACTURER.contains(
                    "Motorola Solutions"
                )
            ) {
                initBarcodeManager();
                initScanner();
            }
        }
    }

    fun initBarcodeManager() {
        barcodeManager =
            emdkManager?.getInstance(EMDKManager.FEATURE_TYPE.BARCODE) as BarcodeManager
        if (barcodeManager == null) {
            Toast.makeText(
                this@FragmentHostActivity,
                "Barcode scanning is not supported.",
                Toast.LENGTH_LONG
            ).show();
            finish();
        }
    }

    fun initScanner() {
        if (scanner == null) {
            barcodeManager =
                this.emdkManager?.getInstance(EMDKManager.FEATURE_TYPE.BARCODE) as BarcodeManager
            scanner = barcodeManager!!.getDevice(BarcodeManager.DeviceIdentifier.DEFAULT)
            scanner?.addDataListener(this)
            scanner?.addStatusListener(this)
            scanner?.triggerType = Scanner.TriggerType.HARD
            try {
                scanner?.enable()
            } catch (e: ScannerException) {
                e.printStackTrace()
            }
        }
    }

    fun deInitScanner() {
        if (scanner != null) {
            try {
                scanner!!.release()
            } catch (e: Exception) {
            }
            scanner = null
        }
    }

    override fun onData(scanDataCollection: ScanDataCollection?) {
        var dataStr: String? = ""
        if (scanDataCollection != null && scanDataCollection.result == ScannerResults.SUCCESS) {
            val scanData = scanDataCollection.scanData
            for (data in scanData) {
                val barcodeData = data.data
                val labelType = data.labelType
                dataStr = barcodeData
            }

            val navHostFragment =
                supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment

            when (val foregroundFragment = navHostFragment.childFragmentManager.fragments[0]) {
                is SupervisorFragment -> {
                    foregroundFragment.setBarcode(dataStr)
                }
                is EdiDetailsFragment -> {
                    foregroundFragment.setBarcode(dataStr)
                }
                is DropStorageFragment -> {
                    foregroundFragment.setBarcode(dataStr)
                }
                is DropBerthFragment -> {
                    foregroundFragment.setBarcode(dataStr)
                }
                is PickupRailFragment -> {
                    foregroundFragment.setBarcode(dataStr)
                }
                is PickupStorageFragment -> {
                    foregroundFragment.setBarcode(dataStr)
                }
                is VesselLoadingFragment -> {
                    foregroundFragment.setBarcode(dataStr)
                }
                is IntercartingFragment -> {
                    foregroundFragment.setBarcode(dataStr)
                }
                is BtsFragment -> {
                    foregroundFragment.setBarcode(dataStr)
                }
                is BttFragment -> {
                    foregroundFragment.setBarcode(dataStr)
                }
                is LoadBttProductsFragment -> {
                    foregroundFragment.setBarcode(dataStr)
                }
                is PackingListDetailsFragment -> {
                    foregroundFragment.setBarcode(dataStr)
                }
            }
        }
    }

    override fun onStatus(statusData: StatusData) {
        val state: StatusData.ScannerStates = statusData.state
        var statusStr = ""
        when (state) {
            StatusData.ScannerStates.IDLE -> {
                statusStr = statusData.friendlyName + " is enabled and idle..."
                setConfig()
                try {
                    scanner!!.read()
                } catch (e: ScannerException) {
                }
            }
            StatusData.ScannerStates.WAITING -> statusStr =
                "Scanner is waiting for trigger press..."
            StatusData.ScannerStates.SCANNING -> statusStr = "Scanning..."
            StatusData.ScannerStates.DISABLED -> {}
            StatusData.ScannerStates.ERROR -> statusStr = "An error has occurred."
            else -> {}
        }
        setStatusText(statusStr)
    }

    private fun setConfig() {
        if (scanner != null) {
            try {
                val config = scanner!!.config
                if (config.isParamSupported("config.scanParams.decodeHapticFeedback")) {
                    config.scanParams.decodeHapticFeedback = true
                }
                if (config.isParamSupported("config.scanParams.decodeAudioFeedbackURI")) {
                    config.scanParams.decodeAudioFeedbackUri = ""
                }
                scanner!!.config = config
            } catch (e: ScannerException) {
                Log.e("MyTag", e.message!!)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        deInitScanner()
    }

    override fun onPause() {
        super.onPause()
        deInitScanner()
        resumeFlag = true
    }

    private fun setStatusText(msg: String) {
        Log.e(TAG, "StatusText: $msg")
    }

    fun showCustomDialog(title: String?, message: String?) {
        var alertDialog: AlertDialog? = null
        val builder: AlertDialog.Builder
        if (title.equals(""))
            builder = AlertDialog.Builder(this)
                .setMessage(Html.fromHtml(message))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Okay") { dialogInterface, which ->
                    alertDialog?.dismiss()
                }
        else if (message.equals(""))
            builder = AlertDialog.Builder(this)
                .setTitle(title)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Okay") { dialogInterface, which ->
                    alertDialog?.dismiss()
                }
        else
            builder = AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Okay") { dialogInterface, which ->
                    if (title.equals("Session Expired")) {
                        //session.clearSharedPrefs()
                        Intent(this, LoginActivity::class.java).apply {
                            startActivity(this)
                            finish()
                        }
                    } else {
                        alertDialog?.dismiss()
                    }
                }
        alertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    var mediaPlayer: MediaPlayer? = null

    fun setScannerAudio(fileName: String) {
        val resId = resources.getIdentifier(
            fileName,
            "raw",
            packageName
        )
        mediaPlayer = MediaPlayer.create(this, resId)
        mediaPlayer!!.start()
        mediaPlayer!!.setOnCompletionListener(onCompletionListener)
    }

    var onCompletionListener = MediaPlayer.OnCompletionListener { releaseMediaPlayerResources() }

    private fun releaseMediaPlayerResources() {
        try {
            if (mediaPlayer != null) {
                if (mediaPlayer!!.isPlaying)
                    mediaPlayer!!.stop()
                mediaPlayer!!.release()
                mediaPlayer = null
            }
        } catch (e: Exception) {
            mediaPlayer = null
        }
    }

    override fun onStop() {
        releaseMediaPlayerResources()
        super.onStop()
    }

    fun showSteelSlabDialog(foregroundFragment: Fragment) {

        val builder = AlertDialog.Builder(this)
        val customLayout: View = layoutInflater.inflate(R.layout.dialog_steel_slab, null)
        val edName: AutoCompleteTextView = customLayout.findViewById(R.id.edName)

        val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, suggestions)
        edName.threshold = 0
        edName.setAdapter(arrayAdapter)

        val btnOk: Button = customLayout.findViewById(R.id.btnOk)
        val btnCancel: Button = customLayout.findViewById(R.id.btnCancel)

        builder.setView(customLayout)
        val dialog = builder.create()
        btnOk.setOnClickListener {
            if (edName.text.isNotEmpty()) {
                when (foregroundFragment) {
                    is SupervisorFragment -> {
                        foregroundFragment.setBarcode(edName.text.toString())
                    }
                    is EdiDetailsFragment -> {
                        foregroundFragment.setBarcode(edName.text.toString())
                    }
                    is DropStorageFragment -> {
                        foregroundFragment.setBarcode(edName.text.toString())
                    }
                    is DropBerthFragment -> {
                        foregroundFragment.setBarcode(edName.text.toString())
                    }
                    is PickupRailFragment -> {
                        foregroundFragment.setBarcode(edName.text.toString())
                    }
                    is PickupStorageFragment -> {
                        foregroundFragment.setBarcode(edName.text.toString())
                    }
                    is VesselLoadingFragment -> {
                        foregroundFragment.setBarcode(edName.text.toString())
                    }
                    is PackingListDetailsFragment -> {
                        foregroundFragment.setBarcode(edName.text.toString())
                    }
                    is IntercartingFragment -> {
                        foregroundFragment.setBarcode(edName.text.toString())
                    }
                    is BtsFragment -> {
                        foregroundFragment.setBarcode(edName.text.toString())
                    }
                    is BttFragment -> {
                        foregroundFragment.setBarcode(edName.text.toString())
                    }
                    is LoadBttProductsFragment -> {
                        foregroundFragment.setBarcode(edName.text.toString())
                    }
                }
            }
            dialog.dismiss()
        }
        btnCancel.setOnClickListener { view -> dialog.dismiss() }
        dialog.setCancelable(false)
        dialog.show()
        val window = dialog.window
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

}