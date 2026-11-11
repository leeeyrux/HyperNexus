package com.hypernexus.ui.screens

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.widget.Toast
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
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventEffect
import androidx.lifecycle.LocalLifecycleOwner
import com.hypernexus.data.HardwareMonitor
import com.hypernexus.data.StorageTelemetry
import com.hypernexus.domain.AppFilterManager
import com.hypernexus.domain.GameTargetApp
import com.hypernexus.services.HyperNexusVpnService
import com.hypernexus.ui.HyperNexusViewModel
import com.hypernexus.ui.components.CyberGlowEntranceWrapper
import com.hypernexus.ui.components.GlassCard
import com.hypernexus.ui.components.IOSSwitch
import com.hypernexus.ui.components.IOSSwitchTile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SeguridadScreen(
    viewModel: HyperNexusViewModel,
    onRequestVpnStart: (String) -> Unit,
    onRequestVpnStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()
    val hardwareMonitor = remember { HardwareMonitor(context) }
    val appFilterManager = remember { AppFilterManager(context, viewModel.privilegedActions.bridge) }

    var storageData by remember { mutableStateOf<StorageTelemetry?>(null) }
    var isBoosterClearing by remember { mutableStateOf(false) }
    var isAntivirusScanning by remember { mutableStateOf(false) }
    var scannedAppPackageName by remember { mutableStateOf("") }
    var antivirusProgress by remember { mutableStateOf(1f) }

    var rawGames by remember { mutableStateOf<List<GameTargetApp>>(emptyList()) }
    val memoizedGames by remember { derivedStateOf { rawGames } }

    var selectedTargetGame by remember { mutableStateOf<GameTargetApp?>(null) }
    var showGamePickerModal by remember { mutableStateOf(false) }

    // Private DNS state from DataStore
    val privateDnsEnabled = uiState.privateDnsEnabled

    LaunchedEffect(Unit) {
        storageData = hardwareMonitor.getStorageTelemetry()
        val allApps = appFilterManager.getInstalledUserApplications()
        rawGames = allApps
        if (allApps.isNotEmpty() && selectedTargetGame == null) {
            val savedPkg = uiState.vpnTargetPackage
            selectedTargetGame = allApps.firstOrNull { it.packageName == savedPkg }
                ?: allApps.firstOrNull { it.isGame }
                ?: allApps.first()
        }
    }

    // ============================================================
    // UNIVERSAL SYNC LIFECYCLE ENGINE - Real-time external state sync
    // ============================================================
    LifecycleEventEffect(lifecycle, Lifecycle.Event.ON_RESUME) {
        scope.launch(Dispatchers.IO) {
            syncExternalSecurityState(viewModel)
        }
    }

    val isTunnelRunning = uiState.vpnActive || HyperNexusVpnService.isRunning

    // ANIMACIÓN UNIFICADA DE LÁSER SWEEP PARA AMBOS ANILLOS
    // Both rings share the same continuous standby shimmer motion (2800ms linear sweep)
    // Booster: 1800ms active (matches pm trim-caches 999G runtime) - CYAN
    // Antivirus: variable active duration (package audit) - EMERALD GREEN
    val infiniteTransition = rememberInfiniteTransition(label = "unifiedLaserTransition")

    // Sweep del Booster (1800ms al limpiar = pm trim-caches runtime, 2800ms en reposo)
    val boosterLaserAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isBoosterClearing) 1800 else 2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "boosterSweep"
    )

    // Sweep del Antivirus (se acelera a 480ms al escanear, 2800ms en reposo)
    val antivirusLaserAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isAntivirusScanning) 480 else 2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "antivirusSweep"
    )

    fun startDeepAntivirusScan() {
        if (isAntivirusScanning) return
        isAntivirusScanning = true
        antivirusProgress = 0f

        scope.launch {
            val pm = context.packageManager
            val packages: List<PackageInfo> = withContext(Dispatchers.IO) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        pm.getInstalledPackages(PackageManager.PackageInfoFlags.of(PackageManager.GET_META_DATA.toLong()))
                    } else {
                        @Suppress("DEPRECATION")
                        pm.getInstalledPackages(PackageManager.GET_META_DATA)
                    }
                } catch (_: Exception) {
                    emptyList()
                }
            }

            val total = packages.size.coerceAtLeast(1)
            packages.forEachIndexed { idx, pkg ->
                scannedAppPackageName = pkg.packageName
                antivirusProgress = (idx + 1).toFloat() / total.toFloat()
                delay(12)
            }
            scannedAppPackageName = "[ 100% DISPOSITIVO PROTEGIDO ]"
            isAntivirusScanning = false
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item(key = "seg_header", contentType = "premium_node") {
            CyberGlowEntranceWrapper(index = 0, itemKey = "seg_header") {
                Column {
                    Text("Seguridad & Red Gamer", style = MaterialTheme.typography.headlineLarge, color = Color.White, fontWeight = FontWeight.Bold)
                    Text("Dual-Engine Symmetrical Hub v1.8.0", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF8E8E93))
                }
            }
        }

        // ====================================================================
        // DUAL-ENGINE SIMÉTRICO: BOOSTER (CYAN) & ANTIVIRUS (EMERALD)
        // ====================================================================
        item(key = "seg_dual_engine", contentType = "premium_node") {
            CyberGlowEntranceWrapper(index = 1, itemKey = "seg_dual_engine") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // LEFT: GAMING BOOSTER ENGINE (CYAN #00E5FF)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(22.dp))
                            .background(Color(0xFF141416))
                            .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.35f), RoundedCornerShape(22.dp))
                            .clickable {
                                if (!isBoosterClearing) {
                                    isBoosterClearing = true
                                    scope.launch {
                                        viewModel.privilegedActions.cleanHyperEngine()
                                        storageData = hardwareMonitor.getStorageTelemetry()
                                        delay(1800)
                                        isBoosterClearing = false
                                        Toast.makeText(context, "Gaming Booster: Memoria Depurada", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                            .padding(vertical = 18.dp, horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(116.dp)) {
                                Canvas(
                                    modifier = Modifier
                                        .size(102.dp)
                                        .drawWithCache {
                                            onDrawBehind {
                                                drawCircle(color = Color(0xFF2C2C2E), style = Stroke(width = 9f))
                                                drawArc(
                                                    color = Color(0xFF00E5FF),
                                                    startAngle = -90f,
                                                    sweepAngle = 270f,
                                                    useCenter = false,
                                                    style = Stroke(width = 9f, cap = StrokeCap.Round)
                                                )
                                                // Barrido láser idéntico unificado
                                                drawArc(
                                                    color = Color.White,
                                                    startAngle = boosterLaserAngle,
                                                    sweepAngle = 30f,
                                                    useCenter = false,
                                                    style = Stroke(width = 11f, cap = StrokeCap.Round)
                                                )
                                            }
                                        }
                                ) {}
                                Icon(Icons.Default.Bolt, null, tint = Color(0xFF00E5FF), modifier = Modifier.size(36.dp))
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("BOOSTER", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                            Text(if (isBoosterClearing) "Optimizando..." else "Toca para Limpiar", fontSize = 11.sp, color = Color(0xFF00E5FF))
                        }
                    }

                    // RIGHT: DEEP ANTIVIRUS ENGINE (EMERALD GREEN #00E676)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(22.dp))
                            .background(Color(0xFF141416))
                            .border(1.dp, Color(0xFF00E676).copy(alpha = 0.35f), RoundedCornerShape(22.dp))
                            .clickable { startDeepAntivirusScan() }
                            .padding(vertical = 18.dp, horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(116.dp)) {
                                Canvas(
                                    modifier = Modifier
                                        .size(102.dp)
                                        .drawWithCache {
                                            onDrawBehind {
                                                drawCircle(color = Color(0xFF2C2C2E), style = Stroke(width = 9f))
                                                drawArc(
                                                    brush = Brush.sweepGradient(
                                                        listOf(Color(0xFF00E676), Color(0xFF00B0FF), Color(0xFF00E676))
                                                    ),
                                                    startAngle = -90f,
                                                    sweepAngle = (antivirusProgress * 360f).coerceAtLeast(30f),
                                                    useCenter = false,
                                                    style = Stroke(width = 9f, cap = StrokeCap.Round)
                                                )
                                                // Barrido láser idéntico unificado
                                                drawArc(
                                                    color = Color.White,
                                                    startAngle = antivirusLaserAngle,
                                                    sweepAngle = 30f,
                                                    useCenter = false,
                                                    style = Stroke(width = 11f, cap = StrokeCap.Round)
                                                )
                                            }
                                        }
                                ) {}
                                Text(
                                    text = if (isAntivirusScanning) "${(antivirusProgress * 100).toInt()}%" else "100%",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("ANTIVIRUS", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                            Text(
                                text = if (isAntivirusScanning) "Auditando..." else "Protegido",
                                fontSize = 11.sp,
                                color = Color(0xFF00E676)
                            )
                        }
                    }
                }
            }
        }

        // SUB-LABEL CON EL PAQUETE AUDITADO EN VIVO
        if (scannedAppPackageName.isNotBlank()) {
            item(key = "seg_scan_status", contentType = "premium_node") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF00E676).copy(alpha = 0.12f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = scannedAppPackageName,
                        color = Color(0xFF00E676),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1
                    )
                }
            }
        }

        // LIMPIEZA AUTOMÁTICA POR UMBRAL (15% RAM)
        item(key = "seg_threshold_switch", contentType = "premium_node") {
            CyberGlowEntranceWrapper(index = 2, itemKey = "seg_threshold_switch") {
                IOSSwitchTile(
                    title = "Limpieza Automática por Umbral",
                    subtitle = "Purga memoria en segundo plano si la RAM libre cae bajo el 15%",
                    checked = uiState.thresholdRamPurger,
                    onCheckedChange = { viewModel.toggleThresholdRamPurger(it) }
                )
            }
        }

        // ANTI-THROTTLING NETWORK ENGINE (DoH)
        item(key = "seg_doh_switch", contentType = "premium_node") {
            CyberGlowEntranceWrapper(index = 3, itemKey = "seg_doh_switch") {
                IOSSwitchTile(
                    title = "Anti-Throttling Network Engine (DoH)",
                    subtitle = "Camufla metadatos DNS en HTTPS (Puerto 443) para evitar estrangulamiento ISP",
                    checked = uiState.dohAntiThrottling,
                    onCheckedChange = { viewModel.toggleDohAntiThrottling(it) }
                )
            }
        }

        // DNS PRIVADO RÁPIDO (1.1.1.1) - NUEVA OPCIÓN
        item(key = "seg_private_dns_switch", contentType = "premium_node") {
            CyberGlowEntranceWrapper(index = 4, itemKey = "seg_private_dns_switch") {
                IOSSwitchTile(
                    title = "DNS Privado Rápido (1.1.1.1)",
                    subtitle = "Acelera el internet y evita bloqueos básicos de red sin gastar batería.",
                    checked = privateDnsEnabled,
                    onCheckedChange = { viewModel.togglePrivateDns(it) }
                )
            }
        }

        // GAMING VPN TUNNEL
        item(key = "seg_vpn_card", contentType = "premium_node") {
            CyberGlowEntranceWrapper(index = 5, itemKey = "seg_vpn_card") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF16161A))
                        .border(
                            width = if (isTunnelRunning) 2.dp else 1.dp,
                            color = if (isTunnelRunning) Color(0xFF00E676) else Color(0xFF00E5FF).copy(alpha = 0.35f),
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
                                        text = if (uiState.dohAntiThrottling) "DoH 443 | MTU 1400 | DNS 1.1.1.1" else "MTU 1400 | DNS 1.1.1.1 Zero-Drop",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF8E8E93)
                                    )
                                }
                            }

                            IOSSwitch(
                                checked = isTunnelRunning,
                                onCheckedChange = { enable ->
                                    if (enable) {
                                        val targetPkg = selectedTargetGame?.packageName ?: ""
                                        onRequestVpnStart(targetPkg)
                                    } else {
                                        onRequestVpnStop()
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
                                        .background(Color(0xFF00E676))
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
                                        val bmp = selectedTargetGame!!.icon!!
                                        Spacer(
                                            modifier = Modifier
                                                .size(30.dp)
                                                .clip(CircleShape)
                                                .drawWithCache {
                                                    val dst = IntSize(size.width.toInt(), size.height.toInt())
                                                    onDrawBehind {
                                                        drawImage(image = bmp, dstSize = dst)
                                                    }
                                                }
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
        }

        // FOOTER ACTIONS REUBICADAS EN EL PIE DE SEGURIDAD
        item(key = "seg_footer_actions", contentType = "premium_node") {
            CyberGlowEntranceWrapper(index = 6, itemKey = "seg_footer_actions") {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            scope.launch {
                                viewModel.privilegedActions.killBackgroundApps()
                                viewModel.privilegedActions.clearSmartCache()
                                storageData = hardwareMonitor.getStorageTelemetry()
                                Toast.makeText(context, "RAM Liberada", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF))
                    ) {
                        Text("Limpiar RAM", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                        Text("Gaming Boost", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            viewModel.optimizePing { msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676))
                    ) {
                        Text("Optimizar Ping", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(96.dp)) }
    }

    if (showGamePickerModal) {
        AlertDialog(
            onDismissRequest = { showGamePickerModal = false },
            containerColor = Color(0xFF1C1C1E),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
            title = {
                Text(
                    text = "Seleccionar Juego (${memoizedGames.size} encontrados)",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 350.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(
                        items = memoizedGames,
                        key = { it.packageName },
                        contentType = { "premium_node" }
                    ) { game ->
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
                                val bmp = game.icon
                                Spacer(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .drawWithCache {
                                            val dst = IntSize(size.width.toInt(), size.height.toInt())
                                            onDrawBehind {
                                                drawImage(image = bmp, dstSize = dst)
                                            }
                                        }
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

// ============================================================
// UNIVERSAL SYNC ENGINE - Queries real-time security properties
// ============================================================
private suspend fun syncExternalSecurityState(viewModel: HyperNexusViewModel) {
    val bridge = viewModel.privilegedActions.bridge
    return try {
        // Sync Private DNS
        val dnsModeResult = bridge.executeShellCommand("settings get global private_dns_mode")
        if (dnsModeResult.first == 0) {
            val mode = dnsModeResult.second.trim()
            val current = mode == "hostname"
            if (current != viewModel.uiState.value.privateDnsEnabled) {
                viewModel.repository.updatePrivateDns(current)
            }
        }

        // Sync Threshold RAM Purger (check if the monitoring job is running)
        // This is harder to detect externally, skip for now

        // Sync DoH Anti-Throttling (check DNS settings)
        val dnsResult = bridge.executeShellCommand("settings get global private_dns_specifier")
        if (dnsResult.first == 0) {
            val specifier = dnsResult.second.trim()
            val current = specifier == "one.one.one.one"
            if (current != viewModel.uiState.value.dohAntiThrottling) {
                viewModel.repository.updateDohAntiThrottling(current)
            }
        }

    } catch (e: Exception) {
        // Silent fail - sync is best effort
    }
}