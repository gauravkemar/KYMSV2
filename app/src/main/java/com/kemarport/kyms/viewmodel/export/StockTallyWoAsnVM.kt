package com.kemarport.kyms.viewmodel.export

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.kemarport.kyms.helper.Constants
import com.kemarport.kyms.helper.Resource
import com.kemarport.kyms.models.export.DelayedEDI.DelayedEDIResponse
import com.kemarport.kyms.models.export.withoutasn.CreateJobMasterRequest
import com.kemarport.kyms.models.export.withoutasn.CreateJobMasterResponse
import com.kemarport.kyms.repository.KYMSRepository
import kotlinx.coroutines.launch
import okio.IOException
import org.json.JSONObject
import retrofit2.Response

class StockTallyWoAsnVM(
    application: Application,
    private val kymsRepository: KYMSRepository
) : AndroidViewModel(application) {

    val createJobMasterMutableLiveData: MutableLiveData<Resource<CreateJobMasterResponse>> =
        MutableLiveData()
    val delayedEdiMutableLiveData: MutableLiveData<Resource<com.kemarport.kyms.models.export.DelayedEDI.DelayedEDIResponse>> = MutableLiveData()

    fun createJobMaster(
        token: String?,
        createJobMasterRequest: CreateJobMasterRequest,
        baseUrl: String
    ) = viewModelScope.launch {
        safeAPICall(token, createJobMasterRequest, baseUrl)
    }

    fun getDelayedEdiData(
        baseUrl: String,
        token: String?
    ) = viewModelScope.launch {
        safeAPICall(baseUrl, token)
    }

    private fun handleUserCreateJobMasterRequest(response: Response<CreateJobMasterResponse>): Resource<CreateJobMasterResponse> {
        var errorMessage = ""
        if (response.isSuccessful) {
            response.body()?.let { userResponse ->
                return Resource.Success(userResponse)
            }
        } else if (response.errorBody() != null) {
            val errorObject = response.errorBody()?.let {
                JSONObject(it.charStream().readText())
            }
            errorObject?.let {
                errorMessage = it.getString("message")
            }
        }
        return Resource.Error(errorMessage)
    }

    private fun handleEdiResponse(response: Response<DelayedEDIResponse>): Resource<DelayedEDIResponse> {
        if (response.isSuccessful) {
            response.body()?.let { ediResponse ->
                return Resource.Success(ediResponse)
            }
        }
        return Resource.Error(response.message())
    }

    private suspend fun safeAPICall(
        token: String?,
        createJobMasterRequest: CreateJobMasterRequest,
        baseUrl: String
    ) {
        createJobMasterMutableLiveData.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val response =
                    kymsRepository.createJobMaster(token, baseUrl, createJobMasterRequest)
                createJobMasterMutableLiveData.postValue(handleUserCreateJobMasterRequest(response))
            } else {
                createJobMasterMutableLiveData.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> createJobMasterMutableLiveData.postValue(Resource.Error(Constants.NETWORK_FAILURE))
                else -> createJobMasterMutableLiveData.postValue(Resource.Error(Constants.CONFIG_ERROR))
            }
        }
    }

    private suspend fun safeAPICall(baseUrl:String, token: String?) {
        delayedEdiMutableLiveData.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val response = kymsRepository.getDelayedEdiData(baseUrl, token)
                delayedEdiMutableLiveData.postValue(handleEdiResponse(response))
            } else {
                delayedEdiMutableLiveData.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> delayedEdiMutableLiveData.postValue(Resource.Error(Constants.NETWORK_FAILURE))
                else -> delayedEdiMutableLiveData.postValue(Resource.Error(Constants.CONFIG_ERROR))
            }
        }
    }

    private fun hasInternetConnection(): Boolean {
        val connectivityManager = getApplication<Application>().getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val capabilities =
                connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            return when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            connectivityManager.activeNetworkInfo?.run {
                return when (type) {
                    ConnectivityManager.TYPE_WIFI -> true
                    ConnectivityManager.TYPE_MOBILE -> true
                    ConnectivityManager.TYPE_ETHERNET -> true
                    else -> false
                }
            }
        }

        return false
    }
}