package com.kemarport.kyms.models.export.EDI

data class EdiConfirmationRequest(
    val LocationId: Int?,
    val coilList: MutableList<Coil?>
)