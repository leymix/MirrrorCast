package com.mirrorcast.domain.repository

import com.mirrorcast.domain.model.ConnectionState
import com.mirrorcast.domain.model.Device
import com.mirrorcast.domain.model.StreamConfig
import kotlinx.coroutines.flow.StateFlow

interface StreamingRepository {
    val connectionState: StateFlow<ConnectionState>
    
    suspend fun connect(device: Device)
    suspend fun startStreaming(config: StreamConfig, resultCode: Int, data: android.content.Intent)
    suspend fun stopStreaming()
    suspend fun disconnect()
    
    // Receiver methods
    suspend fun startListening()
    suspend fun stopListening()
}
