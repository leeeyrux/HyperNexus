package com.hypernexus.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hypernexus.PrivilegedActions
import com.hypernexus.services.FpsOverlayService
import com.hypernexus.ui.components.GlassCard
import com.hypernexus.ui.components.IOSSwitchTile
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(
    privilegedActions: PrivilegedActions,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var ramInfo by remember { mutableStateOf<PrivilegedActions.RamInfo?>(null) }
    var cpuCores by remember { mutableStateOf<List<PrivilegedActions.CpuCoreInfo>>(emptyList()) }
    var temperature by remember { mutableStateOf(0f) }
    var selectedHz by remember { mutableStateOf(0) }
    var fpsOverlayActive by remember { mutableStateOf(FpsOverlayService.isRunning) }
    
    LaunchedEffect(Unit) {
        while (true) {
            ramInfo = privilegedActions.getRamUsage()
            cpuCores = privilegedActions.getCpuFrequencies()
            temperature = privilegedActions.getTemperature()
            delay(2000)
        }
    }
    
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { DashboardHeader() }
        
        // CONTROLES DE ALTO IMPACTO EN PANEL
        item {
            GlassCard {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Speed, null, tint = Color(0xFF007AFF), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Tasa de Refresco (Hasta 144Hz)", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val rates = listOf(0 to "Auto", 60 to "60Hz", 90 to "90Hz", 120 to "120Hz", 144 to "144Hz")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        rates.forEach { (rate, label) ->
                            Button(
                                onClick = {
                                    selectedHz = rate
                                    scope.launch {
                                        privilegedActions.forceRefreshRate(rate)
                                        Toast.makeText(context, "Frecuencia forzada: $label", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f).height(38.dp),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(0.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedHz == rate) Color(0xFF007AFF) else Color(0xFF2C2C2E)
                                )
                            ) {
                                Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
        
        item {
            IOSSwitchTile(
                title = "Contador de FPS RedMagic",
                subtitle = "Píldora gamer flotante con lectura de FPS reales",
                checked = fpsOverlayActive,
                onCheckedChange = { enabled ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))
                        context.startActivity(intent)
                    } else {
                        fpsOverlayActive = enabled
                        val intent = Intent(context, FpsOverlayService::class.java)
                        if (enabled) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) context.startForegroundService(intent)
                            else context.startService(intent)
                        } else {
                            context.stopService(intent)
                        }
                    }
                }
            )
        }
        
        item { RamCard(ramInfo = ramInfo) }
        item { CpuCard(cpuCores = cpuCores) }
        item { TemperatureCard(temperature = temperature) }
        
        item {
            QuickActionsCard(
                onOptimizeClick = {
                    scope.launch {
                        privilegedActions.enableHighPerformance()
                        privilegedActions.enableDex2OatSpeed()
                        Toast.makeText(context, "Sistema Optimizado", Toast.LENGTH_SHORT).show()
                    }
                },
                onCleanRamClick = {
                    scope.launch {
                        privilegedActions.killBackgroundApps()
                        privilegedActions.clearSmartCache()
                        Toast.makeText(context, "RAM y Caché Liberados", Toast.LENGTH_SHORT).show()
                    }
                },
                onGameModeClick = {
                    scope.launch {
                        privilegedActions.enableGamingMode()
                        Toast.makeText(context, "¡Modo Gaming Extremo Activado!", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
fun DashboardHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "HyperNexus",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Panel de Control Principal",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF8E8E93)
            )
        }
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "HN",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }
    }
}

@Composable
fun RamCard(ramInfo: PrivilegedActions.RamInfo?) {
    GlassCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Memory, "RAM", tint = Color(0xFF007AFF), modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Memoria RAM", style = MaterialTheme.typography.titleMedium, color = Color.White)
                Spacer(modifier = Modifier.height(4.dp))
                if (ramInfo != null) {
                    val usedGb = ramInfo.usedKb / 1024 / 1024
                    val totalGb = ramInfo.totalKb / 1024 / 1024
                    Text("$usedGb GB / $totalGb GB", style = MaterialTheme.typography.bodyLarge, color = Color(0xFF8E8E93))
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = ramInfo.usagePercent / 100f,
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = when {
                            ramInfo.usagePercent > 90 -> Color(0xFFFF3B30)
                            ramInfo.usagePercent > 70 -> Color(0xFFFF9500)
                            else -> Color(0xFF30D158)
                        },
                        trackColor = Color(0xFF2C2C2E)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${ramInfo.usagePercent}% en uso", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8E8E93))
                } else {
                    Text("Cargando...", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF8E8E93))
                }
            }
        }
    }
}

@Composable
fun CpuCard(cpuCores: List<PrivilegedActions.CpuCoreInfo>) {
    GlassCard {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Speed, "CPU", tint = Color(0xFF30D158), modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Procesador", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    Text("${cpuCores.size} núcleos activos", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF8E8E93))
                }
            }
            if (cpuCores.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                cpuCores.chunked(2).forEach { rowCores ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowCores.forEach { core ->
                            Box(
                                modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(Color(0xFF2C2C2E)).padding(12.dp)
                            ) {
                                Column {
                                    Text("Core #${core.coreId}", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8E8E93))
                                    Text("${core.currentMhz} MHz", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                        if (rowCores.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun TemperatureCard(temperature: Float) {
    GlassCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.Thermostat,
                "Temp",
                tint = when {
                    temperature > 45 -> Color(0xFFFF3B30)
                    temperature > 38 -> Color(0xFFFF9500)
                    else -> Color(0xFF30D158)
                },
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Temperatura Térmica", style = MaterialTheme.typography.titleMedium, color = Color.White)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (temperature > 0) "${"%.1f".format(temperature)}°C" else "--°C",
                    style = MaterialTheme.typography.headlineLarge,
                    color = when {
                        temperature > 45 -> Color(0xFFFF3B30)
                        temperature > 38 -> Color(0xFFFF9500)
                        else -> Color.White
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun QuickActionsCard(
    onOptimizeClick: () -> Unit,
    onCleanRamClick: () -> Unit,
    onGameModeClick: () -> Unit
) {
    GlassCard {
        Column {
            Text("Acciones Rápidas", style = MaterialTheme.typography.titleMedium, color = Color.White)
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionButton(Icons.Default.Bolt, "Optimizar\nAhora", Color(0xFF30D158), onOptimizeClick, Modifier.weight(1f))
                QuickActionButton(Icons.Default.CleaningServices, "Limpiar\nRAM", Color(0xFF007AFF), onCleanRamClick, Modifier.weight(1f))
                QuickActionButton(Icons.Default.Gamepad, "Modo\nGaming", Color(0xFFFF9500), onGameModeClick, Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(72.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color.copy(alpha = 0.15f)),
        contentPadding = PaddingValues(4.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = color, lineHeight = 12.sp)
        }
    }
}
