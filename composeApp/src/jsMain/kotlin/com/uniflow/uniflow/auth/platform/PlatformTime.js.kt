package com.uniflow.uniflow.auth

actual fun getCurrentTimeMillis(): Long = js("Date.now()") as Long
