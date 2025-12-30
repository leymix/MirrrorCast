package com.mirrorcast.data.streaming

import android.content.Context
import android.content.Intent
import android.util.Log
import com.mirrorcast.domain.model.ConnectionState
import com.mirrorcast.domain.model.Device
import com.mirrorcast.domain.model.StreamConfig
import com.mirrorcast.domain.repository.StreamingRepository
import com.mirrorcast.domain.repository.SettingsRepository
import com.mirrorcast.streaming.sender.ScreenCaptureService
import com.mirrorcast.streaming.sender.TcpSender
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.first

class SocketStreamingRepository(
    private val context: Context,
    private val settingsRepository: SettingsRepository
) : StreamingRepository {

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Idle)
    override val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val tcpSender = TcpSender()
    private var currentDevice: Device? = null

    
    private var activeDevice: Device? = null

    override suspend fun connect(device: Device) {
        _connectionState.value = ConnectionState.Connecting(device)
        // Handshake validation could go here, for now we assume success if reachable
        activeDevice = device
        _connectionState.value = ConnectionState.Connected(device)
    }

    override suspend fun startStreaming(config: StreamConfig, resultCode: Int, data: Intent) {
        val device = activeDevice ?: return
        
        // Load latest config from settings if passed config is default, or just use passed
        // For MVP, lets ensure we use the persisted settings
        val currentConfig = settingsRepository.streamConfig.first()

        val intent = Intent(context, ScreenCaptureService::class.java).apply {
            action = ScreenCaptureService.ACTION_START
            putExtra(ScreenCaptureService.EXTRA_RESULT_CODE, resultCode)
            putExtra(ScreenCaptureService.EXTRA_DATA, data)
            putExtra(ScreenCaptureService.EXTRA_IP, device.ipAddress)
            putExtra(ScreenCaptureService.EXTRA_PORT, device.port)
            putExtra(ScreenCaptureService.EXTRA_CONFIG, currentConfig)
        }
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
        
        _connectionState.value = ConnectionState.Streaming(device)
    }

    override suspend fun stopStreaming() {
        val intent = Intent(context, ScreenCaptureService::class.java).apply {
            action = ScreenCaptureService.ACTION_STOP
        }
        context.startService(intent)
        val device = activeDevice
        if (device != null) {
            _connectionState.value = ConnectionState.Connected(device)
        } else {
            _connectionState.value = ConnectionState.Idle
        }
    }

    override suspend fun disconnect() {
        activeDevice = null
        _connectionState.value = ConnectionState.Idle
    }
    
    // Receiver stubs
    override suspend fun startListening() {}
    override suspend fun stopListening() {}
}
