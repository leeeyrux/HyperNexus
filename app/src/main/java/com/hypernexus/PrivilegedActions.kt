package com.hypernexus

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PrivilegedActions(private val bridge: ShizukuBridge) {
    
    companion object {
        private const val TAG = "PrivilegedActions"
    }
    
    // ========== RENDIMIENTO (Tu Script 1 mejorado) ==========
    
    /**
     * Compilación instantánea Dex2Oat
     * "Reorganiza el código interno de tus apps para que arranquen más rápido"
     */
    suspend fun enableDex2OatSpeed(): Result<Unit> = withContext(Dispatchers.IO) {
        val commands = listOf(
            "settings put global pm.dexopt.bg-dexopt speed-profile",
            "settings put global pm.dexopt.boot speed-profile",
            "settings put global pm.dexopt.first-boot speed-profile",
            "setprop persist.sys.compile.system_apks speed",
            "setprop persist.sys.compile.system_folders speed",
            "setprop persist.sys.compile.system_files speed",
            "cmd package compile -m speed-profile -a"  // Compilar todas las apps
        )
        
        executeScriptWithValidation(commands, "Dex2Oat Speed")
    }
    
    /**
     * Modo Alto Rendimiento
     * "Le dice al sistema que use toda la RAM disponible sin restricciones"
     */
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
    
    /**
     * Respuesta Táctil mejorada
     * "Mejora la velocidad con la que la pantalla detecta tus dedos al jugar"
     */
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
    
    /**
     * Reducción de Logs
     * "Silencia registros internos para reducir trabajo del procesador"
     */
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
    
    // ========== GRÁFICOS (Tu Script 2) ==========
    
    /**
     * Desbloqueo de FPS (VSYNC off)
     * ⚠️ "Permite que los juegos superen los 60 FPS si el hardware lo permite"
     */
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
    
    /**
     * Renderizado Completo (Dirty Regions off)
     * "Dibuja toda la pantalla cada vez, evitando tirones en juegos"
     */
    suspend fun disableDirtyRegions(): Result<Unit> = withContext(Dispatchers.IO) {
        val commands = listOf(
            "setprop debug.hwui.render_dirty_regions false",
            "setprop debug.hwui.show_dirty_regions false",
            "setprop debug.hwui.show_layers_updates false",
            "setprop debug.hwui.show_overdraw false"
        )
        
        executeScriptWithValidation(commands, "Disable Dirty Regions")
    }
    
    /**
     * Mostrar Dirty Regions (para debug/visualización)
     * Cuando está activo, muestra en pantalla qué áreas se están redibujando
     */
    suspend fun showDirtyRegions(enable: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        val value = if (enable) "true" else "false"
        val commands = listOf(
            "setprop debug.hwui.show_dirty_regions $value",
            "setprop debug.hwui.show_layers_updates $value"
        )
        
        executeScriptWithValidation(commands, "Show Dirty Regions: $enable")
    }
    
    /**
     * Antialiasing Controlado
     * "Activa bordes suaves solo en apps que lo soportan"
     */
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
    
    /**
     * Tweaks de Caché HWUI (Riesgosos - con validación)
     * Mejora carga de texturas pero puede causar inestabilidad
     */
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
    
    // ========== MONITOREO ==========
    
    /**
     * Obtiene uso de RAM en tiempo real
     */
    suspend fun getRamUsage(): RamInfo = withContext(Dispatchers.IO) {
        val result = bridge.executeShellCommand("cat /proc/meminfo")
        val info = result.second
        
        val total = extractValue(info, "MemTotal") ?: 0
        val available = extractValue(info, "MemAvailable") ?: 0
        val used = total - available
        val percentage = if (total > 0) (used * 100 / total) else 0
        
        RamInfo(
            totalKb = total,
            usedKb = used,
            availableKb = available,
            usagePercent = percentage.toInt()
        )
    }
    
    /**
     * Obtiene frecuencias de CPU por núcleo
     */
    suspend fun getCpuFrequencies(): List<CpuCoreInfo> = withContext(Dispatchers.IO) {
        val cores = mutableListOf<CpuCoreInfo>()
        val cpuCount = Runtime.getRuntime().availableProcessors()
        
        for (i in 0 until cpuCount) {
            val maxFreq = readCpuFile("/sys/devices/system/cpu/cpu$i/cpufreq/cpuinfo_max_freq")
            val currentFreq = readCpuFile("/sys/devices/system/cpu/cpu$i/cpufreq/scaling_cur_freq")
            
            cores.add(CpuCoreInfo(
                coreId = i,
                currentMhz = (currentFreq?.toIntOrNull() ?: 0) / 1000,
                maxMhz = (maxFreq?.toIntOrNull() ?: 0) / 1000
            ))
        }
        
        cores
    }
    
    /**
     * Obtiene temperatura del sistema
     */
    suspend fun getTemperature(): Float = withContext(Dispatchers.IO) {
        val thermalZones = listOf(
            "/sys/class/thermal/thermal_zone0/temp",
            "/sys/class/thermal/thermal_zone1/temp",
            "/sys/class/thermal/thermal_zone2/temp"
        )
        
        var maxTemp = 0f
        for (zone in thermalZones) {
            val temp = readCpuFile(zone)?.toFloatOrNull()
            if (temp != null && temp > maxTemp) {
                maxTemp = temp
            }
        }
        
        // Convertir a Celsius (algunos dispositivos reportan en miligrados)
        if (maxTemp > 1000) maxTemp / 1000 else maxTemp
    }
    
    // ========== UTILIDADES ==========
    
    private suspend fun executeScriptWithValidation(
        commands: List<String>,
        scriptName: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.i(TAG, "Executing script: $scriptName")
            val results = bridge.executeScript(commands)
            
            val failed = results.filter { it.first != 0 }
            if (failed.isNotEmpty()) {
                Log.w(TAG, "Script $scriptName had ${failed.size} failed commands")
                // No fallamos completamente, algunos comandos pueden no aplicar en todos los dispositivos
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error executing script $scriptName", e)
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
            if (result.first == 0) result.second.trim() else null
        } catch (e: Exception) {
            null
        }
    }
    
    // ========== DATA CLASSES ==========
    
    data class RamInfo(
        val totalKb: Long,
        val usedKb: Long,
        val availableKb: Long,
        val usagePercent: Int
    )
    
    data class CpuCoreInfo(
        val coreId: Int,
        val currentMhz: Int,
        val maxMhz: Int
    )
}