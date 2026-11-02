package com.hypernexus.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hypernexus.ui.HyperNexusViewModel
import com.hypernexus.ui.components.GlassCard
import kotlinx.coroutines.launch

@Composable
fun TerminalScreen(
    viewModel: HyperNexusViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()

    var inputCommand by remember { mutableStateOf("") }
    var consoleLogs by remember { mutableStateOf("HyperNexus ADB Shell v1.0 [Shizuku Protocol]\nEscriba un comando o seleccione uno predefinido.\n") }
    var isExecuting by remember { mutableStateOf(false) }

    val presetCommands = remember {
        listOf(
            "Forzar Modo Doze Extremo" to "dumpsys deviceidle force-idle",
            "Optimizar Batería (Deep Sleep)" to "cmd deviceidle conserve enabled 1",
            "Limpiar Caché del Sistema" to "pm trim-caches 999G",
            "Liberar Memoria RAM" to "am kill-all",
            "Verificar Estado de Superficie Gráfica" to "dumpsys surfaceflinger"
        )
    }

    fun runCommand(cmd: String) {
        val target = cmd.trim()
        if (target.isBlank() || isExecuting) return
        isExecuting = true
        consoleLogs += "\n$ $target\n"

        scope.launch {
            val result = viewModel.privilegedActions.bridge.executeShellCommand(target)
            consoleLogs += if (result.first == 0) {
                if (result.second.isBlank()) "[Comando ejecutado con éxito (Exit 0)]\n" else "${result.second}\n"
            } else {
                "[Error de salida: ${result.first}]\n${result.second}\n"
            }
            isExecuting = false
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. BANNER ANTI-CURIOSO CARMESÍ
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF24060A))
                    .border(1.dp, Color(0xFFFF003C), RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Warning, null, tint = Color(0xFFFF003C), modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "🚨 ¡ADVERTENCIA GRAVE!",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFFFF003C),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Si no sabe a lo que le va a mover, mejor déjele ahí 'Che Jorge el Curioso' o se va a quedar sin teléfono. Esta es una herramienta avanzada; un mal valor puede corromper la pantalla o forzar un formateo de fábrica. El creador de la app NO se hace responsable de dispositivos dañados por mal uso. ¿Está absolutamente seguro de proceder?",
                            color = Color.White,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        // 2. CONSOLA DE SALIDA INTERACTIVA
        item {
            GlassCard {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Consola Shizuku", style = MaterialTheme.typography.titleSmall, color = Color.White)
                        IconButton(onClick = { consoleLogs = "HyperNexus Shell Limpia.\n" }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.DeleteSweep, "Limpiar", tint = Color(0xFF8E8E93))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0F0F12))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = consoleLogs,
                            color = Color(0xFF30D158),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        // 3. CAMPO DE ENTRADA Y ACCIONES
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputCommand,
                    onValueChange = { inputCommand = it },
                    placeholder = { Text("Ej. setprop debug.sf.hw 1", fontSize = 12.sp, color = Color(0xFF636366)) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF007AFF),
                        unfocusedBorderColor = Color(0xFF2C2C2E),
                        focusedContainerColor = Color(0xFF1C1C1E),
                        unfocusedContainerColor = Color(0xFF1C1C1E)
                    ),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (inputCommand.isNotBlank()) {
                            viewModel.toggleFavoriteCommand(inputCommand.trim())
                            Toast.makeText(context, "Favorito actualizado", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF1C1C1E))
                ) {
                    val isFav = uiState.favoriteCommands.contains(inputCommand.trim())
                    Icon(
                        imageVector = if (isFav) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Favorito",
                        tint = if (isFav) Color(0xFFFF9500) else Color.White
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        runCommand(inputCommand)
                        inputCommand = ""
                    },
                    modifier = Modifier.height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF)),
                    enabled = !isExecuting && inputCommand.isNotBlank()
                ) {
                    if (isExecuting) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Send, null, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        // 4. FAVORITOS GUARDADOS
        if (uiState.favoriteCommands.isNotEmpty()) {
            item {
                Text("Comandos Guardados", style = MaterialTheme.typography.titleMedium, color = Color.White)
            }
            items(uiState.favoriteCommands.toList()) { favCmd ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF1C1C1E))
                        .clickable { runCommand(favCmd) }
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PlayArrow, null, tint = Color(0xFF30D158), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(favCmd, color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    }
                    IconButton(onClick = { viewModel.toggleFavoriteCommand(favCmd) }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, null, tint = Color(0xFFFF3B30), modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // 5. PRESETS POPULARES
        item {
            Text("Comandos Útiles Predefinidos", style = MaterialTheme.typography.titleMedium, color = Color.White)
        }

        items(presetCommands) { (title, cmd) ->
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { runCommand(cmd) }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(title, style = MaterialTheme.typography.titleSmall, color = Color.White)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(cmd, style = MaterialTheme.typography.bodySmall, color = Color(0xFF007AFF), fontFamily = FontFamily.Monospace)
                    }
                    Icon(Icons.Default.FlashOn, null, tint = Color(0xFFFF9500), modifier = Modifier.size(22.dp))
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}
