package com.kemarport.kyms.viewmodel.importupdate

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.kemarport.kyms.helper.Constants
import com.kemarport.kyms.helper.Resource
import com.kemarport.kyms.helper.Utils
import com.kemarport.kyms.models.export.generalrequestandresponse.GeneralRequestLocationId
import com.kemarport.kyms.models.export.generalrequestandresponse.GeneralResponse
import com.kemarport.kyms.models.export.locations.LocationsResponse
import com.kemarport.kyms.models.importupdate.GetMasterMobileResponse
import com.kemarport.kyms.repository.KYMSRepository
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Response
import java.io.IOException

class ImportDropperViewModel (
    application: Application,
    private val kymsRepository: KYMSRepository
) : AndroidViewModel(application) {


    val backToStockMutableLiveData: MutableLiveData<Resource<GeneralResponse>> = MutableLiveData()

    fun backToStock(
        token: String?,
        generalRequestLocationId: GeneralRequestLocationId,
        baseUrl: String
    ) = viewModelScope.launch {
        safeAPICallBackToStock(token, generalRequestLocationId, baseUrl)
    }

    private suspend fun safeAPICallBackToStock(
        token: String?,
        generalRequestLocationId: GeneralRequestLocationId,
        baseUrl: String
    ) {
        backToStockMutableLiveData.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication())) {
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
    private fun handleBackToStockResponse(response: Response<GeneralResponse>): Resource<GeneralResponse> {
        var errorMessage = ""
        if (response.isSuccessful) {
            response.body()?.let { response ->
                return Resource.Success(response)
            }
        } else if (response.errorBody() != null) {
            val errorObject = response.errorBody()?.let {
                JSONObject(it.charStream().readText())
            }
            errorObject?.let {
                errorMessage = it.getString(Constants.HTTP_ERROR_MESSAGE)
            }
        }
        return Resource.Error(errorMessage)
    }


////////////////////////////////
    val locationsMutableLiveData: MutableLiveData<Resource<LocationsResponse>> = MutableLiveData()
    fun getLocations(
        baseUrl: String,
        token: String?
    ) = viewModelScope.launch {
        safeAPICallForLocations(baseUrl, token)
    }
    private suspend fun safeAPICallForLocations(
        baseUrl: String,
        token: String?
    ) {
        locationsMutableLiveData.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication())) {
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
    private fun handleLocationsResponse(response: Response<LocationsResponse>): Resource<LocationsResponse> {
        var errorMessage = ""
        if (response.isSuccessful) {
            response.body()?.let { response ->
                return Resource.Success(response)
            }
        } else if (response.errorBody() != null) {
            val errorObject = response.errorBody()?.let {
                JSONObject(it.charStream().readText())
            }
            errorObject?.let {
                errorMessage = it.getString(Constants.HTTP_ERROR_MESSAGE)
            }
        }
        return Resource.Error(errorMessage)
    }

    /////////////////
    val importAddProductInStockMutableLiveData: MutableLiveData<Resource<GetMasterMobileResponse>> =
        MutableLiveData()
    fun addProductInStock(
        token: String?,
        baseUrl: String,
        BatchNo: String?,
        LocationId: Int?,
        Coordinate: String?
    ) =
        viewModelScope.launch {
            if (BatchNo != null) {
                safeAPICallAddProductInStock(token, baseUrl,BatchNo,LocationId,Coordinate)
            }
        }

    private suspend fun safeAPICallAddProductInStock(
        token: String?,
        baseUrl: String,
        BatchNo: String,
        LocationId: Int?,
        Coordinate: String?
    ) {
        importAddProductInStockMutableLiveData.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication())) {
                val response =
                    kymsRepository.getStoredProduct(token,baseUrl, BatchNo,LocationId ,Coordinate)
                importAddProductInStockMutableLiveData.postValue(handleAddProductInStoreResponse(response))
            } else {
                importAddProductInStockMutableLiveData.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> importAddProductInStockMutableLiveData.postValue(
                    Resource.Error(
                        Constants.NETWORK_FAILURE
                    )
                )
                else -> importAddProductInStockMutableLiveData.postValue(Resource.Error(Constants.CONFIG_ERROR))
            }
        }
    }
    private fun handleAddProductInStoreResponse(response: Response<GetMasterMobileResponse>): Resource<GetMasterMobileResponse> {
        var errorMessage = ""
        if (response.isSuccessful) {
            response.body()?.let { response ->
                return Resource.Success(response)
            }
        } else if (response.errorBody() != null) {
            val errorObject = response.errorBody()?.let {
                JSONObject(it.charStream().readText())
            }
            errorObject?.let {
                errorMessage = it.getString(Constants.HTTP_ERROR_MESSAGE)
            }
        }
        return Resource.Error(errorMessage)
    }
}