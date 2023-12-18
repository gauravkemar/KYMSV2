package com.kemarport.kyms.repository

import com.kemarport.kyms.api.RetrofitInstance
import com.kemarport.kyms.models.generalrequestandresponse.GeneralRequestBerthLocation
import com.kemarport.kyms.models.generalrequestandresponse.GeneralRequestLocationId
import com.kemarport.kyms.models.login.LoginRequest
import com.kemarport.kyms.models.vesselandintercarting.VesselAndIntercartingRequest
import com.kemarport.kyms.models.withoutasn.CreateJobMasterRequest

class KYMSRepository {

    //suspend fun userLogin(loginRequest: LoginRequest) = RetrofitInstance.api(baseUrl).userLogin(loginRequest)
    suspend fun userLogin(
        loginRequest: LoginRequest,
        baseUrl: String
    ) =
        RetrofitInstance.api(baseUrl).userLogin(loginRequest)

    /*suspend fun getEdiDetailsData(jobId: Int, pageNo: Int, pageSize: Int, searchText: String) =
        RetrofitInstance.api(baseUrl).getAllEdiDetailsData(jobId, pageNo, pageSize, searchText)*/

    /*suspend fun validatePicker(batchNo: String?) = RetrofitInstance.api(baseUrl).validatePicker(batchNo)

    suspend fun pickCoil(ediConfirmationRequest: EdiConfirmationRequest) =
        RetrofitInstance.api(baseUrl).pickCoil(ediConfirmationRequest)*/

    /*suspend fun getCoilStatusWhileDropping(batchNo: String?, locationID: Int?) =
        RetrofitInstance.api(baseUrl).getCoilStatusWhileDropping(batchNo, locationID)

    suspend fun addProductInStock(ediConfirmationRequest: EdiConfirmationRequest) =
        RetrofitInstance.api(baseUrl).addProductInStock(ediConfirmationRequest)*/

    /*suspend fun validationStockToBerth(
        berthLocation: String?,
        batchNo: String?,
        operationType: String?
    ) = RetrofitInstance.api(baseUrl).validationStockToBerth(berthLocation, batchNo, operationType)

    suspend fun stockToBerth(
        coilRequest: CoilRequest
    ) = RetrofitInstance.api(baseUrl).stockToBerth(coilRequest)*/

    /*suspend fun validationBerthToVessel(
        batchNo: String?,
        operationType: String?
    ) = RetrofitInstance.api(baseUrl).validationBerthToVessel(batchNo, operationType)*/

    //**********************************************************************************************************************//

    suspend fun getEdiData(
        baseUrl: String,
        token: String?
    ) = RetrofitInstance.api(baseUrl).getEdiData(token)

    suspend fun getDelayedEdiData(
        baseUrl: String,
        token: String?
    ) = RetrofitInstance.api(baseUrl).getDelayedEdiData(token)

    suspend fun unloadingCoil(
        token: String?,
        batchNo: String?,
        rakeRefNo: String,
        jobId: Int,
        baseUrl: String
    ) = RetrofitInstance.api(baseUrl).unloadingCoil(token, batchNo, rakeRefNo, jobId)

    suspend fun getCurrentScanningList(
        jobId: Int,
        baseUrl: String,
        token: String?
    ) =
        RetrofitInstance.api(baseUrl).getCurrentScanningList(token, jobId)

    suspend fun getLocations(
        token: String?,
        baseUrl: String
    ) = RetrofitInstance.api(baseUrl).getLocations(token)

    suspend fun addProductInStock(
        token: String?,
        generalRequestLocationId: GeneralRequestLocationId,
        baseUrl: String
    ) = RetrofitInstance.api(baseUrl).addProductInStock(token, generalRequestLocationId)

    suspend fun pickCoilFromStock(
        token: String?,
        generalRequestBerthLocation: GeneralRequestBerthLocation,
        baseUrl: String
    ) = RetrofitInstance.api(baseUrl).pickCoilFromStock(token, generalRequestBerthLocation)

    suspend fun loadingOnVessel(
        token: String?,
        vesselAndIntercartingRequest: VesselAndIntercartingRequest,
        baseUrl: String
    ) = RetrofitInstance.api(baseUrl).loadingOnVessel(token, vesselAndIntercartingRequest)

    suspend fun markedForBTS(
        token: String?,
        batchNo: String?,
        btsDescription: String?,
        baseUrl: String
    ) = RetrofitInstance.api(baseUrl).markedForBTS(token, batchNo, btsDescription)

    suspend fun backToStock(
        token: String?,
        generalRequestLocationId: GeneralRequestLocationId,
        baseUrl: String
    ) = RetrofitInstance.api(baseUrl).backToStock(token, generalRequestLocationId)

    suspend fun markedBTT(
        token: String?,
        batchNo: String?,
        locationId: Int?,
        remark: String?,
        baseUrl: String
    ) = RetrofitInstance.api(baseUrl).markedBTT(token, batchNo, locationId, remark)

    suspend fun loadCoilBTT(
        token: String?,
        vehicleNumber: String?,
        batchNo: String?,
        remark: String?,
        baseUrl: String
    ) = RetrofitInstance.api(baseUrl).loadCoilBTT(token, vehicleNumber, batchNo, remark)

    suspend fun getBatchNoList(
        token: String?,
        baseUrl: String
    ) = RetrofitInstance.api(baseUrl).getBatchNoList(token)

    suspend fun createJobMaster(
        token: String?,
        baseUrl: String,
        createJobMasterRequest: CreateJobMasterRequest
    ) = RetrofitInstance.api(baseUrl).createJobMaster(token, createJobMasterRequest)

    suspend fun getPackingListData(
        token: String?,
        baseUrl: String
    ) = RetrofitInstance.api(baseUrl).getPackingListData(token)

    suspend fun getPackingListDetails(
        token: String?,
        baseUrl: String,
        packingListId : Int
    ) = RetrofitInstance.api(baseUrl).getPackingListDetails(token,packingListId)

    suspend fun dischargePortList(
        token: String?,
        baseUrl: String,
        packingListId : Int
    ) = RetrofitInstance.api(baseUrl).dischargePortList(token,packingListId)

}