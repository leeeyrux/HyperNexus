package com.hypernexus

import android.os.Build
import android.util.Log
import androidx.annotation.Keep
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PrivilegedActions(private val bridge: ShizukuBridge) {
    
    companion object {
        private const val TAG = "PrivilegedActions"
    }
    
    @Keep
    data class DeviceHardwareInfo(
        val model: String,
        val manufacturer: String,
        val soc: String,
        val androidVersion: String,
        val uptime: String
    )

    suspend fun getDeviceHardwareInfo(): DeviceHardwareInfo = withContext(Dispatchers.IO) {
        val rawUptime = readCpuFile("/proc/uptime")?.split(" ")?.get(0)?.toDoubleOrNull() ?: 0.0
        val days = (rawUptime / 86400).toInt()
        val hours = ((rawUptime % 86400) / 3600).toInt()
        val minutes = (((rawUptime % 86400) % 3600) / 60).toInt()
        
        DeviceHardwareInfo(
            model = Build.MODEL,
            manufacturer = Build.MANUFACTURER.uppercase(),
            soc = Build.HARDWARE,
            androidVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
            uptime = "${days}d ${hours}h ${minutes}m"
        )
    }
    
    suspend fun changeDisplayDensity(dpi: Int): Result<Unit> = withContext(Dispatchers.IO) {
        val cmd = if (dpi == 0) "wm density reset" else "wm density $dpi"
        executeScriptWithValidation(listOf(cmd), "Change Density to $dpi")
    }

    suspend fun forceRefreshRate(rate: Int): Result<Unit> = withContext(Dispatchers.IO) {
        val commands = if (rate == 0) {
            listOf(
                "settings delete system peak_refresh_rate",
                "settings delete system min_refresh_rate",
                "settings delete system user_refresh_rate"
            )
        } else {
            listOf(
                "settings put system peak_refresh_rate $rate.0",
                "settings put system min_refresh_rate $rate.0",
                "settings put system user_refresh_rate $rate"
            )
        }
        executeScriptWithValidation(commands, "Force Refresh Rate: $rate Hz")
    }

    suspend fun setAnimationScales(scale: Float): Result<Unit> = withContext(Dispatchers.IO) {
        val commands = listOf(
            "settings put global window_animation_scale $scale",
            "settings put global transition_animation_scale $scale",
            "settings put global animator_duration_scale $scale"
        )
        executeScriptWithValidation(commands, "Animation Scale: $scale")
    }

    suspend fun enableDex2OatSpeed(): Result<Unit> = withContext(Dispatchers.IO) {
        val commands = listOf(
            "settings put global pm.dexopt.bg-dexopt speed-profile",
            "cmd package compile -m speed-profile -a"
        )
        executeScriptWithValidation(commands, "Dex2Oat Speed")
    }
    
    suspend fun enableHighPerformance(): Result<Unit> = withContext(Dispatchers.IO) {
        val commands = listOf(
            "setprop persist.sys.performance_enabled 3",
            "settings put global window_animation_scale 0.5"
        )
        executeScriptWithValidation(commands, "High Performance")
    }
    
    suspend fun enableGamingMode(): Result<Unit> = withContext(Dispatchers.IO) {
        val commands = listOf(
            "setprop persist.sys.performance_enabled 3",
            "setprop debug.sf.disable_hw_overlays 1",
            "setprop persist.sys.Touchscreen.tap_response_time 0.01",
            "setprop persist.sys.enable.fast_rendering 1",
            "cmd power set-mode 0",
            "settings put global window_animation_scale 0.25",
            "settings put global transition_animation_scale 0.25",
            "settings put global animator_duration_scale 0.25"
        )
        executeScriptWithValidation(commands, "Gaming Mode Extreme")
    }
    
    suspend fun enableFastTouch(): Result<Unit> = withContext(Dispatchers.IO) {
        val commands = listOf(
            "setprop persist.sys.Touchscreen.tap_response_time 0.02",
            "setprop persist.sys.enable.fast_rendering 1"
        )
        executeScriptWithValidation(commands, "Fast Touch")
    }
    
    suspend fun disableLogging(): Result<Unit> = withContext(Dispatchers.IO) {
        val commands = listOf(
            "setprop persist.sys.log.tag.event OFF",
            "setprop persist.sys.enable_logs false"
        )
        executeScriptWithValidation(commands, "Disable Logging")
    }

    suspend fun toggleCpuBoost(enable: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        val gov = if (enable) "performance" else "schedutil"
        val cpuCount = Runtime.getRuntime().availableProcessors()
        val commands = mutableListOf<String>()
        for (i in 0 until cpuCount) {
            commands.add("echo $gov > /sys/devices/system/cpu/cpu$i/cpufreq/scaling_governor")
        }
        executeScriptWithValidation(commands, "CPU Boost: $enable")
    }

    suspend fun configureGpuRenderer(type: String): Result<Unit> = withContext(Dispatchers.IO) {
        val commands = when (type.lowercase()) {
            "vulkan" -> listOf("setprop debug.hwui.renderer vulkan")
            "opengl" -> listOf("setprop debug.hwui.renderer opengl")
            else -> listOf("setprop debug.hwui.renderer skiagl")
        }
        executeScriptWithValidation(commands, "GPU Renderer: $type")
    }

    suspend fun toggleHardwareAcceleration(enable: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        val commands = listOf(
            "setprop persist.sys.composition.type gpu",
            "setprop debug.sf.disable_hw_overlays ${if (enable) "1" else "0"}"
        )
        executeScriptWithValidation(commands, "HW Acceleration: $enable")
    }

    suspend fun forceGpuRendering(enable: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        val value = if (enable) "1" else "0"
        val commands = listOf(
            "setprop debug.egl.hw $value",
            "setprop debug.sf.hw $value"
        )
        executeScriptWithValidation(commands, "Force GPU Rendering: $enable")
    }

    suspend fun disableVsync(): Result<Unit> = withContext(Dispatchers.IO) {
        val commands = listOf(
            "setprop debug.hwui.disable_vsync true",
            "setprop persist.sys.stable.fps.enable true"
        )
        executeScriptWithValidation(commands, "Disable VSYNC")
    }
    
    suspend fun disableDirtyRegions(): Result<Unit> = withContext(Dispatchers.IO) {
        val commands = listOf(
            "setprop debug.hwui.render_dirty_regions false",
            "setprop debug.hwui.show_dirty_regions false"
        )
        executeScriptWithValidation(commands, "Disable Dirty Regions")
    }

    suspend fun showDirtyRegions(enable: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        val value = if (enable) "true" else "false"
        val commands = listOf(
            "setprop debug.hwui.show_dirty_regions $value"
        )
        executeScriptWithValidation(commands, "Show Dirty Regions: $enable")
    }
    
    suspend fun configureAntialiasing(enabled: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        val commands = if (enabled) listOf("setprop debug.egl.force_msaa 4") else listOf("setprop debug.egl.force_msaa 0")
        executeScriptWithValidation(commands, "Antialiasing: $enabled")
    }
    
    suspend fun optimizeHwuiCache(): Result<Unit> = withContext(Dispatchers.IO) {
        val commands = listOf("setprop debug.hwui.texture_cache_size 24", "setprop debug.hwui.layer_cache_size 8")
        executeScriptWithValidation(commands, "HWUI Cache Optimization")
    }
    
    suspend fun clearSmartCache(): Result<Unit> = withContext(Dispatchers.IO) {
        val commands = listOf("pm trim-caches 4096G", "fstrim -v /data")
        executeScriptWithValidation(commands, "Smart Cache Cleaner")
    }

    suspend fun killBackgroundApps(): Result<Unit> = withContext(Dispatchers.IO) {
        executeScriptWithValidation(listOf("cmd activity kill-all", "am kill-all"), "Smart App Killer")
    }
    
    suspend fun getRamUsage(): RamInfo = withContext(Dispatchers.IO) {
        val info = readCpuFile("/proc/meminfo") ?: ""
        val total = extractValue(info, "MemTotal") ?: 0L
        val available = extractValue(info, "MemAvailable") ?: 0L
        val used = total - available
        val percentage = if (total > 0L) (used * 100L / total) else 0L
        RamInfo(total, used, available, percentage.toInt())
    }
    
    suspend fun getCpuFrequencies(): List<CpuCoreInfo> = withContext(Dispatchers.IO) {
        val cores = mutableListOf<CpuCoreInfo>()
        val cpuCount = Runtime.getRuntime().availableProcessors()
        for (i in 0 until cpuCount) {
            val maxFreq = readCpuFile("/sys/devices/system/cpu/cpu$i/cpufreq/cpuinfo_max_freq")
            val currentFreq = readCpuFile("/sys/devices/system/cpu/cpu$i/cpufreq/scaling_cur_freq")
            cores.add(CpuCoreInfo(i, (currentFreq?.trim()?.toIntOrNull() ?: 0) / 1000, (maxFreq?.trim()?.toIntOrNull() ?: 0) / 1000))
        }
        cores
    }
    
    suspend fun getTemperature(): Float = withContext(Dispatchers.IO) {
        val thermalZones = listOf("/sys/class/thermal/thermal_zone0/temp", "/sys/class/power_supply/battery/temp")
        var maxTemp = 0f
        for (zone in thermalZones) {
            val tempRaw = readCpuFile(zone)?.trim()?.toFloatOrNull()
            if (tempRaw != null && tempRaw > maxTemp) { maxTemp = tempRaw }
        }
        if (maxTemp > 1000f) maxTemp / 1000f else maxTemp
    }
    
    private suspend fun executeScriptWithValidation(commands: List<String>, scriptName: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.i(TAG, "Lanzando lote de comandos: $scriptName")
            bridge.executeScript(commands)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Fallo en: $scriptName", e)
            Result.failure(e)
        }
    }
    
    private fun extractValue(text: String, key: String): Long? {
        val regex = Regex("$key:\\s+(\\d+)")
        return regex.find(text)?.groupValues?.get(1)?.toLongOrNull()
    }
    
    private fun readCpuFile(path: String): String? {
        return try {
            val result = bridge.executeShellCommand("cat $path")
            if (result.first == 0 || result.second.isNotEmpty()) result.second.trim() else null
        } catch (e: Exception) { null }
    }
    
    @Keep
    data class RamInfo(val totalKb: Long, val usedKb: Long, val availableKb: Long, val usagePercent: Int)
    
    @Keep
    data class CpuCoreInfo(val coreId: Int, val currentMhz: Int, val maxMhz: Int)
}
