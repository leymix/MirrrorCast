package com.mirrorcast.streaming.sender


import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import com.mirrorcast.MainActivity
import com.mirrorcast.R
import com.mirrorcast.streaming.codec.H264Encoder
import com.mirrorcast.ui.overlay.FloatingControlBar
import com.mirrorcast.ui.theme.MirrorCastTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ScreenCaptureService : Service() {

    private var mediaProjectionManager: MediaProjectionManager? = null
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var encoder: H264Encoder? = null
    private var tcpSender: TcpSender? = null

    override fun onCreate() {
        super.onCreate()
        mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, 0)
                val data = intent.getParcelableExtra<Intent>(EXTRA_DATA)
                val ip = intent.getStringExtra(EXTRA_IP)
                val port = intent.getIntExtra(EXTRA_PORT, 8988)
                val config = intent.getParcelableExtra<com.mirrorcast.domain.model.StreamConfig>(EXTRA_CONFIG)
                
                if (resultCode != 0 && data != null && ip != null && config != null) {
                    startForegroundService()
                    startProjection(resultCode, data, ip, port, config)
                }
            }
            ACTION_STOP -> stopSelf()
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        stopProjection()
        removeFloatingControl()
        super.onDestroy()
    }

    private fun startProjection(resultCode: Int, data: Intent, ip: String, port: Int, config: com.mirrorcast.domain.model.StreamConfig) {
        mediaProjection = mediaProjectionManager?.getMediaProjection(resultCode, data)
        
        // 1. Setup Encoder
        try {
            // Initializing Encoder 
            encoder = H264Encoder(
                width = config.width,
                height = config.height,
                bitrate = config.bitrate,
                frameRate = config.frameRate
            ) { encodedData ->
                 // On Data Encoded -> Send to Network
                 tcpSender?.send(encodedData)
            }
            
            val surface = encoder?.inputSurfaceProvider
            if (surface != null) {
                virtualDisplay = mediaProjection?.createVirtualDisplay(
                    "MirrorCast",
                    config.width, config.height, config.dpi,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    surface, null, null
                )
            } else {
                Log.e(TAG, "Failed to create input surface")
                stopSelf()
                return
            }

            // 2. Start Network
            tcpSender = TcpSender()
            CoroutineScope(Dispatchers.IO).launch {
                tcpSender?.connect(ip, port)
            }
            
            // 3. Show Floating Control
            showFloatingControl()

        } catch (e: Exception) {
            Log.e(TAG, "Error starting projection", e)
            stopSelf()
        }
    }

    private fun stopProjection() {
        try {
            tcpSender?.disconnect()
            encoder?.stop()
            virtualDisplay?.release()
            mediaProjection?.stop()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping projection", e)
        }
    }
    
    // Window Manager Overlay
    private var windowManager: WindowManager? = null
    private var floatingView: ComposeView? = null
    
    private fun showFloatingControl() {
        if (!android.provider.Settings.canDrawOverlays(this)) return
        
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        params.y = 100

        floatingView = ComposeView(this).apply {
            setContent {
                MirrorCastTheme {
                   FloatingControlBar(
                       onStop = { 
                           stopSelf() // Stop service on click
                       },
                       onPause = { /* Pause logic */ },
                       onDrag = { x, y -> 
                           params.x += x.toInt()
                           params.y += y.toInt()
                           windowManager?.updateViewLayout(this, params)
                       }
                   )
                }
            }
        }
        
        try {
            windowManager?.addView(floatingView, params)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add overlay", e)
        }
    }
    
    private fun removeFloatingControl() {
        if (floatingView != null) {
            try {
                windowManager?.removeView(floatingView)
            } catch (e: Exception) {}
            floatingView = null
        }
    }

    private fun startForegroundService() {
        val notification = createNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID, 
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Screen Mirroring Active")
            .setContentText("Your screen is being shared")
            .setSmallIcon(R.mipmap.ic_launcher) // Ensure this exists
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "MirrorCast Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    override fun onBind(intent: Intent): IBinder? = null

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val EXTRA_RESULT_CODE = "EXTRA_RESULT_CODE"
        const val EXTRA_DATA = "EXTRA_DATA"
        const val EXTRA_IP = "EXTRA_IP"
        const val EXTRA_PORT = "EXTRA_PORT"
        const val EXTRA_CONFIG = "EXTRA_CONFIG"
        
        const val CHANNEL_ID = "MirrorCastChannel"
        const val NOTIFICATION_ID = 1
        
        private const val TAG = "ScreenCaptureService"
    }
}
