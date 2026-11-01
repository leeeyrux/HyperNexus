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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hypernexus.PrivilegedActions
import com.hypernexus.ui.components.GlassCard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(
    privilegedActions: PrivilegedActions,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var ramInfo by remember { mutableStateOf<PrivilegedActions.RamInfo?>(null) }
    var cpuCores by remember { mutableStateOf<List<PrivilegedActions.CpuCoreInfo>>(emptyList()) }
    var temperature by remember { mutableStateOf(0f) }
    
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
        item { RamCard(ramInfo = ramInfo) }
        item { CpuCard(cpuCores = cpuCores) }
        item { TemperatureCard(temperature = temperature) }
        item {
            QuickActionsCard(
                onOptimizeClick = {
                    scope.launch {
                        privilegedActions.enableHighPerformance()
                        privilegedActions.enableDex2OatSpeed()
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
                text = "Panel de Control",
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
            Icon(
                imageVector = Icons.Default.Memory,
                contentDescription = "RAM",
                tint = Color(0xFF007AFF),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Memoria RAM",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (ramInfo != null) {
                    val usedGb = ramInfo.usedKb / 1024 / 1024
                    val totalGb = ramInfo.totalKb / 1024 / 1024
                    Text(
                        text = "$usedGb GB / $totalGb GB",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF8E8E93)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { ramInfo.usagePercent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = when {
                            ramInfo.usagePercent > 90 -> Color(0xFFFF3B30)
                            ramInfo.usagePercent > 70 -> Color(0xFFFF9500)
                            else -> Color(0xFF30D158)
                        },
                        trackColor = Color(0xFF2C2C2E)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${ramInfo.usagePercent}% en uso",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF8E8E93)
                    )
                } else {
                    Text(
                        text = "Cargando...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF8E8E93)
                    )
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
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = "CPU",
                    tint = Color(0xFF30D158),
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Procesador",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    Text(
                        text = "${cpuCores.size} núcleos activos",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF8E8E93)
                    )
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
                            CpuCoreItem(core = core, modifier = Modifier.weight(1f))
                        }
                        if (rowCores.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun CpuCoreItem(
    core: PrivilegedActions.CpuCoreInfo,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF2C2C2E))
            .padding(12.dp)
    ) {
        Column {
            Text(
                text = "Núcleo ${core.coreId}",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF8E8E93)
            )
            Text(
                text = "${core.currentMhz} MHz",
                style = MaterialTheme.typography.titleMedium,
                color = if (core.currentMhz > 0) Color.White else Color(0xFF8E8E93),
                fontWeight = FontWeight.SemiBold
            )
            if (core.maxMhz > 0) {
                val usage = (core.currentMhz.toFloat() / core.maxMhz * 100).toInt()
                Text(
                    text = "$usage% uso",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF007AFF)
                )
            }
        }
    }
}

@Composable
fun TemperatureCard(temperature: Float) {
    GlassCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Thermostat,
                contentDescription = "Temp",
                tint = when {
                    temperature > 45 -> Color(0xFFFF3B30)
                    temperature > 38 -> Color(0xFFFF9500)
                    else -> Color(0xFF30D158)
                },
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Temperatura",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
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
                if (temperature > 0) {
                    Text(
                        text = when {
                            temperature > 45 -> "⚠️ Temperatura alta, desactiva tweaks"
                            temperature > 38 -> "Temperatura moderada"
                            else -> "Temperatura óptima"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF8E8E93)
                    )
                }
            }
        }
    }
}

@Composable
fun QuickActionsCard(onOptimizeClick: () -> Unit) {
    GlassCard {
        Column {
            Text(
                text = "Acciones Rápidas",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionButton(
                    icon = Icons.Default.Bolt,
                    label = "Optimizar\nAhora",
                    color = Color(0xFF30D158),
                    onClick = onOptimizeClick,
                    modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                    icon = Icons.Default.CleaningServices,
                    label = "Limpiar\nRAM",
                    color = Color(0xFF007AFF),
                    onClick = { /* TODO */ },
                    modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                    icon = Icons.Default.Gamepad,
                    label = "Modo\nGaming",
                    color = Color(0xFFFF9500),
                    onClick = { /* TODO */ },
                    modifier = Modifier.weight(1f)
                )
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
        colors = ButtonDefaults.buttonColors(
            containerColor = color.copy(alpha = 0.15f)
        ),
        contentPadding = PaddingValues(4.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                lineHeight = 12.sp
            )
        }
    }
}
