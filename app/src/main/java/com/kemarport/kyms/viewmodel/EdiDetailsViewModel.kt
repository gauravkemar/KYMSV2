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
import com.kemarport.kyms.models.itemscanning.ItemScanningResponse
import com.kemarport.kyms.models.unloadedcoils.CurrentScanningResponse
import com.kemarport.kyms.repository.KYMSRepository
import kotlinx.coroutines.launch
import okio.IOException
import org.json.JSONObject
import retrofit2.Response

class EdiDetailsViewModel(
    application: Application,
    private val kymsRepository: KYMSRepository
) : AndroidViewModel(application) {
    //    val ediDetailsMutableLiveData: MutableLiveData<Resource<EdiDetailsResponse>> = MutableLiveData()
    val unloadingCoilMutableLiveData: MutableLiveData<Resource<ItemScanningResponse>> =
        MutableLiveData()
    val currentScanningMutableLiveData: MutableLiveData<Resource<CurrentScanningResponse>> =
        MutableLiveData()

    /*fun getEdiDetailsData(
        jobId: Int,
        pageNo: Int,
        pageSize: Int,
        searchText: String
    ) = viewModelScope.launch {
        ediDetailsMutableLiveData.postValue(Resource.Loading())
        val response = kymsRepository.getEdiDetailsData(jobId, pageNo, pageSize, searchText)
        ediDetailsMutableLiveData.postValue(handleEdiDetailsResponse(response))
    }

    private fun handleEdiDetailsResponse(response: Response<EdiDetailsResponse>): Resource<EdiDetailsResponse> {
        if (response.isSuccessful) {
            response.body()?.let { ediDetailsResponse ->
                return Resource.Success(ediDetailsResponse)
            }
        }

        return Resource.Error(response.message())
    }*/

    fun unloadingCoil(
        token: String?,
        batchNo: String?,
        rakeRefNo: String,
        jobId: Int,
        baseUrl: String
    ) = viewModelScope.launch {
        safeAPICallUnloadingCoil(token, batchNo, rakeRefNo, jobId, baseUrl)
    }

    private fun handleItemScanningResponse(response: Response<ItemScanningResponse>): Resource<ItemScanningResponse> {
        var errorMessage = ""
        if (response.isSuccessful) {
            response.body()?.let { itemScanningResponse ->
                return Resource.Success(itemScanningResponse)
            }
        } else if (response.errorBody() != null) {
            val errorObject = response.errorBody()?.let {
                JSONObject(it.charStream().readText())
            }
            errorObject?.let {
                errorMessage = it.getString("statusMessage")
            }
        }
        return Resource.Error(errorMessage)
    }

    fun getCurrentScanningList(jobId: Int, baseUrl: String, token: String?) = viewModelScope.launch {
        safeAPICallGetCurrentScanningList(jobId, baseUrl, token)
    }

    private fun handleCurrentScanningListResponse(response: Response<CurrentScanningResponse>): Resource<CurrentScanningResponse> {
        if (response.isSuccessful) {
            response.body()?.let { currentScanningResponse ->
                return Resource.Success(currentScanningResponse)
            }
        }
        return Resource.Error(response.message())
    }

    private suspend fun safeAPICallUnloadingCoil(
        token: String?,
        batchNo: String?,
        rakeRefNo: String,
        jobId: Int,
        baseUrl: String
    ) {
        unloadingCoilMutableLiveData.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val response =
                    kymsRepository.unloadingCoil(token, batchNo, rakeRefNo, jobId, baseUrl)
                unloadingCoilMutableLiveData.postValue(handleItemScanningResponse(response))
            } else {
                unloadingCoilMutableLiveData.postValue(Resource.Error(NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> unloadingCoilMutableLiveData.postValue(
                    Resource.Error(
                        NETWORK_FAILURE
                    )
                )
                else -> unloadingCoilMutableLiveData.postValue(Resource.Error(Constants.CONFIG_ERROR))
            }
        }
    }

    private suspend fun safeAPICallGetCurrentScanningList(jobId: Int, baseUrl: String, token: String?) {
        currentScanningMutableLiveData.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val response = kymsRepository.getCurrentScanningList(jobId, baseUrl, token)
                currentScanningMutableLiveData.postValue(handleCurrentScanningListResponse(response))
            } else {
                currentScanningMutableLiveData.postValue(Resource.Error(NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> currentScanningMutableLiveData.postValue(
                    Resource.Error(
                        NETWORK_FAILURE
                    )
                )
                else -> currentScanningMutableLiveData.postValue(Resource.Error(Constants.CONFIG_ERROR))
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