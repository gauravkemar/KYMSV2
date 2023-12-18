package com.kemarport.kyms.models.unloadedcoils

data class CurrentScanningResponseItem(
    val actualRakeCode: String,
    val batchNo: String,
    val grossWeight: Any,
    val heatNo: Any,
    val portOfDischarge: String,
    val netWeight: Any,
    val productStatus: String,
    val rakeRefNo: String,
    val shipToPartyName: String,
    val wagonNo: Any
)

