package com.hypernexus.ui.screens

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hypernexus.PrivilegedActions
import com.hypernexus.ui.components.*
import com.hypernexus.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun GraphicsScreen(
    privilegedActions: PrivilegedActions,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    
    // Estados
    var vsyncDisabled by remember { mutableStateOf(false) }
    var dirtyRegionsDisabled by remember { mutableStateOf(false) }
    var showDirtyRegions by remember { mutableStateOf(false) }
    var antialiasingEnabled by remember { mutableStateOf(false) }
    var hwuiCacheOptimized by remember { mutableStateOf(false) }
    var gpuRenderingForced by remember { mutableStateOf(false) }
    
    var isApplying by remember { mutableStateOf(false) }
    
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundBlack)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Text(
                text = "Gráficos",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Desbloquea el potencial visual de tu dispositivo",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
        
        // VSYNC (La joya de la corona)
        item {
            IOSSwitchTile(
                title = "Desbloqueo de FPS",
                subtitle = "Permite que los juegos superen los 60 FPS si el hardware lo permite",
                checked = vsyncDisabled,
                onCheckedChange = { enabled ->
                    vsyncDisabled = enabled
                    scope.launch {
                        isApplying = true
                        if (enabled) {
                            privilegedActions.disableVsync()
                        }
                        isApplying = false
                    }
                },
                warningText = "⚠️ Puede aumentar temperatura y consumo de batería"
            )
        }
        
        // Renderizado Completo
        item {
            IOSSwitchTile(
                title = "Renderizado Completo",
                subtitle = "Dibuja toda la pantalla cada vez, evitando tirones en juegos",
                checked = dirtyRegionsDisabled,
                onCheckedChange = { enabled ->
                    dirtyRegionsDisabled = enabled
                    scope.launch {
                        isApplying = true
                        if (enabled) {
                            privilegedActions.disableDirtyRegions()
                        }
                        isApplying = false
                    }
                },
                warningText = "Usa más GPU, puede causar calentamiento en sesiones largas"
            )
        }
        
        // Mostrar Dirty Regions (Debug)
        item {
            IOSSwitchTile(
                title = "Modo Debug Visual",
                subtitle = "Muestra en pantalla qué áreas se están redibujando (útil para diagnóstico)",
                checked = showDirtyRegions,
                onCheckedChange = { enabled ->
                    showDirtyRegions = enabled
                    scope.launch {
                        privilegedActions.showDirtyRegions(enabled)
                    }
                }
            )
        }
        
        // Antialiasing
        item {
            IOSSwitchTile(
                title = "Antialiasing Controlado",
                subtitle = "Activa bordes suaves solo en apps que lo soportan",
                checked = antialiasingEnabled,
                onCheckedChange = { enabled ->
                    antialiasingEnabled = enabled
                    scope.launch {
                        privilegedActions.configureAntialiasing(enabled)
                    }
                }
            )
        }
        
        // GPU Rendering Forzado
        item {
            IOSSwitchTile(
                title = "Aceleración Gráfica Forzada",
                subtitle = "Usa el procesador gráfico para mover los menús. Hace que todo se sienta mucho más fluido",
                checked = gpuRenderingForced,
                onCheckedChange = { enabled ->
                    gpuRenderingForced = enabled
                    scope.launch {
                        // TODO: Implementar en PrivilegedActions
                        // privilegedActions.forceGpuRendering(enabled)
                    }
                }
            )
        }
        
        // HWUI Cache (Riesgoso)
        item {
            IOSSwitchTile(
                title = "Optimización de Caché Gráfico",
                subtitle = "Mejora carga de texturas en juegos (avanzado)",
                checked = hwuiCacheOptimized,
                onCheckedChange = { enabled ->
                    hwuiCacheOptimized = enabled
                    scope.launch {
                        if (enabled) {
                            privilegedActions.optimizeHwuiCache()
                        }
                    }
                },
                warningText = "⚠️ Experimental: Puede causar inestabilidad en algunos dispositivos"
            )
        }
        
        // Sección de explicación
        item {
            GlassCard {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.VideogameAsset,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Para Gamers",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "El Desbloqueo de FPS (VSYNC) es el ajuste más importante para juegos competitivos. Permite que la pantalla muestre más de 60 cuadros por segundo, reduciendo el input lag y haciendo que los tiros se sientan más precisos.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        lineHeight = 20.sp
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Recomendado para: Free Fire, PUBG, Call of Duty Mobile, Genshin Impact",
                        style = MaterialTheme.typography.labelMedium,
                        color = AccentBlue
                    )
                }
            }
        }
        
        // Espacio final
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}