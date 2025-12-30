package com.mirrorcast.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class StreamConfig(
    val width: Int = 1080,
    val height: Int = 1920,
    val dpi: Int = 420,
    val bitrate: Int = 6000000, // 6 Mbps
    val frameRate: Int = 30,
    val iFrameInterval: Int = 2 // 1 second
) : Parcelable
