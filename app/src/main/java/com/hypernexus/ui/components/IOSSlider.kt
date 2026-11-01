package com.hypernexus.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * COMPONENTE: SLIDER ESTILO iOS PREMIUM
 * Deslizador táctil de baja latencia con burbuja flotante predictiva.
 * Diseñado específicamente para el ajuste de buffers gráficos y frecuencias de CPU.
 */
@Composable
fun IOSSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    steps: Int = 0,
    showBubble: Boolean = true,
    formatValue: (Float) -> String = { it.roundToInt().toString() }
) {
    var sliderSize by remember { mutableStateOf(IntSize.Zero) }
    var isDragging by remember { mutableStateOf(false) }
    
    val density = LocalDensity.current
    val trackHeight = 6.dp
    val thumbSize = 28.dp
    
    val fraction = ((value - valueRange.start) / 
            (valueRange.endInclusive - valueRange.start)).coerceIn(0f, 1f)
    
    BoxWithConstraints(
        modifier = modifier
            .height(40.dp)
            .onSizeChanged { sliderSize = it }
    ) {
        val widthInPx = constraints.maxWidth.toFloat()
        val thumbTravelDistance = widthInPx - with(density) { thumbSize.toPx() }
        val thumbX = thumbTravelDistance * fraction
        
        // Track Inactivo (Fondo Gris Industrial)
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxWidth()
                .height(trackHeight)
                .clip(RoundedCornerShape(trackHeight / 2))
                .background(Color(0xFF3A3A3C))
        )
        
        // Track Activo (Progreso Azul iOS / Opaco Inactivo)
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .width(with(density) { (thumbX + thumbSize.toPx() / 2).toDp() })
                .height(trackHeight)
                .clip(RoundedCornerShape(trackHeight / 2))
                .background(if (enabled) Color(0xFF007AFF) else Color(0xFF007AFF).copy(alpha = 0.4f))
        )
        
        // Thumb (Botón Deslizador Blanco Puro)
        Box(
            modifier = Modifier
                .offset(x = with(density) { thumbX.toDp() })
                .size(thumbSize)
                .shadow(elevation = if (isDragging) 8.dp else 4.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(Color.White)
                .pointerInput(enabled) {
                    if (enabled) {
                        detectDragGestures(
                            onDragStart = { isDragging = true },
                            onDragEnd = { isDragging = false },
                            onDragCancel = { isDragging = false },
                            onDrag = { change, _ ->
                                change.consume()
                                val newX = change.position.x.coerceIn(0f, thumbTravelDistance)
                                val newFraction = newX / thumbTravelDistance
                                val newValue = valueRange.start + 
                                    (valueRange.endInclusive - valueRange.start) * newFraction
                                
                                val steppedValue = if (steps > 0) {
                                    val stepSize = (valueRange.endInclusive - valueRange.start) / (steps + 1)
                                    (valueRange.start + (newValue / stepSize).roundToInt() * stepSize)
                                        .coerceIn(valueRange.start, valueRange.endInclusive)
                                } else newValue
                                
                                onValueChange(steppedValue)
                            }
                        )
                    }
                }
        )
        
        // Burbuja Flotante Dinámica (Estilo Control de Brillo)
        if (showBubble && isDragging) {
            Box(
                modifier = Modifier
                    .offset(
                        x = with(density) { (thumbX + thumbSize.toPx() / 2).toDp() } - 30.dp,
                        y = (-50).dp
                    )
                    .size(width = 60.dp, height = 40.dp)
                    .shadow(8.dp, RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xE61C1C1E))
                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = formatValue(value),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
            }
        }
    }
}

/**
 * COMPONENTE: CONTENEDOR INTEGRADO (SLIDER TILE)
 * Encapsula la tarjeta GlassCard junto con etiquetas de título, subtítulo e indicador.
 */
@Composable
fun IOSSliderTile(
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    enabled: Boolean = true,
    steps: Int = 0,
    formatValue: (Float) -> String = { it.roundToInt().toString() }
) {
    GlassCard(modifier = modifier) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (enabled) Color.White else Color(0xFF8E8E93)
                    )
                    subtitle?.let {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF8E8E93)
                        )
                    }
                }
                
                Text(
                    text = formatValue(value),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF007AFF)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            IOSSlider(
                value = value,
                onValueChange = onValueChange,
                valueRange = valueRange,
                enabled = enabled,
                steps = steps,
                formatValue = formatValue
            )
        }
    }
}
