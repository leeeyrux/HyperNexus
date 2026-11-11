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
    val glassEffectEnabled = uiState.glassEffectEnabled

    // ============================================================
    // UNIVERSAL SYNC LIFECYCLE ENGINE - Real-time external state sync
    // ============================================================
    LifecycleEventEffect(lifecycle, Lifecycle.Event.ON_RESUME) {
        viewModel.syncGraphicsState()
    }

    val renderers = listOf(
        "vulkan_booster" to "Vulkan Booster (Juegos Pesados)",
        "vulkan" to "Vulkan (Rendimiento)",
        "vulkan_lite" to "Vulkan Lite (Ahorro Batería)",
        "opengl" to "OpenGL (SkiaGL)",
        "opengl_lite" to "OpenGL Lite (Estable)"
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
                    Text("Gráficos", style = MaterialTheme.typography.headlineLarge, color = Color.White, fontWeight = FontWeight.Bold)
                    Text("Motor gráfico v1.8.0", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF8E8E93))
                }
            }
        }

        item(key = "graf_hud_switch", contentType = "premium_card_node") {
            CyberGlowEntranceWrapper(index = 1, itemKey = "graf_hud_switch") {
                IOSSwitchTile(
                    title = "HUD en Juego (FPS, Ping, Temp...)",
                    subtitle = "Flotante. Tap: solo FPS / barra completa [Hora, FPS, Ping, Temp, Batería]",
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
                GlassCard(glassEnabled = glassEffectEnabled) {
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Motor de Renderizado", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                            Text(uiState.graphicsRenderer ?: "Ninguno", color = Color(0xFF007AFF), fontSize = 12.sp)
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
                                        Toast.makeText(context, "Motor: $key", Toast.LENGTH_SHORT).show()
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
                GlassCard(glassEnabled = glassEffectEnabled) {
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Cómo Dibuja la Pantalla", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                            Text(uiState.compositionType ?: "Ninguna", color = Color(0xFF007AFF), fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            compositions.forEach { comp ->
                                val isSelected = uiState.compositionType.equals(comp, ignoreCase = true)
                                Button(
                                    onClick = {
                                        viewModel.setCompositionType(comp)
                                        Toast.makeText(context, "Modo: $comp", Toast.LENGTH_SHORT).show()
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
                    title = "Gráficos Fluidos",
                    subtitle = "Mejor sincronización, menos tirones",
                    checked = uiState.fluidGraphics,
                    onCheckedChange = { viewModel.toggleFluidGraphics(it) }
                )
            }
        }

        item(key = "graf_sf_accel", contentType = "premium_card_node") {
            CyberGlowEntranceWrapper(index = 5, itemKey = "graf_sf_accel") {
                IOSSwitchTile(
                    title = "Aceleración Hardware (SF)",
                    subtitle = "La GPU compone la pantalla, más rápido",
                    checked = uiState.sfHardwareAccel,
                    onCheckedChange = { viewModel.toggleSfHardwareAccel(it) }
                )
            }
        }

        item(key = "graf_pacing", contentType = "premium_card_node") {
            CyberGlowEntranceWrapper(index = 6, itemKey = "graf_pacing") {
                IOSSwitchTile(
                    title = "Cadencia de Frames (GPU)",
                    subtitle = "Sincroniza frames para suavidad",
                    checked = uiState.gpuPacingControl,
                    onCheckedChange = { viewModel.toggleGpuPacing(it) }
                )
            }
        }

        item(key = "graf_thermal", contentType = "premium_card_node") {
            CyberGlowEntranceWrapper(index = 7, itemKey = "graf_thermal") {
                IOSSwitchTile(
                    title = "Límite Térmico (Juegos)",
                    subtitle = "Evita que el móvil baje rendimiento por calor",
                    checked = uiState.thermalController,
                    onCheckedChange = { viewModel.toggleThermalController(it) }
                )
            }
        }

        item(key = "graf_touch", contentType = "premium_card_node") {
            CyberGlowEntranceWrapper(index = 8, itemKey = "graf_touch") {
                IOSSwitchTile(
                    title = "Táctil Optimizado",
                    subtitle = "Más respuesta al deslizar y tocar",
                    checked = uiState.touchscreenOptimized,
                    onCheckedChange = { viewModel.toggleTouchscreenOptimized(it) }
                )
            }
        }

        item(key = "graf_booster_skia", contentType = "premium_card_node") {
            CyberGlowEntranceWrapper(index = 9, itemKey = "graf_booster_skia") {
                IOSSwitchTile(
                    title = "Refuerzo OpenGL (SkiaGL)",
                    subtitle = "Acelera gráficos 2D/3D del sistema",
                    checked = uiState.boosterSkiaGl,
                    onCheckedChange = { viewModel.toggleBoosterSkiaGl(it) }
                )
            }
        }

        item(key = "graf_booster_vk", contentType = "premium_card_node") {
            CyberGlowEntranceWrapper(index = 10, itemKey = "graf_booster_vk") {
                IOSSwitchTile(
                    title = "Refuerzo Vulkan",
                    subtitle = "Más caché para Vulkan",
                    checked = uiState.boosterVulkan,
                    onCheckedChange = { viewModel.toggleBoosterVulkan(it) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}
