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
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateZramGamer(enable)
            val swappiness = if (enable) "130" else "60"
            val pressure = if (enable) "50" else "100"
            privilegedActions.bridge.executeShellCommand(
                "echo $swappiness > /proc/sys/vm/swappiness 2>/dev/null; " +
                "echo $pressure > /proc/sys/vm/vfs_cache_pressure 2>/dev/null; " +
                "setprop persist.sys.zram_tuning ${if (enable) 1 else 0}"
            )
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

    fun toggleGlassEffect(enable: Boolean) {
        viewModelScope.launch {
            repository.updateGlassEffect(enable)
        }
    }

    fun setGameFpsTarget(fps: Int) {
        viewModelScope.launch {
            repository.updateGameFpsTarget(fps)
        }
    }

    fun setGameDownscale(enable: Boolean) {
        viewModelScope.launch {
            repository.updateGameDownscale(enable)
        }
    }

    fun selectGameTarget(packageName: String) {
        viewModelScope.launch {
            repository.updateGameTargetPackage(packageName)
        }
    }

    fun selectVpnTarget(packageName: String) {
        viewModelScope.launch {
            repository.updateVpnState(uiState.value.vpnActive, packageName)
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
                // NOTA: los toggles que administra la propia app (Input Lag, ZRAM,
                // DoH, DNS privado) NO se sincronizan desde el sistema, porque eso
                // pisaba la preferencia guardada y el switch volvía a OFF.
                // Aquí solo se reflejan ajustes que pueden cambiar desde fuera.

                // Disable HW Overlays (solo si el prop devuelve algo definitivo)
                val hwOverlay = bridge.executeShellCommand("getprop debug.sf.disable_hw_overlays")
                if (hwOverlay.first == 0) {
                    val raw = hwOverlay.second.trim()
                    if (raw.isNotEmpty()) {
                        val current = raw == "1"
                        if (current != uiState.value.disableHwOverlays) {
                            repository.updateDisableHwOverlays(current)
                        }
                    }
                }

                // Force 4x MSAA
                val msaa = bridge.executeShellCommand("getprop debug.egl.force_msaa")
                if (msaa.first == 0) {
                    val raw = msaa.second.trim()
                    if (raw.isNotEmpty()) {
                        val current = raw == "4"
                        if (current != uiState.value.msaa4x) {
                            repository.updateMsaa4x(current)
                        }
                    }
                }

                // Performance Governor
                val gov = bridge.executeShellCommand("cat /sys/devices/system/cpu/cpu0/cpufreq/scaling_governor")
                if (gov.first == 0) {
                    val raw = gov.second.trim().lowercase()
                    if (raw.isNotEmpty()) {
                        val current = raw == "performance"
                        if (current != uiState.value.perfGovernor) {
                            repository.updatePerfGovernor(current)
                        }
                    }
                }

                // Escalas de animación (solo si se pudo parsear un float real)
                val trans = bridge.executeShellCommand("settings get global transition_animation_scale")
                if (trans.first == 0) {
                    val parsed = trans.second.trim().toFloatOrNull()
                    if (parsed != null && kotlin.math.abs(parsed - (uiState.value.transitionScale ?: parsed)) > 0.01f) {
                        repository.updateTransitionScale(parsed)
                    }
                }

                val anim = bridge.executeShellCommand("settings get global animator_duration_scale")
                if (anim.first == 0) {
                    val parsed = anim.second.trim().toFloatOrNull()
                    if (parsed != null && kotlin.math.abs(parsed - (uiState.value.animatorScale ?: parsed)) > 0.01f) {
                        repository.updateAnimatorScale(parsed)
                    }
                }

                val win = bridge.executeShellCommand("settings get global window_animation_scale")
                if (win.first == 0) {
                    val parsed = win.second.trim().toFloatOrNull()
                    if (parsed != null && kotlin.math.abs(parsed - (uiState.value.windowScale ?: parsed)) > 0.01f) {
                        repository.updateWindowScale(parsed)
                    }
                }

                // DPI (solo si el último token es numérico)
                val dpi = bridge.executeShellCommand("wm density")
                if (dpi.first == 0) {
                    val parsed = dpi.second.trim().split(" ").lastOrNull()?.toIntOrNull()
                    if (parsed != null && parsed != uiState.value.displayDpi) {
                        repository.updateDpi(parsed)
                    }
                }

            } catch (_: Exception) {
                // Sync best-effort
            }
        }
    }

    fun syncGraphicsState() {
        viewModelScope.launch(Dispatchers.IO) {
            val bridge = privilegedActions.bridge
            try {
                // Igual que en Rendimiento: nunca se escribe false por un fallo
                // de lectura. Solo se aplica un valor cuando es concluyente.

                val fluid = bridge.executeShellCommand("getprop debug.sf.latch_unsignaled")
                if (fluid.first == 0) {
                    val raw = fluid.second.trim()
                    if (raw.isNotEmpty()) {
                        val current = raw == "1"
                        if (current != uiState.value.fluidGraphics) repository.updateFluidGraphics(current)
                    }
                }

                val sf = bridge.executeShellCommand("getprop debug.sf.hw")
                if (sf.first == 0) {
                    val raw = sf.second.trim()
                    if (raw.isNotEmpty()) {
                        val current = raw == "1"
                        if (current != uiState.value.sfHardwareAccel) repository.updateSfHardwareAccel(current)
                    }
                }

                val pacing = bridge.executeShellCommand("getprop debug.sf.early_phase_offset_ns")
                if (pacing.first == 0) {
                    val parsed = pacing.second.trim().toLongOrNull()
                    if (parsed != null) {
                        val current = parsed != 0L
                        if (current != uiState.value.gpuPacingControl) repository.updateGpuPacing(current)
                    }
                }

                val thermal = bridge.executeShellCommand("getprop debug.thermal.bypass")
                if (thermal.first == 0) {
                    val raw = thermal.second.trim()
                    if (raw.isNotEmpty()) {
                        val current = raw == "1"
                        if (current != uiState.value.thermalController) repository.updateThermalController(current)
                    }
                }

                val touch = bridge.executeShellCommand("getprop persist.sys.Touchscreen.tap_response_time")
                if (touch.first == 0) {
                    val parsed = touch.second.trim().toFloatOrNull()
                    if (parsed != null) {
                        val current = parsed < 0.01f
                        if (current != uiState.value.touchscreenOptimized) repository.updateTouchscreenOptimized(current)
                    }
                }

                val renderer = bridge.executeShellCommand("getprop debug.hwui.renderer")
                if (renderer.first == 0) {
                    val raw = renderer.second.trim()
                    if (raw.isNotEmpty()) {
                        val skia = raw.contains("skiagl", ignoreCase = true)
                        if (skia != uiState.value.boosterSkiaGl) repository.updateBoosterSkiaGl(skia)
                        val vulkan = raw.contains("skiavk", ignoreCase = true)
                        if (vulkan != uiState.value.boosterVulkan) repository.updateBoosterVulkan(vulkan)
                    }
                }

                // Composición: solo si el sistema devuelve un token conocido
                val comp = bridge.executeShellCommand("getprop persist.sys.composition.type")
                if (comp.first == 0) {
                    val mapped = when (comp.second.trim().lowercase()) {
                        "gpu" -> "GPU"
                        "c2d", "cpu" -> "CPU"
                        "dynamic" -> "Dynamic"
                        else -> null
                    }
                    if (mapped != null && mapped != uiState.value.compositionType) {
                        repository.updateCompositionType(mapped)
                    }
                }

            } catch (_: Exception) {
                // Sync best-effort
            }
        }
    }

    /**
     * ZRAM es una preferencia administrada por la app. No se sincroniza desde
     * el sistema para no pisar el switch cuando el shell no aplica el cambio.
     */
    fun syncExperimentalState() {
        // Intencionalmente vacío: la preferencia manda.
    }

    /**
     * DoH y DNS privado son preferencias administradas por la app. No se
     * sincronizan desde el sistema para evitar que el switch se revierta.
     */
    fun syncSecurityState() {
        // Intencionalmente vacío: la preferencia manda.
    }
}
