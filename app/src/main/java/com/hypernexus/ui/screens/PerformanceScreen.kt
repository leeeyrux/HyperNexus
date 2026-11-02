package com.hypernexus.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hypernexus.ui.HyperNexusViewModel
import com.hypernexus.ui.components.IOSSliderTile
import com.hypernexus.ui.components.IOSSwitchTile
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun PerformanceScreen(
    viewModel: HyperNexusViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()

    // 1. Rango DPI Seguro: 320 a 500 en pasos exactos de 15
    val safeDpi = uiState.displayDpi.toFloat().coerceIn(320f, 500f)
    var localDpi by remember(uiState.displayDpi) { mutableStateOf(safeDpi) }

    // 2. Multiplicadores de Animación: 0x, 0.5x, 1x, 1.5x, 2x
    val safeAnim = uiState.animationScale.coerceIn(0f, 2.0f)
    var localAnim by remember(uiState.animationScale) { mutableStateOf(safeAnim) }

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

        // SLIDER DPI: Rango 320..500 con 11 steps intermedios = intervalos exactos de 15 DPI
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

        // SLIDER ANIMACIONES: Rango 0.0..2.0 con 3 steps = 0x, 0.5x, 1.0x, 1.5x, 2.0x
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
}
