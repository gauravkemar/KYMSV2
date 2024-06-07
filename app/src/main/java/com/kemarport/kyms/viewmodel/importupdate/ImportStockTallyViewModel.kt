package com.kemarport.kyms.viewmodel.importupdate

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.kemarport.kyms.helper.Constants
import com.kemarport.kyms.helper.Resource
import com.kemarport.kyms.helper.Utils
import com.kemarport.kyms.models.importupdate.GetMasterMobileResponse
import com.kemarport.kyms.models.importupdate.importstocktally.GetReceivedProductResponse
import com.kemarport.kyms.repository.KYMSRepository
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Response
import java.io.IOException

class ImportStockTallyViewModel(
    application: Application,
    private val kymsRepository: KYMSRepository
) : AndroidViewModel(application) {

    private var skipRows = 0
    private val rowSize = 15
    private var isLoading = false



    fun loadNextPage(baseUrl: String, token: String?) {
        if (!isLoading) {
            viewModelScope.launch {
                safeAPICallGetJobMasterMobile(token, baseUrl, skipRows, rowSize)
            }
        }
    }

    fun loadPreviousPage(baseUrl: String, token: String?) {
        if (!isLoading && skipRows >= rowSize) {
            viewModelScope.launch {
                if (token != null) {
                    safeAPICallGetJobMasterMobile(token, baseUrl, skipRows - rowSize, rowSize)
                }
            }
        }
    }


    val getJobMasterMobileLiveData: MutableLiveData<Resource<GetMasterMobileResponse>> =
        MutableLiveData()

    fun getJobMasterMobile(
        baseUrl: String,
        token: String?,
        skiprow: Int,
        rowsize: Int
    ) {
        viewModelScope.launch {
            safeAPICallGetJobMasterMobile(token, baseUrl, skiprow, rowsize)
        }
    }

    private suspend fun safeAPICallGetJobMasterMobile(
        token: String?,
        baseUrl: String,
        skiprow: Int,
        rowsize: Int
    ) {
        getJobMasterMobileLiveData.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication())) {
                /*val response = kymsRepository.getJobMasterMobile( token,baseUrl,skiprow,rowsize)
                getJobMasterMobileLiveData.postValue(handleGetJobMasterMobileResponse(response))*/
                val response = kymsRepository.getJobMasterMobile(token, baseUrl, skiprow, rowsize)
                response.body()?.let { ediResponse ->
                    if (ediResponse != null) {
                        getJobMasterMobileLiveData.postValue(
                            handleGetJobMasterMobileResponse(
                                response
                            )
                        )
                        skipRows += rowsize // Update skipRows for the next page
                    } else {
                        // Handle the case when the retrieved list is empty
                        // Call the API for the previous page
                        loadPreviousPage(baseUrl, token)
                    }
                }

            } else {
                getJobMasterMobileLiveData.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> getJobMasterMobileLiveData.postValue(Resource.Error(Constants.NETWORK_FAILURE))
                else -> getJobMasterMobileLiveData.postValue(Resource.Error(Constants.CONFIG_ERROR))
            }
        }
    }

    private fun handleGetJobMasterMobileResponse(response: Response<GetMasterMobileResponse>): Resource<GetMasterMobileResponse> {
        var errorMessage = ""
        if (response.isSuccessful) {
            response.body()?.let { appDetailsResponse ->
                return Resource.Success(appDetailsResponse)
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



    ///////////////////////////////////////////////////////////////////////////////////////////////////
    private var jobDetailSkipRow = 0
    private val jobDetailSRowSize = 15
    private var jobDetailSIsLoading = false

    fun jobDetailsLoadNextPage(baseUrl: String, token: String?,importJobMasterId:Int?) {
        if (!jobDetailSIsLoading) {
            viewModelScope.launch {
                safeAPICallGetJobDetailsMobile(token, baseUrl,importJobMasterId, jobDetailSkipRow, jobDetailSRowSize)
            }
        }
    }

    fun jobDetailsLoadPreviousPage(baseUrl: String, token: String?,importJobMasterId:Int?) {
        if (!jobDetailSIsLoading && jobDetailSkipRow >= jobDetailSRowSize) {
            viewModelScope.launch {
                if (token != null) {
                    safeAPICallGetJobDetailsMobile(token, baseUrl, importJobMasterId,jobDetailSkipRow - jobDetailSRowSize, jobDetailSRowSize)
                }
            }
        }
    }


    val getJobDetailsMobileLiveData: MutableLiveData<Resource<GetMasterMobileResponse>> =
        MutableLiveData()

    fun getJobDetailsMobile(
        baseUrl: String,
        token: String?,
        importJobMasterId: Int?,
        skiprow: Int,
        rowsize: Int
    ) {
        viewModelScope.launch {
            safeAPICallGetJobDetailsMobile(token, baseUrl,importJobMasterId, skiprow, rowsize)
        }
    }

    private suspend fun safeAPICallGetJobDetailsMobile(
        token: String?,
        baseUrl: String,
        importJobMasterId: Int?,
        skiprow: Int,
        rowsize: Int
    ) {
        getJobDetailsMobileLiveData.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication())) {
                /*val response = kymsRepository.getJobMasterMobile( token,baseUrl,skiprow,rowsize)
                getJobMasterMobileLiveData.postValue(handleGetJobMasterMobileResponse(response))*/
                val response = kymsRepository.getJobDetailsMobile(token, baseUrl,importJobMasterId, skiprow, rowsize)
                response.body()?.let { ediResponse ->
                    if (ediResponse != null) {
                        getJobDetailsMobileLiveData.postValue(
                            handleGetJobDetailsMobileResponse(
                                response
                            )
                        )
                        skipRows += rowsize // Update skipRows for the next page
                    } else {
                        // Handle the case when the retrieved list is empty
                        // Call the API for the previous page
                        loadPreviousPage(baseUrl, token)
                    }
                }

            } else {
                getJobDetailsMobileLiveData.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> getJobDetailsMobileLiveData.postValue(Resource.Error(Constants.NETWORK_FAILURE))
                else -> getJobDetailsMobileLiveData.postValue(Resource.Error(Constants.CONFIG_ERROR))
            }
        }
    }

    private fun handleGetJobDetailsMobileResponse(response: Response<GetMasterMobileResponse>): Resource<GetMasterMobileResponse> {
        var errorMessage = ""
        if (response.isSuccessful) {
            response.body()?.let { appDetailsResponse ->
                return Resource.Success(appDetailsResponse)
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



    /////////////////////////////////////////////////////////
    val getReceivedProductMutable: MutableLiveData<Resource<GetMasterMobileResponse>> =
        MutableLiveData()

    fun getReceivedProduct(
        token: String,
        baseUrl: String,
        BatchNo: String?,
        LocationId: Int?,
        Coordinate: String?
    ) {
        viewModelScope.launch {
            safeAPICallGetReceivedProduct(token,baseUrl,BatchNo,LocationId,Coordinate)
        }
    }

    private suspend fun safeAPICallGetReceivedProduct(
        token: String,
        baseUrl: String,
        BatchNo: String?,
        LocationId: Int?,
        Coordinate: String?
    ) {
        getReceivedProductMutable.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication())) {
                val response = kymsRepository.getReceivedProduct(token, baseUrl,BatchNo,LocationId,Coordinate)
                getReceivedProductMutable.postValue(handleGetReceivedProductResponse(response))
            } else {
                getReceivedProductMutable.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is Exception -> {
                    getReceivedProductMutable.postValue(Resource.Error("${t.message}"))
                }
                else -> getReceivedProductMutable.postValue(Resource.Error(Constants.CONFIG_ERROR))
            }
        }
    }

    private fun handleGetReceivedProductResponse(response: Response<GetMasterMobileResponse>): Resource<GetMasterMobileResponse>? {
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