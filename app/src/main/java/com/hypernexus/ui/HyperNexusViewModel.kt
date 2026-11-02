package com.hypernexus.ui

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hypernexus.PrivilegedActions
import com.hypernexus.data.NexusSettings
import com.hypernexus.data.PreferencesRepository
import com.hypernexus.services.RefreshRateKeeperService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HyperNexusViewModel(
    val privilegedActions: PrivilegedActions,
    private val repository: PreferencesRepository
) : ViewModel() {

    val uiState: StateFlow<NexusSettings> = repository.settingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = NexusSettings()
        )

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

    fun setDisplayDpi(dpi: Int) {
        viewModelScope.launch {
            repository.updateDpi(dpi)
            privilegedActions.changeDisplayDensity(dpi)
        }
    }

    fun setAnimationScale(scale: Float) {
        viewModelScope.launch {
            repository.updateAnimationScale(scale)
            privilegedActions.setAnimationScales(scale)
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

    fun toggleMsaa4x(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateMsaa4x(enabled)
            privilegedActions.configureAntialiasing(enabled)
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
}
