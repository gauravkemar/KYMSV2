package com.kemarport.kyms.models.generalrequestandresponse

data class GeneralResponse(
    val batchNumber: String,
    val currentASNScaningListResponses: CurrentASNScaningListResponses,
    val exception: Any,
    val productMessage: String,
    val statusCode: Int,
    val statusMessage: String
)