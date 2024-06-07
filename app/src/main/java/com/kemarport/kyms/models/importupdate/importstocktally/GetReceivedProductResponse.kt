package com.kemarport.kyms.models.importupdate.importstocktally

data class GetReceivedProductResponse(
    val id: Int,
    val batchno: String,
    val stockTallyCount: Int,
    val pendingCount: Int,
    val totalCount: Int,
    val instockCount: Int
)