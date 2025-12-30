package com.mirrorcast.data.discovery

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import com.mirrorcast.domain.model.Device
import com.mirrorcast.domain.repository.DiscoveryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.net.InetAddress
import java.util.concurrent.ConcurrentHashMap

class NsdDiscoveryRepository(context: Context) : DiscoveryRepository {

    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
    
    // Use a map to handle duplicates and ensure uniqueness by service name
    private val _discoveredServices = ConcurrentHashMap<String, Device>()
    private val _devicesFlow = MutableStateFlow<List<Device>>(emptyList())
    
    override val discoveredDevices: StateFlow<List<Device>> = _devicesFlow.asStateFlow()
    
    // Service Type for MirrorCast
    private val SERVICE_TYPE = "_mirrorcast._tcp."
    private var isDiscovering = false

    private val discoveryListener = object : NsdManager.DiscoveryListener {
        override fun onDiscoveryStarted(regType: String) {
            Log.d(TAG, "Service discovery started")
            isDiscovering = true
        }

        override fun onServiceFound(service: NsdServiceInfo) {
            Log.d(TAG, "Service discovery success: $service")
            if (service.serviceType.contains("mirrorcast")) { // Check partial match due to potential protocol suffix
                nsdManager.resolveService(service, resolveListener)
            }
        }

        override fun onServiceLost(service: NsdServiceInfo) {
            Log.e(TAG, "service lost: $service")
            _discoveredServices.remove(service.serviceName)
            updateDevicesList()
        }

        override fun onDiscoveryStopped(serviceType: String) {
            Log.i(TAG, "Discovery stopped: $serviceType")
            isDiscovering = false
        }

        override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
            Log.e(TAG, "Discovery failed: Error code:$errorCode")
            nsdManager.stopServiceDiscovery(this)
        }

        override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
            Log.e(TAG, "Discovery failed: Error code:$errorCode")
            nsdManager.stopServiceDiscovery(this)
        }
    }

    private val resolveListener = object : NsdManager.ResolveListener {
        override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            Log.e(TAG, "Resolve failed: $errorCode")
        }

        override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
            Log.i(TAG, "Resolve Succeeded. $serviceInfo")

            if (serviceInfo.host == null) return

            val device = Device(
                name = serviceInfo.serviceName,
                ipAddress = serviceInfo.host.hostAddress ?: "",
                port = serviceInfo.port
            )
            
            _discoveredServices[serviceInfo.serviceName] = device
            updateDevicesList()
        }
    }

    private fun updateDevicesList() {
        _devicesFlow.update { _discoveredServices.values.toList() }
    }

    override fun startDiscovery() {
        if (!isDiscovering) {
            try {
                nsdManager.discoverServices(
                    SERVICE_TYPE,
                    NsdManager.PROTOCOL_DNS_SD,
                    discoveryListener
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start discovery", e)
            }
        }
    }

    override fun stopDiscovery() {
        if (isDiscovering) {
            try {
                nsdManager.stopServiceDiscovery(discoveryListener)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to stop discovery", e)
            }
        }
    }

    companion object {
        private const val TAG = "NsdDiscoveryRepo"
    }
}
