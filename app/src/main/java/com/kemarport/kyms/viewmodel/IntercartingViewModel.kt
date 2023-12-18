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
import com.kemarport.kyms.helper.Constants.Companion.NETWORK_FAILURE
import com.kemarport.kyms.helper.Constants.Companion.NO_INTERNET
import com.kemarport.kyms.helper.Resource
import com.kemarport.kyms.models.generalrequestandresponse.GeneralResponse
import com.kemarport.kyms.models.vesselandintercarting.VesselAndIntercartingRequest
import com.kemarport.kyms.repository.KYMSRepository
import kotlinx.coroutines.launch
import okio.IOException
import org.json.JSONObject
import retrofit2.Response

class IntercartingViewModel(
    application: Application,
    private val kymsRepository: KYMSRepository
) : AndroidViewModel(application) {
    /*    val validationBerthToVesselMutableLiveData: MutableLiveData<Resource<ValidationResponse>> =
            MutableLiveData()*/
    val interCartingMutableLiveData: MutableLiveData<Resource<GeneralResponse>> =
        MutableLiveData()

    /*fun validationBerthToVessel(
        batchNo: String?,
        operationType: String?
    ) = viewModelScope.launch {
        *//*validationBerthToVesselMutableLiveData.postValue(Resource.Loading())
//        val response = kymsRepository.validationBerthToVessel(batchNo, operationType)
        validationBerthToVesselMutableLiveData.postValue(
            handleValidationBerthVesselResponse(
                response
            )
        )*//*
    }

    *//*private fun handleValidationBerthVesselResponse(response: Response<ValidationResponse>): Resource<ValidationResponse> {
        if (response.isSuccessful) {
            response.body()?.let { validationBerthVessel ->
                return Resource.Success(validationBerthVessel)
            }
        }

        return Resource.Error(response.message())
    }*/

    fun interCarting(
        token: String?,
        vesselAndIntercartingRequest: VesselAndIntercartingRequest,
        baseUrl: String
    ) = viewModelScope.launch {
        safeAPICallIntercarting(token, vesselAndIntercartingRequest, baseUrl)
    }

    private fun handleLoadingOnVesselResponse(response: Response<GeneralResponse>): Resource<GeneralResponse> {
        var errorMessage = ""
        if (response.isSuccessful) {
            response.body()?.let { loadingOnVesselResponse ->
                return Resource.Success(loadingOnVesselResponse)
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

    private suspend fun safeAPICallIntercarting(
        token: String?,
        vesselAndIntercartingRequest: VesselAndIntercartingRequest,
        baseUrl: String
    ) {
        interCartingMutableLiveData.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val response = kymsRepository.loadingOnVessel(token, vesselAndIntercartingRequest, baseUrl)
                interCartingMutableLiveData.postValue(handleLoadingOnVesselResponse(response))
            } else {
                interCartingMutableLiveData.postValue(Resource.Error(NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> interCartingMutableLiveData.postValue(
                    Resource.Error(
                        NETWORK_FAILURE
                    )
                )
                else -> interCartingMutableLiveData.postValue(Resource.Error(Constants.CONFIG_ERROR))
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