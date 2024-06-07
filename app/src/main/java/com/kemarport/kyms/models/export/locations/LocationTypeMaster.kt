package com.kemarport.kyms.models.export.locations

data class LocationTypeMaster(
    val isActive: Boolean,
    val locationGPSMasters: List<Any>,
    val locationMaster: List<LocationMaster>,
    val locationType: String,
    val locationTypeId: Int,
    val totalRecords: Int
)