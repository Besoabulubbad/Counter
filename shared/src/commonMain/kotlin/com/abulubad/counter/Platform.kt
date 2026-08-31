package com.abulubad.counter

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform