package com.hypernexus.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hypernexus.R
import com.hypernexus.data.*
import com.hypernexus.ui.HyperNexusViewModel
import com.hypernexus.ui.components.DeviceSpecsCard
import com.hypernexus.ui.components.GlassCard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PanelScreen(
    viewModel: HyperNexusViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current
    val hardwareMonitor = remember { HardwareMonitor(context) }
    val specs = remember { hardwareMonitor.getStaticSpecs() }

    var liveMetrics by remember { mutableStateOf<LiveHardwareMetrics?>(null) }
    var batteryData by remember { mutableStateOf<BatteryTelemetry?>(null) }
    var networkData by remember { mutableStateOf<NetworkTelemetry?>(null) }

    LaunchedEffect(Unit) {
        while (true) {
            liveMetrics = hardwareMonitor.getLiveMetrics()
            batteryData = hardwareMonitor.getBatteryTelemetry()
            networkData = hardwareMonitor.getNetworkTelemetry()
            delay(1500)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. HEADER CON LOGO OFICIAL
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher),
                        contentDescription = "HyperNexus Logo",
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("HyperNexus", style = MaterialTheme.typography.headlineLarge, color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Hub de Telemetría Operativa", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF8E8E93))
                    }
                }
            }
        }

        // 2. DISCRETE SOCIAL MEDIA BRANDING CARD
        item {
            GlassCard {
                Column {
                    Text("Canales Oficiales del Desarrollador", style = MaterialTheme.typography.labelMedium, color = Color(0xFF8E8E93))
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SocialBadge(label = "Instagram", handle = "@leeeyru.btw", color = Color(0xFFE1306C)) {
                            uriHandler.openUri("https://www.instagram.com/leeeyru.btw?xtok=dG9ib3Fpb21lcHZl")
                        }
                        SocialBadge(label = "TikTok", handle = "@leeeyrux", color = Color.White) {
                            uriHandler.openUri("https://tiktok.com")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SocialBadge(label = "YouTube", handle = "@Leeeyrux", color = Color(0xFFFF003C)) {
                            uriHandler.openUri("https://youtube.com")
                        }
                        SocialBadge(label = "Telegram", handle = "@Leeeyrux_Nexus", color = Color(0xFF007AFF)) {
                            uriHandler.openUri("https://t.me")
                        }
                    }
                }
            }
        }

        // 3. SPECS GENERALES
        item { DeviceSpecsCard(specs = specs) }

        // 4. TELEMETRÍA DE RED EN VIVO
        item {
            GlassCard {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Wifi, null, tint = Color(0xFF007AFF), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Telemetría de Conectividad en Vivo", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Latencia Ping", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8E8E93))
                            val pingVal = networkData?.pingMs ?: -1
                            Text(
                                text = if (pingVal > 0) "$pingVal ms" else "-- ms",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (pingVal in 1..70) Color(0xFF30D158) else Color(0xFFFF9500),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column {
                            Text("Potencia de Señal", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8E8E93))
                            Text(networkData?.signalStrengthDbm ?: "- dBm", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("Tipo de Enlace", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8E8E93))
                            Text(networkData?.connectionType ?: "Verificando...", style = MaterialTheme.typography.titleMedium, color = Color(0xFF007AFF), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 5. TELEMETRÍA DE BATERÍA MASIVA
        item {
            GlassCard {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.BatteryChargingFull, null, tint = Color(0xFF30D158), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Telemetría Energética de Batería", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    batteryData?.let { b ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            TelemetryField(label = "Nivel / Estado", value = "${b.levelPercent}% | ${b.status}")
                            TelemetryField(label = "Salud de Celdas", value = b.health)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            TelemetryField(label = "Tensión Exacta", value = "${b.voltageMv} mV")
                            TelemetryField(label = "Amperaje Instantáneo", value = "${b.currentAmperageMa} mA")
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            TelemetryField(label = "Tecnología Química", value = b.technology)
                            TelemetryField(label = "Capacidad Máx.", value = "4,500 - 5,000 mAh")
                        }
                    } ?: Text("Cargando sensores de energía...", color = Color(0xFF8E8E93), fontSize = 12.sp)
                }
            }
        }

        // 6. MEMORIA RAM & FRECUENCIAS CPU
        item {
            GlassCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Memory, null, tint = Color(0xFF007AFF), modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Memoria RAM", style = MaterialTheme.typography.titleMedium, color = Color.White)
                        Spacer(modifier = Modifier.height(4.dp))
                        liveMetrics?.let { m ->
                            Text("${m.usedRamMb} MB / ${m.totalRamMb} MB", style = MaterialTheme.typography.bodyLarge, color = Color(0xFF8E8E93))
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = m.ramUsagePercent / 100f,
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                color = if (m.ramUsagePercent > 85) Color(0xFFFF3B30) else Color(0xFF30D158),
                                trackColor = Color(0xFF2C2C2E)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("${m.ramUsagePercent}% en uso", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8E8E93))
                        } ?: Text("Cargando memoria...", color = Color(0xFF8E8E93))
                    }
                }
            }
        }

        item {
            GlassCard {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Speed, null, tint = Color(0xFF30D158), modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Frecuencias del CPU", style = MaterialTheme.typography.titleMedium, color = Color.White)
                            Text("${liveMetrics?.cores?.size ?: 0} núcleos activos", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF8E8E93))
                        }
                    }
                    liveMetrics?.cores?.let { cores ->
                        Spacer(modifier = Modifier.height(14.dp))
                        cores.chunked(2).forEach { rowCores ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                rowCores.forEach { core ->
                                    Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).background(Color(0xFF2C2C2E)).padding(10.dp)) {
                                        Column {
                                            Text("Núcleo ${core.coreIndex}", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8E8E93))
                                            Text("${core.currentMhz} MHz", style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.Bold)
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

        // 7. FILA DE ACCIONES RÁPIDAS (EXCLUSIVAMENTE 3)
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        scope.launch {
                            viewModel.privilegedActions.killBackgroundApps()
                            viewModel.privilegedActions.clearSmartCache()
                            Toast.makeText(context, "RAM Liberada", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF))
                ) {
                    Text("Limpiar RAM", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        scope.launch {
                            viewModel.privilegedActions.enableGamingMode()
                            Toast.makeText(context, "Gaming Boost Inyectado", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9500))
                ) {
                    Text("Gaming Boost", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        viewModel.optimizePing { msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF30D158))
                ) {
                    Text("Optimizar Ping", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun SocialBadge(label: String, handle: String, color: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF2C2C2E))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("●", color = color, fontSize = 10.sp)
        Spacer(modifier = Modifier.width(6.dp))
        Text("$label: ", color = Color(0xFF8E8E93), fontSize = 11.sp)
        Text(handle, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun TelemetryField(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color(0xFF8E8E93))
        Text(value, style = MaterialTheme.typography.bodyMedium, color = Color.White, fontWeight = FontWeight.SemiBold)
    }
}
