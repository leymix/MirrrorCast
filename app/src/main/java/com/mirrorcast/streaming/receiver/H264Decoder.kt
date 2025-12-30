package com.mirrorcast.streaming.receiver

import android.media.MediaCodec
import android.media.MediaFormat
import android.util.Log
import android.view.Surface
import java.nio.ByteBuffer

class H264Decoder(
    private val surface: Surface,
    width: Int = 1920,
    height: Int = 1080
) {
    private var mediaCodec: MediaCodec? = null
    
    init {
        try {
            val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height)
            mediaCodec = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
            mediaCodec?.configure(format, surface, null, 0)
            mediaCodec?.start()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to init decoder", e)
        }
    }

    fun decode(data: ByteArray, length: Int) {
        val codec = mediaCodec ?: return
        try {
            val inputBufferIndex = codec.dequeueInputBuffer(10000)
            if (inputBufferIndex >= 0) {
                val inputBuffer = codec.getInputBuffer(inputBufferIndex)
                inputBuffer?.clear()
                inputBuffer?.put(data, 0, length)
                codec.queueInputBuffer(inputBufferIndex, 0, length, System.currentTimeMillis() * 1000, 0)
            }

            val bufferInfo = MediaCodec.BufferInfo()
            var outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, 10000)
            while (outputBufferIndex >= 0) {
                codec.releaseOutputBuffer(outputBufferIndex, true) // Render to surface
                outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, 0)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Decode error", e)
        }
    }

    fun stop() {
        try {
            mediaCodec?.stop()
            mediaCodec?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Stop error", e)
        } finally {
            mediaCodec = null
        }
    }

    companion object {
        private const val TAG = "H264Decoder"
    }
}
