package com.hypernexus.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hypernexus.PrivilegedActions
import com.hypernexus.ui.components.GlassCard
import kotlinx.coroutines.launch

@Composable
fun ExperimentalScreen(
    privilegedActions: PrivilegedActions,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var appList by remember { mutableStateOf<List<PrivilegedActions.InstalledApp>>(emptyList()) }
    var selectedApp by remember { mutableStateOf<PrivilegedActions.InstalledApp?>(null) }
    var isCompiling by remember { mutableStateOf(false) }
    var gameFps by remember { mutableStateOf(120) }
    var downscale80 by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        appList = privilegedActions.getInstalledApps(context)
        if (appList.isNotEmpty() && selectedApp == null) {
            selectedApp = appList.firstOrNull { it.isGame } ?: appList.first()
        }
    }
    
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
                    text = "Game Tuner & DEX2OAT",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color(0xFFFF9500),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Motor de optimización por aplicación estilo Magic Tuner",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF8E8E93)
                )
            }
        }

        // SELECTOR DE APP / JUEGO
        item {
            GlassCard {
                Column {
                    Text("Selecciona una App o Juego", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    var expanded by remember { mutableStateOf(false) }
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF2C2C2E))
                            .clickable { expanded = !expanded }
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedApp?.appName ?: "Cargando apps...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                            Icon(Icons.Default.ArrowDropDown, null, tint = Color.White)
                        }
                    }
                    
                    if (expanded && appList.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 220.dp)
                        ) {
                            appList.forEach { app ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selectedApp == app) Color(0xFF007AFF).copy(alpha = 0.2f) else Color.Transparent)
                                        .clickable {
                                            selectedApp = app
                                            expanded = false
                                        }
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${if (app.isGame) "🎮 " else "📱 "}${app.appName}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (selectedApp == app) Color(0xFF007AFF) else Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ACCIÓN 1: COMPILADOR DEX2OAT EXCLUSIVO
        item {
            GlassCard {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Bolt, null, tint = Color(0xFF30D158), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Compilación DEX2OAT Speed", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Convierte el código de la app seleccionada a código binario nativo AOT. Abre más rápido y reduce tirones en partidas.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF8E8E93)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    Button(
                        onClick = {
                            selectedApp?.let { app ->
                                scope.launch {
                                    isCompiling = true
                                    privilegedActions.compileAppSpeed(app.packageName)
                                    isCompiling = false
                                    Toast.makeText(context, "DEX2OAT completado para ${app.appName}", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF30D158)),
                        enabled = !isCompiling && selectedApp != null
                    ) {
                        if (isCompiling) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Compilando código AOT...")
                        } else {
                            Text("Compilar ${selectedApp?.appName ?: "App"}", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // ACCIÓN 2: GAME MANAGER API TUNER
        item {
            GlassCard {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Gamepad, null, tint = Color(0xFFFF9500), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Game Manager API Tuner", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Aplica modos de rendimiento por hardware a nivel de sistema para el paquete seleccionado.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF8E8E93)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text("Tasa de FPS en este Juego:", style = MaterialTheme.typography.labelMedium, color = Color.White)
                    Spacer(modifier = Modifier.height(6.dp))
                    val fpsOptions = listOf(60, 90, 120, 144)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        fpsOptions.forEach { fps ->
                            Button(
                                onClick = { gameFps = fps },
                                modifier = Modifier.weight(1f).height(38.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (gameFps == fps) Color(0xFFFF9500) else Color(0xFF2C2C2E)
                                )
                            ) {
                                Text("$fps", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = {
                            selectedApp?.let { app ->
                                scope.launch {
                                    val downscale = if (downscale80) 0.8f else 1.0f
                                    privilegedActions.applyGameMode(app.packageName, gameFps, downscale)
                                    Toast.makeText(context, "Game API inyectado a ${app.appName}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9500)),
                        enabled = selectedApp != null
                    ) {
                        Text("Inyectar Game API a ${selectedApp?.appName ?: "App"}", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}
