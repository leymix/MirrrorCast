package com.mirrorcast.streaming.sender

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket

class TcpSender {
    private var socket: Socket? = null
    private var outputStream: OutputStream? = null

    suspend fun connect(ip: String, port: Int) = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Connecting to $ip:$port")
            socket = Socket()
            socket?.connect(InetSocketAddress(ip, port), 5000)
            socket?.tcpNoDelay = true // Vital for low latency
            outputStream = socket?.getOutputStream()
            Log.d(TAG, "Connected to $ip:$port")
        } catch (e: Exception) {
            Log.e(TAG, "Connection failed", e)
            throw e
        }
    }

    fun send(data: ByteArray) {
        try {
            // Length-Prefixed Framing for Robustness
            val length = data.size
            
            // 4-byte header (Big Endian)
            val header = ByteArray(4)
            header[0] = (length shr 24).toByte()
            header[1] = (length shr 16).toByte()
            header[2] = (length shr 8).toByte()
            header[3] = length.toByte()
            
            synchronized(this) {
                outputStream?.write(header)
                outputStream?.write(data)
                outputStream?.flush() // Ensure immediate send for low latency
            }
        } catch (e: Exception) {
            Log.e(TAG, "Send failed", e)
        }
    }

    fun disconnect() {
        try {
            outputStream?.close()
            socket?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Disconnect error", e)
        } finally {
            outputStream = null
            socket = null
        }
    }

    companion object {
        private const val TAG = "TcpSender"
    }
}
