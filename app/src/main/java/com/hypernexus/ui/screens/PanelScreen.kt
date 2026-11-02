package com.hypernexus.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hypernexus.R
import com.hypernexus.data.HardwareMonitor
import com.hypernexus.data.LiveHardwareMetrics
import com.hypernexus.services.RedMagicOverlayService
import com.hypernexus.ui.HyperNexusViewModel
import com.hypernexus.ui.components.DeviceSpecsCard
import com.hypernexus.ui.components.GlassCard
import com.hypernexus.ui.components.IOSSwitchTile
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PanelScreen(
    viewModel: HyperNexusViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()
    val hardwareMonitor = remember { HardwareMonitor(context) }
    val specs = remember { hardwareMonitor.getStaticSpecs() }

    var liveMetrics by remember { mutableStateOf<LiveHardwareMetrics?>(null) }

    LaunchedEffect(Unit) {
        while (true) {
            liveMetrics = hardwareMonitor.getLiveMetrics()
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
        // ENCABEZADO CON LOGO OFICIAL INLINE
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
                        Text(
                            text = "HyperNexus",
                            style = MaterialTheme.typography.headlineLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Panel de Rendimiento",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF8E8E93)
                        )
                    }
                }
            }
        }

        item { DeviceSpecsCard(specs = specs) }

        item {
            GlassCard {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Speed, null, tint = Color(0xFF007AFF), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Forzar Tasa de Refresco", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    val rates = listOf(0 to "Auto", 60 to "60Hz", 90 to "90Hz", 120 to "120Hz", 144 to "144Hz")
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        rates.forEach { (rate, label) ->
                            Button(
                                onClick = {
                                    viewModel.setRefreshRate(context, rate)
                                    Toast.makeText(context, "Frecuencia: $label", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f).height(38.dp),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(0.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (uiState.refreshRate == rate) Color(0xFF007AFF) else Color(0xFF2C2C2E)
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
                title = "Contador Red Magic Multi-Info",
                subtitle = "Toca el HUD para alternar: [Individual FPS] ⮂ [Hora | FPS | Ping | Temp | Batería]",
                checked = uiState.fpsOverlayActive,
                onCheckedChange = { enabled ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))
                        context.startActivity(intent)
                    } else {
                        viewModel.setFpsOverlay(enabled)
                        val intent = Intent(context, RedMagicOverlayService::class.java)
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

        item {
            GlassCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Thermostat, null, tint = Color(0xFFFF9500), modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Temperatura del Sistema", style = MaterialTheme.typography.titleMedium, color = Color.White)
                        Spacer(modifier = Modifier.height(4.dp))
                        val t = liveMetrics?.thermalTemp ?: 0f
                        Text(if (t > 0) "${"%.1f".format(t)}°C" else "--°C", style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = {
                        scope.launch {
                            viewModel.privilegedActions.killBackgroundApps()
                            viewModel.privilegedActions.clearSmartCache()
                            Toast.makeText(context, "RAM Liberada", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF))
                ) {
                    Icon(Icons.Default.CleaningServices, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Limpiar RAM")
                }

                Button(
                    onClick = {
                        scope.launch {
                            viewModel.privilegedActions.enableGamingMode()
                            Toast.makeText(context, "Modo Gaming Inyectado", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9500))
                ) {
                    Icon(Icons.Default.Bolt, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Gaming Boost")
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}
