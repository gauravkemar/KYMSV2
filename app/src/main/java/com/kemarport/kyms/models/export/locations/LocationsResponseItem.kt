package com.kemarport.kyms.models.export.locations

data class LocationsResponseItem(
    val barcode: String,
    val createdBy: Any,
    val createdDate: Any,
    val isActive: Boolean,
    val locationCode: String,
    val locationGPSMasters: List<Any>,
    val locationId: Int,
    val locationName: String,
    val locationTypeId: Int,
    val locationTypeMaster: LocationTypeMaster,
    val modifiedBy: Any,
    val modifiedDate: Any,
    val totalRecords: Int
)