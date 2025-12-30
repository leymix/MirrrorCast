package com.mirrorcast.domain.repository

import com.mirrorcast.domain.model.StreamConfig
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val streamConfig: Flow<StreamConfig>
    suspend fun updateConfig(config: StreamConfig)
}
