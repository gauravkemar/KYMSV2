package com.kemarport.kyms.models.EDI

data class EdiResponseItem(
    val createdBy: String,
    val createdDate: String,
    val fileStatus: Any,
    val isActive: Boolean,
    val jobDetailsResponse: List<Any>,
    val jobFileName: String,
    val jobId: Int,
    val jobName: String,
    val jobStatus: String,
    val modifiedBy: Any,
    val modifiedDate: Any,
    val pendingUnloadingRecord: Int,
    val rakeArrivalConfirmationTime: Any,
    val rakeArrivalConfirmedBy: Any,
    val rakeArrivalTime: Any,
    val rakeDepartureConfirmationTime: Any,
    val rakeDepartureConfirmedBy: Any,
    val rakeDepartureTime: Any,
    val rakeRefNo: String,
    val source: Any,
    val transportMode: String,
    val unloadedRecord: Int
)