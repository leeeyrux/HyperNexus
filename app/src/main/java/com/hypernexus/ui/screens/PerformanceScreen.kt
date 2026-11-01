package com.hypernexus.ui.screens
import kotlinx.coroutines.launch

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hypernexus.PrivilegedActions
import com.hypernexus.ui.components.*

@Composable
fun PerformanceScreen(
    privilegedActions: PrivilegedActions,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    
    var dex2oatEnabled by remember { mutableStateOf(false) }
    var highPerformanceEnabled by remember { mutableStateOf(false) }
    var fastTouchEnabled by remember { mutableStateOf(false) }
    var loggingDisabled by remember { mutableStateOf(false) }
    
    var dpiValue by remember { mutableStateOf(400f) }
    var animationScale by remember { mutableStateOf(1f) }
    var isApplying by remember { mutableStateOf(false) }
    
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Rendimiento",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Optimiza la velocidad de tu dispositivo",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF8E8E93)
            )
        }
        
        item {
            IOSSwitchTile(
                title = "Compilación Instantánea",
                subtitle = "Reorganiza el código interno de tus apps para que arranquen más rápido",
                checked = dex2oatEnabled,
                onCheckedChange = { enabled ->
                    dex2oatEnabled = enabled
                    scope.launch {
                        isApplying = true
                        if (enabled) privilegedActions.enableDex2OatSpeed()
                        isApplying = false
                    }
                },
                warningText = if (highPerformanceEnabled) "Puede tardar unos minutos la primera vez" else null
            )
        }
        
        item {
            IOSSwitchTile(
                title = "Modo Alto Rendimiento",
                subtitle = "Usa toda la RAM disponible sin restricciones para evitar cierres",
                checked = highPerformanceEnabled,
                onCheckedChange = { enabled ->
                    highPerformanceEnabled = enabled
                    scope.launch {
                        isApplying = true
                        if (enabled) privilegedActions.enableHighPerformance()
                        isApplying = false
                    }
                }
            )
        }
        
        item {
            IOSSwitchTile(
                title = "Respuesta Rápida",
                subtitle = "Mejora la velocidad con la que la pantalla detecta tus dedos al jugar",
                checked = fastTouchEnabled,
                onCheckedChange = { enabled ->
                    fastTouchEnabled = enabled
                    scope.launch {
                        isApplying = true
                        if (enabled) privilegedActions.enableFastTouch()
                        isApplying = false
                    }
                },
                warningText = "Puede aumentar ligeramente el consumo de batería"
            )
        }
        
        item {
            IOSSwitchTile(
                title = "Modo Silencioso",
                subtitle = "Silencia registros internos para reducir trabajo del procesador",
                checked = loggingDisabled,
                onCheckedChange = { enabled ->
                    loggingDisabled = enabled
                    scope.launch {
                        isApplying = true
                        if (enabled) privilegedActions.disableLogging()
                        isApplying = false
                    }
                }
            )
        }
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
                    containerColor = Color(0xFF30D158)
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
        
        item {
            GlassCard {
                Row {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFF007AFF),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "¿Qué hace cada ajuste?",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "• Compilación Instantánea: Optimiza el código de las apps\n" +
                                   "• Alto Rendimiento: Libera RAM y acelera el sistema\n" +
                                   "• Respuesta Rápida: Mejora la sensibilidad táctil\n" +
                                   "• Modo Silencioso: Reduce carga del procesador",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF8E8E93),
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
