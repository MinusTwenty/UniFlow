package com.uniflow.uniflow.auth

interface FakeAuthApi {
    suspend fun login(request: LoginRequest): AuthResponse
    suspend fun refreshToken(oldToken: String): AuthResponse?
}
