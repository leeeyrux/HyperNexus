package com.hypernexus.ui.components

import androidx.compose.foundation.background
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
import com.hypernexus.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun IOSSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    steps: Int = 0,
    showBubble: Boolean = true,  // Burbuja flotante estilo iOS
    formatValue: (Float) -> String = { it.roundToInt().toString() }
) {
    var sliderSize by remember { mutableStateOf(IntSize.Zero) }
    var isDragging by remember { mutableStateOf(false) }
    
    val density = LocalDensity.current
    val trackHeight = 6.dp
    val thumbSize = 28.dp
    
    // Calcular posición del thumb
    val fraction = ((value - valueRange.start) / (valueRange.endInclusive - valueRange.start))
        .coerceIn(0f, 1f)
    
    BoxWithConstraints(
        modifier = modifier
            .height(40.dp)
            .onSizeChanged { sliderSize = it }
    ) {
        val widthInPx = constraints.maxWidth.toFloat()
        val thumbTravelDistance = widthInPx - with(density) { thumbSize.toPx() }
        val thumbX = thumbTravelDistance * fraction
        
        // Track de fondo
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxWidth()
                .height(trackHeight)
                .clip(RoundedCornerShape(trackHeight / 2))
                .background(SliderTrackInactive)
        )
        
        // Track activo (progreso)
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .width(with(density) { (thumbX + thumbSize.toPx() / 2).toDp() })
                .height(trackHeight)
                .clip(RoundedCornerShape(trackHeight / 2))
                .background(if (enabled) SliderTrackActive else SliderTrackActive.copy(alpha = 0.5f))
        )
        
        // Thumb
        Box(
            modifier = Modifier
                .offset(x = with(density) { thumbX.toDp() })
                .size(thumbSize)
                .shadow(
                    elevation = if (isDragging) 8.dp else 4.dp,
                    shape = CircleShape
                )
                .clip(CircleShape)
                .background(SwitchThumb)
                .pointerInput(enabled) {
                    if (enabled) {
                        detectDragGestures(
                            onDragStart = { isDragging = true },
                            onDragEnd = { isDragging = false },
                            onDragCancel = { isDragging = false },
                            onDrag = { change, _ ->
                                change.consume()
                                val newX = (change.position.x).coerceIn(0f, thumbTravelDistance)
                                val newFraction = newX / thumbTravelDistance
                                val newValue = valueRange.start + (valueRange.endInclusive - valueRange.start) * newFraction
                                
                                // Aplicar steps si existen
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
        
        // Burbuja de valor (estilo iOS brightness)
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
                    color = TextPrimary
                )
            }
        }
    }
}

// Slider con label estilo Settings
@Composable
fun IOSSliderTile(
    title: String,
    subtitle: String? = null,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
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
                        color = if (enabled) TextPrimary else TextSecondary
                    )
                    subtitle?.let {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
                
                // Valor actual
                Text(
                    text = formatValue(value),
                    style = MaterialTheme.typography.titleMedium,
                    color = AccentBlue
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