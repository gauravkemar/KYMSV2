package com.kemarport.kyms.viewmodel.export

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.kemarport.kyms.helper.Resource
import com.kemarport.kyms.models.export.EDI.EdiConfirmationResponse
import com.kemarport.kyms.models.export.itemscanning.ValidationResponse
import com.kemarport.kyms.repository.KYMSRepository

class DropBerthViewModel(
    application: Application,
    private val kymsRepository: KYMSRepository
) : AndroidViewModel(application) {
    val validationStockToBerthMutableLiveData: MutableLiveData<Resource<ValidationResponse>> =
        MutableLiveData()
    val stockToBerthMutableLiveData: MutableLiveData<Resource<EdiConfirmationResponse>> =
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
}