package com.kemarport.kyms.models.login

data class UserAcces(
    val canCreate: Boolean,
    val canDeactivate: Boolean,
    val canRead: Boolean,
    val canUpdate: Boolean,
    val screenCode: String
)