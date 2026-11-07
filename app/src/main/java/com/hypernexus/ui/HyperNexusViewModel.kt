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
}
