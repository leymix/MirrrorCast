package com.mirrorcast.streaming.codec

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.util.Log
import android.view.Surface
import java.io.IOException

class H264Encoder(
    private val width: Int,
    private val height: Int,
    private val bitrate: Int = 5000000, // 5Mbps
    private val frameRate: Int = 30,
    private val iFrameInterval: Int = 2,
    private val onDataEncoded: (ByteArray) -> Unit
) {

    private var mediaCodec: MediaCodec? = null
    private var inputSurface: Surface? = null
    private var isRunning = false
    private val bufferInfo = MediaCodec.BufferInfo()

    val inputSurfaceProvider: Surface?
        get() = inputSurface

    init {
        prepare()
    }

    private fun prepare() {
        try {
            val format = MediaFormat.createVideoFormat(
                MediaFormat.MIMETYPE_VIDEO_AVC,
                width,
                height
            ).apply {
                setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
                setInteger(MediaFormat.KEY_BIT_RATE, bitrate)
                setInteger(MediaFormat.KEY_FRAME_RATE, frameRate)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, iFrameInterval)
            }

            mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
            mediaCodec?.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            
            inputSurface = mediaCodec?.createInputSurface()
            mediaCodec?.start()

            startDraining()
        } catch (e: IOException) {
            Log.e(TAG, "Failed to create codec", e)
            throw RuntimeException(e)
        }
    }

    private fun startDraining() {
        isRunning = true
        Thread {
            while (isRunning) {
                drainEncoder()
            }
        }.start()
    }

    private fun drainEncoder() {
        val codec = mediaCodec ?: return
        
        try {
            val outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, 10000)
            if (outputBufferIndex >= 0) {
                val encodedData = codec.getOutputBuffer(outputBufferIndex)
                
                if (encodedData != null) {
                    encodedData.position(bufferInfo.offset)
                    encodedData.limit(bufferInfo.offset + bufferInfo.size)
                    
                    val data = ByteArray(bufferInfo.size)
                    encodedData.get(data)
                    
                    // Callback
                    onDataEncoded(data)
                }

                codec.releaseOutputBuffer(outputBufferIndex, false)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in encoder loop", e)
        }
    }

    fun stop() {
        isRunning = false
        try {
            mediaCodec?.stop()
            mediaCodec?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping codec", e)
        } finally {
            mediaCodec = null
            inputSurface = null
        }
    }

    companion object {
        private const val TAG = "H264Encoder"
    }
}
