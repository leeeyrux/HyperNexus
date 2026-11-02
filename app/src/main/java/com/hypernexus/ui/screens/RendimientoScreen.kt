package com.hypernexus.ui.screens

import android.content.res.Resources
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
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
import com.hypernexus.ui.HyperNexusViewModel
import com.hypernexus.ui.components.GlassCard
import com.hypernexus.ui.components.IOSSwitchTile
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun RendimientoScreen(
    viewModel: HyperNexusViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()

    // Opciones discretas de pasos de 20 para DPI
    val dpiOptions = remember { listOf(320, 340, 360, 380, 400, 420, 440, 460, 480, 500) }

    // Multiplicadores discretos para animaciones
    val scaleOptions = remember { listOf(0.0f, 0.5f, 1.0f, 1.5f, 2.0f) }

    // Frecuencias de refresco reubicadas
    val refreshRates = remember { listOf(0 to "Auto", 60 to "60Hz", 90 to "90Hz", 120 to "120Hz", 144 to "144Hz") }

    // Escalador de Resolución
    val displayMetrics = Resources.getSystem().displayMetrics
    val nativeWidth = displayMetrics.widthPixels.coerceAtLeast(720)
    val nativeHeight = displayMetrics.heightPixels.coerceAtLeast(1280)
    val aspectRatio = nativeHeight.toFloat() / nativeWidth.toFloat()

    val resolutions = remember(nativeWidth, nativeHeight) {
        listOf(
            "Nativa (${nativeWidth}x${nativeHeight})" to (nativeWidth to nativeHeight),
            "1080p (${1080}x${(1080 * aspectRatio).roundToInt()})" to (1080 to (1080 * aspectRatio).roundToInt()),
            "720p (${720}x${(720 * aspectRatio).roundToInt()})" to (720 to (720 * aspectRatio).roundToInt()),
            "540p (${540}x${(540 * aspectRatio).roundToInt()})" to (540 to (540 * aspectRatio).roundToInt()),
            "Restablecer a Original (Reset)" to (0 to 0)
        )
    }

    var selectedResolutionTarget by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var showAntiCuriosoDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Rendimiento", style = MaterialTheme.typography.headlineLarge, color = Color.White, fontWeight = FontWeight.Bold)
            Text("Ajustes por pasos seguros sin deslizamiento errático", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF8E8E93))
        }

        // CONTROL DE HERZIOS REUBICADO DESDE PANEL
        item {
            GlassCard {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Speed, null, tint = Color(0xFF007AFF), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Forzar Tasa de Refresco Global", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        refreshRates.forEach { (rate, label) ->
                            val isSelected = uiState.refreshRate == rate
                            Button(
                                onClick = {
                                    viewModel.setRefreshRate(context, rate)
                                    Toast.makeText(context, "Frecuencia: $label", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f).height(38.dp),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(0.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) Color(0xFF007AFF) else Color(0xFF2C2C2E)
                                )
                            ) {
                                Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // 1. DENSIDAD DE PANTALLA (DPI) EN PASOS DE 20
        item {
            GlassCard {
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Densidad de Pantalla (DPI)", style = MaterialTheme.typography.titleMedium, color = Color.White)
                        Text("${uiState.displayDpi} DPI", color = Color(0xFF007AFF), fontWeight = FontWeight.Bold)
                    }
                    Text("Selecciona un valor predeterminado seguro", style = MaterialTheme.typography.bodySmall, color = Color(0xFF8E8E93))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        dpiOptions.forEach { dpi ->
                            val isSelected = uiState.displayDpi == dpi
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) Color(0xFF007AFF) else Color(0xFF2C2C2E))
                                    .clickable {
                                        viewModel.setDisplayDpi(dpi)
                                        Toast.makeText(context, "DPI: $dpi", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text("$dpi", color = Color.White, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }

        // 2. ESCALA DE TRANSICIÓN
        item {
            StepOptionCard(
                title = "Escala de Transición",
                currentValue = uiState.transitionScale,
                options = scaleOptions,
                onSelected = {
                    viewModel.setTransitionScale(it)
                    Toast.makeText(context, "Transición: ${formatScale(it)}", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // 3. ESCALA DE ANIMACIÓN / DURACIÓN
        item {
            StepOptionCard(
                title = "Escala de Animación / Duración",
                currentValue = uiState.animatorScale,
                options = scaleOptions,
                onSelected = {
                    viewModel.setAnimatorScale(it)
                    Toast.makeText(context, "Duración: ${formatScale(it)}", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // 4. ESCALA DE VENTANA
        item {
            StepOptionCard(
                title = "Escala de Ventana",
                currentValue = uiState.windowScale,
                options = scaleOptions,
                onSelected = {
                    viewModel.setWindowScale(it)
                    Toast.makeText(context, "Ventana: ${formatScale(it)}", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // ESCALADOR DE RESOLUCIÓN ANTI-CURIOSO
        item {
            GlassCard {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AspectRatio, null, tint = Color(0xFF007AFF), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Escalador de Resolución Dinámico", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Reduce la resolución manteniendo la relación de aspecto nativa para ganar FPS.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF8E8E93)
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    resolutions.forEach { (label, target) ->
                        Button(
                            onClick = {
                                selectedResolutionTarget = target
                                showAntiCuriosoDialog = true
                            },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (target.first == 0) Color(0xFF2C2C2E) else Color(0xFF007AFF).copy(alpha = 0.15f)
                            )
                        ) {
                            Text(label, color = if (target.first == 0) Color(0xFFFF9500) else Color.White, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // NUEVAS OPCIONES DE PERFIL DE BAJO NIVEL
        item {
            IOSSwitchTile(
                title = "Disable HW Overlays",
                subtitle = "Fuerza composición directa por GPU en SurfaceFlinger",
                checked = uiState.disableHwOverlays,
                onCheckedChange = { viewModel.toggleDisableHwOverlays(it) }
            )
        }

        item {
            IOSSwitchTile(
                title = "Enable Forced 4x MSAA",
                subtitle = "Inyección global de anti-aliasing OpenGL ES 2.0+",
                checked = uiState.msaa4x,
                onCheckedChange = { viewModel.toggleForced4xMsaa(it) }
            )
        }

        item {
            IOSSwitchTile(
                title = "Performance Governor Scheduler",
                subtitle = "Fuerza frecuencia máxima en todos los clústeres del CPU",
                checked = uiState.perfGovernor,
                onCheckedChange = { viewModel.togglePerfGovernor(it) }
            )
        }

        item {
            IOSSwitchTile(
                title = "Force High-Priority Dex2oat",
                subtitle = "Asigna 8 hilos dedicados para compilación de paquetes AOT",
                checked = uiState.highPriorityDex2oat,
                onCheckedChange = { viewModel.toggleHighPriorityDex2oat(it) }
            )
        }

        item {
            IOSSwitchTile(
                title = "Modo Alto Rendimiento",
                subtitle = "Elimina los límites de consumo de memoria RAM",
                checked = uiState.highPerformance,
                onCheckedChange = { viewModel.toggleHighPerformance(it) }
            )
        }

        item {
            IOSSwitchTile(
                title = "Respuesta Táctil Ultra Rápida",
                subtitle = "Reduce el tiempo de muestreo táctil a 0.02ms",
                checked = uiState.fastTouch,
                onCheckedChange = { viewModel.toggleFastTouch(it) }
            )
        }

        item {
            IOSSwitchTile(
                title = "Modo Silencioso de Registros",
                subtitle = "Desactiva logs del sistema para liberar ciclos de CPU",
                checked = uiState.silentLogging,
                onCheckedChange = { viewModel.toggleSilentLogging(it) }
            )
        }

        item {
            Button(
                onClick = {
                    scope.launch {
                        viewModel.setTransitionScale(0f)
                        viewModel.setAnimatorScale(0f)
                        viewModel.setWindowScale(0f)
                        viewModel.toggleHighPerformance(true)
                        viewModel.toggleFastTouch(true)
                        viewModel.toggleDisableHwOverlays(true)
                        Toast.makeText(context, "Perfil de Máxima Velocidad Aplicado", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF30D158))
            ) {
                Icon(Icons.Default.Bolt, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Activar Perfil Extremo (0x Todo)", fontWeight = FontWeight.Bold)
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }

    // DIÁLOGO CRIMSON ANTI-CURIOSO
    if (showAntiCuriosoDialog && selectedResolutionTarget != null) {
        AlertDialog(
            onDismissRequest = {
                showAntiCuriosoDialog = false
                selectedResolutionTarget = null
            },
            containerColor = Color(0xFF1A0A0D),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.border(2.dp, Color(0xFFFF003C), RoundedCornerShape(20.dp)),
            icon = {
                Icon(Icons.Default.Warning, null, tint = Color(0xFFFF003C), modifier = Modifier.size(36.dp))
            },
            title = {
                Text(
                    text = "🚨 ¡ADVERTENCIA GRAVE!",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFFFF003C),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Si no sabe a lo que le va a mover, mejor déjele ahí 'Che Jorge el Curioso' o se va a quedar sin teléfono. Esta es una herramienta avanzada; un mal valor puede corromper la pantalla o forzar un formateo de fábrica. El creador de la app NO se hace responsable de dispositivos dañados por mal uso. ¿Está absolutamente seguro de proceder?",
                    color = Color.White,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val (w, h) = selectedResolutionTarget!!
                        viewModel.applyScreenResolution(w, h)
                        Toast.makeText(context, if (w == 0) "Resolución restablecida" else "Resolución aplicada: ${w}x$h", Toast.LENGTH_LONG).show()
                        showAntiCuriosoDialog = false
                        selectedResolutionTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF003C))
                ) {
                    Text("Sí, asumo el riesgo", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showAntiCuriosoDialog = false
                        selectedResolutionTarget = null
                    },
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                ) {
                    Text("Mejor no (Salvar mi teléfono)", color = Color.White)
                }
            }
        )
    }
}

@Composable
private fun StepOptionCard(
    title: String,
    currentValue: Float,
    options: List<Float>,
    onSelected: (Float) -> Unit
) {
    GlassCard {
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = Color.White)
                Text(formatScale(currentValue), color = Color(0xFF007AFF), fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                options.forEach { opt ->
                    val isSelected = currentValue == opt
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) Color(0xFF007AFF) else Color(0xFF2C2C2E))
                            .clickable { onSelected(opt) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(formatScale(opt), color = Color.White, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

private fun formatScale(scale: Float): String = if (scale == 0f) "0x" else "${scale}x"
