package com.hypernexus.services

import android.annotation.SuppressLint
import android.app.*
import android.content.*
import android.content.pm.ServiceInfo
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.*
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import java.io.File
import java.io.RandomAccessFile
import java.net.InetSocketAddress
import java.net.Socket
import java.text.SimpleDateFormat
import java.util.*

class RedMagicOverlayService : Service(), Choreographer.FrameCallback {

    companion object {
        private const val TAG = "RedMagicOverlay"
        const val CHANNEL_ID = "hypernexus_hud_channel"
        const val NOTIFICATION_ID = 777
        var isRunning = false
    }

    private var windowManager: WindowManager? = null
    private var rootContainer: LinearLayout? = null
    private var windowParams: WindowManager.LayoutParams? = null

    private lateinit var fpsText: TextView
    private lateinit var timeText: TextView
    private lateinit var pingText: TextView
    private lateinit var tempText: TextView
    private lateinit var batteryText: TextView

    private lateinit var sep1: TextView
    private lateinit var sep2: TextView
    private lateinit var sep3: TextView
    private lateinit var sep4: TextView

    private var isExpanded = false
    private var frameCount = 0
    private var lastFrameTimestamp = 0L

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    private var currentPingMs = -1
    private var currentTempCelsius = 0f

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        startSafeForeground()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Log.e(TAG, "No overlay permission granted.")
            stopSelf()
            return
        }

        try {
            initOverlayWindow()
            Choreographer.getInstance().postFrameCallback(this)
            startMetricsCollectors()
        } catch (e: Exception) {
            Log.e(TAG, "Fatal error initializing overlay window", e)
            stopSelf()
        }
    }

    private fun startSafeForeground() {
        createNotificationChannel()
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("HyperNexus Red Magic HUD")
            .setContentText("Overlay de métricas activo")
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .setOngoing(true)
            .build()

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Standard startForeground fallback", e)
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chan = NotificationChannel(CHANNEL_ID, "Gaming HUD Overlay", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(chan)
        }
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
            x = 80
            y = 120
        }

        rootContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dpToPx(12), dpToPx(6), dpToPx(12), dpToPx(6))
            background = createRedMagicBackground()
        }

        buildHudViews()
        applyModeLayout(false)
        attachTouchHandler()

        windowManager?.addView(rootContainer, windowParams)
    }

    private fun createRedMagicBackground(): GradientDrawable {
        return GradientDrawable().apply {
            // #D9262626: Contenedor gris oscuro Red Magic (#262626 al 85% de alpha)
            setColor(Color.parseColor("#D9262626"))
            cornerRadius = dpToPx(18).toFloat()
            setStroke(dpToPx(1), Color.parseColor("#4DFFFFFF"))
        }
    }

    private fun buildHudViews() {
        val futuristicFont = Typeface.create("sans-serif-medium", Typeface.NORMAL)

        fpsText = createStyledText("-- FPS", futuristicFont)
        timeText = createStyledText("00:00", futuristicFont)
        pingText = createStyledText("-- ms", futuristicFont)
        tempText = createStyledText("--°C", futuristicFont)
        batteryText = createStyledText("--%", futuristicFont)

        sep1 = createSeparator()
        sep2 = createSeparator()
        sep3 = createSeparator()
        sep4 = createSeparator()
    }

    private fun createStyledText(text: String, typeface: Typeface): TextView {
        return TextView(this).apply {
            this.text = text
            this.typeface = typeface
            this.textSize = 12f
            setTextColor(Color.WHITE)
            letterSpacing = 0.05f
        }
    }

    private fun createSeparator(): TextView {
        return TextView(this).apply {
            text = " | "
            textSize = 11f
            setTextColor(Color.parseColor("#4DFFFFFF"))
        }
    }

    private fun applyModeLayout(expanded: Boolean) {
        val container = rootContainer ?: return
        container.removeAllViews()

        if (expanded) {
            // Modo Expandido: [ Hora | FPS | Ping | Temp | Batería ]
            container.addView(timeText)
            container.addView(sep1)
            container.addView(fpsText)
            container.addView(sep2)
            container.addView(pingText)
            container.addView(sep3)
            container.addView(tempText)
            container.addView(sep4)
            container.addView(batteryText)
        } else {
            // Modo Individual: Solo FPS
            container.addView(fpsText)
        }

        try {
            if (container.isAttachedToWindow) {
                windowManager?.updateViewLayout(container, windowParams)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating HUD layout mode", e)
        }
    }

    private fun attachTouchHandler() {
        val container = rootContainer ?: return
        container.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var touchStartX = 0f
            private var touchStartY = 0f
            private var isClick = true

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                val params = windowParams ?: return false
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        touchStartX = event.rawX
                        touchStartY = event.rawY
                        isClick = true
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = (event.rawX - touchStartX).toInt()
                        val dy = (event.rawY - touchStartY).toInt()
                        if (kotlin.math.abs(dx) > 8 || kotlin.math.abs(dy) > 8) {
                            isClick = false
                        }
                        params.x = initialX + dx
                        params.y = initialY + dy
                        try {
                            if (container.isAttachedToWindow) {
                                windowManager?.updateViewLayout(container, params)
                            }
                        } catch (_: Exception) {}
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

        val diff = frameTimeNanos - lastFrameTimestamp
        if (diff >= 1_000_000_000L) {
            val fps = ((frameCount * 1_000_000_000.0) / diff).toInt()
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

    private fun startMetricsCollectors() {
        serviceScope.launch {
            while (isActive) {
                if (isExpanded) {
                    timeText.text = timeFormat.format(Date())

                    val batteryIntent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
                    batteryIntent?.let {
                        val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                        val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                        if (level != -1 && scale != -1) {
                            val pct = (level * 100) / scale
                            batteryText.text = "$pct%"
                        }
                    }

                    if (currentTempCelsius > 0) {
                        tempText.text = "${"%.0f".format(currentTempCelsius)}°C"
                    }
                }
                delay(1000)
            }
        }

        serviceScope.launch(Dispatchers.IO) {
            while (isActive) {
                if (isExpanded) {
                    currentPingMs = measurePing()
                    currentTempCelsius = readThermalTemp()

                    withContext(Dispatchers.Main) {
                        pingText.text = if (currentPingMs in 0..999) "${currentPingMs}ms" else "--ms"
                        pingText.setTextColor(
                            when {
                                currentPingMs in 1..65 -> Color.parseColor("#00FF66")
                                currentPingMs in 66..130 -> Color.parseColor("#FFA500")
                                else -> Color.WHITE
                            }
                        )
                    }
                }
                delay(2500)
            }
        }
    }

    private fun measurePing(): Int {
        return try {
            val start = System.currentTimeMillis()
            val socket = Socket()
            socket.connect(InetSocketAddress("1.1.1.1", 53), 1200)
            val latency = (System.currentTimeMillis() - start).toInt()
            socket.close()
            latency
        } catch (_: Exception) {
            -1
        }
    }

    private fun readThermalTemp(): Float {
        val thermalPaths = listOf(
            "/sys/class/thermal/thermal_zone0/temp",
            "/sys/class/thermal/thermal_zone1/temp",
            "/sys/class/power_supply/battery/temp"
        )
        for (path in thermalPaths) {
            try {
                val f = File(path)
                if (f.exists()) {
                    val reader = RandomAccessFile(f, "r")
                    val raw = reader.readLine()?.trim()?.toIntOrNull() ?: 0
                    reader.close()
                    if (raw > 0) return if (raw > 1000) raw / 1000f else raw.toFloat()
                }
            } catch (_: Exception) {}
        }
        return 32f
    }

    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        Choreographer.getInstance().removeFrameCallback(this)
        serviceScope.cancel()

        try {
            rootContainer?.let {
                if (it.isAttachedToWindow) {
                    windowManager?.removeView(it)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error detaching HUD in onDestroy", e)
        }
        rootContainer = null
    }
}
