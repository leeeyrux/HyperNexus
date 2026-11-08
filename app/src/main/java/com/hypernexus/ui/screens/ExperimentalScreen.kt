package com.hypernexus.ui.screens

import android.widget.Toast
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
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hypernexus.domain.AppFilterManager
import com.hypernexus.domain.GameTargetApp
import com.hypernexus.ui.HyperNexusViewModel
import com.hypernexus.ui.components.CyberGlowEntranceWrapper
import com.hypernexus.ui.components.GlassCard
import com.hypernexus.ui.components.IOSSwitchTile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    var rawAppList by remember { mutableStateOf<List<GameTargetApp>>(emptyList()) }
    val memoizedAppList by remember { derivedStateOf { rawAppList } }

    var selectedApp by remember { mutableStateOf<GameTargetApp?>(null) }
    var isCompiling by remember { mutableStateOf(false) }
    var gameFps by remember { mutableStateOf(120) }
    var downscale80 by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var zramGamerActive by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val apps = appFilterManager.getInstalledUserApplications()
        rawAppList = apps
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
        item(key = "exp_header", contentType = "premium_node") {
            CyberGlowEntranceWrapper(index = 0, itemKey = "exp_header") {
                Column {
                    Text("Game Tuner & ZRAM Gamer", style = MaterialTheme.typography.headlineLarge, color = Color(0xFFFF9500), fontWeight = FontWeight.Bold)
                    Text("Compilación PGO y retención de texturas en RAM v1.7.5", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF8E8E93))
                }
            }
        }

        item(key = "exp_app_picker", contentType = "premium_node") {
            CyberGlowEntranceWrapper(index = 1, itemKey = "exp_app_picker") {
                GlassCard {
                    Column {
                        Text("Selecciona una App o Juego (${memoizedAppList.size} detectados)", style = MaterialTheme.typography.titleMedium, color = Color.White)
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
                                        Spacer(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .drawWithCache {
                                                    val dst = IntSize(size.width.toInt(), size.height.toInt())
                                                    onDrawBehind {
                                                        drawImage(image = bmp, dstSize = dst)
                                                    }
                                                }
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

                        if (expanded && memoizedAppList.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth().heightIn(max = 280.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(
                                    items = memoizedAppList,
                                    key = { it.packageName },
                                    contentType = { "premium_node" }
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
                                            val bmp = app.icon
                                            Spacer(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .drawWithCache {
                                                        val dst = IntSize(size.width.toInt(), size.height.toInt())
                                                        onDrawBehind {
                                                            drawImage(image = bmp, dstSize = dst)
                                                        }
                                                    }
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
        }

        // COMPILACIÓN PGO ATÓMICA CON LOS 8 HILOS CONCURRENTES
        item(key = "exp_dex2oat_card", contentType = "premium_node") {
            CyberGlowEntranceWrapper(index = 2, itemKey = "exp_dex2oat_card") {
                GlassCard {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Bolt, null, tint = Color(0xFF30D158), modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Compilación PGO (Profile-Guided Optimization)", style = MaterialTheme.typography.titleMedium, color = Color.White)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Fuerza 8 hilos dedicados y genera binario AOT adaptativo con perfil de velocidad completo para evitar tirones.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF8E8E93)
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                selectedApp?.let { app ->
                                    scope.launch(Dispatchers.IO) {
                                        isCompiling = true
                                        // 8 hilos completos + núcleos 0 a 7 forzados
                                        viewModel.privilegedActions.bridge.executeShellCommand(
                                            "setprop dalvik.vm.dex2oat-threads 8; setprop dalvik.vm.dex2oat-cpu-set 0,1,2,3,4,5,6,7; cmd package compile -m speed-profile -f ${app.packageName}"
                                        )
                                        withContext(Dispatchers.Main) {
                                            isCompiling = false
                                            Toast.makeText(context, "PGO completado para: ${app.appName}", Toast.LENGTH_LONG).show()
                                        }
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
                                Text("Compilando código PGO...")
                            } else {
                                Text("Compilar ${selectedApp?.appName ?: "Juego"}", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // GAME MANAGER API TUNER
        item(key = "exp_game_api_card", contentType = "premium_node") {
            CyberGlowEntranceWrapper(index = 3, itemKey = "exp_game_api_card") {
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
        }

        // TWEAK: OPTIMIZAR ASIGNACIÓN ZRAM GAMER
        item(key = "exp_zram_card", contentType = "premium_node") {
            CyberGlowEntranceWrapper(index = 4, itemKey = "exp_zram_card") {
                IOSSwitchTile(
                    title = "Optimizar Asignación ZRAM Gamer",
                    subtitle = "Ajusta swappiness para retener texturas en RAM física y expulsar apps inactivas a swap, eliminando tirones en giros de 360°",
                    checked = zramGamerActive,
                    onCheckedChange = { active ->
                        zramGamerActive = active
                        scope.launch(Dispatchers.IO) {
                            val swappiness = if (active) "130" else "60"
                            val pressure = if (active) "50" else "100"
                            viewModel.privilegedActions.bridge.executeShellCommand(
                                "echo $swappiness > /proc/sys/vm/swappiness 2>/dev/null; " +
                                "echo $pressure > /proc/sys/vm/vfs_cache_pressure 2>/dev/null; " +
                                "setprop persist.sys.zram_tuning ${if (active) 1 else 0}"
                            )
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, if (active) "ZRAM Gamer activada (Texturas fijas en RAM)" else "ZRAM restablecida", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(96.dp)) }
    }
}
