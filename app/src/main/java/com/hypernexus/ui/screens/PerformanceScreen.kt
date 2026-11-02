package com.hypernexus.ui.screens

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hypernexus.PrivilegedActions
import com.hypernexus.ui.components.*
import kotlinx.coroutines.launch

@Composable
fun PerformanceScreen(
    privilegedActions: PrivilegedActions,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
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
                text = "Optimiza la velocidad y los tiempos de respuesta del sistema",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF8E8E93)
            )
        }
        
        item {
            IOSSwitchTile(
                title = "Compilación AOT Instantánea",
                subtitle = "Reorganiza el código de las aplicaciones para acelerar arranques",
                checked = dex2oatEnabled,
                onCheckedChange = { enabled ->
                    dex2oatEnabled = enabled
                    scope.launch {
                        isApplying = true
                        if (enabled) privilegedActions.enableDex2OatSpeed()
                        isApplying = false
                    }
                }
            )
        }
        
        item {
            IOSSwitchTile(
                title = "Modo Alto Rendimiento",
                subtitle = "Elimina los límites de consumo de memoria RAM en segundo plano",
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
                title = "Respuesta Táctil Ultra Rápida",
                subtitle = "Reduce el tiempo de muestreo táctil a 0.02ms para juegos",
                checked = fastTouchEnabled,
                onCheckedChange = { enabled ->
                    fastTouchEnabled = enabled
                    scope.launch {
                        isApplying = true
                        if (enabled) privilegedActions.enableFastTouch()
                        isApplying = false
                    }
                }
            )
        }
        
        item {
            IOSSwitchTile(
                title = "Modo Silencioso de Registros",
                subtitle = "Desactiva los logs del sistema para liberar ciclos de reloj del CPU",
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
                subtitle = "Ajusta la escala de la interfaz al instante",
                value = dpiValue,
                onValueChange = { 
                    dpiValue = it
                    scope.launch { privilegedActions.changeDisplayDensity(it.toInt()) }
                },
                valueRange = 320f..540f,
                steps = 10,
                formatValue = { "${it.toInt()} DPI" }
            )
        }
        
        item {
            IOSSliderTile(
                title = "Velocidad de Animaciones",
                subtitle = "Multiplicador de ventanas y transiciones del sistema",
                value = animationScale,
                onValueChange = { 
                    animationScale = it
                    scope.launch { privilegedActions.setAnimationScales(it) }
                },
                valueRange = 0.0f..1.5f,
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
                        privilegedActions.setAnimationScales(0.25f)
                        animationScale = 0.25f
                        isApplying = false
                        dex2oatEnabled = true
                        highPerformanceEnabled = true
                        fastTouchEnabled = true
                        loggingDisabled = true
                        Toast.makeText(context, "Perfil Máximo Rendimiento Aplicado", Toast.LENGTH_SHORT).show()
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
                    Text("Inyectando comandos...")
                } else {
                    Icon(Icons.Default.Bolt, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Activar Todo el Perfil de Rendimiento",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
        
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}
