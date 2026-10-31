package com.hypernexus.ui.components
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hypernexus.ui.theme.*

@Composable
fun IOSSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    width: Dp = 52.dp,
    height: Dp = 32.dp
) {
    val thumbSize = height - 4.dp
    val thumbTravelDistance = width - thumbSize - 4.dp
    
    // Animación del thumb
    val thumbPosition by animateDpAsState(
        targetValue = if (checked) thumbTravelDistance else 2.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "thumbPosition"
    )
    
    // Animación del color de fondo
    val backgroundColor by animateColorAsState(
        targetValue = when {
            !enabled -> SwitchGray.copy(alpha = 0.5f)
            checked -> SwitchGreen
            else -> SwitchGray
        },
        animationSpec = tween(durationMillis = 200),
        label = "backgroundColor"
    )
    
    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .clip(RoundedCornerShape(height / 2))
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = if (checked) Color.Transparent else Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(height / 2)
            )
            .clickable(
                enabled = enabled,
                onClick = { onCheckedChange(!checked) },
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        // Thumb (círculo blanco)
        Box(
            modifier = Modifier
                .offset(x = thumbPosition)
                .size(thumbSize)
                .shadow(
                    elevation = 4.dp,
                    shape = CircleShape,
                    ambientColor = Color.Black.copy(alpha = 0.2f),
                    spotColor = Color.Black.copy(alpha = 0.3f)
                )
                .clip(CircleShape)
                .background(SwitchThumb)
        )
    }
}

// Versión con label integrado estilo iOS Settings
@Composable
fun IOSSwitchTile(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    warningText: String? = null  // Para advertencias como "Puede aumentar temperatura"
) {
    GlassCard(modifier = modifier) {
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
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
                
                // Advertencia opcional
                warningText?.let {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "⚠️",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = it,
                            style = MaterialTheme.typography.labelSmall,
                            color = WarningOrange
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            IOSSwitch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled
            )
        }
    }
}
