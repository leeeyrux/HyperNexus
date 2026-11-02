package com.hypernexus.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Choreographer
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView

class FpsOverlayService : Service(), Choreographer.FrameCallback {

    private var windowManager: WindowManager? = null
    private var overlayView: LinearLayout? = null
    private var fpsTextView: TextView? = null

    private var frameCount = 0
    private var lastTime = 0L

    companion object {
        const val CHANNEL_ID = "HyperNexus_FPS_Channel"
        const val NOTIFICATION_ID = 1337
        var isRunning = false
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
        initOverlay()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "HyperNexus FPS Overlay",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            Notification.Builder(this)
        }
        return builder
            .setContentTitle("HyperNexus Game Engine")
            .setContentText("Overlay de FPS activo estilo RedMagic")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .build()
    }

    private fun initOverlay() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            stopSelf()
            return
        }

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 80
            y = 150
        }

        // Píldora Cyberpunk / RedMagic
        overlayView = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(32, 16, 32, 16)
            
            val shape = GradientDrawable().apply {
                setColor(Color.parseColor("#E60F0F12")) // Negro humo traslúcido
                cornerRadius = 48f
                setStroke(3, Color.parseColor("#FF003C")) // Borde Rojo Neón RedMagic
            }
            background = shape
        }

        val badge = TextView(this).apply {
            text = "RED● "
            setTextColor(Color.parseColor("#FF003C"))
            textSize = 11f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        fpsTextView = TextView(this).apply {
            text = "-- FPS"
            setTextColor(Color.parseColor("#00FF66")) // Verde cibernético
            textSize = 14f
            typeface = android.graphics.Typeface.MONOSPACE
        }

        overlayView?.addView(badge)
        overlayView?.addView(fpsTextView)

        // Arrastrar la píldora por toda la pantalla con suavidad
        overlayView?.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f

            override fun onTouch(v: View?, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        params.x = initialX + (event.rawX - initialTouchX).toInt()
                        params.y = initialY + (event.rawY - initialTouchY).toInt()
                        windowManager?.updateViewLayout(overlayView, params)
                        return true
                    }
                }
                return false
            }
        })

        try {
            windowManager?.addView(overlayView, params)
            lastTime = System.nanoTime()
            Choreographer.getInstance().postFrameCallback(this)
        } catch (e: Exception) {
            stopSelf()
        }
    }

    override fun doFrame(frameTimeNanos: Long) {
        frameCount++
        val elapsed = frameTimeNanos - lastTime
        if (elapsed >= 500_000_000L) { // Actualiza cada 500ms
            val fps = ((frameCount * 1_000_000_000.0) / elapsed).toInt()
            fpsTextView?.text = "$fps FPS"
            
            // Si baja de 45 fps se pone amarillo/naranja, si sube va en verde puro
            if (fps < 45) {
                fpsTextView?.setTextColor(Color.parseColor("#FF9500"))
            } else {
                fpsTextView?.setTextColor(Color.parseColor("#00FF66"))
            }

            frameCount = 0
            lastTime = frameTimeNanos
        }
        Choreographer.getInstance().postFrameCallback(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        Choreographer.getInstance().removeFrameCallback(this)
        if (overlayView != null) {
            windowManager?.removeView(overlayView)
            overlayView = null
        }
    }
}
