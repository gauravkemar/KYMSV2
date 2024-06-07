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
import com.kemarport.kyms.models.export.generalrequestandresponse.GeneralResponse
import com.kemarport.kyms.repository.KYMSRepository
import kotlinx.coroutines.launch
import okio.IOException
import org.json.JSONObject
import retrofit2.Response

class LoadCoilsBTTViewModel(
    application: Application,
    private val kymsRepository: KYMSRepository
) : AndroidViewModel(application) {
    val loadCoilsBttMutableLiveData: MutableLiveData<Resource<GeneralResponse>> = MutableLiveData()

    fun loadCoilBTT(
        token: String?,
        vehicleNumber: String?,
        batchNo: String?,
        remark: String?,
        baseUrl: String
    ) = viewModelScope.launch {
        safeAPICallLoadCoilsBTT(token, vehicleNumber, batchNo, remark, baseUrl)
    }

    private fun handleLoadCoilsBTTResponse(response: Response<GeneralResponse>): Resource<GeneralResponse> {
        var errorMessage = ""
        if (response.isSuccessful) {
            response.body()?.let { loadCoilsBttResponse ->
                return Resource.Success(loadCoilsBttResponse)
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

    private suspend fun safeAPICallLoadCoilsBTT(
        token: String?,
        vehicleNumber: String?,
        batchNo: String?,
        remark: String?,
        baseUrl: String
    ) {
        loadCoilsBttMutableLiveData.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val response = kymsRepository.loadCoilBTT(token, vehicleNumber, batchNo, remark, baseUrl)
                loadCoilsBttMutableLiveData.postValue(handleLoadCoilsBTTResponse(response))
            } else {
                loadCoilsBttMutableLiveData.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> loadCoilsBttMutableLiveData.postValue(Resource.Error(Constants.NETWORK_FAILURE))
                else -> loadCoilsBttMutableLiveData.postValue(Resource.Error(Constants.CONFIG_ERROR))
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