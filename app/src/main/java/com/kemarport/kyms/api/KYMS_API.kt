package com.kemarport.kyms.api

import com.kemarport.kyms.models.DelayedEDI.DelayedEDIResponse
import com.kemarport.kyms.models.EDI.EdiResponse
import com.kemarport.kyms.models.generalrequestandresponse.BatchNoListResponse
import com.kemarport.kyms.models.generalrequestandresponse.GeneralRequestBerthLocation
import com.kemarport.kyms.models.generalrequestandresponse.GeneralRequestLocationId
import com.kemarport.kyms.models.generalrequestandresponse.GeneralResponse
import com.kemarport.kyms.models.itemscanning.ItemScanningResponse
import com.kemarport.kyms.models.loadedCoils.CurrentLoadingResponse
import com.kemarport.kyms.models.locations.LocationsResponse
import com.kemarport.kyms.models.login.LoginRequest
import com.kemarport.kyms.models.login.LoginResponse
import com.kemarport.kyms.models.packingList.DischargePortResponse
import com.kemarport.kyms.models.packingList.PackingListResponse
import com.kemarport.kyms.models.unloadedcoils.CurrentScanningResponse
import com.kemarport.kyms.models.vesselandintercarting.VesselAndIntercartingRequest
import com.kemarport.kyms.models.withoutasn.CreateJobMasterRequest
import com.kemarport.kyms.models.withoutasn.CreateJobMasterResponse
import retrofit2.Response
import retrofit2.http.*

interface KYMS_API {

    @POST("UserManagement/authenticate")
    suspend fun userLogin(
        @Body
        loginRequest: LoginRequest
    ): Response<LoginResponse>

    /*@GET("Job/getAllASNDetails")
    suspend fun getAllEdiDetailsData(
        @Query("JobId")
        jobId: Int,
        @Query("PageNo")
        pageNo: Int,
        @Query("PageSize")
        pageSize: Int,
        @Query("SearchText")
        searchText: String
    ): Response<EdiDetailsResponse>*/

    /*@GET("Stock/ValidatePicker")
    suspend fun validatePicker(
        @Query("BatchNumber")
        batchNo: String?
    ): Response<ValidationResponse>

    @POST("Stock/pickCoil")
    suspend fun pickCoil(
        @Body
        ediConfirmationRequest: EdiConfirmationRequest
    ): Response<EdiConfirmationResponse>*/

    /*@GET("Transaction/getCoilStatus")
    suspend fun getCoilStatusWhileDropping(
        @Query("BatchNumber")
        batchNo: String?,
        @Query("LocationID")
        locationID: Int?
    ): Response<ValidationResponse>

    @POST("Transaction/stockStore")
    suspend fun addProductInStock(
        @Body
        ediConfirmationRequest: EdiConfirmationRequest
    ): Response<EdiConfirmationResponse>*/

    /*@GET("Packing/validationStockToBerth")
    suspend fun validationStockToBerth(
        @Query("Berthlocation")
        berthLocation: String?,
        @Query("BatchNumber")
        batchNo: String?,
        @Query("Type")
        operationType: String?
    ): Response<ValidationResponse>

    @POST("Packing/StockToBerth")
    suspend fun stockToBerth(
        @Body
        coilRequest: CoilRequest
    ): Response<EdiConfirmationResponse>*/

    /*@GET("Packing/ValidationBerthToVessel")
    suspend fun validationBerthToVessel(
        @Query("BatchNumber")
        batchNo: String?,
        @Query("TransactionType")
        operationType: String?
    ): Response<ValidationResponse>*/

    //**********************************************************************************************************************//

    @GET("Job/getAllASN?skiprow=0&rowSize=15&SearchText&status=Active")
    suspend fun getEdiData(
        @Header("Authorization")
        token: String?
    ): Response<EdiResponse>

    @GET("Job/DelayASN")
    suspend fun getDelayedEdiData(
        @Header("Authorization")
        token: String?
    ): Response<DelayedEDIResponse>


    @GET("Transaction/UnloadingCoil")
    suspend fun unloadingCoil(
        @Header("Authorization")
        token: String?,
        @Query("batchNo")
        batchNo: String?,
        @Query("rakerefno")
        rakeRefNo: String,
        @Query("jobID")
        jobId: Int
    ): Response<ItemScanningResponse>

    @GET("Transaction/getUnloadedList")
    suspend fun getCurrentScanningList(
        @Header("Authorization")
        token: String?,
        @Query("JobId")
        jobId: Int
    ): Response<CurrentScanningResponse>

    @GET("LocationMaster/GetAllLocationMobile")
    suspend fun getLocations(
        @Header("Authorization")
        token: String?
    ): Response<LocationsResponse>

    @POST("Transaction/stockStore")
    suspend fun addProductInStock(
        @Header("Authorization")
        token: String?,
        @Body
        generalRequestLocationId: GeneralRequestLocationId
    ): Response<GeneralResponse>

    @POST("Packing/StockToBerth")
    suspend fun pickCoilFromStock(
        @Header("Authorization")
        token: String?,
        @Body
        generalRequestBerthLocation: GeneralRequestBerthLocation
    ): Response<GeneralResponse>

    @POST("Packing/loadingVessel")
    suspend fun loadingOnVessel(
        @Header("Authorization")
        token: String?,
        @Body
        vesselAndIntercartingRequest: VesselAndIntercartingRequest
    ): Response<GeneralResponse>

    @GET("Transaction/MarkedBTS")
    suspend fun markedForBTS(
        @Header("Authorization")
        token: String?,
        @Query("CoilBatchNo")
        batchNo: String?,
        @Query("BTSDescription")
        btsDescription: String?
    ): Response<GeneralResponse>

    @POST("Transaction/BackToStock")
    suspend fun backToStock(
        @Header("Authorization")
        token: String?,
        @Body
        generalRequestLocationId: GeneralRequestLocationId
    ): Response<GeneralResponse>

    @GET("BTT/MarkedBTT")
    suspend fun markedBTT(
        @Header("Authorization")
        token: String?,
        @Query("BatchNo")
        batchNo: String?,
        @Query("LocationId")
        locationId: Int?,
        @Query("Remark")
        remark: String?
    ): Response<GeneralResponse>

    @GET("BTT/LoadCoilBTT")
    suspend fun loadCoilBTT(
        @Header("Authorization")
        token: String?,
        @Query("VehicleNo")
        vehicleNumber: String?,
        @Query("CoilBatchNo")
        batchNo: String?,
        @Query("Remark")
        remark: String?
    ): Response<GeneralResponse>

    @GET("Job/ListBatchNo")
    suspend fun getBatchNoList(
        @Header("Authorization")
        token: String?,
    ): Response<BatchNoListResponse>

    @POST("Job/CreateJobMaster")
    suspend fun createJobMaster(
        @Header("Authorization")
        token: String?,
        @Body
        createJobMasterRequest: CreateJobMasterRequest
    ): Response<CreateJobMasterResponse>

    @GET("Packing/getallPackingMasterList?skiprow=0&rowSize=15&searchColumn&searchText")
    suspend fun getPackingListData(
        @Header("Authorization")
        token: String?
    ): Response<PackingListResponse>

    @GET("Packing/getDispatchedList")
    suspend fun getPackingListDetails(
        @Header("Authorization") token: String?,
        @Query("packingListId") packingListId: Int
    ): Response<CurrentLoadingResponse>

    @GET("Packing/getPortofDischarge")
    suspend fun dischargePortList(
        @Header("Authorization")
        token: String?,
        @Query("packingId")
        vehicleNumber: Int?
    ): Response<DischargePortResponse>


}