package com.uniflow.uniflow.auth

object AuthInputNormalizer {

    fun normalizeIdentifier(raw: String): String {
        val value = raw.trim()

        if (value.isEmpty()) return value

        return if ("@" in value) {
            value.substringBefore("@").trim()
        } else {
            value
        }
    }
}