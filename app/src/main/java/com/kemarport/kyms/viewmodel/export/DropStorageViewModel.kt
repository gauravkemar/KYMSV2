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
import com.kemarport.kyms.models.export.generalrequestandresponse.GeneralRequestLocationId
import com.kemarport.kyms.models.export.generalrequestandresponse.GeneralResponse
import com.kemarport.kyms.models.export.locations.LocationsResponse
import com.kemarport.kyms.repository.KYMSRepository
import kotlinx.coroutines.launch
import okio.IOException
import org.json.JSONObject
import retrofit2.Response

class DropStorageViewModel(
    application: Application,
    private val kymsRepository: KYMSRepository
) : AndroidViewModel(application) {
    val locationsMutableLiveData: MutableLiveData<Resource<LocationsResponse>> = MutableLiveData()
    val addProductInStockMutableLiveData: MutableLiveData<Resource<GeneralResponse>> =
        MutableLiveData()
    val backToStockMutableLiveData: MutableLiveData<Resource<GeneralResponse>> = MutableLiveData()

    fun getLocations(
        baseUrl: String,
        token: String?
    ) = viewModelScope.launch {
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

    /*fun getCoilStatusWhileDropping(
        batchNo: String?,
        locationID: Int?
    ) = viewModelScope.launch {
        coilStatusDroppingMutableLiveData.postValue(Resource.Loading())
        val response = kymsRepository.getCoilStatusWhileDropping(batchNo, locationID)
        coilStatusDroppingMutableLiveData.postValue(handleCoilStatusDroppingResponse(response))
    }

    private fun handleCoilStatusDroppingResponse(response: Response<ValidationResponse>): Resource<ValidationResponse> {
        if (response.isSuccessful) {
            response.body()?.let { validationResponse ->
                return Resource.Success(validationResponse)
            }
        }

        return Resource.Error(response.message())
    }

    fun addProductInStock(ediConfirmationRequest: EdiConfirmationRequest) = viewModelScope.launch {
        addProductInStockMutableLiveData.postValue(Resource.Loading())
        val response = kymsRepository.addProductInStock(ediConfirmationRequest)
        addProductInStockMutableLiveData.postValue(handleAddProductInStockResponse(response))
    }

    private fun handleAddProductInStockResponse(response: Response<EdiConfirmationResponse>): Resource<EdiConfirmationResponse> {
        if (response.isSuccessful) {
            response.body()?.let { ediConfirmationResponse ->
                return Resource.Success(ediConfirmationResponse)
            }
        }

        return Resource.Error(response.message())
    }*/

    fun addProductInStock(
        token: String?,
        generalRequestLocationId: com.kemarport.kyms.models.export.generalrequestandresponse.GeneralRequestLocationId,
        baseUrl: String
    ) =
        viewModelScope.launch {
            safeAPICallAddProductInStock(token, generalRequestLocationId, baseUrl)
        }

    private fun handleAddProductInStoreResponse(response: Response<GeneralResponse>): Resource<GeneralResponse> {
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

    fun backToStock(
        token: String?,
        generalRequestLocationId: com.kemarport.kyms.models.export.generalrequestandresponse.GeneralRequestLocationId,
        baseUrl: String
    ) = viewModelScope.launch {
        safeAPICallBackToStock(token, generalRequestLocationId, baseUrl)
    }

    private fun handleBackToStockResponse(response: Response<GeneralResponse>): Resource<GeneralResponse> {
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

    private suspend fun safeAPICallForLocations(
        baseUrl: String,
        token: String?
    ) {
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

    private suspend fun safeAPICallAddProductInStock(
        token: String?,
        generalRequestLocationId: GeneralRequestLocationId,
        baseUrl: String
    ) {
        addProductInStockMutableLiveData.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val response =
                    kymsRepository.addProductInStock(token, generalRequestLocationId, baseUrl)
                addProductInStockMutableLiveData.postValue(handleAddProductInStoreResponse(response))
            } else {
                addProductInStockMutableLiveData.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> addProductInStockMutableLiveData.postValue(
                    Resource.Error(
                        Constants.NETWORK_FAILURE
                    )
                )
                else -> addProductInStockMutableLiveData.postValue(Resource.Error(Constants.CONFIG_ERROR))
            }
        }
    }

    private suspend fun safeAPICallBackToStock(
        token: String?,
        generalRequestLocationId:GeneralRequestLocationId,
        baseUrl: String
    ) {
        backToStockMutableLiveData.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val response = kymsRepository.backToStock(token, generalRequestLocationId, baseUrl)
                backToStockMutableLiveData.postValue(handleBackToStockResponse(response))
            } else {
                backToStockMutableLiveData.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> backToStockMutableLiveData.postValue(
                    Resource.Error(
                        Constants.NETWORK_FAILURE
                    )
                )
                else -> backToStockMutableLiveData.postValue(Resource.Error(Constants.CONFIG_ERROR))
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