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

// Curva estándar de desaceleración iNexus Flow-State (easeOutQuad)
val EaseOutQuadEasing = CubicBezierEasing(0.25f, 0.46f, 0.45f, 0.94f)

@Composable
fun CyberGlowEntranceWrapper(
    index: Int,
    modifier: Modifier = Modifier,
    itemKey: Any? = null,
    content: @Composable () -> Unit
) {
    // Escudo de Memoria: Bloquea animaciones al scrollear hacia arriba o abajo en la misma pestaña
    var hasAnimated by rememberSaveable(key = "flow_anim_${index}_${itemKey ?: index}") {
        mutableStateOf(false)
    }

    if (hasAnimated) {
        Box(modifier = modifier) {
            content()
        }
        return
    }

    val density = LocalDensity.current
    val startOffsetY = with(density) { 30.dp.toPx() }

    val animProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Desfase en cascada secuencial: 50ms exactos por índice
        val delayMs = (index * 50L).coerceAtMost(500L)
        delay(delayMs)
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600, easing = EaseOutQuadEasing)
        )
        hasAnimated = true
    }

    val progress = animProgress.value
    val currentAlpha = progress
    val currentTranslationY = startOffsetY * (1f - progress)

    Box(
        modifier = modifier.graphicsLayer {
            alpha = currentAlpha
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
