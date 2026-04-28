package com.uniflow.uniflow.auth

expect object PasswordHasher {
    fun sha256(input: ByteArray): ByteArray
    fun generateSalt(size: Int = 16): ByteArray
}
