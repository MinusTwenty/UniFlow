package com.uniflow.uniflow.auth

import kotlinx.coroutines.delay
import kotlin.random.Random
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class FakeAuthRepository : FakeAuthApi {

    private val fakeUsers = mutableListOf(
        User(id = "1", email = "134288@student.ujs.sk", password = "demo123")
    )

    private val activeTokens = mutableMapOf<String, Long>()

    override suspend fun login(request: LoginRequest): AuthResponse {
        delay(500)

        val user = fakeUsers.find { it.email == request.email }

        return if (user != null && user.password == request.password) {
            val accessToken = generateToken()
            val refreshToken = if (request.rememberMe) generateToken() else null
            val expiryDays = if (request.rememberMe) 7 else 1

            // ✅ Cross-platform timestamp (milliseconds)
            val expiryTime = getCurrentTimeMillis() + expiryDays.toDuration(DurationUnit.DAYS).inWholeMilliseconds

            activeTokens[accessToken] = expiryTime

            AuthResponse(accessToken, refreshToken, expiryDays)
        } else {
            throw IllegalArgumentException("Invalid email or password")
        }
    }

    override suspend fun refreshToken(oldToken: String): AuthResponse? {
        delay(300)
        return if (activeTokens.containsKey(oldToken)) {
            val newToken = generateToken()
            AuthResponse(newToken, oldToken, 7)
        } else null
    }

    private fun generateToken(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..32).joinToString("") { chars[Random.nextInt(chars.length)].toString() }
    }
}