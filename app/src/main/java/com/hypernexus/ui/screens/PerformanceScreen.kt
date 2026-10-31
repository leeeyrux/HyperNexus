package com.hypernexus.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hypernexus.PrivilegedActions
import com.hypernexus.ui.components.*
import com.hypernexus.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun PerformanceScreen(
    privilegedActions: PrivilegedActions,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    
    // Estados de los switches
    var dex2oatEnabled by remember { mutableStateOf(false) }
    var highPerformanceEnabled by remember { mutableStateOf(false) }
    var fastTouchEnabled by remember { mutableStateOf(false) }
    var loggingDisabled by remember { mutableStateOf(false) }
    
    // Estados de sliders
    var dpiValue by remember { mutableStateOf(400f) }
    var animationScale by remember { mutableStateOf(1f) }
    
    // Estado de carga
    var isApplying by remember { mutableStateOf(false) }
    
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundBlack)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Text(
                text = "Rendimiento",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Optimiza la velocidad de tu dispositivo",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
        
        // Compilación Instantánea (Dex2Oat)
        item {
            IOSSwitchTile(
                title = "Compilación Instantánea",
                subtitle = "Reorganiza el código interno de tus apps para que arranquen más rápido",
                checked = dex2oatEnabled,
                onCheckedChange = { enabled ->
                    dex2oatEnabled = enabled
                    scope.launch {
                        isApplying = true
                        if (enabled) {
                            privilegedActions.enableDex2OatSpeed()
                        }
                        isApplying = false
                    }
                },
                warningText = if (enabled) "Puede tardar unos minutos la primera vez" else null
            )
        }
        
        // Modo Alto Rendimiento
        item {
            IOSSwitchTile(
                title = "Modo Alto Rendimiento",
                subtitle = "Usa toda la RAM disponible sin restricciones para evitar cierres",
                checked = highPerformanceEnabled,
                onCheckedChange = { enabled ->
                    highPerformanceEnabled = enabled
                    scope.launch {
                        isApplying = true
                        if (enabled) {
                            privilegedActions.enableHighPerformance()
                        }
                        isApplying = false
                    }
                }
            )
        }
        
        // Respuesta Táctil
        item {
            IOSSwitchTile(
                title = "Respuesta Rápida",
                subtitle = "Mejora la velocidad con la que la pantalla detecta tus dedos al jugar",
                checked = fastTouchEnabled,
                onCheckedChange = { enabled ->
                    fastTouchEnabled = enabled
                    scope.launch {
                        isApplying = true
                        if (enabled) {
                            privilegedActions.enableFastTouch()
                        }
                        isApplying = false
                    }
                },
                warningText = "Puede aumentar ligeramente el consumo de batería"
            )
        }
        
        // Reducción de Logs
        item {
            IOSSwitchTile(
                title = "Modo Silencioso",
                subtitle = "Silencia registros internos para reducir trabajo del procesador",
                checked = loggingDisabled,
                onCheckedChange = { enabled ->
                    loggingDisabled = enabled
                    scope.launch {
                        isApplying = true
                        if (enabled) {
                            privilegedActions.disableLogging()
                        }
                        isApplying = false
                    }
                }
            )
        }
        
        // Slider de DPI
        item {
            IOSSliderTile(
                title = "Densidad de Pantalla (DPI)",
                subtitle = "Ajusta el tamaño de elementos en pantalla",
                value = dpiValue,
                onValueChange = { dpiValue = it },
                valueRange = 300f..480f,
                steps = 5,
                formatValue = { "${it.toInt()} DPI" }
            )
        }
        
        // Slider de Animaciones
        item {
            IOSSliderTile(
                title = "Velocidad de Animaciones",
                subtitle = "Reduce el tiempo de las animaciones para que las apps se abran al instante",
                value = animationScale,
                onValueChange = { animationScale = it },
                valueRange = 0.5f..2f,
                steps = 5,
                formatValue = { "${it}x" }
            )
        }
        
        // Botón Aplicar Todo
        item {
            Button(
                onClick = {
                    scope.launch {
                        isApplying = true
                        privilegedActions.enableDex2OatSpeed()
                        privilegedActions.enableHighPerformance()
                        privilegedActions.enableFastTouch()
                        privilegedActions.disableLogging()
                        isApplying = false
                        dex2oatEnabled = true
                        highPerformanceEnabled = true
                        fastTouchEnabled = true
                        loggingDisabled = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SuccessGreen
                ),
                enabled = !isApplying
            ) {
                if (isApplying) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Aplicando...")
                } else {
                    Icon(Icons.Default.Bolt, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Activar Modo Máximo Rendimiento",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
        
        // Info card
        item {
            GlassCard {
                Row {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = AccentBlue,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "¿Qué hace cada ajuste?",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "• Compilación Instantánea: Optimiza el código de las apps\n" +
                                   "• Alto Rendimiento: Libera RAM y acelera el sistema\n" +
                                   "• Respuesta Rápida: Mejora la sensibilidad táctil\n" +
                                   "• Modo Silencioso: Reduce carga del procesador",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
        
        // Espacio final
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}