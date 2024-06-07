package com.kemarport.kyms.models.export.login

data class LoginResponse(
    val email: String,
    val firstName: String,
    val isVerified: Boolean,
    val jwtToken: String,
    val lastName: String,
    val menuAccess: List<MenuAcces>,
    val mobileNumber: String,
    val refreshToken: String,
    val roleName: String,
    val userAccess: List<UserAcces>,
    val userName: String
)