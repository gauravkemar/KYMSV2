package com.kemarport.kyms.models.DelayedEDI

data class DelayedEDIResponseItem(
    val createdBy: String,
    val createdDate: String,
    val fileStatus: String,
    val isActive: Boolean,
    val jobDetailsResponse: List<Any>,
    val jobFileName: String,
    val jobId: Int,
    val jobName: String,
    val jobStatus: String,
    val modifiedBy: Any,
    val modifiedDate: Any,
    val pendingUnloadingRecord: Int,
    val rakeArrivalBy: Any,
    val rakeArrivalTime: Any,
    val rakeDepartureBy: Any,
    val rakeDepartureTime: Any,
    val rakeRefNo: String,
    val source: Any,
    val transportMode: String,
    val unloadedRecord: Int
)