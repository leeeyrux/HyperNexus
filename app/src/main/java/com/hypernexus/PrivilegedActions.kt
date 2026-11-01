package com.hypernexus

import android.util.Log
import androidx.annotation.Keep
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PrivilegedActions(private val bridge: ShizukuBridge) {
    
    companion object {
        private const val TAG = "PrivilegedActions"
    }
    
    // ==========================================
    // SECCIÓN 1: RENDIMIENTO (PERFORMANCE)
    // ==========================================
    
    suspend fun enableDex2OatSpeed(): Result<Unit> = withContext(Dispatchers.IO) {
        val commands = listOf(
            "settings put global pm.dexopt.bg-dexopt speed-profile",
            "settings put global pm.dexopt.boot speed-profile",
            "settings put global pm.dexopt.first-boot speed-profile",
            "setprop persist.sys.compile.system_apks speed",
            "setprop persist.sys.compile.system_folders speed",
            "setprop persist.sys.compile.system_files speed",
            "cmd package compile -m speed-profile -a"
        )
        executeScriptWithValidation(commands, "Dex2Oat Speed")
    }
    
    suspend fun enableHighPerformance(): Result<Unit> = withContext(Dispatchers.IO) {
        val commands = listOf(
            "setprop ro.config.low_ram false",
            "setprop persist.sys.performance_enabled 3",
            "setprop persist.sys.performance_folders 100",
            "setprop persist.sys.optimizer_files 100",
            "setprop persist.sys.performance_limiter off",
            "settings put global window_animation_scale 0.5",
            "settings put global transition_animation_scale 0.5",
            "settings put global animator_duration_scale 0.5"
        )
        executeScriptWithValidation(commands, "High Performance")
    }
    
    suspend fun enableFastTouch(): Result<Unit> = withContext(Dispatchers.IO) {
        val commands = listOf(
            "setprop persist.sys.Mouse_Sensitivity 5.50",
            "setprop persist.sys.MouseSpeed 1",
            "setprop persist.sys.touchswipedeadzone 5",
            "setprop persist.sys.Touchscreen.swipe_sensitivity 1.0",
            "setprop persist.sys.Touchscreen.gesture_recognition true",
            "setprop persist.sys.Touchscreen.tap_response_time 0.02",
            "setprop persist.sys.enable.fast_rendering 1",
            "setprop persist.sys.smooth.ui 60"
        )
        executeScriptWithValidation(commands, "Fast Touch")
    }
    
    suspend fun disableLogging(): Result<Unit> = withContext(Dispatchers.IO) {
        val commands = listOf(
            "setprop persist.sys.log.tag.event OFF",
            "setprop persist.sys.system.logs.enabled false",
            "setprop persist.sys.enable_logs false",
            "setprop persist.sys.log_size 0kb",
            "setprop persist.sys.system.logcat.live disabled"
        )
        executeScriptWithValidation(commands, "Disable Logging")
    }
    
    // ==========================================
    // SECCIÓN 2: GRÁFICOS (GRAPHICS)
    // ==========================================
    
    suspend fun disableVsync(): Result<Unit> = withContext(Dispatchers.IO) {
        val commands = listOf(
            "setprop debug.hwui.disable_vsync true",
            "setprop persist.sys.stable.fps.enable true",
            "setprop persist.sys.dont.lower.fps true",
            "setprop persist.sys.stabilizer.fps true",
            "setprop persist.sys.ro.vendor.smart_dfps disable",
            "setprop persist.sys.ro.vendor.dfps disable"
        )
        executeScriptWithValidation(commands, "Disable VSYNC")
    }
    
    suspend fun disableDirtyRegions(): Result<Unit> = withContext(Dispatchers.IO) {
        val commands = listOf(
            "setprop debug.hwui.render_dirty_regions false",
            "setprop debug.hwui.show_dirty_regions false",
            "setprop debug.hwui.show_layers_updates false",
            "setprop debug.hwui.show_overdraw false"
        )
        executeScriptWithValidation(commands, "Disable Dirty Regions")
    }
    
    suspend fun showDirtyRegions(enable: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        val value = if (enable) "true" else "false"
        val commands = listOf(
            "setprop debug.hwui.show_dirty_regions $value",
            "setprop debug.hwui.show_layers_updates $value"
        )
        executeScriptWithValidation(commands, "Show Dirty Regions: $enable")
    }

    suspend fun configureAntialiasing(enabled: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        val commands = if (enabled) {
            listOf(
                "setprop debug.egl.force_msaa 4",
                "setprop debug.hwui.disable_scissor_opt false"
            )
        } else {
            listOf(
                "setprop debug.egl.force_msaa 0",
                "setprop debug.egl.force_fxaa false",
                "setprop debug.egl.force_taa false",
                "setprop debug.egl.force_ssaa false",
                "setprop debug.egl.force_smaa false",
                "setprop debug.egl.force_mlaa false",
                "setprop debug.egl.force_txaa false",
                "setprop debug.egl.force_csaa false"
            )
        }
        executeScriptWithValidation(commands, "Antialiasing: $enabled")
    }
    
    suspend fun optimizeHwuiCache(): Result<Unit> = withContext(Dispatchers.IO) {
        val commands = listOf(
            "setprop debug.hwui.texture_cache_size 24",
            "setprop debug.hwui.layer_cache_size 8",
            "setprop debug.hwui.path_cache_size 1",
            "setprop debug.hwui.text_large_cache_width 512",
            "setprop debug.hwui.text_large_cache_height 256",
            "setprop debug.hwui.text_small_cache_width 256",
            "setprop debug.hwui.text_small_cache_height 128",
            "setprop debug.hwui.use_gpu_pixel_buffers true"
        )
        executeScriptWithValidation(commands, "HWUI Cache Optimization")
    }
    
    // ==========================================
    // SECCIÓN 3: TELEMETRÍA Y MONITOREO
    // ==========================================
    
    suspend fun getRamUsage(): RamInfo = withContext(Dispatchers.IO) {
        val info = readCpuFile("/proc/meminfo") ?: ""
        val total = extractValue(info, "MemTotal") ?: 0L
        val available = extractValue(info, "MemAvailable") ?: 0L
        val used = total - available
        val percentage = if (total > 0L) (used * 100L / total) else 0L
        RamInfo(totalKb = total, usedKb = used, availableKb = available, usagePercent = percentage.toInt())
    }
    
    suspend fun getCpuFrequencies(): List<CpuCoreInfo> = withContext(Dispatchers.IO) {
        val cores = mutableListOf<CpuCoreInfo>()
        val cpuCount = Runtime.getRuntime().availableProcessors()
        for (i in 0 until cpuCount) {
            val maxFreq = readCpuFile("/sys/devices/system/cpu/cpu$i/cpufreq/cpuinfo_max_freq")
            val currentFreq = readCpuFile("/sys/devices/system/cpu/cpu$i/cpufreq/scaling_cur_freq")
            cores.add(CpuCoreInfo(
                coreId = i,
                currentMhz = (currentFreq?.trim()?.toIntOrNull() ?: 0) / 1000,
                maxMhz = (maxFreq?.trim()?.toIntOrNull() ?: 0) / 1000
            ))
        }
        cores
    }
    
    suspend fun getTemperature(): Float = withContext(Dispatchers.IO) {
        val thermalZones = listOf(
            "/sys/class/thermal/thermal_zone0/temp",
            "/sys/class/thermal/thermal_zone1/temp",
            "/sys/class/thermal/thermal_zone2/temp",
            "/sys/class/power_supply/battery/temp"
        )
        var maxTemp = 0f
        for (zone in thermalZones) {
            val tempRaw = readCpuFile(zone)?.trim()?.toFloatOrNull()
            if (tempRaw != null && tempRaw > maxTemp) { maxTemp = tempRaw }
        }
        if (maxTemp > 1000f) maxTemp / 1000f else maxTemp
    }
    
    // ==========================================
    // SECCIÓN 4: AUXILIARES
    // ==========================================
    
    private suspend fun executeScriptWithValidation(commands: List<String>, scriptName: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.i(TAG, "Lanzando lote de comandos: $scriptName")
            val results = bridge.executeScript(commands)
            val failed = results.filter { it.first != 0 }
            if (failed.isNotEmpty()) {
                Log.w(TAG, "El script $scriptName tiene ${failed.size} comandos no soportados.")
            }
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
            // Cambiado para admitir respuestas exitosas del shell nativo heredado
            if (result.first == 0 || result.second.isNotEmpty()) result.second.trim() else null
        } catch (e: Exception) {
            null
        }
    }
    
    // ==========================================
    // SECCIÓN 5: MODELOS BLINDADOS (@KEEP)
    // ==========================================
    
    @Keep
    data class RamInfo(val totalKb: Long, val usedKb: Long, val availableKb: Long, val usagePercent: Int)
    
    @Keep
    data class CpuCoreInfo(val coreId: Int, val currentMhz: Int, val maxMhz: Int)
}
