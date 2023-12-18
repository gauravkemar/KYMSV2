package com.kemarport.kyms.models.EDI

data class EdiConfirmationResponse(
    val errorMessage: Any,
    val exception: Any,
    val partiallySave: List<PartiallySave>,
    val responseMessage: String,
    val statusCode: Int
)