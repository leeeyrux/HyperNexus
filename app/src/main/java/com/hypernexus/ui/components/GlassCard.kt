package com.hypernexus.ui.components

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * COMPONENTE BASE: CONTENEDOR ESTILO NEOMÓRFICO / GLASSMOPHISM
 * Diseña tarjetas con efecto de desenfoque traslúcido para la API 31+. 
 * Incluye un fallback matemático con gradientes oscuros para sistemas antiguos.
 * 
 * @param glassEnabled Si true usa blur real (API 31+) o gradiente simulado; si false usa solo fondo semitransparente sin blur (ahorro de GPU en gama baja)
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    blurRadius: Dp = 20.dp,
    backgroundColor: Color = Color(0xFF1C1C1E).copy(alpha = 0.7f),
    borderColor: Color = Color.White.copy(alpha = 0.1f),
    glassEnabled: Boolean = true, // Nuevo: controla si se aplica blur/efecto glass
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    
    Box(
        modifier = modifier.shadow(
            elevation = 8.dp,
            shape = shape,
            ambientColor = Color.Black.copy(alpha = 0.3f),
            spotColor = Color.Black.copy(alpha = 0.5f)
        )
    ) {
        // RENDERIZADO DINÁMICO DEL EFECTO DE DESENFOQUE (BLUR FUSED ANCHOR)
        // Solo aplica blur/gradiente si glassEnabled=true; si false -> fondo sólido semitransparente (0 coste GPU)
        if (glassEnabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(shape)
                        .blur(blurRadius)
                        .background(backgroundColor)
                )
            } else {
                // PROCESAMIENTO PARA ANDROID < 12: GRADIENTE DE OPACIDAD SIMULADO
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(shape)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    backgroundColor.copy(alpha = 0.9f),
                                    backgroundColor.copy(alpha = 0.95f)
                                )
                            )
                        )
                )
            }
        } else {
            // MODO AHORRO: solo fondo semitransparente, SIN blur ni gradiente (ideal para Helio G36 / gama baja)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(shape)
                    .background(backgroundColor.copy(alpha = 0.85f))
            )
        }
        
        // BORDE METALIZADO ULTRA FINO (GLASS BORDER EFFECT) - siempre presente
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .border(
                    width = 1.dp,
                    color = borderColor,
                    shape = shape
                )
        )
        
        // INYECCIÓN DE CONTENIDO MODULAR
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            content = content
        )
    }
}
