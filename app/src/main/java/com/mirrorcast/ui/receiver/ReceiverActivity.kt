package com.mirrorcast.ui.receiver

import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.mirrorcast.streaming.receiver.H264Decoder
import com.mirrorcast.streaming.receiver.TcpReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.mirrorcast.data.discovery.NsdAdvertisingService

class ReceiverActivity : ComponentActivity(), SurfaceHolder.Callback {

    private lateinit var surfaceView: SurfaceView
    private var decoder: H264Decoder? = null
    private var tcpReceiver: TcpReceiver? = null
    private var advertisingService: NsdAdvertisingService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Critical: Check network before loading any UI - prevent black screen
        if (!isNetworkAvailable()) {
            android.widget.Toast.makeText(
                this, 
                "Bağlantı yapınız: Receiver kullanmak için WiFi/Ethernet bağlantısı gereklidir", 
                android.widget.Toast.LENGTH_LONG
            ).show()
            // Finish immediately before any UI is rendered
            finish()
            return
        }

        // Only proceed if network is available
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        surfaceView = SurfaceView(this)
        setContentView(surfaceView)
        surfaceView.holder.addCallback(this)

        advertisingService = NsdAdvertisingService(this)
        advertisingService?.registerService("MirrorCast Receiver")
    }
    
    // Improved check using modern API
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
        val network = connectivityManager?.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI) || 
               capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_ETHERNET)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        startReceiving(holder.surface)
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        stopReceiving()
    }

    private fun startReceiving(surface: android.view.Surface) {
        decoder = H264Decoder(surface)
        
        tcpReceiver = TcpReceiver { data, length ->
            decoder?.decode(data, length)
        }

        lifecycleScope.launch(Dispatchers.IO) {
            tcpReceiver?.start()
        }
    }

    private fun stopReceiving() {
        tcpReceiver?.stop()
        decoder?.stop()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        advertisingService?.unregisterService()
    }
}
