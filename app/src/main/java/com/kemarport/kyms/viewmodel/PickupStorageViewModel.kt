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
import com.kemarport.kyms.models.generalrequestandresponse.GeneralRequestBerthLocation
import com.kemarport.kyms.models.generalrequestandresponse.GeneralResponse
import com.kemarport.kyms.repository.KYMSRepository
import kotlinx.coroutines.launch
import okio.IOException
import org.json.JSONObject
import retrofit2.Response

class PickupStorageViewModel(
    application: Application,
    private val kymsRepository: KYMSRepository
) : AndroidViewModel(application) {
    val pickCoilFromStockMutableLiveData: MutableLiveData<Resource<GeneralResponse>> =
        MutableLiveData()

    /*fun validationStockToBerth(
        berthLocation: String?,
        batchNo: String?,
        operationType: String?
    ) = viewModelScope.launch {
        validationStockToBerthMutableLiveData.postValue(Resource.Loading())
        val response =
            kymsRepository.validationStockToBerth(berthLocation, batchNo, operationType)
        validationStockToBerthMutableLiveData.postValue(
            handleValidationPickStorageResponse(
                response
            )
        )
    }

    private fun handleValidationPickStorageResponse(response: Response<ValidationResponse>): Resource<ValidationResponse> {
        if (response.isSuccessful) {
            response.body()?.let { validationResponse ->
                return Resource.Success(validationResponse)
            }
        }

        return Resource.Error(response.message())
    }

    fun stockToBerth(coilRequest: CoilRequest) = viewModelScope.launch {
        stockToBerthMutableLiveData.postValue(Resource.Loading())
        val response = kymsRepository.stockToBerth(coilRequest)
        stockToBerthMutableLiveData.postValue(handlePickCoilResponse(response))
    }

    private fun handlePickCoilResponse(response: Response<EdiConfirmationResponse>): Resource<EdiConfirmationResponse> {
        if (response.isSuccessful) {
            response.body()?.let { pickCoilResponse ->
                return Resource.Success(pickCoilResponse)
            }
        }

        return Resource.Error(response.message())
    }*/

    fun pickCoilFromStock(
        token: String?, generalRequestBerthLocation: GeneralRequestBerthLocation,
        baseUrl: String
    ) = viewModelScope.launch {
        safeAPICallPickCoilFromStock(token, generalRequestBerthLocation, baseUrl)
    }

    private fun handlePickCoilFromStockResponse(response: Response<GeneralResponse>): Resource<GeneralResponse> {
        var errorMessage = ""
        if (response.isSuccessful) {
            response.body()?.let { generalResponse ->
                return Resource.Success(generalResponse)
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

    private suspend fun safeAPICallPickCoilFromStock(
        token: String?,
        generalRequestBerthLocation: GeneralRequestBerthLocation,
        baseUrl: String
    ) {
        pickCoilFromStockMutableLiveData.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val response =
                    kymsRepository.pickCoilFromStock(token, generalRequestBerthLocation, baseUrl)
                pickCoilFromStockMutableLiveData.postValue(handlePickCoilFromStockResponse(response))
            } else {
                pickCoilFromStockMutableLiveData.postValue(Resource.Error(NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> pickCoilFromStockMutableLiveData.postValue(
                    Resource.Error(
                        NETWORK_FAILURE
                    )
                )
                else -> pickCoilFromStockMutableLiveData.postValue(Resource.Error(Constants.CONFIG_ERROR))
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