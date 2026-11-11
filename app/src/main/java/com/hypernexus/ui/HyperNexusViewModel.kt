package com.hypernexus.ui

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hypernexus.PrivilegedActions
import com.hypernexus.data.NexusSettings
import com.hypernexus.data.PreferencesRepository
import com.hypernexus.services.HyperNexusVpnService
import com.hypernexus.services.RefreshRateKeeperService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

class HyperNexusViewModel(
    val privilegedActions: PrivilegedActions,
    private val repository: PreferencesRepository
) : ViewModel() {

    private var thresholdJob: Job? = null

    val uiState: StateFlow<NexusSettings> = repository.settingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = NexusSettings()
        )

    init {
        viewModelScope.launch {
            repository.settingsFlow.collectLatest { settings ->
                if (settings.thresholdRamPurger && thresholdJob == null) {
                    startMemoryThresholdMonitoring()
                } else if (!settings.thresholdRamPurger && thresholdJob != null) {
                    thresholdJob?.cancel()
                    thresholdJob = null
                }
            }
        }
    }

    fun toggleThresholdRamPurger(enable: Boolean) {
        viewModelScope.launch {
            repository.updateThresholdRamPurger(enable)
        }
    }

    fun toggleDohAntiThrottling(enable: Boolean) {
        viewModelScope.launch {
            repository.updateDohAntiThrottling(enable)
        }
    }

    fun toggleInputLagBypass(enable: Boolean) {
        viewModelScope.launch {
            repository.updateInputLagBypass(enable)
            privilegedActions.setInputLagBypass(enable)
        }
    }

    fun toggleZramGamer(enable: Boolean) {
        viewModelScope.launch {
            repository.updateZramGamer(enable)
            // ZRAM commands are executed directly in the screen via bridge
        }
    }

    fun togglePrivateDns(enable: Boolean) {
        viewModelScope.launch {
            repository.updatePrivateDns(enable)
            val mode = if (enable) "hostname" else "opportunistic"
            val specifier = if (enable) "one.one.one.one" else ""
            val commands = if (enable) {
                listOf(
                    "settings put global private_dns_mode $mode",
                    "settings put global private_dns_specifier $specifier"
                )
            } else {
                listOf("settings put global private_dns_mode opportunistic")
            }
            privilegedActions.bridge.executeScript(commands)
        }
    }

    private fun startMemoryThresholdMonitoring() {
        thresholdJob = viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                delay(8000)
                try {
                    val usagePercent = readMemoryUsagePercent()
                    if (usagePercent >= 85) {
                        privilegedActions.clearSmartCache()
                        Log.i("HyperNexus", "Limpieza silenciosa por umbral de memoria ejecutada ($usagePercent%)")
                    }
                } catch (_: Exception) {}
            }
        }
    }

    private fun readMemoryUsagePercent(): Int {
        return try {
            val file = File("/proc/meminfo")
            if (file.exists()) {
                val lines = file.readLines()
                val totalKb = lines.firstOrNull { it.startsWith("MemTotal:") }
                    ?.filter { it.isDigit() }?.toLongOrNull() ?: 1L
                val availKb = lines.firstOrNull { it.startsWith("MemAvailable:") }
                    ?.filter { it.isDigit() }?.toLongOrNull() ?: 0L
                val used = (totalKb - availKb).coerceAtLeast(0)
                ((used.toDouble() / totalKb.toDouble()) * 100).toInt()
            } else 50
        } catch (_: Exception) {
            50
        }
    }

    fun toggleVpnTunnel(context: Context, targetPackage: String, enable: Boolean) {
        viewModelScope.launch {
            repository.updateVpnState(enable, targetPackage)
            val useDoh = uiState.value.dohAntiThrottling
            val intent = Intent(context, HyperNexusVpnService::class.java).apply {
                action = if (enable) HyperNexusVpnService.ACTION_START else HyperNexusVpnService.ACTION_STOP
                if (enable) {
                    putExtra(HyperNexusVpnService.EXTRA_TARGET_PACKAGE, targetPackage)
                    putExtra(HyperNexusVpnService.EXTRA_USE_DOH, useDoh)
                }
            }
            if (enable) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) context.startForegroundService(intent)
                else context.startService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    fun setRefreshRate(context: Context, rate: Int) {
        viewModelScope.launch {
            repository.updateRefreshRate(rate)
            privilegedActions.forceRefreshRate(rate)
            val intent = Intent(context, RefreshRateKeeperService::class.java).apply {
                putExtra("TARGET_HZ", rate)
            }
            if (rate > 0) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) context.startForegroundService(intent)
                else context.startService(intent)
            } else {
                context.stopService(intent)
            }
        }
    }

    fun optimizePing(onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            val res = privilegedActions.optimizePing()
            onSuccess(res.second)
        }
    }

    fun setDisplayDpi(dpi: Int) {
        viewModelScope.launch {
            repository.updateDpi(dpi)
            privilegedActions.changeDisplayDensity(dpi)
        }
    }

    fun setTransitionScale(scale: Float) {
        viewModelScope.launch {
            repository.updateTransitionScale(scale)
            privilegedActions.setTransitionScale(scale)
        }
    }

    fun setAnimatorScale(scale: Float) {
        viewModelScope.launch {
            repository.updateAnimatorScale(scale)
            privilegedActions.setAnimatorScale(scale)
        }
    }

    fun setWindowScale(scale: Float) {
        viewModelScope.launch {
            repository.updateWindowScale(scale)
            privilegedActions.setWindowScale(scale)
        }
    }

    fun applyScreenResolution(width: Int, height: Int) {
        viewModelScope.launch {
            val cmd = if (width <= 0 || height <= 0) "wm size reset" else "wm size ${width}x${height}"
            privilegedActions.bridge.executeShellCommand(cmd)
        }
    }

    fun toggleDisableHwOverlays(enable: Boolean) {
        viewModelScope.launch {
            repository.updateDisableHwOverlays(enable)
            privilegedActions.setDisableHwOverlays(enable)
        }
    }

    fun toggleForced4xMsaa(enable: Boolean) {
        viewModelScope.launch {
            repository.updateMsaa4x(enable)
            privilegedActions.setForced4xMsaa(enable)
        }
    }

    fun togglePerfGovernor(enable: Boolean) {
        viewModelScope.launch {
            repository.updatePerfGovernor(enable)
            privilegedActions.setPerformanceGovernor(enable)
        }
    }

    fun toggleHighPriorityDex2oat(enable: Boolean) {
        viewModelScope.launch {
            repository.updateHighPriorityDex2oat(enable)
            privilegedActions.setHighPriorityDex2oat(enable)
        }
    }

    fun toggleFluidGraphics(enable: Boolean) {
        viewModelScope.launch {
            repository.updateFluidGraphics(enable)
            privilegedActions.setFluidGraphics(enable)
        }
    }

    fun toggleSfHardwareAccel(enable: Boolean) {
        viewModelScope.launch {
            repository.updateSfHardwareAccel(enable)
            privilegedActions.setSfHardwareAcceleration(enable)
        }
    }

    fun toggleGpuPacing(enable: Boolean) {
        viewModelScope.launch {
            repository.updateGpuPacing(enable)
            privilegedActions.setGpuPacingControl(enable)
        }
    }

    fun setCompositionType(type: String) {
        viewModelScope.launch {
            repository.updateCompositionType(type)
            privilegedActions.setCompositionType(type)
        }
    }

    fun toggleThermalController(enable: Boolean) {
        viewModelScope.launch {
            repository.updateThermalController(enable)
            privilegedActions.setThermalController(enable)
        }
    }

    fun toggleTouchscreenOptimized(enable: Boolean) {
        viewModelScope.launch {
            repository.updateTouchscreenOptimized(enable)
            privilegedActions.setTouchscreenOptimized(enable)
        }
    }

    fun toggleBoosterSkiaGl(enable: Boolean) {
        viewModelScope.launch {
            repository.updateBoosterSkiaGl(enable)
            privilegedActions.setBoosterRenderSkiaGl(enable)
        }
    }

    fun toggleBoosterVulkan(enable: Boolean) {
        viewModelScope.launch {
            repository.updateBoosterVulkan(enable)
            privilegedActions.setBoosterRenderVulkan(enable)
        }
    }

    fun toggleModule(id: String, scriptPayload: String) {
        viewModelScope.launch {
            val isNowActive = !uiState.value.activeModules.contains(id)
            repository.toggleModuleActive(id)
            if (isNowActive) {
                privilegedActions.executeScriptRaw(scriptPayload)
            }
        }
    }

    fun installCustomModule(path: String) {
        viewModelScope.launch {
            repository.addInstalledModule(path)
        }
    }

    fun removeCustomModule(path: String) {
        viewModelScope.launch {
            repository.removeInstalledModule(path)
        }
    }

    fun toggleVsync(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateVsync(enabled)
            if (enabled) privilegedActions.disableVsync()
        }
    }

    fun toggleDirtyRegions(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateDirtyRegions(enabled)
            if (enabled) privilegedActions.disableDirtyRegions()
        }
    }

    fun toggleShowDirty(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateShowDirty(enabled)
            privilegedActions.showDirtyRegions(enabled)
        }
    }

    fun toggleForceGpu(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateForceGpu(enabled)
            privilegedActions.forceGpuRendering(enabled)
        }
    }

    fun toggleHwuiCache(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateHwuiCache(enabled)
            if (enabled) privilegedActions.optimizeHwuiCache()
        }
    }

    fun toggleDex2oat(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateDex2oat(enabled)
            if (enabled) privilegedActions.enableDex2OatSpeed()
        }
    }

    fun toggleHighPerformance(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateHighPerformance(enabled)
            if (enabled) privilegedActions.enableHighPerformance()
        }
    }

    fun toggleFastTouch(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateFastTouch(enabled)
            if (enabled) privilegedActions.enableFastTouch()
        }
    }

    fun toggleSilentLogging(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateSilentLogging(enabled)
            if (enabled) privilegedActions.disableLogging()
        }
    }

    fun setGraphicsRenderer(type: String) {
        viewModelScope.launch {
            repository.updateRenderer(type)
            privilegedActions.configureGpuRenderer(type)
        }
    }

    fun setFpsOverlay(active: Boolean) {
        viewModelScope.launch {
            repository.updateFpsOverlay(active)
        }
    }

    fun toggleFavoriteCommand(cmd: String) {
        viewModelScope.launch {
            repository.toggleFavoriteCommand(cmd)
        }
    }

    // ============================================================
    // SYNC METHODS - Called from screens on ON_RESUME
    // ============================================================
    fun syncSystemState() {
        viewModelScope.launch(Dispatchers.IO) {
            val bridge = privilegedActions.bridge
            try {
                // Sync Disable HW Overlays
                val hwOverlayResult = bridge.executeShellCommand("getprop debug.sf.disable_hw_overlays")
                if (hwOverlayResult.first == 0) {
                    val current = hwOverlayResult.second.trim() == "1"
                    if (current != uiState.value.disableHwOverlays) {
                        repository.updateDisableHwOverlays(current)
                    }
                }

                // Sync Force 4x MSAA
                val msaaResult = bridge.executeShellCommand("getprop debug.egl.force_msaa")
                if (msaaResult.first == 0) {
                    val current = msaaResult.second.trim() == "4"
                    if (current != uiState.value.msaa4x) {
                        repository.updateMsaa4x(current)
                    }
                }

                // Sync Performance Governor
                val govResult = bridge.executeShellCommand("cat /sys/devices/system/cpu/cpu0/cpufreq/scaling_governor")
                if (govResult.first == 0) {
                    val current = govResult.second.trim().lowercase() == "performance"
                    if (current != uiState.value.perfGovernor) {
                        repository.updatePerfGovernor(current)
                    }
                }

                // Sync Animation Scales
                val transResult = bridge.executeShellCommand("settings get global transition_animation_scale")
                if (transResult.first == 0) {
                    val current = transResult.second.trim().toFloatOrNull() ?: 1f
                    if (kotlin.math.abs(current - (uiState.value.transitionScale ?: 1f)) > 0.01f) {
                        repository.updateTransitionScale(current)
                    }
                }

                val animResult = bridge.executeShellCommand("settings get global animator_duration_scale")
                if (animResult.first == 0) {
                    val current = animResult.second.trim().toFloatOrNull() ?: 1f
                    if (kotlin.math.abs(current - (uiState.value.animatorScale ?: 1f)) > 0.01f) {
                        repository.updateAnimatorScale(current)
                    }
                }

                val winResult = bridge.executeShellCommand("settings get global window_animation_scale")
                if (winResult.first == 0) {
                    val current = winResult.second.trim().toFloatOrNull() ?: 1f
                    if (kotlin.math.abs(current - (uiState.value.windowScale ?: 1f)) > 0.01f) {
                        repository.updateWindowScale(current)
                    }
                }

                // Sync DPI
                val dpiResult = bridge.executeShellCommand("wm density")
                if (dpiResult.first == 0) {
                    val output = dpiResult.second.trim()
                    val dpi = output.split(" ").lastOrNull()?.toIntOrNull()
                    if (dpi != null && dpi != uiState.value.displayDpi) {
                        repository.updateDpi(dpi)
                    }
                }

                // Sync Input Lag Bypass
                val touchLatencyResult = bridge.executeShellCommand("getprop persist.sys.touch.latency")
                if (touchLatencyResult.first == 0) {
                    val current = touchLatencyResult.second.trim() == "0"
                    if (current != uiState.value.inputLagBypass) {
                        repository.updateInputLagBypass(current)
                    }
                }

            } catch (e: Exception) {
                // Silent fail - sync is best effort
            }
        }
    }

    fun syncGraphicsState() {
        viewModelScope.launch(Dispatchers.IO) {
            val bridge = privilegedActions.bridge
            try {
                // Sync Fluid Graphics
                val fluidResult = bridge.executeShellCommand("getprop debug.sf.latch_unsignaled")
                if (fluidResult.first == 0) {
                    val current = fluidResult.second.trim() == "1"
                    if (current != uiState.value.fluidGraphics) {
                        repository.updateFluidGraphics(current)
                    }
                }

                // Sync SF Hardware Accel
                val sfResult = bridge.executeShellCommand("getprop debug.sf.hw")
                if (sfResult.first == 0) {
                    val current = sfResult.second.trim() == "1"
                    if (current != uiState.value.sfHardwareAccel) {
                        repository.updateSfHardwareAccel(current)
                    }
                }

                // Sync GPU Pacing
                val pacingResult = bridge.executeShellCommand("getprop debug.sf.early_phase_offset_ns")
                if (pacingResult.first == 0) {
                    val current = pacingResult.second.trim().toLongOrNull() != 0L
                    if (current != uiState.value.gpuPacingControl) {
                        repository.updateGpuPacing(current)
                    }
                }

                // Sync Thermal Controller
                val thermalResult = bridge.executeShellCommand("getprop debug.thermal.bypass")
                if (thermalResult.first == 0) {
                    val current = thermalResult.second.trim() == "1"
                    if (current != uiState.value.thermalController) {
                        repository.updateThermalController(current)
                    }
                }

                // Sync Touchscreen Optimized
                val touchResult = bridge.executeShellCommand("getprop persist.sys.Touchscreen.tap_response_time")
                if (touchResult.first == 0) {
                    val current = touchResult.second.trim().toFloatOrNull() != null && touchResult.second.trim().toFloatOrNull()! < 0.01f
                    if (current != uiState.value.touchscreenOptimized) {
                        repository.updateTouchscreenOptimized(current)
                    }
                }

                // Sync Booster SkiaGL
                val skiaResult = bridge.executeShellCommand("getprop debug.hwui.renderer")
                if (skiaResult.first == 0) {
                    val current = skiaResult.second.trim().contains("skiagl", ignoreCase = true)
                    if (current != uiState.value.boosterSkiaGl) {
                        repository.updateBoosterSkiaGl(current)
                    }
                }

                // Sync Booster Vulkan
                val vkResult = bridge.executeShellCommand("getprop debug.hwui.renderer")
                if (vkResult.first == 0) {
                    val current = vkResult.second.trim().contains("skiavk", ignoreCase = true)
                    if (current != uiState.value.boosterVulkan) {
                        repository.updateBoosterVulkan(current)
                    }
                }

                // Sync Composition Type
                val compResult = bridge.executeShellCommand("getprop persist.sys.composition.type")
                if (compResult.first == 0) {
                    val current = compResult.second.trim().lowercase()
                    val mapped = when (current) {
                        "gpu" -> "GPU"
                        "c2d" -> "CPU"
                        else -> "Dynamic"
                    }
                    if (mapped != uiState.value.compositionType) {
                        repository.updateCompositionType(mapped)
                    }
                }

            } catch (e: Exception) {
                // Silent fail - sync is best effort
            }
        }
    }

    fun syncExperimentalState() {
        viewModelScope.launch(Dispatchers.IO) {
            val bridge = privilegedActions.bridge
            try {
                // Sync ZRAM Gamer
                val zramResult = bridge.executeShellCommand("getprop persist.sys.zram_tuning")
                if (zramResult.first == 0) {
                    val current = zramResult.second.trim() == "1"
                    if (current != uiState.value.zramGamer) {
                        repository.updateZramGamer(current)
                    }
                }

                // Also check swappiness as fallback
                val swapResult = bridge.executeShellCommand("cat /proc/sys/vm/swappiness")
                if (swapResult.first == 0) {
                    val current = swapResult.second.trim().toIntOrNull() ?: 60
                    val isZramActive = current >= 100
                    if (isZramActive != uiState.value.zramGamer) {
                        repository.updateZramGamer(isZramActive)
                    }
                }

            } catch (e: Exception) {
                // Silent fail - sync is best effort
            }
        }
    }

    fun syncSecurityState() {
        viewModelScope.launch(Dispatchers.IO) {
            val bridge = privilegedActions.bridge
            try {
                // Sync Private DNS
                val dnsModeResult = bridge.executeShellCommand("settings get global private_dns_mode")
                if (dnsModeResult.first == 0) {
                    val mode = dnsModeResult.second.trim()
                    val current = mode == "hostname"
                    if (current != uiState.value.privateDnsEnabled) {
                        repository.updatePrivateDns(current)
                    }
                }

                // Sync DoH Anti-Throttling (check DNS settings)
                val dnsResult = bridge.executeShellCommand("settings get global private_dns_specifier")
                if (dnsResult.first == 0) {
                    val specifier = dnsResult.second.trim()
                    val current = specifier == "one.one.one.one"
                    if (current != uiState.value.dohAntiThrottling) {
                        repository.updateDohAntiThrottling(current)
                    }
                }

            } catch (e: Exception) {
                // Silent fail - sync is best effort
            }
        }
    }
}
