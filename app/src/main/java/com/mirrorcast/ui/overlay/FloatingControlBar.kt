package com.mirrorcast.ui.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FloatingControlBar(
    onStop: () -> Unit,
    onPause: () -> Unit,
    onDrag: (Float, Float) -> Unit
) {
    Box(
        modifier = Modifier
            .width(360.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF101622).copy(alpha = 0.9f)) // Glass-like dark
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Drag Handle
            Icon(
                Icons.Default.DragIndicator,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            onDrag(dragAmount.x, dragAmount.y)
                        }
                    }
                    .padding(8.dp)
            )

            // Divider
            Divider()

            // Pause
            IconButton(onClick = onPause) {
                Icon(Icons.Default.Pause, contentDescription = "Pause", tint = Color.White)
            }

            // Rotate
            IconButton(onClick = { /* Rotate logic */ }) {
                Icon(Icons.Default.ScreenRotation, contentDescription = "Rotate", tint = Color.White)
            }

            // Divider
            Divider()

            // Stop
            IconButton(
                onClick = onStop,
                modifier = Modifier
                    .background(Color(0xFF135BEC).copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            ) {
                Icon(Icons.Default.StopCircle, contentDescription = "Stop", tint = Color(0xFF135BEC)) // Primary Blue
            }

            // Collapse
            IconButton(onClick = { /* Collapse */ }) {
                Icon(Icons.Default.ExpandMore, contentDescription = "Collapse", tint = Color.Gray)
            }
        }
    }
}

@Composable
fun Divider() {
    Box(
        modifier = Modifier
            .height(24.dp)
            .width(1.dp)
            .background(Color.White.copy(alpha = 0.1f))
    )
}
