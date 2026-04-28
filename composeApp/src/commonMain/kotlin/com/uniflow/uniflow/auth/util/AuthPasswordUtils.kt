package com.uniflow.uniflow.auth

fun hashPasswordWithSalt(password: String, salt: ByteArray): ByteArray {
    val passwordBytes = password.encodeToByteArray()
    val combined = ByteArray(salt.size + passwordBytes.size)

    salt.copyInto(combined, destinationOffset = 0)
    passwordBytes.copyInto(combined, destinationOffset = salt.size)

    return PasswordHasher.sha256(combined)
}

fun verifyPassword(
    password: String,
    salt: ByteArray,
    expectedHash: ByteArray
): Boolean {
    val actualHash = hashPasswordWithSalt(password, salt)
    return actualHash.contentEquals(expectedHash)
}
