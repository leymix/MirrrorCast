package com.mirrorcast.ui.update

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mirrorcast.data.update.UpdateInfo
import com.mirrorcast.data.update.UpdateRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for checking app updates
 */
class UpdateCheckerViewModel(
    private val updateRepository: UpdateRepository
) : ViewModel() {

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    /**
     * Check for updates
     */
    fun checkForUpdate() {
        viewModelScope.launch {
            _updateState.value = UpdateState.Checking
            try {
                val updateInfo = updateRepository.checkForUpdate()
                if (updateInfo != null) {
                    _updateState.value = UpdateState.UpdateAvailable(updateInfo)
                } else {
                    _updateState.value = UpdateState.NoUpdate
                }
            } catch (e: Exception) {
                _updateState.value = UpdateState.Error(e.message ?: "Güncelleme kontrolü başarısız")
            }
        }
    }

    /**
     * Reset update state
     */
    fun resetState() {
        _updateState.value = UpdateState.Idle
    }

    companion object {
        fun provideFactory(
            updateRepository: UpdateRepository
        ): androidx.lifecycle.ViewModelProvider.Factory {
            return object : androidx.lifecycle.ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return UpdateCheckerViewModel(updateRepository) as T
                }
            }
        }
    }
}

/**
 * Update check states
 */
sealed class UpdateState {
    object Idle : UpdateState()
    object Checking : UpdateState()
    data class UpdateAvailable(val updateInfo: UpdateInfo) : UpdateState()
    object NoUpdate : UpdateState()
    data class Error(val message: String) : UpdateState()
}

