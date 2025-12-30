package com.mirrorcast.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.ScreenshotMonitor
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import com.mirrorcast.ui.home.SettingsViewModel

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: com.mirrorcast.ui.home.SettingsViewModel
) {
    val config by viewModel.config.collectAsState()

    Scaffold(
        topBar = {
            Row( // Changed from TopAppBar to Row to maintain original structure and avoid compilation issues with parameters
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back")
                }
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.size(48.dp)) // Balance
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Display Section
            SectionHeader("Display")
            SettingsCard {
                SettingsItem(
                    icon = Icons.Default.ScreenshotMonitor,
                    title = "Resolution",
                    value = "${config.height}p", // Vertical
                    onClick = {
                         // Cycle resolution for MVP
                         if (config.height == 1920) {
                             viewModel.updateResolution(720, 1280) // 720p
                         } else {
                             viewModel.updateResolution(1080, 1920) // 1080p
                         }
                    }
                )
                // Orientation is driven by device rotation usually, but we can force encoder orientation
                OrientationSelector()
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Stream Quality Section
            SectionHeader("Stream Quality")
            SettingsCard {
                SettingsItem(
                    icon = Icons.Default.Speed,
                    title = "Bitrate",
                    value = "${config.bitrate / 1000000} Mbps",
                    onClick = {
                        // Cycle bitrate
                        if (config.bitrate == 6000000) {
                             viewModel.updateBitrate(3000000) // 3 Mbps
                        } else {
                             viewModel.updateBitrate(6000000) // 6 Mbps
                        }
                    }
                )
                LatencySelector()
                InfoText("Lower bitrate improves latency but reduces quality.")
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Audio Section
            SectionHeader("Audio")
            SettingsCard {
                AudioToggle()
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
             // Reset
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable { }
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Reset to Defaults", fontWeight = FontWeight.Bold)
            }
             
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "App Version 1.0.4",
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun SectionHeader(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = Color.Gray,
        modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
    )
}

@Composable
fun SettingsCard(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(bottom = 8.dp)
    ) {
        content()
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    value: String? = null,
    showChevron: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
        
        if (value != null) {
            Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
        }
        
        if (showChevron) {
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
        }
    }
}

@Composable
fun OrientationSelector() {
    var selected by remember { mutableStateOf("landscape") }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.background)
            .padding(4.dp)
    ) {
        SelectorOption(
            text = "Portrait",
            icon = Icons.Default.Smartphone,
            selected = selected == "portrait",
            modifier = Modifier.weight(1f)
        ) { selected = "portrait" }
        
        SelectorOption(
            text = "Landscape",
            icon = Icons.Default.ScreenRotation,
            selected = selected == "landscape",
            modifier = Modifier.weight(1f)
        ) { selected = "landscape" }
    }
}

@Composable
fun LatencySelector() {
    var selected by remember { mutableStateOf("standard") }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.background)
            .padding(4.dp)
    ) {
        SelectorOption(
            text = "Standard",
            icon = null,
            selected = selected == "standard",
            modifier = Modifier.weight(1f)
        ) { selected = "standard" }
        
        SelectorOption(
            text = "Low (Gaming)",
            icon = null,
            selected = selected == "low",
            modifier = Modifier.weight(1f)
        ) { selected = "low" }
    }
}

@Composable
fun SelectorOption(
    text: String,
    icon: ImageVector?,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) MaterialTheme.colorScheme.surface else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    icon, 
                    contentDescription = null, 
                    modifier = Modifier.size(16.dp),
                    tint = if (selected) MaterialTheme.colorScheme.primary else Color.Gray
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text, 
                fontWeight = FontWeight.Medium,
                color = if (selected) MaterialTheme.colorScheme.primary else Color.Gray
            )
        }
    }
}

@Composable
fun AudioToggle() {
    var checked by remember { mutableStateOf(true) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.VolumeUp, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("Cast Audio", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text("Stream sound to receiver", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        Switch(
            checked = checked,
            onCheckedChange = { checked = it },
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            )
        )
    }
}

@Composable
fun InfoText(text: String) {
    Row(modifier = Modifier.padding(16.dp)) {
        Icon(Icons.Default.Info, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
    }
}
