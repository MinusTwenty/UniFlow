package com.uniflow.uniflow.auth

import com.uniflow.database.UniFlowDatabase
import kotlinx.coroutines.delay
import kotlin.random.Random
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class DbAuthRepository(
    private val db: UniFlowDatabase
) : FakeAuthApi {

    private val activeTokens = mutableMapOf<String, Long>()

    override suspend fun login(request: LoginRequest): AuthResponse {
        delay(300)

        val normalizedIdentifier = AuthInputNormalizer.normalizeIdentifier(request.identifier)

        if (normalizedIdentifier.isBlank()) {
            throw IllegalArgumentException("Azonosító megadása kötelező")
        }

        val dbUser = db.authQueries
            .getUserByUsername(normalizedIdentifier)
            .executeAsOneOrNull()
            ?: throw IllegalArgumentException("Nincs ilyen felhasználó")

        val isValid = verifyPassword(
            password = request.password,
            salt = dbUser.password_salt,
            expectedHash = dbUser.password_hash
        )

        if (!isValid) {
            throw IllegalArgumentException("Hibás jelszó")
        }

        val accessToken = generateToken()
        val refreshToken = if (request.rememberMe) generateToken() else null
        val expiryDays = if (request.rememberMe) 7 else 1
        val expiryTime =
            getCurrentTimeMillis() + expiryDays.toDuration(DurationUnit.DAYS).inWholeMilliseconds

        activeTokens[accessToken] = expiryTime

        return AuthResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresInDays = expiryDays,
            userId = dbUser.id
        )
    }

    override suspend fun refreshToken(oldToken: String): AuthResponse? {
        delay(200)

        return if (activeTokens.containsKey(oldToken)) {
            val newToken = generateToken()
            AuthResponse(
                accessToken = newToken,
                refreshToken = oldToken,
                expiresInDays = 7,
                userId = -1L
            )
        } else {
            null
        }
    }

    private fun generateToken(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..32).joinToString("") {
            chars[Random.nextInt(chars.length)].toString()
        }
    }
}