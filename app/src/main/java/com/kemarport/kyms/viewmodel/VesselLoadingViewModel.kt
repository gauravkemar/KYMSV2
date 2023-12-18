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
import com.kemarport.kyms.models.loadedCoils.CurrentLoadingResponse
import com.kemarport.kyms.models.packingList.DischargePortResponse
import com.kemarport.kyms.models.packingList.PackingListResponse
import com.kemarport.kyms.models.vesselandintercarting.VesselAndIntercartingRequest
import com.kemarport.kyms.repository.KYMSRepository
import kotlinx.coroutines.launch
import okio.IOException
import org.json.JSONObject
import retrofit2.Response

class VesselLoadingViewModel(
    application: Application,
    private val kymsRepository: KYMSRepository
) : AndroidViewModel(application) {
    /*val validationBerthToVesselMutableLiveData: MutableLiveData<Resource<ValidationResponse>> =
        MutableLiveData()*/
    val loadingOnVesselMutableLiveData: MutableLiveData<Resource<GeneralResponse>> =
        MutableLiveData()

    /*fun validationBerthToVessel(
        batchNo: String?,
        operationType: String?
    ) = viewModelScope.launch {
        validationBerthToVesselMutableLiveData.postValue(Resource.Loading())
        val response = kymsRepository.validationBerthToVessel(batchNo, operationType)
        validationBerthToVesselMutableLiveData.postValue(
            handleValidationBerthVesselResponse(
                response
            )
        )
    }

    private fun handleValidationBerthVesselResponse(response: Response<ValidationResponse>): Resource<ValidationResponse> {
        if (response.isSuccessful) {
            response.body()?.let { validationBerthVessel ->
                return Resource.Success(validationBerthVessel)
            }
        }

        return Resource.Error(response.message())
    }*/

    fun loadingOnVessel(
        token: String?,
        vesselAndIntercartingRequest: VesselAndIntercartingRequest,
        baseUrl: String
    ) = viewModelScope.launch {
        safeAPICallLoadingOnVessel(token, vesselAndIntercartingRequest, baseUrl)
    }

    private fun handleLoadingOnVesselResponse(response: Response<GeneralResponse>): Resource<GeneralResponse> {
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

    private suspend fun safeAPICallLoadingOnVessel(
        token: String?,
        vesselAndIntercartingRequest: VesselAndIntercartingRequest,
        baseUrl: String
    ) {
        loadingOnVesselMutableLiveData.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val response =
                    kymsRepository.loadingOnVessel(token, vesselAndIntercartingRequest, baseUrl)
                loadingOnVesselMutableLiveData.postValue(handleLoadingOnVesselResponse(response))
            } else {
                loadingOnVesselMutableLiveData.postValue(Resource.Error(NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> loadingOnVesselMutableLiveData.postValue(
                    Resource.Error(
                        NETWORK_FAILURE
                    )
                )
                else -> loadingOnVesselMutableLiveData.postValue(Resource.Error(Constants.CONFIG_ERROR))
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

    val packingListMutableLiveData: MutableLiveData<Resource<PackingListResponse>> =
        MutableLiveData()

    fun packingList(
        token: String?,
        baseUrl: String
    ) = viewModelScope.launch {
        safeAPICallPackingList(token, baseUrl)
    }

    private suspend fun safeAPICallPackingList(
        token: String?,
        baseUrl: String
    ) {
        packingListMutableLiveData.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val response =
                    kymsRepository.getPackingListData(token, baseUrl)
                packingListMutableLiveData.postValue(handlePackingListResponse(response))
            } else {
                packingListMutableLiveData.postValue(Resource.Error(NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> packingListMutableLiveData.postValue(
                    Resource.Error(
                        NETWORK_FAILURE
                    )
                )
                else -> packingListMutableLiveData.postValue(Resource.Error(Constants.CONFIG_ERROR))
            }
        }
    }

    private fun handlePackingListResponse(response: Response<PackingListResponse>): Resource<PackingListResponse> {
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

    val packingListDetailsMutableLiveData: MutableLiveData<Resource<CurrentLoadingResponse>> =
        MutableLiveData()

    fun packingListDetails(
        token: String?,
        baseUrl: String,
        packingListId : Int
    ) = viewModelScope.launch {
        safeAPICallPackingListDetails(token, baseUrl,packingListId)
    }

    private suspend fun safeAPICallPackingListDetails(
        token: String?,
        baseUrl: String,
        packingListId : Int
    ) {
        packingListDetailsMutableLiveData.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val response =
                    kymsRepository.getPackingListDetails(token, baseUrl,packingListId)
                packingListDetailsMutableLiveData.postValue(handlePackingListDetailsResponse(response))
            } else {
                packingListDetailsMutableLiveData.postValue(Resource.Error(NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> packingListDetailsMutableLiveData.postValue(
                    Resource.Error(
                        NETWORK_FAILURE
                    )
                )
                else -> packingListDetailsMutableLiveData.postValue(Resource.Error(Constants.CONFIG_ERROR))
            }
        }
    }

    private fun handlePackingListDetailsResponse(response: Response<CurrentLoadingResponse>): Resource<CurrentLoadingResponse> {
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


    val dischargePortListMutableLiveData: MutableLiveData<Resource<DischargePortResponse>> =
        MutableLiveData()

    fun dischargePortList(
        token: String?,
        baseUrl: String,
        packingListId : Int
    ) = viewModelScope.launch {
        safeAPICallDischargePortList(token, baseUrl,packingListId)
    }

    private suspend fun safeAPICallDischargePortList(
        token: String?,
        baseUrl: String,
        packingListId : Int
    ) {
        dischargePortListMutableLiveData.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val response =
                    kymsRepository.dischargePortList(token, baseUrl,packingListId)
                dischargePortListMutableLiveData.postValue(handleDischargePortListResponse(response))
            } else {
                dischargePortListMutableLiveData.postValue(Resource.Error(NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> dischargePortListMutableLiveData.postValue(
                    Resource.Error(
                        NETWORK_FAILURE
                    )
                )
                else -> dischargePortListMutableLiveData.postValue(Resource.Error(Constants.CONFIG_ERROR))
            }
        }
    }

    private fun handleDischargePortListResponse(response: Response<DischargePortResponse>): Resource<DischargePortResponse> {
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


}