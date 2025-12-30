package com.mirrorcast.ui.home

import android.app.Activity
import android.content.Context
import android.media.projection.MediaProjectionManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mirrorcast.MirrorCastApp
import com.mirrorcast.domain.model.ConnectionState
import com.mirrorcast.ui.components.BigConnectButton
import com.mirrorcast.ui.components.DeviceListItem

@Composable
fun HomeScreen(
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel
) {
    val context = LocalContext.current

    
    val connectionState by viewModel.connectionState.collectAsState()
    val nearbyDevices by viewModel.discoveredDevices.collectAsState()

    val mediaProjectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            // Check Overlay Permission
            if (!android.provider.Settings.canDrawOverlays(context)) {
                // Launch Settings to grant permission
                val intent = android.content.Intent(
                    android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    android.net.Uri.parse("package:${context.packageName}")
                )
                context.startActivity(intent)
                // We should technically wait for return, but for MVP we assume user grants
                // Or we can just start without overlay if they refuse
            }
            viewModel.startMirroring(result.resultCode, result.data!!)
        }
    }

    LaunchedEffect(connectionState) {
        if (connectionState is ConnectionState.Connected) {
            // Once connected (socket ready), ask for permission to stream
            launcher.launch(mediaProjectionManager.createScreenCaptureIntent())
        }
    }

    val isNetworkAvailable by viewModel.isNetworkAvailable.collectAsState()
    
    // Check if there's an actual device connection
    val isDeviceConnected = connectionState is ConnectionState.Connected || 
                           connectionState is ConnectionState.Streaming
    
    // Show alert dialog when Receiver is clicked without connection
    var showConnectionAlert by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp) // Approx 16 + safe area
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                 // Home Button
                 Column(horizontalAlignment = Alignment.CenterHorizontally) {
                     Icon(Icons.Default.Home, contentDescription = "Home", tint = MaterialTheme.colorScheme.primary)
                     Text("Home", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                 }
                 
                 // Media / Receiver Button
                 Column(
                     horizontalAlignment = Alignment.CenterHorizontally,
                     modifier = Modifier
                         .then(
                             if (isDeviceConnected && isNetworkAvailable) {
                                 Modifier.clickable {
                                     val intent = android.content.Intent(context, com.mirrorcast.ui.receiver.ReceiverActivity::class.java)
                                     context.startActivity(intent)
                                 }
                             } else {
                                 Modifier.clickable {
                                     showConnectionAlert = true
                                 }
                             }
                         )
                 ) {
                     val alpha = if (isDeviceConnected && isNetworkAvailable) 1.0f else 0.4f
                     Icon(Icons.Default.Tv, contentDescription = "Receiver", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha))
                     val label = if (isDeviceConnected && isNetworkAvailable) "Receiver" else "Receiver\n(Offline)"
                     Text(
                         text = label, 
                         style = MaterialTheme.typography.labelSmall, 
                         color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
                         textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                         lineHeight = 10.sp
                     )
                 }
                 
                 // Settings
                 Column(
                     horizontalAlignment = Alignment.CenterHorizontally,
                     modifier = Modifier.clickable { onSettingsClick() }
                 ) {
                     Icon(Icons.Default.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                     Text("Settings", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                 }
            }
        }
    ) { padding ->
        // Connection Alert Dialog
        if (showConnectionAlert) {
            AlertDialog(
                onDismissRequest = { showConnectionAlert = false },
                title = {
                    Text(
                        text = "Bağlantı Gerekli",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = "Receiver'ı kullanmak için önce bir cihaza bağlanmanız gerekiyor. Bağlantı sayfasına yönlendirileceksiniz.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showConnectionAlert = false
                            viewModel.startDiscovery()
                        }
                    ) {
                        Text("Bağlantı Yap")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showConnectionAlert = false }
                    ) {
                        Text("İptal")
                    }
                }
            )
        }
        
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CastLink",
                    style = MaterialTheme.typography.headlineSmall, // ~xl
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    IconButton(onClick = { /* Help */ }) {
                        Icon(Icons.Default.HelpOutline, contentDescription = "Help")
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Status Indicator (Simplified)
            Text(
                text = "Ready to Connect",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelMedium
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Hero Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Start Mirroring Cast",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tap below to cast your screen to a nearby TV instantly.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                
                Spacer(modifier = Modifier.height(48.dp))
                
                BigConnectButton(
                    onClick = { viewModel.startDiscovery() } // Initially starts discovery
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Nearby Devices
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Nearby Devices",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                TextButton(onClick = { viewModel.startDiscovery() }) {
                    Text("Scan Again")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(nearbyDevices) { device ->
                    DeviceListItem(
                        device = device,
                        onClick = { viewModel.connectToDevice(device) }
                    )
                }
                
                // Show prompt if empty
                if (nearbyDevices.isEmpty()) {
                    item {
                        Text(
                            text = "Scanning...",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}
