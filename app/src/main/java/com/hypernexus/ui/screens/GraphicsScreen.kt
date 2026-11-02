package com.hypernexus.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hypernexus.ui.HyperNexusViewModel
import com.hypernexus.ui.components.GraphicsRendererSelector
import com.hypernexus.ui.components.IOSSwitchTile

@Composable
fun GraphicsScreen(
    viewModel: HyperNexusViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Gráficos", style = MaterialTheme.typography.headlineLarge, color = Color.White, fontWeight = FontWeight.Bold)
            Text("Aceleración por GPU y motores de render Skia", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF8E8E93))
        }

        item {
            GraphicsRendererSelector(
                selectedRenderer = uiState.graphicsRenderer,
                onRendererSelected = {
                    viewModel.setGraphicsRenderer(it)
                    Toast.makeText(context, "Pipeline: $it", Toast.LENGTH_SHORT).show()
                }
            )
        }

        item {
            IOSSwitchTile(
                title = "Desbloqueo de FPS (VSYNC Bypass)",
                subtitle = "Permite superar los 60 FPS fijos en títulos competitivos",
                checked = uiState.vsyncDisabled,
                onCheckedChange = { viewModel.toggleVsync(it) }
            )
        }

        item {
            IOSSwitchTile(
                title = "Forzar Renderizado GPU",
                subtitle = "Obliga al procesador gráfico a mover capas 2D del sistema",
                checked = uiState.forceGpuRendering,
                onCheckedChange = { viewModel.toggleForceGpu(it) }
            )
        }

        item {
            IOSSwitchTile(
                title = "Renderizado de Pantalla Completa",
                subtitle = "Evita tirones redibujando toda la escena de forma homogénea",
                checked = uiState.dirtyRegionsDisabled,
                onCheckedChange = { viewModel.toggleDirtyRegions(it) }
            )
        }

        item {
            IOSSwitchTile(
                title = "Modo Debug de Cuadros Sucios",
                subtitle = "Muestra visualmente las zonas de la pantalla que se redibujan",
                checked = uiState.showDirtyRegions,
                onCheckedChange = { viewModel.toggleShowDirty(it) }
            )
        }

        item {
            IOSSwitchTile(
                title = "MSAA 4x Forzado",
                subtitle = "Suavizado de contornos gráficos en apps compatibles",
                checked = uiState.msaa4x,
                onCheckedChange = { viewModel.toggleMsaa4x(it) }
            )
        }

        item {
            IOSSwitchTile(
                title = "Optimización de Caché HWUI",
                subtitle = "Expande el búfer de texturas gráficas a 24MB",
                checked = uiState.hwuiCacheOptimized,
                onCheckedChange = { viewModel.toggleHwuiCache(it) }
            )
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}
