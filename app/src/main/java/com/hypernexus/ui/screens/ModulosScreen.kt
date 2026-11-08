package com.hypernexus.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hypernexus.ui.HyperNexusViewModel
import com.hypernexus.ui.components.CyberGlowEntranceWrapper
import com.hypernexus.ui.components.GlassCard
import com.hypernexus.ui.components.IOSSwitch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

data class PresetModule(
    val id: String,
    val title: String,
    val description: String,
    val script: String
)

@Composable
fun ModulosScreen(
    viewModel: HyperNexusViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()

    val moduleOutputs = remember { mutableStateMapOf<String, String>() }
    val expandedOutputs = remember { mutableStateMapOf<String, Boolean>() }

    val presetModules = remember {
        listOf(
            PresetModule(
                id = "fps_stabilizer",
                title = "1. FPS Stabilizer Tweak",
                description = "Fuerza tasa de cuadros uniforme y bloquea caídas de sincronización vertical.",
                script = "setprop persist.sys.stable.fps.enable true; setprop debug.hwui.render_dirty_regions false; setprop debug.sf.showupdates 0; setprop debug.sf.showcpu 0"
            ),
            PresetModule(
                id = "touch_booster",
                title = "2. Touch Response Booster",
                description = "Minimiza la fricción táctil a 0.01ms e incrementa el muestreo digital.",
                script = "setprop persist.sys.Touchscreen.tap_response_time 0.01; setprop persist.sys.enable.fast_rendering 1; setprop touch.pressure.scale 0.001"
            ),
            PresetModule(
                id = "thermal_bypass",
                title = "3. Thermal Throttling Bypass",
                description = "Modifica directivas de atenuación térmica del CPU para sesiones de juego prolongadas.",
                script = "setprop debug.thermal.bypass 1; settings put global thermal_limit_refresh 0; cmd power set-mode 0"
            ),
            PresetModule(
                id = "ping_optimizer",
                title = "4. Network Ping Optimizer",
                description = "Limpia la caché de sockets DNS y agranda los búferes de recepción TCP WiFi.",
                script = "ndc resolver flushdefaultif; setprop net.tcp.buffersize.wifi 524288,1048576,2097152,4096,262144,1048576; setprop persist.sys.wifi.low_latency true"
            ),
            PresetModule(
                id = "ram_lmk_aggressive",
                title = "5. RAM Management Aggressive Tweak",
                description = "Modifica parámetros minfree de LMK y purgeable assets para multitarea pesada.",
                script = "setprop persist.sys.purgeable_assets 1; echo '18432,23040,27648,32256,36864,46080' > /sys/module/lowmemorykiller/parameters/minfree 2>/dev/null; echo 100 > /proc/sys/vm/vfs_cache_pressure 2>/dev/null"
            ),
            PresetModule(
                id = "gpu_sustained_clocks",
                title = "6. GPU Performance Sustained Profiles",
                description = "Previene la caída de reloj en GPUs Adreno y Mali bajo cargas continuadas.",
                script = "setprop debug.hwui.fps_divisor 1; setprop persist.sys.power.profile performance; setprop debug.renderengine.backend 2; setprop debug.hwui.use_buffer_age true"
            ),
            PresetModule(
                id = "kernel_entropy_multiplier",
                title = "7. Kernel Entropy Multiplier",
                description = "Aumenta umbrales de entropía del kernel reduciendo micro-tirones de entrada.",
                script = "echo 4096 > /proc/sys/kernel/random/write_wakeup_threshold 2>/dev/null; echo 2048 > /proc/sys/kernel/random/read_wakeup_threshold 2>/dev/null"
            ),
            PresetModule(
                id = "audio_bitrate_enhancer",
                title = "8. System Audio Bitrate Enhancer",
                description = "Optimiza la calidad de remuestreo del framework de audio de Android a 24/32 bits.",
                script = "setprop af.resampler.quality 4; setprop persist.sys.audio.high_res 1; setprop persist.af.resampler.quality 4"
            )
        )
    }

    fun executeAndCapture(id: String, script: String) {
        scope.launch(Dispatchers.IO) {
            val res = viewModel.privilegedActions.executeScriptRaw(script)
            val lines = res.second.lines().filter { it.isNotBlank() }
            val last3 = if (lines.isNotEmpty()) lines.takeLast(3).joinToString("\n") else "[Inyección exitosa: Exit 0]"
            withContext(Dispatchers.Main) {
                moduleOutputs[id] = last3
            }
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val reader = BufferedReader(InputStreamReader(inputStream))
                val content = reader.readText()
                reader.close()

                val fileName = it.lastPathSegment?.substringAfterLast("/") ?: "custom_mod.sh"
                viewModel.installCustomModule("$fileName:::$content")
                Toast.makeText(context, "Módulo '$fileName' cargado con éxito", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Error al leer archivo: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item(key = "mod_header", contentType = "premium_node") {
            CyberGlowEntranceWrapper(index = 0, itemKey = "mod_header") {
                Column {
                    Text("Módulos Universales", style = MaterialTheme.typography.headlineLarge, color = Color.White, fontWeight = FontWeight.Bold)
                    Text("Inyector de scripts, mods y telemetría de salida v1.7.5", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF8E8E93))
                }
            }
        }

        item(key = "mod_install_btn", contentType = "premium_node") {
            CyberGlowEntranceWrapper(index = 1, itemKey = "mod_install_btn") {
                Button(
                    onClick = { filePickerLauncher.launch("*/*") },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF))
                ) {
                    Icon(Icons.Default.FileOpen, null)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Instalar Módulo desde Almacenamiento (.sh / .txt)", fontWeight = FontWeight.Bold)
                }
            }
        }

        if (uiState.installedModulePaths.isNotEmpty()) {
            item(key = "mod_custom_title", contentType = "premium_node") {
                Text("Módulos Personalizados (${uiState.installedModulePaths.size})", style = MaterialTheme.typography.titleMedium, color = Color.White)
            }

            items(
                items = uiState.installedModulePaths.toList(),
                key = { it },
                contentType = { "premium_node" }
            ) { rawModule ->
                val parts = rawModule.split(":::")
                val name = parts.getOrNull(0) ?: "Custom Script"
                val script = parts.getOrNull(1) ?: ""
                val isActive = uiState.activeModules.contains(name)
                val isExpanded = expandedOutputs[name] ?: false
                val output = moduleOutputs[name] ?: "Sin ejecución registrada."

                CyberGlowEntranceWrapper(index = 2, itemKey = rawModule) {
                    GlassCard {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(name, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(script.take(70) + if (script.length > 70) "..." else "", style = MaterialTheme.typography.bodySmall, color = Color(0xFF8E8E93), fontFamily = FontFamily.Monospace)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IOSSwitch(
                                        checked = isActive,
                                        onCheckedChange = {
                                            viewModel.toggleModule(name, script)
                                            if (it) executeAndCapture(name, script)
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(onClick = { viewModel.removeCustomModule(rawModule) }) {
                                        Icon(Icons.Default.Delete, "Eliminar", tint = Color(0xFFFF003C))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (isExpanded) "▾ [ Ocultar Salida ]" else "▸ [ Ver Salida ]",
                                color = Color(0xFF00E5FF),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable { expandedOutputs[name] = !isExpanded }
                            )

                            AnimatedVisibility(visible = isExpanded) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF0F0F12))
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = output,
                                        color = Color(0xFF8E8E93),
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        lineHeight = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item(key = "mod_presets_title", contentType = "premium_node") {
            Text("Sugerencias de Inyección Rápida", style = MaterialTheme.typography.titleMedium, color = Color.White)
        }

        items(
            items = presetModules,
            key = { it.id },
            contentType = { "premium_node" }
        ) { mod ->
            val isActive = uiState.activeModules.contains(mod.id)
            val isExpanded = expandedOutputs[mod.id] ?: false
            val output = moduleOutputs[mod.id] ?: "Sin ejecución registrada."

            CyberGlowEntranceWrapper(index = 3, itemKey = mod.id) {
                GlassCard {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(mod.title, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(mod.description, style = MaterialTheme.typography.bodySmall, color = Color(0xFF8E8E93))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            IOSSwitch(
                                checked = isActive,
                                onCheckedChange = {
                                    viewModel.toggleModule(mod.id, mod.script)
                                    if (it) executeAndCapture(mod.id, mod.script)
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isExpanded) "▾ [ Ocultar Salida ]" else "▸ [ Ver Salida ]",
                            color = Color(0xFF00E5FF),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { expandedOutputs[mod.id] = !isExpanded }
                        )

                        AnimatedVisibility(visible = isExpanded) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF0F0F12))
                                    .padding(8.dp)
                                ) {
                                    Text(
                                        text = output,
                                        color = Color(0xFF8E8E93),
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        lineHeight = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(96.dp)) }
    }
}
