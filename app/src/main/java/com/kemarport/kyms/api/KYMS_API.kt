package com.kemarport.kyms.api

import com.kemarport.kyms.helper.Constants
import com.kemarport.kyms.models.export.DelayedEDI.DelayedEDIResponse
import com.kemarport.kyms.models.export.EDI.EdiResponse
import com.kemarport.kyms.models.export.generalrequestandresponse.BatchNoListResponse
import com.kemarport.kyms.models.export.generalrequestandresponse.GeneralRequestBerthLocation
import com.kemarport.kyms.models.export.generalrequestandresponse.GeneralRequestLocationId
import com.kemarport.kyms.models.export.generalrequestandresponse.GeneralResponse
import com.kemarport.kyms.models.export.itemscanning.ItemScanningResponse
import com.kemarport.kyms.models.export.loadedCoils.CurrentLoadingResponse
import com.kemarport.kyms.models.export.locations.LocationsResponse
import com.kemarport.kyms.models.export.login.LoginRequest
import com.kemarport.kyms.models.export.login.LoginResponse
import com.kemarport.kyms.models.export.packingList.DischargePortResponse
import com.kemarport.kyms.models.export.packingList.PackingListResponse
import com.kemarport.kyms.models.export.unloadedcoils.CurrentScanningResponse
import com.kemarport.kyms.models.export.vesselandintercarting.VesselAndIntercartingRequest
import com.kemarport.kyms.models.export.withoutasn.CreateJobMasterRequest
import com.kemarport.kyms.models.export.withoutasn.CreateJobMasterResponse
import com.kemarport.kyms.models.importupdate.GetMasterMobileResponse
import com.kemarport.kyms.models.importupdate.importstocktally.GetReceivedProductResponse
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
        generalRequestLocationId:GeneralRequestLocationId
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
        generalRequestLocationId:GeneralRequestLocationId
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

    //////////////////////////////////////////////////////////////////////////////////////////////

    //import

    @GET( Constants.GET_JOB_MASTER_MOBILE)
    suspend fun getJobMasterMobile(
        @Header("Authorization")
        token: String?,
        @Query("skiprow")
        skiprow: Int?,
        @Query("rowSize")
        rowSize: Int?,
    ): Response<GetMasterMobileResponse>

    @GET( Constants.GET_JOB_DETAILS_MOBILE)
    suspend fun getJobDetailsMobile(
        @Header("Authorization")
        token: String?,
        @Query("importJobMasterId")
        importJobMasterId: Int?,
        @Query("skiprow")
        skiprow: Int?,
        @Query("rowSize")
        rowSize: Int?,
    ): Response<GetMasterMobileResponse>

    @GET( Constants.GET_RECEIVED_PRODUCT)
    suspend fun getReceivedProduct(
        @Header("Authorization")
        token: String?,
        @Query("BatchNo")
        BatchNo: String?,
        @Query("LocationId")
        LocationId: Int?,
        @Query("Coordinate")
        Coordinate: String?,
    ): Response<GetMasterMobileResponse>

    @GET( Constants.GET_STORED_PRODUCT)
    suspend fun getStoredProduct(
        @Header("Authorization")
        token: String?,
        @Query("BatchNo")
        BatchNo: String?,
        @Query("LocationId")
        LocationId: Int?,
        @Query("Coordinate")
        Coordinate: String?,
    ): Response<GetMasterMobileResponse>




}