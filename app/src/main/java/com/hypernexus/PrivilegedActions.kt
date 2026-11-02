package com.hypernexus

import android.os.Build
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PrivilegedActions(private val bridge: ShizukuBridge) {

    companion object {
        private const val TAG = "PrivilegedActions"
    }

    suspend fun forceRefreshRate(rate: Int): Result<Unit> = withContext(Dispatchers.IO) {
        val commands = if (rate <= 0) {
            listOf(
                "settings delete system peak_refresh_rate",
                "settings delete system min_refresh_rate",
                "settings delete system user_refresh_rate",
                "settings delete global oneplus_screen_refresh_rate",
                "settings delete system miui_refresh_rate",
                "setprop debug.layout.force_refresh_rate 0"
            )
        } else {
            listOf(
                "settings put system peak_refresh_rate $rate.0",
                "settings put system min_refresh_rate $rate.0",
                "settings put system user_refresh_rate $rate",
                "settings put global oneplus_screen_refresh_rate ${if (rate >= 90) 2 else 1}",
                "settings put system miui_refresh_rate $rate",
                "service call SurfaceFlinger 1035 i32 $rate",
                "setprop debug.layout.force_refresh_rate $rate"
            )
        }
        executeScriptWithValidation(commands, "Force Refresh Rate: $rate Hz")
    }

    suspend fun configureGpuRenderer(type: String): Result<Unit> = withContext(Dispatchers.IO) {
        val commands = when (type.lowercase()) {
            "vulkan" -> listOf(
                "setprop debug.hwui.renderer vulkan",
                "setprop renderthread.skia.pipeline SkiaVulkan",
                "setprop debug.renderengine.backend 2"
            )
            "vulkan_lite" -> listOf(
                "setprop debug.hwui.renderer vulkan",
                "setprop debug.hwui.texture_cache_size 16",
                "setprop debug.renderengine.backend 2"
            )
            "opengl" -> listOf(
                "setprop debug.hwui.renderer skiagl",
                "setprop debug.hwui.renderer opengl",
                "setprop renderthread.skia.pipeline SkiaGL"
            )
            "opengl_lite" -> listOf(
                "setprop debug.hwui.renderer skiagl",
                "setprop debug.egl.force_msaa 0",
                "setprop debug.hwui.fps_divisor 1"
            )
            else -> listOf("setprop debug.hwui.renderer skiagl")
        }
        executeScriptWithValidation(commands, "GPU Renderer: $type")
    }

    suspend fun setAnimationScales(scale: Float): Result<Unit> = withContext(Dispatchers.IO) {
        val commands = listOf(
            "settings put global window_animation_scale $scale",
            "settings put global transition_animation_scale $scale",
            "settings put global animator_duration_scale $scale"
        )
        executeScriptWithValidation(commands, "Animation Scale: $scale")
    }

    suspend fun changeDisplayDensity(dpi: Int): Result<Unit> = withContext(Dispatchers.IO) {
        val cmd = if (dpi == 0) "wm density reset" else "wm density $dpi"
        executeScriptWithValidation(listOf(cmd), "Change Density: $dpi")
    }

    suspend fun enableDex2OatSpeed(): Result<Unit> = withContext(Dispatchers.IO) {
        executeScriptWithValidation(
            listOf("settings put global pm.dexopt.bg-dexopt speed-profile", "cmd package compile -m speed-profile -a"),
            "Global Dex2Oat Speed"
        )
    }

    suspend fun compileAppSpeed(packageName: String): Result<Unit> = withContext(Dispatchers.IO) {
        executeScriptWithValidation(listOf("cmd package compile -m speed -f $packageName"), "DEX2OAT Speed: $packageName")
    }

    suspend fun applyGameMode(packageName: String, fps: Int = 120, downscale: Float = 1.0f): Result<Unit> = withContext(Dispatchers.IO) {
        val commands = mutableListOf(
            "cmd game mode performance $packageName",
            "cmd game set --mode 2 $packageName"
        )
        if (fps > 0) commands.add("cmd game set --fps $fps $packageName")
        if (downscale < 1.0f) commands.add("cmd game set --downscale $downscale $packageName")
        executeScriptWithValidation(commands, "Game API Tuner: $packageName")
    }

    suspend fun enableHighPerformance(): Result<Unit> = withContext(Dispatchers.IO) {
        executeScriptWithValidation(
            listOf("setprop persist.sys.performance_enabled 3", "settings put global window_animation_scale 0.5"),
            "High Performance"
        )
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
        executeScriptWithValidation(
            listOf("setprop persist.sys.Touchscreen.tap_response_time 0.02", "setprop persist.sys.enable.fast_rendering 1"),
            "Fast Touch"
        )
    }

    suspend fun disableLogging(): Result<Unit> = withContext(Dispatchers.IO) {
        executeScriptWithValidation(
            listOf("setprop persist.sys.log.tag.event OFF", "setprop persist.sys.enable_logs false"),
            "Disable Logging"
        )
    }

    suspend fun forceGpuRendering(enable: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        val v = if (enable) "1" else "0"
        executeScriptWithValidation(listOf("setprop debug.egl.hw $v", "setprop debug.sf.hw $v"), "Force GPU: $enable")
    }

    suspend fun disableVsync(): Result<Unit> = withContext(Dispatchers.IO) {
        executeScriptWithValidation(
            listOf("setprop debug.hwui.disable_vsync true", "setprop persist.sys.stable.fps.enable true"),
            "Disable VSYNC"
        )
    }

    suspend fun disableDirtyRegions(): Result<Unit> = withContext(Dispatchers.IO) {
        executeScriptWithValidation(
            listOf("setprop debug.hwui.render_dirty_regions false", "setprop debug.hwui.show_dirty_regions false"),
            "Disable Dirty Regions"
        )
    }

    suspend fun showDirtyRegions(enable: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        executeScriptWithValidation(listOf("setprop debug.hwui.show_dirty_regions ${if (enable) "true" else "false"}"), "Show Dirty Regions: $enable")
    }

    suspend fun configureAntialiasing(enabled: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        val v = if (enabled) "4" else "0"
        executeScriptWithValidation(listOf("setprop debug.egl.force_msaa $v"), "Antialiasing: $enabled")
    }

    suspend fun optimizeHwuiCache(): Result<Unit> = withContext(Dispatchers.IO) {
        executeScriptWithValidation(
            listOf("setprop debug.hwui.texture_cache_size 24", "setprop debug.hwui.layer_cache_size 8"),
            "HWUI Cache Optimization"
        )
    }

    suspend fun clearSmartCache(): Result<Unit> = withContext(Dispatchers.IO) {
        executeScriptWithValidation(listOf("pm trim-caches 4096G", "fstrim -v /data"), "Smart Cache Cleaner")
    }

    suspend fun killBackgroundApps(): Result<Unit> = withContext(Dispatchers.IO) {
        executeScriptWithValidation(listOf("cmd activity kill-all", "am kill-all"), "Smart App Killer")
    }

    private suspend fun executeScriptWithValidation(commands: List<String>, scriptName: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.i(TAG, "Lanzando: $scriptName")
            bridge.executeScript(commands)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Fallo en: $scriptName", e)
            Result.failure(e)
        }
    }
}
