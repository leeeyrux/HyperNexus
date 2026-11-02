package com.hypernexus.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Gamepad
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
import com.hypernexus.domain.AppFilterManager
import com.hypernexus.domain.GameTargetApp
import com.hypernexus.ui.HyperNexusViewModel
import com.hypernexus.ui.components.GlassCard
import kotlinx.coroutines.launch

@Composable
fun ExperimentalScreen(
    viewModel: HyperNexusViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val appFilterManager = remember {
        AppFilterManager(context, viewModel.privilegedActions.bridge)
    }

    var appList by remember { mutableStateOf<List<GameTargetApp>>(emptyList()) }
    var selectedApp by remember { mutableStateOf<GameTargetApp?>(null) }
    var isCompiling by remember { mutableStateOf(false) }
    var gameFps by remember { mutableStateOf(120) }
    var downscale80 by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val apps = appFilterManager.getInstalledUserApplications()
        appList = apps
        if (apps.isNotEmpty() && selectedApp == null) {
            selectedApp = apps.firstOrNull { it.isGame } ?: apps.first()
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
                Text("Game Tuner & DEX2OAT", style = MaterialTheme.typography.headlineLarge, color = Color(0xFFFF9500), fontWeight = FontWeight.Bold)
                Text("Motor de optimización por aplicación con priorización gaming", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF8E8E93))
            }
        }

        item {
            GlassCard {
                Column {
                    Text("Selecciona una App o Juego (${appList.size} detectados)", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF2C2C2E))
                            .clickable { expanded = !expanded }
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                selectedApp?.icon?.let { bmp ->
                                    Image(
                                        bitmap = bmp,
                                        contentDescription = null,
                                        modifier = Modifier.size(34.dp).clip(RoundedCornerShape(8.dp))
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                }
                                Text(
                                    text = selectedApp?.appName ?: "Cargando aplicaciones...",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Icon(Icons.Default.ArrowDropDown, null, tint = Color.White)
                        }
                    }

                    if (expanded && appList.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth().heightIn(max = 280.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(
                                items = appList,
                                key = { it.packageName }
                            ) { app ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selectedApp?.packageName == app.packageName) Color(0xFF007AFF).copy(alpha = 0.2f) else Color.Transparent)
                                        .clickable {
                                            selectedApp = app
                                            expanded = false
                                        }
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (app.icon != null) {
                                        Image(
                                            bitmap = app.icon,
                                            contentDescription = null,
                                            modifier = Modifier.size(28.dp).clip(RoundedCornerShape(6.dp))
                                        )
                                    } else {
                                        Text(if (app.isGame) "🎮" else "📱", fontSize = 18.sp)
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = app.appName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (app.isGame) Color(0xFF30D158) else Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

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
                        text = "Convierte el código a binario nativo AOT para evitar tirones y recortar tiempos de carga.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF8E8E93)
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            selectedApp?.let { app ->
                                scope.launch {
                                    isCompiling = true
                                    viewModel.privilegedActions.compileAppSpeed(app.packageName)
                                    isCompiling = false
                                    Toast.makeText(context, "DEX2OAT finalizado: ${app.appName}", Toast.LENGTH_LONG).show()
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

        item {
            GlassCard {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Gamepad, null, tint = Color(0xFFFF9500), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Game Manager API Tuner", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Ajusta políticas por hardware en el paquete seleccionado.", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF8E8E93))
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("FPS Destino en Juego:", style = MaterialTheme.typography.labelMedium, color = Color.White)
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
                                    viewModel.privilegedActions.applyGameMode(app.packageName, gameFps, downscale)
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
