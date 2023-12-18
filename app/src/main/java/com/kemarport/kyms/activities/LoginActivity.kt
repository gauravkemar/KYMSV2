package com.kemarport.kyms.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.kemarport.kyms.R
import com.kemarport.kyms.databinding.ActivityLoginBinding
import com.kemarport.kyms.helper.Constants
import com.kemarport.kyms.helper.Resource
import com.kemarport.kyms.helper.SessionManager
import com.kemarport.kyms.models.login.LoginRequest
import com.kemarport.kyms.models.login.LoginResponse
import com.kemarport.kyms.repository.KYMSRepository
import com.kemarport.kyms.viewmodel.LoginViewModel
import com.kemarport.kyms.viewmodel.LoginViewModelProviderFactory
import es.dmoral.toasty.Toasty

class LoginActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityLoginBinding
    private var serverIpSharedPrefText: String? = null
    private var portSharedPrefText: Int? = 0
    private var adminDetails: HashMap<String, String?>? = null
    private var session: SessionManager? = null
    private var baseUrl: String =""
    var viewModel: LoginViewModel? = null
    private val TAG = "LoginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        binding.listener = this
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        Toasty.Config.getInstance()
            .setGravity(Gravity.TOP)
            .apply() // required

        val actionBar = supportActionBar
        actionBar?.hide()

        val kymsRepository = KYMSRepository()
        val viewModelProviderFactory =
            LoginViewModelProviderFactory(application, kymsRepository)
        viewModel = ViewModelProvider(this, viewModelProviderFactory)[LoginViewModel::class.java]

        session = SessionManager(this)
        adminDetails = session!!.getAdminDetails()
        serverIpSharedPrefText = adminDetails!!["serverIp"]
        portSharedPrefText = adminDetails!!["port"]?.toInt()
        baseUrl = if (serverIpSharedPrefText == "192.168.1.52") {
            "http://$serverIpSharedPrefText:$portSharedPrefText/api/"
        } else {
            "http://$serverIpSharedPrefText:$portSharedPrefText/service/api/"
        }
       // baseUrl=Constants.BASE_URL_NEW
        Log.d(TAG, "Base URL: $baseUrl")

        viewModel!!.userLoginMutableLiveData.observe(this, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    binding.btnLogin.visibility = View.INVISIBLE
                    response.data?.let { loginResponse ->
                        Log.e(TAG, loginResponse.toString())
                        logUserIn(binding, loginResponse)
                    }
                }
                is Resource.Error -> {
                    hideProgressBar()
                    binding.btnLogin.visibility = View.VISIBLE
                    response.message?.let { message ->
                        Log.e(TAG, "Login failed: $message")
                        Toasty.error(this, baseUrl+" - "+message, Toast.LENGTH_LONG).show()
                    }
                }
                is Resource.Loading -> {
                    showProgressBar()
                    binding.btnLogin.visibility = View.INVISIBLE
                }
            }
        })
    }

    override fun onClick(view: View?) {
        view?.let {
            when (it.id) {
                R.id.btnLogin -> {
                    val username = binding.etUsername.text?.trim().toString()
                    val password = binding.etPassword.text?.trim().toString()
                    if (username.isNotEmpty() && password.isNotEmpty()) {
                        if (password.length < 6) {
                            Toasty.warning(this, "Minimum length of password must be 6 characters.")
                                .show()
                        } else {
                            if (username == "admin" && password == "Pass@123") {
                                logAdminIn(binding)
                            } else {
                                val loginRequest = LoginRequest(password, username)
                                viewModel!!.userLogin(loginRequest, baseUrl)
                            }
                        }
                    } else {
                        Toasty.error(this, "All fields are required.").show()
                    }
                }
                else -> {

                }
            }
        }
    }

    private fun logUserIn(binding: ActivityLoginBinding, loginResponse: LoginResponse) {
        val username = binding.etUsername.text?.trim().toString()
        val password = binding.etPassword.text?.trim().toString()
        val roleName = loginResponse.roleName
        val firstName = loginResponse.firstName
        val lastName = loginResponse.lastName
        val jwtToken = loginResponse.jwtToken
        val refreshToken = loginResponse.refreshToken
        val email = loginResponse.email
        session!!.createLoginSession(
            username,
            null,
            roleName,
            null,
            password,
            firstName,
            lastName,
            jwtToken,
            refreshToken,
            email
        )
        Intent(this@LoginActivity, FragmentHostActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP and Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(this)
            this@LoginActivity.finish()
        }
    }

    private fun logAdminIn(binding: ActivityLoginBinding) {
        val username = binding.etUsername.text?.trim().toString()
        val password = binding.etPassword.text?.trim().toString()
        session!!.createLoginSession(
            username,
            null,
            "Admin",
            null,
            password,
            null,
            null,
            null,
            null,
            null
        )
        Intent(this@LoginActivity, FragmentHostActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP and Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(this)
            this@LoginActivity.finish()
        }
    }

    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        binding.progressBar.visibility = View.INVISIBLE
    }
}