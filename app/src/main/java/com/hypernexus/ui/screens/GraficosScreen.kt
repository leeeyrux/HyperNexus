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
import com.hypernexus.services.RedMagicOverlayService
import com.hypernexus.ui.HyperNexusViewModel
import com.hypernexus.ui.components.GlassCard
import com.hypernexus.ui.components.IOSSwitchTile
import com.hypernexus.ui.components.StaggeredEntranceCard

@Composable
fun GraficosScreen(
    viewModel: HyperNexusViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

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
            StaggeredEntranceCard(index = 0) {
                Column {
                    Text("Gráficos & Renderizado", style = MaterialTheme.typography.headlineLarge, color = Color.White, fontWeight = FontWeight.Bold)
                    Text("Controladores de bajo nivel Magic Tuner & Vexiro Engine", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF8E8E93))
                }
            }
        }

        item(key = "graf_hud_switch", contentType = "premium_card_node") {
            StaggeredEntranceCard(index = 1) {
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
            StaggeredEntranceCard(index = 2) {
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
            StaggeredEntranceCard(index = 3) {
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
            StaggeredEntranceCard(index = 4) {
                IOSSwitchTile(
                    title = "Modo de Gráficos Fluidos",
                    subtitle = "Optimización profunda de sincronización y buffer SurfaceFlinger",
                    checked = uiState.fluidGraphics,
                    onCheckedChange = { viewModel.toggleFluidGraphics(it) }
                )
            }
        }

        item(key = "graf_sf_accel", contentType = "premium_card_node") {
            StaggeredEntranceCard(index = 5) {
                IOSSwitchTile(
                    title = "Aceleración por Hardware de SF",
                    subtitle = "Prioridad absoluta de hardware para la composición de pantalla",
                    checked = uiState.sfHardwareAccel,
                    onCheckedChange = { viewModel.toggleSfHardwareAccel(it) }
                )
            }
        }

        item(key = "graf_pacing", contentType = "premium_card_node") {
            StaggeredEntranceCard(index = 6) {
                IOSSwitchTile(
                    title = "Control de Intervalo de GPU",
                    subtitle = "Alineación de cadencia de frames en microsegundos",
                    checked = uiState.gpuPacingControl,
                    onCheckedChange = { viewModel.toggleGpuPacing(it) }
                )
            }
        }

        item(key = "graf_thermal", contentType = "premium_card_node") {
            StaggeredEntranceCard(index = 7) {
                IOSSwitchTile(
                    title = "Thermal Controller (Vexiro Engine)",
                    subtitle = "Suprime el límite térmico que reduce el rendimiento en juegos",
                    checked = uiState.thermalController,
                    onCheckedChange = { viewModel.toggleThermalController(it) }
                )
            }
        }

        item(key = "graf_touch", contentType = "premium_card_node") {
            StaggeredEntranceCard(index = 8) {
                IOSSwitchTile(
                    title = "Touchscreen Optimized",
                    subtitle = "Perfil de baja latencia táctil para máxima sensibilidad al deslizar",
                    checked = uiState.touchscreenOptimized,
                    onCheckedChange = { viewModel.toggleTouchscreenOptimized(it) }
                )
            }
        }

        item(key = "graf_booster_skia", contentType = "premium_card_node") {
            StaggeredEntranceCard(index = 9) {
                IOSSwitchTile(
                    title = "Booster Render (SkiaGL)",
                    subtitle = "Refuerzo dedicado para el pipeline OpenGL 2D/3D del sistema",
                    checked = uiState.boosterSkiaGl,
                    onCheckedChange = { viewModel.toggleBoosterSkiaGl(it) }
                )
            }
        }

        item(key = "graf_booster_vk", contentType = "premium_card_node") {
            StaggeredEntranceCard(index = 10) {
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
