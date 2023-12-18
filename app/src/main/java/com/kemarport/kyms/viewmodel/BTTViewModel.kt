package com.kemarport.kyms.viewmodel

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
import com.kemarport.kyms.models.generalrequestandresponse.GeneralResponse
import com.kemarport.kyms.models.locations.LocationsResponse
import com.kemarport.kyms.repository.KYMSRepository
import kotlinx.coroutines.launch
import okio.IOException
import org.json.JSONObject
import retrofit2.Response

class BTTViewModel(
    application: Application,
    private val kymsRepository: KYMSRepository
) : AndroidViewModel(application) {
    val locationsMutableLiveData: MutableLiveData<Resource<LocationsResponse>> = MutableLiveData()
    val markedBTTMutableLiveData: MutableLiveData<Resource<GeneralResponse>> = MutableLiveData()

    fun getLocations(baseUrl: String, token: String?) = viewModelScope.launch {
        safeAPICallForLocations(baseUrl, token)
    }

    private fun handleLocationsResponse(response: Response<LocationsResponse>): Resource<LocationsResponse> {
        if (response.isSuccessful) {
            response.body()?.let { locationsResponse ->
                return Resource.Success(locationsResponse)
            }
        }

        return Resource.Error(response.message())
    }

    fun markedBTT(
        token: String?,
        batchNo: String?,
        locationId: Int?,
        remark: String?,
        baseUrl: String
    ) = viewModelScope.launch {
        safeAPICallMarkedBTT(token, batchNo, locationId, remark, baseUrl)
    }

    private fun handleMarkedBTTResponse(response: Response<GeneralResponse>): Resource<GeneralResponse> {
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

    private suspend fun safeAPICallForLocations(baseUrl: String, token: String?) {
        locationsMutableLiveData.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val response = kymsRepository.getLocations(token, baseUrl)
                locationsMutableLiveData.postValue(handleLocationsResponse(response))
            } else {
                locationsMutableLiveData.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> locationsMutableLiveData.postValue(Resource.Error(Constants.NETWORK_FAILURE))
                else -> locationsMutableLiveData.postValue(Resource.Error(Constants.CONFIG_ERROR))
            }
        }
    }

    private suspend fun safeAPICallMarkedBTT(
        token: String?,
        batchNo: String?,
        locationId: Int?,
        remark: String?,
        baseUrl: String
    ) {
        markedBTTMutableLiveData.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val response = kymsRepository.markedBTT(token, batchNo, locationId, remark, baseUrl)
                markedBTTMutableLiveData.postValue(handleMarkedBTTResponse(response))
            } else {
                markedBTTMutableLiveData.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> markedBTTMutableLiveData.postValue(Resource.Error(Constants.NETWORK_FAILURE))
                else -> markedBTTMutableLiveData.postValue(Resource.Error(Constants.CONFIG_ERROR))
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