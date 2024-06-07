package com.kemarport.kyms.models.export.upload

data class CoilRequest(
    val BerthLocation: String?,
    val TransactionType: String?,
    val coilList: MutableList<Coil?>
)