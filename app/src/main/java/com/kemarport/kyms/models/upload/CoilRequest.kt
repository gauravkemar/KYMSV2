package com.kemarport.kyms.models.upload

data class CoilRequest(
    val BerthLocation: String?,
    val TransactionType: String?,
    val coilList: MutableList<Coil?>
)