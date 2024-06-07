package com.kemarport.kyms.models.export.packingList

data class PackingListResponseItem(
    val dispatchedRecord: Int,
    val fileName: String,
    val isActive: Boolean,
    val packingId: Int,
    val packingListDetails: List<Any>,
    val packingName: String,
    val status: String,
    val totalMasterRecord: Int,
    val totalRecord: Int,
    val unloadingatBirth: String
)