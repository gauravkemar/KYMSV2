package com.kemarport.kyms.models.importupdate

data class GetMasterMobileResponse(
    val errorMessage: String?,
    val exception: String?,
    val responseList: ArrayList<Any>?,
    val responseMessage: String?,
    val responseObject: Any?,
    val statusCode:Int

)