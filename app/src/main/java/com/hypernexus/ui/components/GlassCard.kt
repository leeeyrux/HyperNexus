package com.hypernexus.ui.components

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hypernexus.ui.theme.*

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    blurRadius: Dp = 20.dp,
    backgroundColor: Color = CardGlass,
    borderColor: Color = Color.White.copy(alpha = 0.1f),
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    
    Box(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.3f),
                spotColor = Color.Black.copy(alpha = 0.5f)
            )
    ) {
        // Blur effect (API 31+) o fallback
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(shape)
                    .blur(blurRadius)
                    .background(backgroundColor)
            )
        } else {
            // Fallback para Android < 12: gradiente simulado
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
        
        // Borde sutil estilo glass
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
        
        // Contenido
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            content = content
        )
    }
}