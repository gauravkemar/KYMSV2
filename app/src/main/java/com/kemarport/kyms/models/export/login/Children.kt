package com.kemarport.kyms.models.export.login

data class Children(
    val children: List<Any>,
    val displayName: String,
    val menuIcon: String,
    val parentCategory: Any,
    val parentId: Int,
    val routingURL: String
)