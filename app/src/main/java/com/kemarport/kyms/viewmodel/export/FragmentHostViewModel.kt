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
import com.kemarport.kyms.models.export.generalrequestandresponse.BatchNoListResponse
import com.kemarport.kyms.repository.KYMSRepository
import kotlinx.coroutines.launch
import okio.IOException
import retrofit2.Response

class FragmentHostViewModel(
    application: Application,
    private val kymsRepository: KYMSRepository
) : AndroidViewModel(application) {
    val allBatchNoMutableLiveData: MutableLiveData<Resource<BatchNoListResponse>> =
        MutableLiveData()

    fun getBatchNoList(token: String?, baseUrl: String) = viewModelScope.launch {
        safeAPICall(baseUrl, token)
    }

    private fun handleEdiResponse(response: Response<BatchNoListResponse>): Resource<BatchNoListResponse> {
        if (response.isSuccessful) {
            response.body()?.let { ediResponse ->
                return Resource.Success(ediResponse)
            }
        }
        return Resource.Error(response.message())
    }

    private suspend fun safeAPICall(baseUrl: String, token: String?) {
        allBatchNoMutableLiveData.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val response = kymsRepository.getBatchNoList(token, baseUrl)
                allBatchNoMutableLiveData.postValue(handleEdiResponse(response))
            } else {
                allBatchNoMutableLiveData.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> allBatchNoMutableLiveData.postValue(Resource.Error(Constants.NETWORK_FAILURE))
                else -> allBatchNoMutableLiveData.postValue(Resource.Error(Constants.CONFIG_ERROR))
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