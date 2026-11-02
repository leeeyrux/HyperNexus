package com.hypernexus.services

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.hypernexus.PrivilegedActions
import com.hypernexus.ShizukuBridge
import kotlinx.coroutines.*

class RefreshRateKeeperService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var privilegedActions: PrivilegedActions
    private var targetHz: Int = 0
    private var isEnforcing = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        privilegedActions = PrivilegedActions(ShizukuBridge(applicationContext))
        startForeground(1002, createNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        targetHz = intent?.getIntExtra("TARGET_HZ", 0) ?: 0
        if (targetHz > 0) {
            startWatchdog()
        } else {
            stopSelf()
        }
        return START_STICKY
    }

    private fun startWatchdog() {
        if (isEnforcing) return
        isEnforcing = true
        serviceScope.launch {
            while (isActive && isEnforcing) {
                privilegedActions.forceRefreshRate(targetHz)
                delay(15_000)
            }
        }
    }

    private fun createNotification(): Notification {
        val channelId = "hypernexus_refresh_keeper"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chan = NotificationChannel(channelId, "Refresh Rate Guardian", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(chan)
        }
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("HyperNexus Display Engine")
            .setContentText("Locking display frequency at target Hz")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        isEnforcing = false
        serviceScope.cancel()
        super.onDestroy()
    }
}
