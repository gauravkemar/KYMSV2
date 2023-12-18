package com.kemarport.kyms.models.EDI

data class EdiConfirmationRequest(
    val LocationId: Int?,
    val coilList: MutableList<Coil?>
)