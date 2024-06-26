package com.kemarport.kyms.models.export.itemscanning

data class ValidationResponse(
    val batchNumber: String,
    val currentASNScaningListResponses: CurrentASNScaningListResponsesX,
    val exception: Any,
    val productMessage: String,
    val statusCode: Int,
    val statusMessage: String
)