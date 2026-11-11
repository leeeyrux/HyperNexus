package com.hypernexus.ui.screens

import android.os.Build
import android.provider.Settings
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.hypernexus.ui.HyperNexusViewModel
import com.hypernexus.ui.components.CyberGlowEntranceWrapper
import com.hypernexus.ui.components.GlassCard
import com.hypernexus.ui.components.IOSSwitchTile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

@Composable
fun RendimientoScreen(
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
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.syncSystemState()
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }

    val dpiOptions = remember { listOf(320, 340, 360, 380, 400, 420, 440, 460, 480, 500) }
    val scaleOptions = remember { listOf(0.0f, 0.5f, 1.0f, 1.5f, 2.0f) }
    val refreshRates = remember { listOf(0 to "Auto", 60 to "60Hz", 90 to "90Hz", 120 to "120Hz", 144 to "144Hz") }

    val safeAspectResolutions = remember {
        listOf(
            "Nativa (Restablecer)" to (0 to 0),
            "1080p (1080x2400)" to (1080 to 2400),
            "720p (720x1600 - Base)" to (720 to 1600),
            "540p (540x1200)" to (540 to 1200)
        )
    }

    var selectedResolutionTarget by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var showAntiCuriosoDialog by remember { mutableStateOf(false) }

    // Use persistent state from DataStore instead of volatile memory
    val inputLagBypassActive = uiState.inputLagBypass
    val glassEffectEnabled = uiState.glassEffectEnabled

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item(key = "rend_header", contentType = "premium_node") {
            CyberGlowEntranceWrapper(index = 0, itemKey = "rend_header") {
                Column {
                    Text("Rendimiento", style = MaterialTheme.typography.headlineLarge, color = Color.White, fontWeight = FontWeight.Bold)
                    Text("Ajustes por pasos seguros sin deslizamiento errático v1.8.0", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF8E8E93))
                }
            }
        }

        item(key = "rend_hz", contentType = "premium_node") {
            CyberGlowEntranceWrapper(index = 1, itemKey = "rend_hz") {
                GlassCard(glassEnabled = glassEffectEnabled) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Speed, null, tint = Color(0xFF007AFF), modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Hz de Pantalla", style = MaterialTheme.typography.titleMedium, color = Color.White)
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            refreshRates.forEach { (rate, label) ->
                                val isSelected = uiState.refreshRate == rate && uiState.refreshRate != 0
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
        }

        item(key = "rend_dpi", contentType = "premium_node") {
            CyberGlowEntranceWrapper(index = 2, itemKey = "rend_dpi") {
                GlassCard(glassEnabled = glassEffectEnabled) {
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Tamaño de Interfaz (DPI)", style = MaterialTheme.typography.titleMedium, color = Color.White)
                            Text(uiState.displayDpi?.let { "$it DPI" } ?: "-- DPI", color = Color(0xFF007AFF), fontWeight = FontWeight.Bold)
                        }
                        Text("Elige un valor seguro", style = MaterialTheme.typography.bodySmall, color = Color(0xFF8E8E93))
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
        }

        item(key = "rend_transition", contentType = "premium_node") {
            CyberGlowEntranceWrapper(index = 3, itemKey = "rend_transition") {
                StepOptionCard(
                    title = "Animación de Transición",
                    selectedValue = uiState.transitionScale,
                    options = scaleOptions,
                    onSelected = {
                        viewModel.setTransitionScale(it)
                        Toast.makeText(context, "Transición: ${formatScale(it)}", Toast.LENGTH_SHORT).show()
                    },
                    glassEnabled = glassEffectEnabled
                )
            }
        }

        item(key = "rend_animator", contentType = "premium_node") {
            CyberGlowEntranceWrapper(index = 4, itemKey = "rend_animator") {
                StepOptionCard(
                    title = "Animación General",
                    selectedValue = uiState.animatorScale,
                    options = scaleOptions,
                    onSelected = {
                        viewModel.setAnimatorScale(it)
                        Toast.makeText(context, "Duración: ${formatScale(it)}", Toast.LENGTH_SHORT).show()
                    },
                    glassEnabled = glassEffectEnabled
                )
            }
        }

        item(key = "rend_window", contentType = "premium_node") {
            CyberGlowEntranceWrapper(index = 5, itemKey = "rend_window") {
                StepOptionCard(
                    title = "Animación de Ventanas",
                    selectedValue = uiState.windowScale,
                    options = scaleOptions,
                    onSelected = {
                        viewModel.setWindowScale(it)
                        Toast.makeText(context, "Ventana: ${formatScale(it)}", Toast.LENGTH_SHORT).show()
                    },
                    glassEnabled = glassEffectEnabled
                )
            }
        }

        // TWEAK: FILTRO DE BAJA LATENCIA TÁCTIL (INPUT LAG BYPASS)
        item(key = "rend_input_lag", contentType = "premium_node") {
            CyberGlowEntranceWrapper(index = 6, itemKey = "rend_input_lag") {
                IOSSwitchTile(
                    title = "Respuesta Táctil Rápida",
                    subtitle = "Elimina el retraso al tocar la pantalla",
                    checked = inputLagBypassActive,
                    onCheckedChange = {
                        viewModel.toggleInputLagBypass(it)
                        Toast.makeText(context, if (it) "Activado" else "Desactivado", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        item(key = "rend_resolution", contentType = "premium_node") {
            CyberGlowEntranceWrapper(index = 7, itemKey = "rend_resolution") {
                GlassCard(glassEnabled = glassEffectEnabled) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AspectRatio, null, tint = Color(0xFF007AFF), modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Resolución de Pantalla", style = MaterialTheme.typography.titleMedium, color = Color.White)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Basado en 720x1600 para que no fallen botones ni gestos.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF8E8E93)
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        safeAspectResolutions.forEach { (label, target) ->
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
        }

        item(key = "rend_hw_overlays", contentType = "premium_node") {
            CyberGlowEntranceWrapper(index = 8, itemKey = "rend_hw_overlays") {
                IOSSwitchTile(
                    title = "GPU Directa (sin capas)",
                    subtitle = "La GPU dibuja directo, más fluidez",
                    checked = uiState.disableHwOverlays,
                    onCheckedChange = { viewModel.toggleDisableHwOverlays(it) }
                )
            }
        }

        item(key = "rend_msaa", contentType = "premium_node") {
            CyberGlowEntranceWrapper(index = 9, itemKey = "rend_msaa") {
                IOSSwitchTile(
                    title = "Suavizado 4x (bordes)",
                    subtitle = "Bordes lisos en todo el sistema",
                    checked = uiState.msaa4x,
                    onCheckedChange = { viewModel.toggleForced4xMsaa(it) }
                )
            }
        }

        item(key = "rend_governor", contentType = "premium_node") {
            CyberGlowEntranceWrapper(index = 10, itemKey = "rend_governor") {
                IOSSwitchTile(
                    title = "CPU al Máximo",
                    subtitle = "Todos los núcleos a tope siempre",
                    checked = uiState.perfGovernor,
                    onCheckedChange = { viewModel.togglePerfGovernor(it) }
                )
            }
        }

        // EFECTO GLASS EN TARJETAS (OPCIONAL - AHORRO GPU GAMA BAJA)
        item(key = "rend_glass_effect", contentType = "premium_node") {
            CyberGlowEntranceWrapper(index = 11, itemKey = "rend_glass_effect") {
                IOSSwitchTile(
                    title = "Efecto Cristal en Tarjetas",
                    subtitle = "Apágalo en móviles básicos para ganar FPS. Sin desenfoque = más velocidad.",
                    checked = glassEffectEnabled,
                    onCheckedChange = { viewModel.toggleGlassEffect(it) }
                )
            }
        }

        item(key = "rend_quick_profile", contentType = "premium_node") {
            CyberGlowEntranceWrapper(index = 12, itemKey = "rend_quick_profile") {
                Button(
                    onClick = {
                        scope.launch {
                            viewModel.setTransitionScale(0f)
                            viewModel.setAnimatorScale(0f)
                            viewModel.setWindowScale(0f)
                            viewModel.toggleHighPerformance(true)
                            viewModel.toggleFastTouch(true)
                            viewModel.toggleDisableHwOverlays(true)
                            Toast.makeText(context, "Modo Extremo Activado", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF30D158))
                ) {
                    Icon(Icons.Default.Bolt, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Modo Extremo (Todo Apagado / al Máximo)", fontWeight = FontWeight.Bold)
                }
            }
        }

        item { Spacer(modifier = Modifier.height(96.dp)) }
    }

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
    selectedValue: Float?,
    options: List<Float>,
    onSelected: (Float) -> Unit,
    glassEnabled: Boolean = true
) {
    GlassCard(glassEnabled = glassEnabled) {
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = Color.White)
                Text(selectedValue?.let { formatScale(it) } ?: "--", color = Color(0xFF007AFF), fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                options.forEach { opt ->
                    val isSelected = selectedValue == opt
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


