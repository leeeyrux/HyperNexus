package com.hypernexus.services

import android.annotation.SuppressLint
import android.app.*
import android.content.*
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.TrafficStats
import android.os.*
import android.provider.Settings
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import java.text.SimpleDateFormat
import java.util.*

class RedMagicOverlayService : Service(), Choreographer.FrameCallback {

    private lateinit var windowManager: WindowManager
    private lateinit var rootContainer: LinearLayout
    private lateinit var windowParams: WindowManager.LayoutParams

    private lateinit var fpsText: TextView
    private lateinit var timeText: TextView
    private lateinit var netText: TextView
    private lateinit var batteryText: TextView
    private lateinit var tempText: TextView
    private lateinit var separator1: TextView
    private lateinit var separator2: TextView
    private lateinit var separator3: TextView
    private lateinit var separator4: TextView

    private var isExpanded = false
    private var frameCount = 0
    private var lastFrameTimestamp = 0L
    private var lastRxBytes = 0L
    private var lastNetCheckTime = 0L

    private val handler = Handler(Looper.getMainLooper())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    private val metricsRunnable = object : Runnable {
        override fun run() {
            updateSystemMetrics()
            handler.postDelayed(this, 1000)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForeground(777, createNotification())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            stopSelf()
            return
        }
        initOverlayWindow()
        Choreographer.getInstance().postFrameCallback(this)
        handler.post(metricsRunnable)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initOverlayWindow() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        windowParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 120
        }

        rootContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dpToPx(14), dpToPx(8), dpToPx(14), dpToPx(8))
            background = createHudBackground()
        }

        buildHudViews()
        applyModeLayout(isExpanded = false)
        attachDragAndClickListener()

        windowManager.addView(rootContainer, windowParams)
        lastRxBytes = TrafficStats.getTotalRxBytes()
        lastNetCheckTime = SystemClock.elapsedRealtime()
    }

    private fun createHudBackground(): GradientDrawable {
        return GradientDrawable().apply {
            setColor(Color.parseColor("#B3141418"))
            cornerRadius = dpToPx(24).toFloat()
            setStroke(dpToPx(1), Color.parseColor("#33FFFFFF"))
        }
    }

    private fun buildHudViews() {
        val boldMono = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)

        timeText = createStyledText("00:00", boldMono)
        fpsText = createStyledText("-- FPS", boldMono)
        netText = createStyledText("0 KB/s", boldMono)
        batteryText = createStyledText("-%", boldMono)
        tempText = createStyledText("--°C", boldMono)

        separator1 = createSeparator()
        separator2 = createSeparator()
        separator3 = createSeparator()
        separator4 = createSeparator()
    }

    private fun createStyledText(text: String, typeface: Typeface): TextView {
        return TextView(this).apply {
            this.text = text
            this.typeface = typeface
            this.textSize = 12f
            setTextColor(Color.WHITE)
        }
    }

    private fun createSeparator(): TextView {
        return TextView(this).apply {
            text = " | "
            textSize = 11f
            setTextColor(Color.parseColor("#4DFFFFFF"))
        }
    }

    private fun applyModeLayout(isExpanded: Boolean) {
        rootContainer.removeAllViews()
        if (isExpanded) {
            rootContainer.addView(timeText)
            rootContainer.addView(separator1)
            rootContainer.addView(fpsText)
            rootContainer.addView(separator2)
            rootContainer.addView(netText)
            rootContainer.addView(separator3)
            rootContainer.addView(batteryText)
            rootContainer.addView(separator4)
            rootContainer.addView(tempText)
        } else {
            rootContainer.addView(fpsText)
        }
        windowManager.updateViewLayout(rootContainer, windowParams)
    }

    private fun attachDragAndClickListener() {
        rootContainer.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var touchStartX = 0f
            private var touchStartY = 0f
            private var isClick = true

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = windowParams.x
                        initialY = windowParams.y
                        touchStartX = event.rawX
                        touchStartY = event.rawY
                        isClick = true
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = (event.rawX - touchStartX).toInt()
                        val dy = (event.rawY - touchStartY).toInt()
                        if (kotlin.math.abs(dx) > 10 || kotlin.math.abs(dy) > 10) {
                            isClick = false
                        }
                        windowParams.x = initialX + dx
                        windowParams.y = initialY + dy
                        windowManager.updateViewLayout(rootContainer, windowParams)
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        if (isClick) {
                            isExpanded = !isExpanded
                            applyModeLayout(isExpanded)
                        }
                        return true
                    }
                }
                return false
            }
        })
    }

    override fun doFrame(frameTimeNanos: Long) {
        frameCount++
        if (lastFrameTimestamp == 0L) lastFrameTimestamp = frameTimeNanos

        val diffNanos = frameTimeNanos - lastFrameTimestamp
        if (diffNanos >= 1_000_000_000L) {
            val fps = ((frameCount * 1_000_000_000.0) / diffNanos).toInt()
            fpsText.text = "$fps FPS"

            when {
                fps >= 90 -> fpsText.setTextColor(Color.parseColor("#00FF66"))
                fps >= 55 -> fpsText.setTextColor(Color.WHITE)
                fps >= 30 -> fpsText.setTextColor(Color.parseColor("#FFA500"))
                else -> fpsText.setTextColor(Color.parseColor("#FF3B30"))
            }

            frameCount = 0
            lastFrameTimestamp = frameTimeNanos
        }
        Choreographer.getInstance().postFrameCallback(this)
    }

    private fun updateSystemMetrics() {
        if (!isExpanded) return

        timeText.text = timeFormat.format(Date())

        val currentRx = TrafficStats.getTotalRxBytes()
        val currentTime = SystemClock.elapsedRealtime()
        val timeDeltaSec = (currentTime - lastNetCheckTime) / 1000.0

        if (timeDeltaSec > 0 && currentRx >= lastRxBytes) {
            val speedBytes = ((currentRx - lastRxBytes) / timeDeltaSec).toLong()
            netText.text = if (speedBytes > 1024 * 1024) {
                String.format(Locale.US, "%.1f MB/s", speedBytes / (1024f * 1024f))
            } else {
                "${speedBytes / 1024} KB/s"
            }
        }
        lastRxBytes = currentRx
        lastNetCheckTime = currentTime

        val batteryIntent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        batteryIntent?.let {
            val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            if (level != -1 && scale != -1) {
                val pct = (level * 100) / scale
                batteryText.text = "$pct%"
            }
            val rawTemp = it.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
            tempText.text = "${rawTemp / 10.0}°C"
        }
    }

    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()

    private fun createNotification(): Notification {
        val channelId = "hypernexus_hud_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chan = NotificationChannel(channelId, "Gaming HUD Overlay", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(chan)
        }
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("HyperNexus Red Magic HUD")
            .setContentText("Performance Overlay is active")
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        Choreographer.getInstance().removeFrameCallback(this)
        handler.removeCallbacks(metricsRunnable)
        if (::rootContainer.isInitialized) {
            windowManager.removeView(rootContainer)
        }
    }
}
