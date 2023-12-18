package com.kemarport.kyms.models.vesselandintercarting

data class VesselAndIntercartingRequest(
    val CoilBatchNo: String?,
    val HatchNo: String?,
    val TransactionType: String?,
    val PackingId: Int?,
    val PortOfDischarge: String?
)