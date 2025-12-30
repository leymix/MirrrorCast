package com.mirrorcast.streaming.receiver

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.ServerSocket
import java.net.Socket

class TcpReceiver(
    private val port: Int = 8988,
    private val onDataReceived: (ByteArray, Int) -> Unit
) {
    private var serverSocket: ServerSocket? = null
    private var clientSocket: Socket? = null
    private var isRunning = false

    suspend fun start() = withContext(Dispatchers.IO) {
        try {
            serverSocket = ServerSocket(port)
            isRunning = true
            Log.d(TAG, "Server started on port $port")
            
            while (isRunning && isActive) {
                Log.d(TAG, "Waiting for connection...")
                val socket = serverSocket?.accept()
                if (socket != null) {
                    Log.d(TAG, "Client connected: ${socket.inetAddress}")
                    clientSocket = socket
                    handleClient(socket)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Server error", e)
        }
    }

    private fun handleClient(socket: Socket) {
        try {
            val inputStream = socket.getInputStream()
            val headerBuffer = ByteArray(4)
            
            while (isRunning && !socket.isClosed) {
                // 1. Read Length (4 bytes)
                if (!readFully(inputStream, headerBuffer, 4)) break
                
                val length = (headerBuffer[0].toInt() and 0xFF shl 24) or
                             (headerBuffer[1].toInt() and 0xFF shl 16) or
                             (headerBuffer[2].toInt() and 0xFF shl 8) or
                             (headerBuffer[3].toInt() and 0xFF)
                             
                if (length <= 0 || length > 10 * 1024 * 1024) { // Sanity check (max 10MB frame)
                    Log.e(TAG, "Invalid frame length: $length")
                    break
                }

                // 2. Read Data (Length bytes)
                val dataBuffer = ByteArray(length)
                if (!readFully(inputStream, dataBuffer, length)) break
                
                // 3. Emit
                onDataReceived(dataBuffer, length)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Client read error", e)
        } finally {
            try { socket.close() } catch (e: Exception) {}
            clientSocket = null
        }
    }
    
    // Helper to ensure we get exactly 'size' bytes
    private fun readFully(inputStream: InputStream, buffer: ByteArray, size: Int): Boolean {
        var totalRead = 0
        while (totalRead < size) {
            val read = inputStream.read(buffer, totalRead, size - totalRead)
            if (read == -1) return false
            totalRead += read
        }
        return true
    }

    fun stop() {
        isRunning = false
        try {
            serverSocket?.close()
            clientSocket?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Stop error", e)
        }
    }

    companion object {
        private const val TAG = "TcpReceiver"
    }
}
