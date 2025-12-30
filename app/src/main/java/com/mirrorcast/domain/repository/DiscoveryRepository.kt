package com.mirrorcast.domain.repository

import com.mirrorcast.domain.model.Device
import kotlinx.coroutines.flow.Flow

interface DiscoveryRepository {
    val discoveredDevices: Flow<List<Device>>
    fun startDiscovery()
    fun stopDiscovery()
}
