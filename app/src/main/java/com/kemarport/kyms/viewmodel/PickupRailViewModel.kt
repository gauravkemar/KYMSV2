package com.kemarport.kyms.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.kemarport.kyms.helper.Resource
import com.kemarport.kyms.models.EDI.EdiConfirmationResponse
import com.kemarport.kyms.models.itemscanning.ValidationResponse
import com.kemarport.kyms.repository.KYMSRepository

class PickupRailViewModel(
    application: Application,
    val kymsRepository: KYMSRepository
) : AndroidViewModel(application) {
    val validatePickerMutableLiveData: MutableLiveData<Resource<ValidationResponse>> =
        MutableLiveData()
    val pickCoilMutableLiveData: MutableLiveData<Resource<EdiConfirmationResponse>> =
        MutableLiveData()

    /*fun validatePicker(batchNo: String?) = viewModelScope.launch {
        validatePickerMutableLiveData.postValue(Resource.Loading())
        val response = kymsRepository.validatePicker(batchNo)
        validatePickerMutableLiveData.postValue(handleValidatePickerResponse(response))
    }

    private fun handleValidatePickerResponse(response: Response<ValidationResponse>): Resource<ValidationResponse> {
        if (response.isSuccessful) {
            response.body()?.let { validationResponse ->
                return Resource.Success(validationResponse)
            }
        }

        return Resource.Error(response.message())
    }

    fun pickCoil(ediConfirmationRequest: EdiConfirmationRequest) = viewModelScope.launch {
        pickCoilMutableLiveData.postValue(Resource.Loading())
        val response = kymsRepository.pickCoil(ediConfirmationRequest)
        pickCoilMutableLiveData.postValue(handlePickCoilResponse(response))
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