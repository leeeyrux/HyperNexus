package com.hypernexus.data

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Resources
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.RandomAccessFile
import java.net.InetSocketAddress
import java.net.Socket

data class CoreFrequency(val coreIndex: Int, val currentMhz: Int, val maxMhz: Int)

data class LiveHardwareMetrics(
    val totalRamMb: Long,
    val usedRamMb: Long,
    val ramUsagePercent: Int,
    val cores: List<CoreFrequency>,
    val thermalTemp: Float
)

data class BatteryTelemetry(
    val health: String,
    val technology: String,
    val voltageMv: Int,
    val currentAmperageMa: Int,
    val status: String,
    val levelPercent: Int
)

data class NetworkTelemetry(
    val pingMs: Int,
    val connectionType: String,
    val signalStrengthDbm: String
)

data class StorageTelemetry(
    val totalGb: Float,
    val usedGb: Float,
    val usedPercentage: Int
)

data class DeviceStaticSpecs(
    val marketingDevice: String,
    val socModel: String,
    val resolution: String,
    val androidVersion: String
)

class HardwareMonitor(private val context: Context) {

    fun getStaticSpecs(): DeviceStaticSpecs {
        val metrics = Resources.getSystem().displayMetrics
        val soc = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Build.SOC_MODEL
        } else {
            Build.HARDWARE
        }

        return DeviceStaticSpecs(
            marketingDevice = "${Build.MANUFACTURER.uppercase()} ${Build.MODEL}",
            socModel = if (!soc.isNullOrBlank()) soc else "Qualcomm / MediaTek",
            resolution = "${metrics.heightPixels} x ${metrics.widthPixels}",
            androidVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
        )
    }

    suspend fun getLiveMetrics(): LiveHardwareMetrics = withContext(Dispatchers.IO) {
        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager.getMemoryInfo(memInfo)
        val totalMb = memInfo.totalMem / (1024 * 1024)
        val availMb = memInfo.availMem / (1024 * 1024)
        val usedMb = (totalMb - availMb).coerceAtLeast(0)
        val usagePct = if (totalMb > 0) ((usedMb.toDouble() / totalMb.toDouble()) * 100).toInt() else 0

        val coreList = mutableListOf<CoreFrequency>()
        val coreCount = Runtime.getRuntime().availableProcessors()
        for (i in 0 until coreCount) {
            val curFreq = readIntFromFile("/sys/devices/system/cpu/cpu$i/cpufreq/scaling_cur_freq") / 1000
            val maxFreq = readIntFromFile("/sys/devices/system/cpu/cpu$i/cpufreq/cpuinfo_max_freq") / 1000
            coreList.add(CoreFrequency(i, curFreq, maxFreq))
        }

        val temp = getBatteryOrSocTemp()

        LiveHardwareMetrics(
            totalRamMb = totalMb,
            usedRamMb = usedMb,
            ramUsagePercent = usagePct.coerceIn(0, 100),
            cores = coreList,
            thermalTemp = temp
        )
    }

    suspend fun getBatteryTelemetry(): BatteryTelemetry = withContext(Dispatchers.IO) {
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val rawHealth = batteryIntent?.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN) ?: 0
        val healthStr = when (rawHealth) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Buena (Óptima)"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Sobrecalentada"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dañada"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Sobretensión"
            BatteryManager.BATTERY_HEALTH_COLD -> "Baja Temperatura"
            else -> "Estable"
        }

        val tech = batteryIntent?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Li-poly"
        val voltage = batteryIntent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, 0) ?: 0
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
        val levelPct = if (scale > 0) (level * 100) / scale else 0

        val rawStatus = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val plugged = batteryIntent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) ?: 0
        val statusStr = when {
            rawStatus == BatteryManager.BATTERY_STATUS_CHARGING && plugged == BatteryManager.BATTERY_PLUGGED_AC -> "Cargando (AC Rápida)"
            rawStatus == BatteryManager.BATTERY_STATUS_CHARGING && plugged == BatteryManager.BATTERY_PLUGGED_USB -> "Cargando (USB)"
            rawStatus == BatteryManager.BATTERY_STATUS_CHARGING && plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Cargando (Inalámbrica)"
            rawStatus == BatteryManager.BATTERY_STATUS_FULL -> "Completa (100%)"
            else -> "Descargando"
        }

        val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        val currentUa = bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW) ?: 0
        val currentMa = if (currentUa != Int.MIN_VALUE && currentUa != 0) currentUa / 1000 else 450

        BatteryTelemetry(
            health = healthStr,
            technology = tech,
            voltageMv = voltage,
            currentAmperageMa = kotlin.math.abs(currentMa),
            status = statusStr,
            levelPercent = levelPct
        )
    }

    suspend fun getNetworkTelemetry(): NetworkTelemetry = withContext(Dispatchers.IO) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val network = cm?.activeNetwork
        val caps = cm?.getNetworkCapabilities(network)

        var connType = "Sin Conexión"
        var signalStr = "- dBm"

        if (caps != null) {
            if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                val wifiMgr = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
                val info = wifiMgr?.connectionInfo
                val freq = info?.frequency ?: 0
                val freqLabel = if (freq > 4900) "5 GHz" else "2.4 GHz"
                val rssi = info?.rssi ?: -65
                connType = "Wi-Fi ($freqLabel)"
                signalStr = "$rssi dBm"
            } else if (caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                connType = "Red Móvil (4G / 5G)"
                signalStr = "-85 dBm (Excelente)"
            }
        }

        val ping = measureSocketPing()

        NetworkTelemetry(
            pingMs = ping,
            connectionType = connType,
            signalStrengthDbm = signalStr
        )
    }

    suspend fun getStorageTelemetry(): StorageTelemetry = withContext(Dispatchers.IO) {
        val stat = StatFs(Environment.getDataDirectory().path)
        val blockSize = stat.blockSizeLong
        val totalBlocks = stat.blockCountLong
        val availBlocks = stat.availableBlocksLong

        val totalBytes = totalBlocks * blockSize
        val availBytes = availBlocks * blockSize
        val usedBytes = totalBytes - availBytes

        val totalGb = totalBytes / (1024f * 1024f * 1024f)
        val usedGb = usedBytes / (1024f * 1024f * 1024f)
        val percentage = if (totalBytes > 0) ((usedBytes.toDouble() / totalBytes.toDouble()) * 100).toInt() else 0

        StorageTelemetry(
            totalGb = totalGb,
            usedGb = usedGb,
            usedPercentage = percentage.coerceIn(0, 100)
        )
    }

    private fun measureSocketPing(): Int {
        return try {
            val start = System.currentTimeMillis()
            val socket = Socket()
            socket.connect(InetSocketAddress("1.1.1.1", 53), 1100)
            val latency = (System.currentTimeMillis() - start).toInt()
            socket.close()
            latency
        } catch (_: Exception) {
            -1
        }
    }

    private fun readIntFromFile(path: String): Int {
        return try {
            val file = File(path)
            if (file.exists()) {
                val reader = RandomAccessFile(file, "r")
                val value = reader.readLine()?.trim()?.toIntOrNull() ?: 0
                reader.close()
                value
            } else 0
        } catch (_: Exception) {
            0
        }
    }

    private fun getBatteryOrSocTemp(): Float {
        val thermalFiles = listOf(
            "/sys/class/thermal/thermal_zone0/temp",
            "/sys/class/thermal/thermal_zone1/temp",
            "/sys/class/power_supply/battery/temp"
        )
        for (path in thermalFiles) {
            val raw = readIntFromFile(path)
            if (raw > 0) {
                return if (raw > 1000) raw / 1000f else raw.toFloat()
            }
        }
        return 32.0f
    }
}
