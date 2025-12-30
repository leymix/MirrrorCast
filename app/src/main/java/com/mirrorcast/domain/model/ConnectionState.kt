package com.mirrorcast.domain.model

sealed class ConnectionState {
    object Idle : ConnectionState()
    object Discovering : ConnectionState()
    data class Connecting(val device: Device) : ConnectionState()
    data class Connected(val device: Device) : ConnectionState()
    data class Streaming(val device: Device) : ConnectionState()
    data class Error(val message: String) : ConnectionState()
}
