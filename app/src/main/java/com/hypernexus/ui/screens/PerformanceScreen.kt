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

@Composable
fun PerformanceScreen(
    viewModel: HyperNexusViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()

    var localDpi by remember(uiState.displayDpi) { mutableStateOf(uiState.displayDpi.toFloat()) }
    var localAnim by remember(uiState.animationScale) { mutableStateOf(uiState.animationScale) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Rendimiento", style = MaterialTheme.typography.headlineLarge, color = Color.White, fontWeight = FontWeight.Bold)
            Text("Ajuste milimétrico de respuesta y velocidad", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF8E8E93))
        }

        item {
            IOSSliderTile(
                title = "Densidad de Pantalla (DPI)",
                subtitle = "Desliza suavemente. Se aplica al soltar el dedo.",
                value = localDpi,
                onValueChange = { localDpi = it },
                onValueChangeFinished = {
                    viewModel.setDisplayDpi(localDpi.toInt())
                    Toast.makeText(context, "DPI: ${localDpi.toInt()}", Toast.LENGTH_SHORT).show()
                },
                valueRange = 320f..540f,
                steps = 11,
                formatValue = { "${it.toInt()} DPI" }
            )
        }

        item {
            IOSSliderTile(
                title = "Escala de Animaciones",
                subtitle = "Bájala a 0x para velocidad de respuesta instantánea.",
                value = localAnim,
                onValueChange = { localAnim = it },
                onValueChangeFinished = {
                    viewModel.setAnimationScale(localAnim)
                    Toast.makeText(context, "Animaciones: ${localAnim}x", Toast.LENGTH_SHORT).show()
                },
                valueRange = 0.0f..1.5f,
                steps = 6,
                formatValue = { if (it == 0f) "0x (Apagadas)" else "${"%.2f".format(it)}x" }
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
