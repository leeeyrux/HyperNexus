package com.hypernexus.ui.components

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

class HazeState

data class HazeStyle(
    val backgroundColor: Color = Color(0x99121216),
    val tint: Color = Color.Black.copy(alpha = 0.35f),
    val blurRadius: Dp = 45.dp
)

@Composable
fun rememberHazeState(): HazeState = remember { HazeState() }

fun Modifier.haze(hazeState: HazeState): Modifier = this

fun Modifier.hazeChild(
    hazeState: HazeState,
    shape: Shape,
    style: HazeStyle = HazeStyle()
): Modifier {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        this.clip(shape).blur(radius = style.blurRadius, edgeTreatment = BlurredEdgeTreatment.Unbounded)
    } else {
        this.clip(shape)
    }
}
