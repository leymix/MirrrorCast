package com.mirrorcast

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.material3.Text
import com.mirrorcast.ui.theme.MirrorCastTheme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.mirrorcast.ui.onboarding.OnboardingScreen
import com.mirrorcast.ui.settings.SettingsScreen
import com.mirrorcast.ui.home.HomeScreen
import com.mirrorcast.ui.connection.ConnectionScreen
import com.mirrorcast.domain.model.Device

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val appContainer = (application as MirrorCastApplication).container
        
        setContent {
            MirrorCastTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var currentScreen by remember { mutableStateOf("onboarding") }
                    
                    when (currentScreen) {
                        "onboarding" -> OnboardingScreen(onFinished = { currentScreen = "home" })
                        "home" -> {
                            val viewModel: com.mirrorcast.ui.home.HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                                factory = com.mirrorcast.ui.home.HomeViewModel.provideFactory(
                                    appContainer.discoveryRepository,
                                    appContainer.streamingRepository,
                                    this
                                )
                            )
                            HomeScreen(
                                onSettingsClick = { currentScreen = "settings" },
                                viewModel = viewModel
                            )
                        }
                        "settings" -> {
                            val viewModel: com.mirrorcast.ui.home.SettingsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                                factory = com.mirrorcast.ui.home.SettingsViewModel.provideFactory(
                                    appContainer.settingsRepository
                                )
                            )
                            SettingsScreen(
                                onBack = { currentScreen = "home" },
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
        }
    }
}
