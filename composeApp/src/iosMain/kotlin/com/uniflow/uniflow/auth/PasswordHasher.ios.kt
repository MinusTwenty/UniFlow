package com.uniflow.uniflow.auth

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import platform.CoreCrypto.CC_SHA256
import platform.posix.arc4random_buf

@OptIn(ExperimentalForeignApi::class)
actual object PasswordHasher {
    actual fun sha256(input: ByteArray): ByteArray {
        val digest = ByteArray(32)

        input.usePinned { inputPinned ->
            digest.usePinned { digestPinned ->
                CC_SHA256(
                    inputPinned.addressOf(0),
                    input.size.toUInt(),
                    digestPinned.addressOf(0).reinterpret()
                )
            }
        }

        return digest
    }

    actual fun generateSalt(size: Int): ByteArray {
        val bytes = ByteArray(size)

        bytes.usePinned {
            arc4random_buf(it.addressOf(0), size.toULong())
        }

        return bytes
    }
}