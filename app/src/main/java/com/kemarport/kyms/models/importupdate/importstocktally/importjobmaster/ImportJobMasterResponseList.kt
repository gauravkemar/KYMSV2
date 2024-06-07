package com.kemarport.kyms.models.importupdate.importstocktally.importjobmaster

data class ImportJobMasterResponseList(
    val arrivalDateTime: String,
    val imoNumber: String,
    val importJobDetailId: Double,
    val importJobMasterId: Double,
    val instockCount: Double,
    val jobFileName: String,
    val pendingCount: Double,
    val stockTallyCount: Double,
    val totalCount: Double,
    val vesselName: String
)