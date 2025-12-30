package com.mirrorcast.domain.model

data class Device(
    val name: String,
    val ipAddress: String,
    val port: Int = 8988,
    val info: Map<String, String> = emptyMap()
)
