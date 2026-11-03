package com.hypernexus.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hypernexus.R
import com.hypernexus.data.*
import com.hypernexus.ui.HyperNexusViewModel
import com.hypernexus.ui.components.CyberGlowEntranceWrapper
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
    val hardwareMonitor = remember { HardwareMonitor(context) }
    val specs = remember { hardwareMonitor.getStaticSpecs() }

    var liveMetrics by remember { mutableStateOf<LiveHardwareMetrics?>(null) }
    var batteryData by remember { mutableStateOf<BatteryTelemetry?>(null) }
    var networkData by remember { mutableStateOf<NetworkTelemetry?>(null) }

    LaunchedEffect(Unit) {
        while (true) {
            try {
                liveMetrics = hardwareMonitor.getLiveMetrics()
                batteryData = hardwareMonitor.getBatteryTelemetry()
                networkData = hardwareMonitor.getNetworkTelemetry()
            } catch (_: Throwable) {}
            delay(1500)
        }
    }

    fun openBrowserUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(context, "No se encontró navegador para abrir el enlace", Toast.LENGTH_SHORT).show()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item(key = "panel_header", contentType = "premium_card_node") {
            CyberGlowEntranceWrapper(index = 0, itemKey = "panel_header") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val logoPainter = runCatching { painterResource(id = R.mipmap.ic_launcher) }
                            .getOrElse { runCatching { painterResource(id = R.drawable.ic_launcher) }.getOrNull() }

                        if (logoPainter != null) {
                            Image(
                                painter = logoPainter,
                                contentDescription = "HyperNexus Logo",
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF007AFF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("HN", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("HyperNexus", style = MaterialTheme.typography.headlineLarge, color = Color.White, fontWeight = FontWeight.Bold)
                            Text("Hub de Telemetría v1.4.0", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF8E8E93))
                        }
                    }
                }
            }
        }

        item(key = "panel_social", contentType = "premium_card_node") {
            CyberGlowEntranceWrapper(index = 1, itemKey = "panel_social") {
                GlassCard {
                    Column {
                        Text("Canales Oficiales y Soporte", style = MaterialTheme.typography.labelMedium, color = Color(0xFF8E8E93))
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SocialBadge(label = "Instagram", icon = Icons.Default.CameraAlt, color = Color(0xFFE1306C), modifier = Modifier.weight(1f)) {
                                openBrowserUrl("https://www.instagram.com/leeeyru.btw?mdxt=dG9ib3Fpb21lcHZl")
                            }
                            SocialBadge(label = "TikTok", icon = Icons.Default.MusicNote, color = Color.White, modifier = Modifier.weight(1f)) {
                                openBrowserUrl("https://www.tiktok.com/@leeeyrux?_r=1&_t=ZS-9B55y4Oskpo")
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SocialBadge(label = "YouTube", icon = Icons.Default.PlayArrow, color = Color(0xFFFF003C), modifier = Modifier.weight(1f)) {
                                openBrowserUrl("https://youtube.com/@leeeyrux?si=IWXQrNuLM5Xasv2D")
                            }
                            SocialBadge(label = "Telegram", icon = Icons.Default.Send, color = Color(0xFF007AFF), modifier = Modifier.weight(1f)) {
                                openBrowserUrl("https://t.me/Leeeyrux_Nexus")
                            }
                        }
                    }
                }
            }
        }

        item(key = "panel_specs", contentType = "premium_card_node") {
            CyberGlowEntranceWrapper(index = 2, itemKey = "panel_specs") {
                DeviceSpecsCard(specs = specs)
            }
        }

        item(key = "panel_network", contentType = "premium_card_node") {
            CyberGlowEntranceWrapper(index = 3, itemKey = "panel_network") {
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
        }

        item(key = "panel_battery", contentType = "premium_card_node") {
            CyberGlowEntranceWrapper(index = 4, itemKey = "panel_battery") {
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
                        } ?: Text("Cargando sensores de energía...", color = Color(0xFF8E8E93), fontSize = 12.sp)
                    }
                }
            }
        }

        item(key = "panel_ram", contentType = "premium_card_node") {
            CyberGlowEntranceWrapper(index = 5, itemKey = "panel_ram") {
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
        }

        item(key = "panel_actions", contentType = "premium_card_node") {
            CyberGlowEntranceWrapper(index = 6, itemKey = "panel_actions") {
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
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun SocialBadge(label: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF2C2C2E))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun TelemetryField(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color(0xFF8E8E93))
        Text(value, style = MaterialTheme.typography.bodyMedium, color = Color.White, fontWeight = FontWeight.SemiBold)
    }
}
