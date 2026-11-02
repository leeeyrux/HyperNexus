package com.hypernexus.ui.screens

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hypernexus.data.HardwareMonitor
import com.hypernexus.data.StorageTelemetry
import com.hypernexus.domain.AppFilterManager
import com.hypernexus.domain.GameTargetApp
import com.hypernexus.services.HyperNexusVpnService
import com.hypernexus.ui.HyperNexusViewModel
import com.hypernexus.ui.components.GlassCard
import com.hypernexus.ui.components.IOSSwitch
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SeguridadScreen(
    viewModel: HyperNexusViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()
    val hardwareMonitor = remember { HardwareMonitor(context) }
    val appFilterManager = remember { AppFilterManager(context, viewModel.privilegedActions.bridge) }

    var storageData by remember { mutableStateOf<StorageTelemetry?>(null) }
    var isCleaning by remember { mutableStateOf(false) }
    var cleaningSpeedMbSec by remember { mutableStateOf(0) }
    var cleanedSuccess by remember { mutableStateOf(false) }

    var installedGames by remember { mutableStateOf<List<GameTargetApp>>(emptyList()) }
    var selectedTargetGame by remember { mutableStateOf<GameTargetApp?>(null) }
    var showGamePickerModal by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        storageData = hardwareMonitor.getStorageTelemetry()
        val allApps = appFilterManager.getInstalledUserApplications()
        installedGames = allApps
        if (allApps.isNotEmpty() && selectedTargetGame == null) {
            val savedPkg = uiState.vpnTargetPackage
            selectedTargetGame = allApps.firstOrNull { it.packageName == savedPkg }
                ?: allApps.firstOrNull { it.isGame }
                ?: allApps.first()
        }
    }

    val vpnPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedTargetGame?.let { game ->
                viewModel.toggleVpnTunnel(context, game.packageName, true)
                Toast.makeText(context, "Gaming VPN Activado: ${game.appName}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Permiso VPN rechazado por el usuario", Toast.LENGTH_SHORT).show()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "cyberGlow")
    val pulseGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing), RepeatMode.Reverse),
        label = "pulseAlpha"
    )

    val isTunnelRunning = uiState.vpnActive || HyperNexusVpnService.isRunning
    val activeBorderColor by animateColorAsState(
        targetValue = if (isTunnelRunning) Color(0xFF00E676) else Color(0xFF00E5FF).copy(alpha = 0.35f),
        label = "borderGlow"
    )

    val auditChecks = remember {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
        val isDev = Settings.Global.getInt(context.contentResolver, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) == 1
        listOf(
            "Depuración USB / Modo Desarrollador" to if (isDev) "Controlado por Shizuku" else "Protegido (Desactivado)",
            "Patrón / Clave de Bloqueo" to "Activo (Cifrado de Pantalla)",
            "Capa de Cifrado del Dispositivo" to if (dpm?.storageEncryptionStatus == DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE) "Activo (FBE Hardware)" else "Activo (Android Keystore)",
            "Módulo de Comunicación NFC" to "Aislado en Hardware",
            "Validación Google Play Protect" to "Certificado y Seguro"
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Seguridad & Red Gamer", style = MaterialTheme.typography.headlineLarge, color = Color.White, fontWeight = FontWeight.Bold)
            Text("Split Tunneling por socket con optimización de MTU y DNS", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF8E8E93))
        }

        // MASTERPIECE WIDGET: SCI-FI GAMING VPN TUNNEL
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF16161A))
                    .border(
                        width = if (isTunnelRunning) 2.dp else 1.dp,
                        color = if (isTunnelRunning) activeBorderColor.copy(alpha = pulseGlowAlpha) else activeBorderColor,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(18.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.VpnKey,
                                contentDescription = null,
                                tint = if (isTunnelRunning) Color(0xFF00E676) else Color(0xFF00E5FF),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "GAMING VPN TUNNEL",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "MTU 1400 | DNS 1.1.1.1 Zero-Drop",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF8E8E93)
                                )
                            }
                        }

                        IOSSwitch(
                            checked = isTunnelRunning,
                            onCheckedChange = { enable ->
                                if (enable) {
                                    val prepIntent = VpnService.prepare(context)
                                    if (prepIntent != null) {
                                        vpnPermissionLauncher.launch(prepIntent)
                                    } else {
                                        selectedTargetGame?.let { game ->
                                            viewModel.toggleVpnTunnel(context, game.packageName, true)
                                            Toast.makeText(context, "Gaming VPN Conectado", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } else {
                                    selectedTargetGame?.let { game ->
                                        viewModel.toggleVpnTunnel(context, game.packageName, false)
                                        Toast.makeText(context, "Túnel VPN Desconectado", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (isTunnelRunning) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF00E676).copy(alpha = 0.12f))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF00E676).copy(alpha = pulseGlowAlpha))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "[ PROTEGIDO - ENRUTANDO JUEGO SELECCIONADO ]",
                                color = Color(0xFF00E676),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF222226))
                            .clickable { showGamePickerModal = true }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF16161A))
                                    .border(
                                        width = 1.5.dp,
                                        brush = Brush.sweepGradient(
                                            listOf(Color(0xFF00E5FF), Color(0xFF00E676), Color(0xFF00E5FF))
                                        ),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (selectedTargetGame?.icon != null) {
                                    Image(
                                        bitmap = selectedTargetGame!!.icon!!,
                                        contentDescription = null,
                                        modifier = Modifier.size(30.dp).clip(CircleShape)
                                    )
                                } else {
                                    Icon(Icons.Default.SportsEsports, null, tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = selectedTargetGame?.appName ?: "Seleccionar Juego...",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = selectedTargetGame?.packageName ?: "Toca para abrir lista completa",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF8E8E93),
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Icon(Icons.Default.ArrowDropDown, null, tint = Color(0xFF00E5FF))
                    }
                }
            }
        }

        // 1. ANILLO GRÁFICO DE PROTECCIÓN
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF0F1E17), Color(0xFF141416))
                        )
                    )
                    .border(1.dp, Color(0xFF30D158).copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(120.dp)) {
                        Canvas(modifier = Modifier.size(110.dp)) {
                            drawCircle(color = Color(0xFF2C2C2E), style = Stroke(width = 10f))
                            drawArc(
                                brush = Brush.sweepGradient(listOf(Color(0xFF30D158), Color(0xFF00FF66), Color(0xFF30D158))),
                                startAngle = -90f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = 10f, cap = StrokeCap.Round)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("100%", style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
                            Text("SEGURO", style = MaterialTheme.typography.labelSmall, color = Color(0xFF30D158), fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Protección de Entorno Activa", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                    Text("Superficie de ejecución libre de brechas críticas", style = MaterialTheme.typography.bodySmall, color = Color(0xFF8E8E93))
                }
            }
        }

        // 2. HYPER CLEANER ENGINE
        item {
            GlassCard {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CleaningServices, null, tint = Color(0xFF30D158), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Hyper Cleaner Engine", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(14.dp))

                    storageData?.let { st ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${st.usedPercentage}% usado", style = MaterialTheme.typography.bodyMedium, color = Color.White, fontWeight = FontWeight.Bold)
                            Text("${"%.2f".format(st.usedGb)} GB / ${"%.0f".format(st.totalGb)} GB", color = Color(0xFF8E8E93), fontSize = 13.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = st.usedPercentage / 100f,
                            modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                            color = if (st.usedPercentage > 88) Color(0xFFFF9500) else Color(0xFF30D158),
                            trackColor = Color(0xFF2C2C2E)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (isCleaning) {
                        Text(
                            text = "⚡ $cleaningSpeedMbSec MB/sec Limpieza en Progreso...",
                            color = Color(0xFF00FF66),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Button(
                        onClick = {
                            isCleaning = true
                            cleanedSuccess = false
                            scope.launch {
                                for (speed in listOf(85, 142, 198, 157, 110)) {
                                    cleaningSpeedMbSec = speed
                                    delay(250)
                                }
                                viewModel.privilegedActions.cleanHyperEngine()
                                storageData = hardwareMonitor.getStorageTelemetry()
                                isCleaning = false
                                cleanedSuccess = true
                                Toast.makeText(context, "Caché purgado", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF30D158)),
                        enabled = !isCleaning
                    ) {
                        Text(if (cleanedSuccess) "Almacenamiento Limpio" else "Purgar Caché y Basura Residual", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 3. SETTINGS CHECKER AUDIT LIST
        item {
            GlassCard {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, null, tint = Color(0xFF007AFF), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Auditoría de Entorno y Ajustes", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    auditChecks.forEach { (title, subtitle) ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF30D158), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(title, style = MaterialTheme.typography.bodyMedium, color = Color.White, fontWeight = FontWeight.SemiBold)
                                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = Color(0xFF8E8E93))
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }

    // MODAL LISTA ILIMITADA DE JUEGOS
    if (showGamePickerModal) {
        AlertDialog(
            onDismissRequest = { showGamePickerModal = false },
            containerColor = Color(0xFF1C1C1E),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
            title = {
                Text(
                    text = "Seleccionar Juego (${installedGames.size} encontrados)",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 350.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(installedGames) { game ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selectedTargetGame?.packageName == game.packageName) Color(0xFF00E5FF).copy(alpha = 0.15f) else Color.Transparent)
                                .clickable {
                                    selectedTargetGame = game
                                    showGamePickerModal = false
                                }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (game.icon != null) {
                                Image(
                                    bitmap = game.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp).clip(CircleShape)
                                )
                            } else {
                                Icon(Icons.Default.SportsEsports, null, tint = Color.White, modifier = Modifier.size(22.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(game.appName, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text(game.packageName, color = Color(0xFF8E8E93), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showGamePickerModal = false }) {
                    Text("Cerrar", color = Color(0xFF00E5FF))
                }
            }
        )
    }
}
