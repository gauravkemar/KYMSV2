package com.kemarport.kyms.models.itemscanning

data class ItemScanningResponse(
    val batchNumber: String,
    val currentASNScaningListResponses: CurrentASNScaningListResponses,
    val exception: Any,
    val productMessage: String,
    val statusCode: Int,
    val statusMessage: String
)