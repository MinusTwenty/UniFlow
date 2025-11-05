package com.uniflow.uniflow

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform