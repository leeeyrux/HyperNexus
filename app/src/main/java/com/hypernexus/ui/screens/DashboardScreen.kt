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
import com.hypernexus.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(
    privilegedActions: PrivilegedActions,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    
    // Estados de monitoreo
    var ramInfo by remember { mutableStateOf<PrivilegedActions.RamInfo?>(null) }
    var cpuCores by remember { mutableStateOf<List<PrivilegedActions.CpuCoreInfo>>(emptyList()) }
    var temperature by remember { mutableStateOf(0f) }
    var isRefreshing by remember { mutableStateOf(false) }
    
    // Actualización en tiempo real
    LaunchedEffect(Unit) {
        while (true) {
            isRefreshing = true
            ramInfo = privilegedActions.getRamUsage()
            cpuCores = privilegedActions.getCpuFrequencies()
            temperature = privilegedActions.getTemperature()
            isRefreshing = false
            delay(2000) // Actualizar cada 2 segundos
        }
    }
    
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundBlack)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header con logo y estado
        item {
            DashboardHeader()
        }
        
        // Tarjeta de RAM
        item {
            RamCard(ramInfo = ramInfo)
        }
        
        // Tarjeta de CPU
        item {
            CpuCard(cpuCores = cpuCores)
        }
        
        // Tarjeta de Temperatura
        item {
            TemperatureCard(temperature = temperature)
        }
        
        // Accesos rápidos
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
        
        // Espacio al final
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
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
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Panel de Control",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
        
        // Logo HN estilo glass
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
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Memory,
                contentDescription = null,
                tint = AccentBlue,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Memoria RAM",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                if (ramInfo != null) {
                    val usedGb = ramInfo.usedKb / 1024 / 1024
                    val totalGb = ramInfo.totalKb / 1024 / 1024
                    
                    Text(
                        text = "$usedGb GB / $totalGb GB",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Barra de progreso estilo iOS
                    LinearProgressIndicator(
                        progress = ramInfo.usagePercent / 100f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = when {
                            ramInfo.usagePercent > 90 -> DangerRed
                            ramInfo.usagePercent > 70 -> WarningOrange
                            else -> SuccessGreen
                        },
                        trackColor = Color(0xFF2C2C2E)
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "${ramInfo.usagePercent}% en uso",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                } else {
                    Text(
                        text = "Cargando...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
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
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    tint = SuccessGreen,
                    modifier = Modifier.size(32.dp)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    Text(
                        text = "Procesador",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = "${cpuCores.size} núcleos activos",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
            
            if (cpuCores.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Grid de núcleos
                cpuCores.chunked(2).forEach { rowCores ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowCores.forEach { core ->
                            CpuCoreItem(
                                core = core,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        // Rellenar si es impar
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
                color = TextSecondary
            )
            Text(
                text = "${core.currentMhz} MHz",
                style = MaterialTheme.typography.titleMedium,
                color = if (core.currentMhz > 0) TextPrimary else TextSecondary,
                fontWeight = FontWeight.SemiBold
            )
            if (core.maxMhz > 0) {
                val usage = (core.currentMhz.toFloat() / core.maxMhz * 100).toInt()
                Text(
                    text = "$usage% uso",
                    style = MaterialTheme.typography.labelSmall,
                    color = AccentBlue
                )
            }
        }
    }
}

@Composable
fun TemperatureCard(temperature: Float) {
    GlassCard {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Thermostat,
                contentDescription = null,
                tint = when {
                    temperature > 45 -> DangerRed
                    temperature > 38 -> WarningOrange
                    else -> SuccessGreen
                },
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Temperatura",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = if (temperature > 0) "%.1f°C".format(temperature) else "--°C",
                    style = MaterialTheme.typography.headlineLarge,
                    color = when {
                        temperature > 45 -> DangerRed
                        temperature > 38 -> WarningOrange
                        else -> TextPrimary
                    },
                    fontWeight = FontWeight.Bold
                )
                
                if (temperature > 0) {
                    Text(
                        text = when {
                            temperature > 45 -> "⚠️ Temperatura alta, considera desactivar tweaks"
                            temperature > 38 -> "Temperatura moderada"
                            else -> "Temperatura óptima"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun QuickActionsCard(
    onOptimizeClick: () -> Unit
) {
    GlassCard {
        Column {
            Text(
                text = "Acciones Rápidas",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionButton(
                    icon = Icons.Default.Bolt,
                    label = "Optimizar\nAhora",
                    color = SuccessGreen,
                    onClick = onOptimizeClick,
                    modifier = Modifier.weight(1f)
                )
                
                QuickActionButton(
                    icon = Icons.Default.CleaningServices,
                    label = "Limpiar\nRAM",
                    color = AccentBlue,
                    onClick = { /* TODO */ },
                    modifier = Modifier.weight(1f)
                )
                
                QuickActionButton(
                    icon = Icons.Default.Gamepad,
                    label = "Modo\nGaming",
                    color = WarningOrange,
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
        contentPadding = PaddingValues(8.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                lineHeight = 14.sp
            )
        }
    }
}