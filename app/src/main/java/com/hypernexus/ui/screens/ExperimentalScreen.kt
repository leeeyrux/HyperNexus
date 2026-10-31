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
fun ExperimentalScreen(
    privilegedActions: PrivilegedActions,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    
    // Estados
    var artCompilerEnabled by remember { mutableStateOf(false) }
    var gameDriverEnabled by remember { mutableStateOf(false) }
    var selectedDriver by remember { mutableStateOf("Default") }
    
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundBlack)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header con advertencia
        item {
            Column {
                Text(
                    text = "Experimental",
                    style = MaterialTheme.typography.headlineLarge,
                    color = WarningOrange,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Funciones avanzadas. Úsalas con precaución.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }
        
        // Advertencia destacada
        item {
            GlassCard {
                Row {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = WarningOrange,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Zona de Riesgo",
                            style = MaterialTheme.typography.titleMedium,
                            color = WarningOrange,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Estos ajustes pueden mejorar el rendimiento pero también causar inestabilidad. Actívalos uno por uno y prueba antes de usarlos en juegos importantes.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
        
        // ART Compiler
        item {
            IOSSwitchTile(
                title = "Compilador ART Forzado",
                subtitle = "Fuerza la compilación Ahead-Of-Time para apps específicas",
                checked = artCompilerEnabled,
                onCheckedChange = { enabled ->
                    artCompilerEnabled = enabled
                    // TODO: Implementar compilación por app
                },
                warningText = "El proceso puede tardar varios minutos"
            )
        }
        
        // Game Driver
        item {
            IOSSwitchTile(
                title = "Game Driver Personalizado",
                subtitle = "Usa drivers gráficos optimizados para juegos en lugar de los del sistema",
                checked = gameDriverEnabled,
                onCheckedChange = { enabled ->
                    gameDriverEnabled = enabled
                }
            )
        }
        
        // Selector de Driver (si está habilitado)
        if (gameDriverEnabled) {
            item {
                GlassCard {
                    Column {
                        Text(
                            text = "Seleccionar Driver",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        val drivers = listOf("Default", "Vulkan", "OpenGL", "ANGLE")
                        
                        drivers.forEach { driver ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (selectedDriver == driver) 
                                            AccentBlue.copy(alpha = 0.2f) 
                                        else 
                                            Color.Transparent
                                    )
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedDriver == driver,
                                    onClick = { selectedDriver = driver },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = AccentBlue
                                    )
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = driver,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (selectedDriver == driver) TextPrimary else TextSecondary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            }
        }
        
        // Botón aplicar experimental
        item {
            Button(
                onClick = {
                    scope.launch {
                        // Aplicar todos los experimentales
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = WarningOrange
                )
            ) {
                Icon(Icons.Default.Science, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Aplicar Ajustes Experimentales",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        
        // Espacio final
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}