package com.uniflow.uniflow.auth

data class LoginRequest(
    val email: String,
    val password: String,
    val rememberMe: Boolean = false
)

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String?,
    val expiresInDays: Int
)

data class User(
    val id: String,
    val email: String,
    val password: String // plain text for fake db only!
)
