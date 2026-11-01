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
import kotlinx.coroutines.launch

@Composable
fun ExperimentalScreen(
    privilegedActions: PrivilegedActions,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    
    var artCompilerEnabled by remember { mutableStateOf(false) }
    var gameDriverEnabled by remember { mutableStateOf(false) }
    var selectedDriver by remember { mutableStateOf("Default") }
    
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Experimental",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color(0xFFFF9500),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Funciones avanzadas. Úsalas con precaución.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF8E8E93)
                )
            }
        }
        
        item {
            GlassCard {
                Row {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFFF9500),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Zona de Riesgo",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFFFF9500),
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Estos ajustes pueden mejorar el rendimiento pero causar inestabilidad. Actívalos uno por uno.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF8E8E93)
                        )
                    }
                }
            }
        }
        
        item {
            IOSSwitchTile(
                title = "Compilador ART Forzado",
                subtitle = "Fuerza la compilación Ahead-Of-Time para apps específicas",
                checked = artCompilerEnabled,
                onCheckedChange = { enabled -> artCompilerEnabled = enabled },
                warningText = "El proceso puede tardar varios minutos"
            )
        }
        
        item {
            IOSSwitchTile(
                title = "Game Driver Personalizado",
                subtitle = "Usa drivers gráficos optimizados para juegos en lugar de los del sistema",
                checked = gameDriverEnabled,
                onCheckedChange = { enabled -> gameDriverEnabled = enabled }
            )
        }
        if (gameDriverEnabled) {
            item {
                GlassCard {
                    Column {
                        Text(
                            text = "Seleccionar Driver",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
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
                                            Color(0xFF007AFF).copy(alpha = 0.2f) 
                                        else Color.Transparent
                                    )
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedDriver == driver,
                                    onClick = { selectedDriver = driver },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = Color(0xFF007AFF)
                                    )
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = driver,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (selectedDriver == driver) Color.White else Color(0xFF8E8E93)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            }
        }
        
        item {
            Button(
                onClick = { scope.launch { /* Aplicar todo */ } },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF9500)
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
        
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
