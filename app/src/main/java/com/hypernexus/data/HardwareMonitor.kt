package com.hypernexus.data

import android.app.ActivityManager
import android.content.Context
import android.content.res.Resources
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.RandomAccessFile

data class CoreFrequency(val coreIndex: Int, val currentMhz: Int, val maxMhz: Int)

data class LiveHardwareMetrics(
    val totalRamMb: Long,
    val usedRamMb: Long,
    val ramUsagePercent: Int,
    val cores: List<CoreFrequency>,
    val thermalTemp: Float
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
