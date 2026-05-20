package com.example.sicenetmultiplatform

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform