package com.hypernexus.ui.screens

import android.content.res.Resources
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Bolt
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
import com.hypernexus.ui.components.IOSSliderTile
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

    // Sliders con estado local desacoplado (Fluidez total, persistencia al soltar)
    val safeDpi = uiState.displayDpi.toFloat().coerceIn(320f, 500f)
    var localDpi by remember(uiState.displayDpi) { mutableStateOf(safeDpi) }

    val safeAnim = uiState.animationScale.coerceIn(0f, 2.0f)
    var localAnim by remember(uiState.animationScale) { mutableStateOf(safeAnim) }

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
            Text("Ajustes protegidos contra bloqueos de pantalla", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF8E8E93))
        }

        // SLIDER DPI: Rango 320..500 con pasos de 15
        item {
            IOSSliderTile(
                title = "Densidad de Pantalla (DPI Seguro)",
                subtitle = "Intervalos de 15 DPI (320 - 500). Seguro contra soft-bricks.",
                value = localDpi,
                onValueChange = {
                    val stepIndex = ((it - 320f) / 15f).roundToInt()
                    localDpi = (320f + stepIndex * 15f).coerceIn(320f, 500f)
                },
                onValueChangeFinished = {
                    val finalDpi = localDpi.roundToInt()
                    viewModel.setDisplayDpi(finalDpi)
                    Toast.makeText(context, "DPI Aplicado: $finalDpi", Toast.LENGTH_SHORT).show()
                },
                valueRange = 320f..500f,
                steps = 11,
                formatValue = { "${it.roundToInt()} DPI" }
            )
        }

        // SLIDER ANIMACIONES: 0x, 0.5x, 1x, 1.5x, 2x
        item {
            IOSSliderTile(
                title = "Escala de Animaciones",
                subtitle = "Multiplicadores del sistema (0x, 0.5x, 1x, 1.5x, 2x)",
                value = localAnim,
                onValueChange = {
                    val stepIndex = (it / 0.5f).roundToInt()
                    localAnim = (stepIndex * 0.5f).coerceIn(0f, 2.0f)
                },
                onValueChangeFinished = {
                    val finalScale = (localAnim * 2).roundToInt() / 2.0f
                    viewModel.setAnimationScale(finalScale)
                    Toast.makeText(context, "Animaciones a ${finalScale}x", Toast.LENGTH_SHORT).show()
                },
                valueRange = 0.0f..2.0f,
                steps = 3,
                formatValue = {
                    val rounded = (it * 2).roundToInt() / 2.0f
                    if (rounded == 0f) "0x (Apagadas)" else "${rounded}x"
                }
            )
        }

        // ESCALADOR DE RESOLUCIÓN CON BLOQUEO ANTI-CURIOSO
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
                        text = "Reduce la resolución para duplicar FPS en juegos pesados manteniendo el formato nativo.",
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
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

        item {
            IOSSwitchTile(
                title = "Compilación AOT Instantánea",
                subtitle = "Reorganiza el código de las aplicaciones para acelerar arranques",
                checked = uiState.dex2oatEnabled,
                onCheckedChange = { viewModel.toggleDex2oat(it) }
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
                subtitle = "Desactiva los logs del sistema para liberar ciclos del CPU",
                checked = uiState.silentLogging,
                onCheckedChange = { viewModel.toggleSilentLogging(it) }
            )
        }

        item {
            Button(
                onClick = {
                    scope.launch {
                        viewModel.toggleDex2oat(true)
                        viewModel.toggleHighPerformance(true)
                        viewModel.toggleFastTouch(true)
                        viewModel.toggleSilentLogging(true)
                        viewModel.setAnimationScale(0.0f)
                        Toast.makeText(context, "Máximo Rendimiento (0x) Aplicado", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF30D158))
            ) {
                Icon(Icons.Default.Bolt, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Activar Perfil Extremo (Animaciones 0x)", fontWeight = FontWeight.Bold)
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
