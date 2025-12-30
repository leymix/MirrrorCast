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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.mirrorcast.ui.onboarding.OnboardingScreen
import com.mirrorcast.ui.settings.SettingsScreen
import com.mirrorcast.ui.home.HomeScreen
import com.mirrorcast.ui.connection.ConnectionScreen
import com.mirrorcast.ui.update.UpdateDialog
import com.mirrorcast.ui.update.UpdateCheckerViewModel
import com.mirrorcast.ui.update.UpdateState
import com.mirrorcast.ui.update.downloadAndInstallApk
import com.mirrorcast.domain.model.Device

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val appContainer = (application as MirrorCastApplication).container
        
        setContent {
            MirrorCastTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val context = LocalContext.current
                    var currentScreen by remember { mutableStateOf("onboarding") }
                    
                    // Update checker ViewModel
                    val updateViewModel: UpdateCheckerViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                        factory = UpdateCheckerViewModel.provideFactory(
                            appContainer.updateRepository
                        )
                    )
                    
                    val updateState by updateViewModel.updateState
                    
                    // Check for updates when app starts (only for production)
                    LaunchedEffect(Unit) {
                        if (com.mirrorcast.BuildConfig.ENVIRONMENT == "production") {
                            updateViewModel.checkForUpdate()
                        }
                    }
                    
                    // Show update dialog if update is available
                    when (val state = updateState) {
                        is UpdateState.UpdateAvailable -> {
                            UpdateDialog(
                                updateInfo = state.updateInfo,
                                onDismiss = {
                                    updateViewModel.resetState()
                                },
                                onDownload = {
                                    downloadAndInstallApk(
                                        context,
                                        state.updateInfo.downloadUrl,
                                        state.updateInfo.versionName
                                    )
                                    updateViewModel.resetState()
                                }
                            )
                        }
                        else -> {}
                    }
                    
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
