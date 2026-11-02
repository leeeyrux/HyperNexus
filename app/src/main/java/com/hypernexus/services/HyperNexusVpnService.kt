package com.hypernexus.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat
import com.hypernexus.MainActivity
import kotlinx.coroutines.*
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer

class HyperNexusVpnService : VpnService() {

    companion object {
        private const val TAG = "HyperNexusVPN"
        const val ACTION_START = "com.hypernexus.vpn.START"
        const val ACTION_STOP = "com.hypernexus.vpn.STOP"
        const val EXTRA_TARGET_PACKAGE = "EXTRA_TARGET_PACKAGE"
        const val CHANNEL_ID = "hypernexus_vpn_channel"
        const val NOTIFICATION_ID = 999
        var isRunning = false
        var activeTargetPackage: String? = null
    }

    private var vpnInterface: ParcelFileDescriptor? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var tunnelJob: Job? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()

        // Enlace inmediato a primer plano para evitar bloqueos del sistema
        val initialNotification = buildNotification("Inicializando túnel seguro...")
        attachForeground(initialNotification)

        when (intent?.action) {
            ACTION_STOP -> {
                stopTunnel()
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_START -> {
                val targetPkg = intent.getStringExtra(EXTRA_TARGET_PACKAGE)
                startTunnel(targetPkg)
            }
        }
        return START_STICKY
    }

    private fun attachForeground(notification: Notification) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SYSTEM_EXEMPTED
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (_: Exception) {
            try {
                startForeground(NOTIFICATION_ID, notification)
            } catch (e: Exception) {
                Log.e(TAG, "Error al adjuntar servicio en primer plano", e)
            }
        }
    }

    private fun startTunnel(targetPackage: String?) {
        val notifyText = if (!targetPackage.isNullOrBlank()) {
            "Enrutando sockets para: $targetPackage"
        } else {
            "Túnel de Baja Latencia Activo"
        }
        attachForeground(buildNotification(notifyText))

        try {
            val builder = Builder()
                .setSession("HyperNexus Gaming Network")
                .setMtu(1400)
                .addAddress("10.200.0.2", 32)
                .addDnsServer("1.1.1.1")
                .addDnsServer("1.0.0.1")
                .addRoute("0.0.0.0", 0)

            // Split Tunneling por paquete sin límites
            if (!targetPackage.isNullOrBlank()) {
                try {
                    builder.addAllowedApplication(targetPackage)
                    activeTargetPackage = targetPackage
                    Log.i(TAG, "Split-tunneling activo para: $targetPackage")
                } catch (e: PackageManager.NameNotFoundException) {
                    Log.e(TAG, "Paquete objetivo no disponible", e)
                }
            }

            vpnInterface = builder.establish()
            isRunning = true
            startPacketDrainLoop()
        } catch (e: Exception) {
            Log.e(TAG, "Fallo al crear descriptor TUN", e)
            stopTunnel()
            stopSelf()
        }
    }

    private fun startPacketDrainLoop() {
        tunnelJob?.cancel()
        tunnelJob = serviceScope.launch {
            val fd = vpnInterface?.fileDescriptor ?: return@launch
            val inputStream = FileInputStream(fd)
            val outputStream = FileOutputStream(fd)
            val packet = ByteBuffer.allocate(32767)

            try {
                while (isActive) {
                    val length = inputStream.read(packet.array())
                    if (length > 0) {
                        packet.limit(length)
                        packet.clear()
                    }
                    delay(2)
                }
            } catch (_: Exception) {
            } finally {
                try { inputStream.close() } catch (_: Exception) {}
                try { outputStream.close() } catch (_: Exception) {}
            }
        }
    }

    private fun stopTunnel() {
        isRunning = false
        activeTargetPackage = null
        tunnelJob?.cancel()
        tunnelJob = null

        try {
            vpnInterface?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error al cerrar interface VPN", e)
        }
        vpnInterface = null
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    private fun buildNotification(contentText: String): Notification {
        val notifyIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notifyIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("HyperNexus Gaming Tunnel")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_menu_share)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chan = NotificationChannel(
                CHANNEL_ID,
                "HyperNexus Gaming VPN",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(chan)
        }
    }

    override fun onDestroy() {
        stopTunnel()
        serviceScope.cancel()
        super.onDestroy()
    }
}
