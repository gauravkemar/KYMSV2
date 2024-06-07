package com.kemarport.kyms.models.export.vesselandintercarting

data class VesselAndIntercartingRequest(
    val CoilBatchNo: String?,
    val HatchNo: String?,
    val TransactionType: String?,
    val PackingId: Int?,
    val PortOfDischarge: String?
)