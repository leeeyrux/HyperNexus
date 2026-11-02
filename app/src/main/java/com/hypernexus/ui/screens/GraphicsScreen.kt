package com.hypernexus.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hypernexus.PrivilegedActions
import com.hypernexus.services.FpsOverlayService
import com.hypernexus.ui.components.*
import kotlinx.coroutines.launch

@Composable
fun GraphicsScreen(
    privilegedActions: PrivilegedActions,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var vsyncDisabled by remember { mutableStateOf(false) }
    var dirtyRegionsDisabled by remember { mutableStateOf(false) }
    var showDirtyRegions by remember { mutableStateOf(false) }
    var antialiasingEnabled by remember { mutableStateOf(false) }
    var hwuiCacheOptimized by remember { mutableStateOf(false) }
    var gpuRenderingForced by remember { mutableStateOf(false) }
    var fpsOverlayActive by remember { mutableStateOf(FpsOverlayService.isRunning) }
    var selectedHz by remember { mutableStateOf(0) }
    
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Gráficos & Pantalla",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Tasa de refresco extrema y optimizaciones visuales",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF8E8E93)
            )
        }
        
        item {
            GlassCard {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Speed, null, tint = Color(0xFF007AFF), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Forzar Tasa de Refresco", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val rates = listOf(0 to "Auto", 60 to "60 Hz", 90 to "90 Hz", 120 to "120 Hz", 144 to "144 Hz")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        rates.forEach { (rate, label) ->
                            Button(
                                onClick = {
                                    selectedHz = rate
                                    scope.launch { 
                                        privilegedActions.forceRefreshRate(rate)
                                        Toast.makeText(context, "Frecuencia: $label", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f).height(40.dp),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(0.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedHz == rate) Color(0xFF007AFF) else Color(0xFF2C2C2E)
                                )
                            ) {
                                Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
        
        item {
            IOSSwitchTile(
                title = "Contador de FPS RedMagic",
                subtitle = "Píldora flotante arrastrable con medición en vivo",
                checked = fpsOverlayActive,
                onCheckedChange = { enabled ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))
                        context.startActivity(intent)
                    } else {
                        fpsOverlayActive = enabled
                        val intent = Intent(context, FpsOverlayService::class.java)
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
            IOSSwitchTile(
                title = "Desbloqueo de FPS (VSYNC Bypass)",
                subtitle = "Permite superar los 60 FPS fijos en títulos competitivos",
                checked = vsyncDisabled,
                onCheckedChange = { enabled ->
                    vsyncDisabled = enabled
                    scope.launch { if (enabled) privilegedActions.disableVsync() }
                }
            )
        }
        
        item {
            IOSSwitchTile(
                title = "Forzar Renderizado GPU",
                subtitle = "Obliga al chip gráfico a procesar capas 2D del sistema",
                checked = gpuRenderingForced,
                onCheckedChange = { enabled ->
                    gpuRenderingForced = enabled
                    scope.launch { privilegedActions.forceGpuRendering(enabled) }
                }
            )
        }

        item {
            IOSSwitchTile(
                title = "Renderizado de Pantalla Completa",
                subtitle = "Evita tirones redibujando toda la escena de forma homogénea",
                checked = dirtyRegionsDisabled,
                onCheckedChange = { enabled ->
                    dirtyRegionsDisabled = enabled
                    scope.launch { if (enabled) privilegedActions.disableDirtyRegions() }
                }
            )
        }
        
        item {
            IOSSwitchTile(
                title = "Modo Debug de Cuadros Sucios",
                subtitle = "Muestra visualmente las zonas de la pantalla que se redibujan",
                checked = showDirtyRegions,
                onCheckedChange = { enabled ->
                    showDirtyRegions = enabled
                    scope.launch { privilegedActions.showDirtyRegions(enabled) }
                }
            )
        }
        
        item {
            IOSSwitchTile(
                title = "MSAA 4x Forzado",
                subtitle = "Bordes ultra suaves y suavizado de contornos gráficos",
                checked = antialiasingEnabled,
                onCheckedChange = { enabled ->
                    antialiasingEnabled = enabled
                    scope.launch { privilegedActions.configureAntialiasing(enabled) }
                }
            )
        }
        
        item {
            IOSSwitchTile(
                title = "Optimización de Caché HWUI",
                subtitle = "Expande el búfer de texturas gráficas a 24MB",
                checked = hwuiCacheOptimized,
                onCheckedChange = { enabled ->
                    hwuiCacheOptimized = enabled
                    scope.launch { if (enabled) privilegedActions.optimizeHwuiCache() }
                }
            )
        }
        
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}
