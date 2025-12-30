package com.mirrorcast.ui.home

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mirrorcast.domain.model.ConnectionState
import com.mirrorcast.domain.repository.DiscoveryRepository
import com.mirrorcast.domain.repository.StreamingRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val discoveryRepository: DiscoveryRepository,
    private val streamingRepository: StreamingRepository,
    private val wifiManager: WifiManager?,
    private val connectivityManager: ConnectivityManager?
) : ViewModel() {

    private val _isNetworkAvailable = kotlinx.coroutines.flow.MutableStateFlow(false)
    val isNetworkAvailable: StateFlow<Boolean> = _isNetworkAvailable.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: android.net.Network) {
            checkConnection()
        }

        override fun onLost(network: android.net.Network) {
            checkConnection()
        }
        
        override fun onCapabilitiesChanged(network: android.net.Network, networkCapabilities: NetworkCapabilities) {
            checkConnection()
        }
    }

    init {
        // Initial check
        checkConnection()
        
        // Register callback
        try {
            val request = android.net.NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
                .build()
            connectivityManager?.registerNetworkCallback(request, networkCallback)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun checkConnection() {
        if (connectivityManager == null) {
            _isNetworkAvailable.value = false
            return
        }
        
        try {
            val activeNetwork = connectivityManager.activeNetwork
            val caps = connectivityManager.getNetworkCapabilities(activeNetwork)
            val isWifiOrEthernet = caps != null && (
                caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || 
                caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
            )
            _isNetworkAvailable.value = isWifiOrEthernet
        } catch (e: Exception) {
            _isNetworkAvailable.value = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            connectivityManager?.unregisterNetworkCallback(networkCallback)
        } catch (e: Exception) {
            // Ignore
        }
    }

    val connectionState: StateFlow<ConnectionState> = streamingRepository.connectionState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ConnectionState.Idle
        )
        
    val discoveredDevices = discoveryRepository.discoveredDevices
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun startDiscovery() {
        discoveryRepository.startDiscovery()
    }
    
    fun stopDiscovery() {
        discoveryRepository.stopDiscovery()
    }
    
    fun connectToDevice(device: com.mirrorcast.domain.model.Device) {
        viewModelScope.launch {
            streamingRepository.connect(device)
            // Note: In a real flow, after connecting (handshake), we would ask for permission.
            // But here we trigger the UI to ask for permission immediately.
        }
    }
    
    fun startMirroring(resultCode: Int, data: android.content.Intent) {
        viewModelScope.launch {
            streamingRepository.startStreaming(com.mirrorcast.domain.model.StreamConfig(), resultCode, data)
        }
    }
    
    // Explicit check method if needed (using the latest state)
    fun isConnectionAvailable(): Boolean {
        return _isNetworkAvailable.value
    }

    companion object {
        fun provideFactory(
            discoveryRepository: DiscoveryRepository,
            streamingRepository: StreamingRepository,
            context: Context
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
                val connectivityManager = context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                return HomeViewModel(discoveryRepository, streamingRepository, wifiManager, connectivityManager) as T
            }
        }
    }
}

class SettingsViewModel(
    private val settingsRepository: com.mirrorcast.domain.repository.SettingsRepository
) : ViewModel() {

    val config = settingsRepository.streamConfig.stateIn(
        viewModelScope, 
        SharingStarted.WhileSubscribed(5000), 
        com.mirrorcast.domain.model.StreamConfig()
    )

    fun updateResolution(width: Int, height: Int) {
        viewModelScope.launch {
            val current = config.value
            settingsRepository.updateConfig(current.copy(width = width, height = height))
        }
    }
    
    fun updateBitrate(bitrate: Int) {
        viewModelScope.launch {
            val current = config.value
            settingsRepository.updateConfig(current.copy(bitrate = bitrate))
        }
    }
    
    companion object {
        fun provideFactory(
            settingsRepository: com.mirrorcast.domain.repository.SettingsRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SettingsViewModel(settingsRepository) as T
            }
        }
    }
}
