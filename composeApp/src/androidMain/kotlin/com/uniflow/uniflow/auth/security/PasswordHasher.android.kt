package com.uniflow.uniflow.auth

import java.security.MessageDigest
import java.security.SecureRandom

actual object PasswordHasher {
    actual fun sha256(input: ByteArray): ByteArray {
        return MessageDigest.getInstance("SHA-256").digest(input)
    }

    actual fun generateSalt(size: Int): ByteArray {
        val bytes = ByteArray(size)
        SecureRandom().nextBytes(bytes)
        return bytes
    }
}
