package com.kemarport.kyms.models.importupdate.importstocktally.importjobmaster

data class GetJobDetailsListItemResponse(
    val batchNumber: String,
    val importJobDetailId: Int,
    val productId: Int,
    val status: String
)
