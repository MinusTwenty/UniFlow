package com.uniflow.uniflow.auth

import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
actual fun getCurrentTimeMillis(): Long = Clock.System.now().toEpochMilliseconds()