package com.hypernexus.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "hypernexus_settings")

data class NexusSettings(
    val refreshRate: Int = 0,
    val vsyncDisabled: Boolean = false,
    val dirtyRegionsDisabled: Boolean = false,
    val showDirtyRegions: Boolean = false,
    val forceGpuRendering: Boolean = false,
    val msaa4x: Boolean = false,
    val hwuiCacheOptimized: Boolean = false,
    val dex2oatEnabled: Boolean = false,
    val highPerformance: Boolean = false,
    val fastTouch: Boolean = false,
    val silentLogging: Boolean = false,
    val displayDpi: Int = 410,
    val animationScale: Float = 1.0f,
    val graphicsRenderer: String = "vulkan_booster",
    val fpsOverlayActive: Boolean = false,
    val favoriteCommands: Set<String> = emptySet(),
    // Legacy Magic Tuner / Vexiro Extensions
    val fluidGraphics: Boolean = false,
    val sfHardwareAccel: Boolean = false,
    val gpuPacingControl: Boolean = false,
    val compositionType: String = "gpu",
    val thermalController: Boolean = false,
    val touchscreenOptimized: Boolean = false,
    val boosterSkiaGl: Boolean = false,
    val boosterVulkan: Boolean = false,
    val activeModules: Set<String> = emptySet(),
    val installedModulePaths: Set<String> = emptySet()
)

class PreferencesRepository(private val context: Context) {

    private object Keys {
        val REFRESH_RATE = intPreferencesKey("refresh_rate")
        val VSYNC_DISABLED = booleanPreferencesKey("vsync_disabled")
        val DIRTY_REGIONS = booleanPreferencesKey("dirty_regions")
        val SHOW_DIRTY_REGIONS = booleanPreferencesKey("show_dirty_regions")
        val FORCE_GPU = booleanPreferencesKey("force_gpu")
        val MSAA_4X = booleanPreferencesKey("msaa_4x")
        val HWUI_CACHE = booleanPreferencesKey("hwui_cache")
        val DEX2OAT = booleanPreferencesKey("dex2oat")
        val HIGH_PERFORMANCE = booleanPreferencesKey("high_performance")
        val FAST_TOUCH = booleanPreferencesKey("fast_touch")
        val SILENT_LOGGING = booleanPreferencesKey("silent_logging")
        val DISPLAY_DPI = intPreferencesKey("display_dpi")
        val ANIMATION_SCALE = floatPreferencesKey("animation_scale")
        val GRAPHICS_RENDERER = stringPreferencesKey("graphics_renderer")
        val FPS_OVERLAY_ACTIVE = booleanPreferencesKey("fps_overlay_active")
        val FAVORITE_COMMANDS = stringSetPreferencesKey("favorite_commands")

        val FLUID_GRAPHICS = booleanPreferencesKey("fluid_graphics")
        val SF_HARDWARE_ACCEL = booleanPreferencesKey("sf_hardware_accel")
        val GPU_PACING = booleanPreferencesKey("gpu_pacing")
        val COMPOSITION_TYPE = stringPreferencesKey("composition_type")
        val THERMAL_CONTROLLER = booleanPreferencesKey("thermal_controller")
        val TOUCHSCREEN_OPT = booleanPreferencesKey("touchscreen_opt")
        val BOOSTER_SKIAGL = booleanPreferencesKey("booster_skiagl")
        val BOOSTER_VULKAN = booleanPreferencesKey("booster_vulkan")
        val ACTIVE_MODULES = stringSetPreferencesKey("active_modules")
        val INSTALLED_MODULES = stringSetPreferencesKey("installed_modules")
    }

    val settingsFlow: Flow<NexusSettings> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { prefs ->
            NexusSettings(
                refreshRate = prefs[Keys.REFRESH_RATE] ?: 0,
                vsyncDisabled = prefs[Keys.VSYNC_DISABLED] ?: false,
                dirtyRegionsDisabled = prefs[Keys.DIRTY_REGIONS] ?: false,
                showDirtyRegions = prefs[Keys.SHOW_DIRTY_REGIONS] ?: false,
                forceGpuRendering = prefs[Keys.FORCE_GPU] ?: false,
                msaa4x = prefs[Keys.MSAA_4X] ?: false,
                hwuiCacheOptimized = prefs[Keys.HWUI_CACHE] ?: false,
                dex2oatEnabled = prefs[Keys.DEX2OAT] ?: false,
                highPerformance = prefs[Keys.HIGH_PERFORMANCE] ?: false,
                fastTouch = prefs[Keys.FAST_TOUCH] ?: false,
                silentLogging = prefs[Keys.SILENT_LOGGING] ?: false,
                displayDpi = prefs[Keys.DISPLAY_DPI] ?: 410,
                animationScale = prefs[Keys.ANIMATION_SCALE] ?: 1.0f,
                graphicsRenderer = prefs[Keys.GRAPHICS_RENDERER] ?: "vulkan_booster",
                fpsOverlayActive = prefs[Keys.FPS_OVERLAY_ACTIVE] ?: false,
                favoriteCommands = prefs[Keys.FAVORITE_COMMANDS] ?: emptySet(),
                fluidGraphics = prefs[Keys.FLUID_GRAPHICS] ?: false,
                sfHardwareAccel = prefs[Keys.SF_HARDWARE_ACCEL] ?: false,
                gpuPacingControl = prefs[Keys.GPU_PACING] ?: false,
                compositionType = prefs[Keys.COMPOSITION_TYPE] ?: "gpu",
                thermalController = prefs[Keys.THERMAL_CONTROLLER] ?: false,
                touchscreenOptimized = prefs[Keys.TOUCHSCREEN_OPT] ?: false,
                boosterSkiaGl = prefs[Keys.BOOSTER_SKIAGL] ?: false,
                boosterVulkan = prefs[Keys.BOOSTER_VULKAN] ?: false,
                activeModules = prefs[Keys.ACTIVE_MODULES] ?: emptySet(),
                installedModulePaths = prefs[Keys.INSTALLED_MODULES] ?: emptySet()
            )
        }

    private suspend fun <T> set(key: Preferences.Key<T>, value: T) {
        context.dataStore.edit { it[key] = value }
    }

    suspend fun updateRefreshRate(rate: Int) = set(Keys.REFRESH_RATE, rate)
    suspend fun updateDpi(dpi: Int) = set(Keys.DISPLAY_DPI, dpi)
    suspend fun updateAnimationScale(scale: Float) = set(Keys.ANIMATION_SCALE, scale)
    suspend fun updateVsync(v: Boolean) = set(Keys.VSYNC_DISABLED, v)
    suspend fun updateDirtyRegions(v: Boolean) = set(Keys.DIRTY_REGIONS, v)
    suspend fun updateShowDirty(v: Boolean) = set(Keys.SHOW_DIRTY_REGIONS, v)
    suspend fun updateForceGpu(v: Boolean) = set(Keys.FORCE_GPU, v)
    suspend fun updateMsaa4x(v: Boolean) = set(Keys.MSAA_4X, v)
    suspend fun updateHwuiCache(v: Boolean) = set(Keys.HWUI_CACHE, v)
    suspend fun updateDex2oat(v: Boolean) = set(Keys.DEX2OAT, v)
    suspend fun updateHighPerformance(v: Boolean) = set(Keys.HIGH_PERFORMANCE, v)
    suspend fun updateFastTouch(v: Boolean) = set(Keys.FAST_TOUCH, v)
    suspend fun updateSilentLogging(v: Boolean) = set(Keys.SILENT_LOGGING, v)
    suspend fun updateRenderer(renderer: String) = set(Keys.GRAPHICS_RENDERER, renderer)
    suspend fun updateFpsOverlay(active: Boolean) = set(Keys.FPS_OVERLAY_ACTIVE, active)

    suspend fun updateFluidGraphics(v: Boolean) = set(Keys.FLUID_GRAPHICS, v)
    suspend fun updateSfHardwareAccel(v: Boolean) = set(Keys.SF_HARDWARE_ACCEL, v)
    suspend fun updateGpuPacing(v: Boolean) = set(Keys.GPU_PACING, v)
    suspend fun updateCompositionType(v: String) = set(Keys.COMPOSITION_TYPE, v)
    suspend fun updateThermalController(v: Boolean) = set(Keys.THERMAL_CONTROLLER, v)
    suspend fun updateTouchscreenOptimized(v: Boolean) = set(Keys.TOUCHSCREEN_OPT, v)
    suspend fun updateBoosterSkiaGl(v: Boolean) = set(Keys.BOOSTER_SKIAGL, v)
    suspend fun updateBoosterVulkan(v: Boolean) = set(Keys.BOOSTER_VULKAN, v)

    suspend fun toggleFavoriteCommand(cmd: String) {
        context.dataStore.edit { prefs ->
            val cur = prefs[Keys.FAVORITE_COMMANDS] ?: emptySet()
            prefs[Keys.FAVORITE_COMMANDS] = if (cur.contains(cmd)) cur - cmd else cur + cmd
        }
    }

    suspend fun toggleModuleActive(id: String) {
        context.dataStore.edit { prefs ->
            val cur = prefs[Keys.ACTIVE_MODULES] ?: emptySet()
            prefs[Keys.ACTIVE_MODULES] = if (cur.contains(id)) cur - id else cur + id
        }
    }

    suspend fun addInstalledModule(path: String) {
        context.dataStore.edit { prefs ->
            val cur = prefs[Keys.INSTALLED_MODULES] ?: emptySet()
            prefs[Keys.INSTALLED_MODULES] = cur + path
        }
    }

    suspend fun removeInstalledModule(path: String) {
        context.dataStore.edit { prefs ->
            val cur = prefs[Keys.INSTALLED_MODULES] ?: emptySet()
            val active = prefs[Keys.ACTIVE_MODULES] ?: emptySet()
            prefs[Keys.INSTALLED_MODULES] = cur - path
            prefs[Keys.ACTIVE_MODULES] = active - path
        }
    }
}
