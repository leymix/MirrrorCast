package com.mirrorcast.di

import android.content.Context
import com.mirrorcast.data.discovery.NsdDiscoveryRepository
import com.mirrorcast.domain.repository.DiscoveryRepository
import com.mirrorcast.domain.repository.StreamingRepository
import com.mirrorcast.data.settings.DataStoreSettingsRepository
import com.mirrorcast.data.streaming.SocketStreamingRepository

interface AppContainer {
    val discoveryRepository: DiscoveryRepository
    val streamingRepository: StreamingRepository
    val settingsRepository: com.mirrorcast.domain.repository.SettingsRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {
    override val settingsRepository by lazy {
        DataStoreSettingsRepository(context)
    }

    override val discoveryRepository: DiscoveryRepository by lazy {
        NsdDiscoveryRepository(context)
    }
    
    override val streamingRepository: StreamingRepository by lazy {
        SocketStreamingRepository(context, settingsRepository)
    }
}
