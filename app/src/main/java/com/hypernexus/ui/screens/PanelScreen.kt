package com.hypernexus.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.hypernexus.ui.components.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

data class ConflatedTelemetrySnapshot(
    val liveMetrics: LiveHardwareMetrics,
    val batteryData: BatteryTelemetry,
    val networkData: NetworkTelemetry
)

@Composable
fun PanelScreen(
    viewModel: HyperNexusViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val hardwareMonitor = remember { HardwareMonitor(context) }
    val specs = remember { hardwareMonitor.getStaticSpecs() }
    val hazeState = rememberHazeState()

    // 1. MOTOR DE TELEMETRÍA ASÍNCRONO CONFLADO A 500MS
    val telemetryFlow = remember {
        flow {
            while (true) {
                try {
                    val m = hardwareMonitor.getLiveMetrics()
                    val b = hardwareMonitor.getBatteryTelemetry()
                    val n = hardwareMonitor.getNetworkTelemetry()
                    emit(ConflatedTelemetrySnapshot(m, b, n))
                } catch (_: Throwable) {}
                delay(500)
            }
        }.flowOn(Dispatchers.Default).conflate()
    }

    val telemetrySnapshot by telemetryFlow.collectAsState(initial = null)

    // CACHÉ DE FORMATEO CON derivedStateOf (Zero-Lag Recomposition)
    val ramUsedMb by remember { derivedStateOf { telemetrySnapshot?.liveMetrics?.usedRamMb ?: 0L } }
    val ramTotalMb by remember { derivedStateOf { telemetrySnapshot?.liveMetrics?.totalRamMb ?: 4096L } }
    val ramPercent by remember { derivedStateOf { telemetrySnapshot?.liveMetrics?.ramUsagePercent ?: 0 } }
    val ramUsageString by remember { derivedStateOf { "$ramUsedMb MB / $ramTotalMb MB" } }
    val ramPercentString by remember { derivedStateOf { "$ramPercent% en uso" } }

    fun openBrowserUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(context, "No se encontró navegador", Toast.LENGTH_SHORT).show()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .haze(hazeState)
            .background(Color.Black)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item(key = "panel_header", contentType = "premium_node") {
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
                            Text("Hub de Telemetría v1.5.1", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF8E8E93))
                        }
                    }
                }
            }
        }

        item(key = "panel_social", contentType = "premium_node") {
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

        item(key = "panel_specs", contentType = "premium_node") {
            CyberGlowEntranceWrapper(index = 2, itemKey = "panel_specs") {
                DeviceSpecsCard(specs = specs)
            }
        }

        // ====================================================================
        // 2. THE PREMIUM OVAL LIQUID GLASS RAM CAPSULE (KIMI IA STYLE)
        // ====================================================================
        item(key = "panel_ram_capsule", contentType = "premium_node") {
            CyberGlowEntranceWrapper(index = 3, itemKey = "panel_ram_capsule") {
                val capsuleShape = RoundedCornerShape(26.dp)
                val hazeStyle = HazeStyle(
                    backgroundColor = Color(0x99121216),
                    tint = Color.Black.copy(alpha = 0.35f),
                    blurRadius = 45.dp
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                        .clip(capsuleShape)
                        .hazeChild(hazeState, capsuleShape, hazeStyle)
                        .background(hazeStyle.backgroundColor)
                        .border(
                            BorderStroke(
                                1.dp,
                                Brush.verticalGradient(
                                    listOf(Color(0x40FFFFFF), Color(0x14FFFFFF))
                                )
                            ),
                            capsuleShape
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF007AFF).copy(alpha = 0.18f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Memory,
                                    contentDescription = "RAM",
                                    tint = Color(0xFF007AFF),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Memoria RAM",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = ramUsageString,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF8E8E93)
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = ramPercentString,
                                style = MaterialTheme.typography.titleSmall,
                                color = if (ramPercent > 85) Color(0xFFFF3B30) else Color(0xFF30D158),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = (ramPercent / 100f).coerceIn(0f, 1f),
                                modifier = Modifier
                                    .width(76.dp)
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = if (ramPercent > 85) Color(0xFFFF3B30) else Color(0xFF30D158),
                                trackColor = Color(0xFF2C2C2E)
                            )
                        }
                    }
                }
            }
        }

        // TELEMETRÍA DE RED EN VIVO
        item(key = "panel_network", contentType = "premium_node") {
            CyberGlowEntranceWrapper(index = 4, itemKey = "panel_network") {
                GlassCard {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Wifi, null, tint = Color(0xFF007AFF), modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Telemetría de Conectividad en Vivo", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        val netData = telemetrySnapshot?.networkData
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Latencia Ping", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8E8E93))
                                val pingVal = netData?.pingMs ?: -1
                                Text(
                                    text = if (pingVal > 0) "$pingVal ms" else "-- ms",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (pingVal in 1..70) Color(0xFF30D158) else Color(0xFFFF9500),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column {
                                Text("Potencia de Señal", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8E8E93))
                                Text(netData?.signalStrengthDbm ?: "- dBm", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("Tipo de Enlace", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8E8E93))
                                Text(netData?.connectionType ?: "Verificando...", style = MaterialTheme.typography.titleMedium, color = Color(0xFF007AFF), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // TELEMETRÍA DE BATERÍA DETALLADA
        item(key = "panel_battery", contentType = "premium_node") {
            CyberGlowEntranceWrapper(index = 5, itemKey = "panel_battery") {
                GlassCard {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.BatteryChargingFull, null, tint = Color(0xFF30D158), modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Telemetría Energética de Batería", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        telemetrySnapshot?.batteryData?.let { b ->
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
        }

        // 8 NÚCLEOS DE CPU
        item(key = "panel_cpu", contentType = "premium_node") {
            CyberGlowEntranceWrapper(index = 6, itemKey = "panel_cpu") {
                GlassCard {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Speed, null, tint = Color(0xFF30D158), modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                val count = telemetrySnapshot?.liveMetrics?.cores?.size?.takeIf { it > 0 } ?: 8
                                Text(
                                    text = "Frecuencias del CPU - $count núcleos activos",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Lectura en tiempo real por clúster (0-7)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF8E8E93)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))

                        val cores = telemetrySnapshot?.liveMetrics?.cores.takeIf { !it.isNullOrEmpty() }
                            ?: (0..7).map { CoreFrequency(it, 0, 0) }

                        cores.chunked(2).forEach { rowCores ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                rowCores.forEach { core ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFF2C2C2E))
                                            .padding(10.dp)
                                    ) {
                                        Column {
                                            Text(
                                                text = "Core #${core.coreIndex}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFF8E8E93)
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            val mhzText = if (core.currentMhz > 0) "${core.currentMhz} MHz" else "En espera"
                                            Text(
                                                text = mhzText,
                                                style = MaterialTheme.typography.titleSmall,
                                                color = if (core.currentMhz > 0) Color.White else Color(0xFF636366),
                                                fontWeight = FontWeight.Bold
                                            )
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

        // ACCIONES RÁPIDAS
        item(key = "panel_actions", contentType = "premium_node") {
            CyberGlowEntranceWrapper(index = 7, itemKey = "panel_actions") {
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

        item { Spacer(modifier = Modifier.height(100.dp)) }
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
