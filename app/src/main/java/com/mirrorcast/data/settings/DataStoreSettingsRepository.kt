package com.mirrorcast.data.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mirrorcast.domain.model.StreamConfig
import com.mirrorcast.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class DataStoreSettingsRepository(private val context: Context) : SettingsRepository {

    private val WIDTH_KEY = intPreferencesKey("width")
    private val HEIGHT_KEY = intPreferencesKey("height")
    private val BITRATE_KEY = intPreferencesKey("bitrate")
    private val FRAME_RATE_KEY = intPreferencesKey("frame_rate")

    override val streamConfig: Flow<StreamConfig> = context.dataStore.data.map { preferences ->
        StreamConfig(
            width = preferences[WIDTH_KEY] ?: 1080,
            height = preferences[HEIGHT_KEY] ?: 1920,
            bitrate = preferences[BITRATE_KEY] ?: 6000000,
            frameRate = preferences[FRAME_RATE_KEY] ?: 30
        )
    }

    override suspend fun updateConfig(config: StreamConfig) {
        context.dataStore.edit { preferences ->
            preferences[WIDTH_KEY] = config.width
            preferences[HEIGHT_KEY] = config.height
            preferences[BITRATE_KEY] = config.bitrate
            preferences[FRAME_RATE_KEY] = config.frameRate
        }
    }
}
