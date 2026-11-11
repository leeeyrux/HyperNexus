package com.hypernexus.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventEffect
import androidx.lifecycle.LocalLifecycleOwner
import com.hypernexus.services.RedMagicOverlayService
import com.hypernexus.ui.HyperNexusViewModel
import com.hypernexus.ui.components.CyberGlowEntranceWrapper
import com.hypernexus.ui.components.GlassCard
import com.hypernexus.ui.components.IOSSwitchTile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun GraficosScreen(
    viewModel: HyperNexusViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()

    // ============================================================
    // UNIVERSAL SYNC LIFECYCLE ENGINE - Real-time external state sync
    // ============================================================
    LifecycleEventEffect(lifecycle, Lifecycle.Event.ON_RESUME) {
        scope.launch(Dispatchers.IO) {
            syncExternalGraphicsState(viewModel)
        }
    }

    val renderers = listOf(
        "vulkan_booster" to "Vulkan Booster (Star Feature para Juegos Pesados)",
        "vulkan" to "Vulkan (Alto Desempeño)",
        "vulkan_lite" to "Vulkan Lite (Bajo Consumo)",
        "opengl" to "OpenGL SkiaGL",
        "opengl_lite" to "OpenGL Lite (Estabilidad)"
    )

    val compositions = listOf("CPU", "GPU", "Dynamic")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item(key = "graf_header", contentType = "premium_card_node") {
            CyberGlowEntranceWrapper(index = 0, itemKey = "graf_header") {
                Column {
                    Text("Gráficos & Renderizado", style = MaterialTheme.typography.headlineLarge, color = Color.White, fontWeight = FontWeight.Bold)
                    Text("Controladores de bajo nivel Magic Tuner & Vexiro Engine v1.8.0", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF8E8E93))
                }
            }
        }

        item(key = "graf_hud_switch", contentType = "premium_card_node") {
            CyberGlowEntranceWrapper(index = 1, itemKey = "graf_hud_switch") {
                IOSSwitchTile(
                    title = "Contador Red Magic Multi-Info",
                    subtitle = "HUD flotante: Toca para alternar [FPS] o [Hora | FPS | Ping | Temp | Batería]",
                    checked = uiState.fpsOverlayActive,
                    onCheckedChange = { enabled ->
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))
                            context.startActivity(intent)
                        } else {
                            viewModel.setFpsOverlay(enabled)
                            val intent = Intent(context, RedMagicOverlayService::class.java)
                            if (enabled) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) context.startForegroundService(intent)
                                else context.startService(intent)
                            } else {
                                context.stopService(intent)
                            }
                        }
                    }
                )
            }
        }

        item(key = "graf_renderer_card", contentType = "premium_card_node") {
            CyberGlowEntranceWrapper(index = 2, itemKey = "graf_renderer_card") {
                GlassCard {
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Configurar Renderizador de GPU", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                            Text(uiState.graphicsRenderer ?: "Sin Seleccionar", color = Color(0xFF007AFF), fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        renderers.forEach { (key, label) ->
                            val isSelected = uiState.graphicsRenderer == key
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) Color(0xFF007AFF).copy(alpha = 0.15f) else Color(0xFF1C1C1E))
                                    .clickable {
                                        viewModel.setGraphicsRenderer(key)
                                        Toast.makeText(context, "Pipeline: $key", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { viewModel.setGraphicsRenderer(key) },
                                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF007AFF))
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isSelected) Color.White else Color(0xFF8E8E93),
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }

        item(key = "graf_composition_card", contentType = "premium_card_node") {
            CyberGlowEntranceWrapper(index = 3, itemKey = "graf_composition_card") {
                GlassCard {
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Composición de Pantalla", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                            Text(uiState.compositionType ?: "Sin Seleccionar", color = Color(0xFF007AFF), fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            compositions.forEach { comp ->
                                val isSelected = uiState.compositionType.equals(comp, ignoreCase = true)
                                Button(
                                    onClick = {
                                        viewModel.setCompositionType(comp)
                                        Toast.makeText(context, "Composición: $comp", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f).height(40.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSelected) Color(0xFF007AFF) else Color(0xFF2C2C2E)
                                    )
                                ) {
                                    Text(comp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                }
                            }
                        }
                    }
                }
            }
        }

        item(key = "graf_fluid_graphics", contentType = "premium_card_node") {
            CyberGlowEntranceWrapper(index = 4, itemKey = "graf_fluid_graphics") {
                IOSSwitchTile(
                    title = "Modo de Gráficos Fluidos",
                    subtitle = "Optimización profunda de sincronización y buffer SurfaceFlinger",
                    checked = uiState.fluidGraphics,
                    onCheckedChange = { viewModel.toggleFluidGraphics(it) }
                )
            }
        }

        item(key = "graf_sf_accel", contentType = "premium_card_node") {
            CyberGlowEntranceWrapper(index = 5, itemKey = "graf_sf_accel") {
                IOSSwitchTile(
                    title = "Aceleración por Hardware de SF",
                    subtitle = "Prioridad absoluta de hardware para la composición de pantalla",
                    checked = uiState.sfHardwareAccel,
                    onCheckedChange = { viewModel.toggleSfHardwareAccel(it) }
                )
            }
        }

        item(key = "graf_pacing", contentType = "premium_card_node") {
            CyberGlowEntranceWrapper(index = 6, itemKey = "graf_pacing") {
                IOSSwitchTile(
                    title = "Control de Intervalo de GPU",
                    subtitle = "Alineación de cadencia de frames en microsegundos",
                    checked = uiState.gpuPacingControl,
                    onCheckedChange = { viewModel.toggleGpuPacing(it) }
                )
            }
        }

        item(key = "graf_thermal", contentType = "premium_card_node") {
            CyberGlowEntranceWrapper(index = 7, itemKey = "graf_thermal") {
                IOSSwitchTile(
                    title = "Thermal Controller (Vexiro Engine)",
                    subtitle = "Suprime el límite térmico que reduce el rendimiento en juegos",
                    checked = uiState.thermalController,
                    onCheckedChange = { viewModel.toggleThermalController(it) }
                )
            }
        }

        item(key = "graf_touch", contentType = "premium_card_node") {
            CyberGlowEntranceWrapper(index = 8, itemKey = "graf_touch") {
                IOSSwitchTile(
                    title = "Touchscreen Optimized",
                    subtitle = "Perfil de baja latencia táctil para máxima sensibilidad al deslizar",
                    checked = uiState.touchscreenOptimized,
                    onCheckedChange = { viewModel.toggleTouchscreenOptimized(it) }
                )
            }
        }

        item(key = "graf_booster_skia", contentType = "premium_card_node") {
            CyberGlowEntranceWrapper(index = 9, itemKey = "graf_booster_skia") {
                IOSSwitchTile(
                    title = "Booster Render (SkiaGL)",
                    subtitle = "Refuerzo dedicado para el pipeline OpenGL 2D/3D del sistema",
                    checked = uiState.boosterSkiaGl,
                    onCheckedChange = { viewModel.toggleBoosterSkiaGl(it) }
                )
            }
        }

        item(key = "graf_booster_vk", contentType = "premium_card_node") {
            CyberGlowEntranceWrapper(index = 10, itemKey = "graf_booster_vk") {
                IOSSwitchTile(
                    title = "Booster Render (Vulkan)",
                    subtitle = "Refuerzo para Vulkan con asignación de caché ampliada",
                    checked = uiState.boosterVulkan,
                    onCheckedChange = { viewModel.toggleBoosterVulkan(it) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

// ============================================================
// UNIVERSAL SYNC ENGINE - Queries real-time graphics properties
// ============================================================
private suspend fun syncExternalGraphicsState(viewModel: HyperNexusViewModel) {
    val bridge = viewModel.privilegedActions.bridge
    return try {
        // Sync Fluid Graphics
        val fluidResult = bridge.executeShellCommand("getprop debug.sf.latch_unsignaled")
        if (fluidResult.first == 0) {
            val current = fluidResult.second.trim() == "1"
            if (current != viewModel.uiState.value.fluidGraphics) {
                viewModel.repository.updateFluidGraphics(current)
            }
        }

        // Sync SF Hardware Accel
        val sfResult = bridge.executeShellCommand("getprop debug.sf.hw")
        if (sfResult.first == 0) {
            val current = sfResult.second.trim() == "1"
            if (current != viewModel.uiState.value.sfHardwareAccel) {
                viewModel.repository.updateSfHardwareAccel(current)
            }
        }

        // Sync GPU Pacing
        val pacingResult = bridge.executeShellCommand("getprop debug.sf.early_phase_offset_ns")
        if (pacingResult.first == 0) {
            val current = pacingResult.second.trim().toLongOrNull() != 0L
            if (current != viewModel.uiState.value.gpuPacingControl) {
                viewModel.repository.updateGpuPacing(current)
            }
        }

        // Sync Thermal Controller
        val thermalResult = bridge.executeShellCommand("getprop debug.thermal.bypass")
        if (thermalResult.first == 0) {
            val current = thermalResult.second.trim() == "1"
            if (current != viewModel.uiState.value.thermalController) {
                viewModel.repository.updateThermalController(current)
            }
        }

        // Sync Touchscreen Optimized
        val touchResult = bridge.executeShellCommand("getprop persist.sys.Touchscreen.tap_response_time")
        if (touchResult.first == 0) {
            val current = touchResult.second.trim().toFloatOrNull() != null && touchResult.second.trim().toFloatOrNull()! < 0.01f
            if (current != viewModel.uiState.value.touchscreenOptimized) {
                viewModel.repository.updateTouchscreenOptimized(current)
            }
        }

        // Sync Booster SkiaGL
        val skiaResult = bridge.executeShellCommand("getprop debug.hwui.renderer")
        if (skiaResult.first == 0) {
            val current = skiaResult.second.trim().contains("skiagl", ignoreCase = true)
            if (current != viewModel.uiState.value.boosterSkiaGl) {
                viewModel.repository.updateBoosterSkiaGl(current)
            }
        }

        // Sync Booster Vulkan
        val vkResult = bridge.executeShellCommand("getprop debug.hwui.renderer")
        if (vkResult.first == 0) {
            val current = vkResult.second.trim().contains("skiavk", ignoreCase = true)
            if (current != viewModel.uiState.value.boosterVulkan) {
                viewModel.repository.updateBoosterVulkan(current)
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
            if (mapped != viewModel.uiState.value.compositionType) {
                viewModel.repository.updateCompositionType(mapped)
            }
        }

    } catch (e: Exception) {
        // Silent fail - sync is best effort
    }
}
