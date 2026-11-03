package com.hypernexus.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

val CyberCubeEasing = CubicBezierEasing(0.2f, 0.8f, 0.2f, 1.0f)

@Composable
fun CyberGlowEntranceWrapper(
    index: Int,
    modifier: Modifier = Modifier,
    itemKey: Any? = null,
    content: @Composable () -> Unit
) {
    var hasAnimated by rememberSaveable(key = "cyber_anim_${index}_${itemKey ?: index}") {
        mutableStateOf(false)
    }

    if (hasAnimated) {
        Box(modifier = modifier) {
            content()
        }
        return
    }

    val density = LocalDensity.current
    val startOffsetX = with(density) { (-45).dp.toPx() }
    val startOffsetY = with(density) { 35.dp.toPx() }

    val animProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        val delayMs = (index * 25L).coerceAtMost(350L)
        delay(delayMs)
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 375, easing = CyberCubeEasing)
        )
        hasAnimated = true
    }

    val progress = animProgress.value
    val currentAlpha = progress
    val currentScale = 0.90f + (0.10f * progress)
    val currentTranslationX = startOffsetX * (1f - progress)
    val currentTranslationY = startOffsetY * (1f - progress)

    Box(
        modifier = modifier.graphicsLayer {
            alpha = currentAlpha
            scaleX = currentScale
            scaleY = currentScale
            translationX = currentTranslationX
            translationY = currentTranslationY
        }
    ) {
        content()
    }
}

@Composable
fun StaggeredEntranceCard(
    index: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    CyberGlowEntranceWrapper(index = index, modifier = modifier, content = content)
}
