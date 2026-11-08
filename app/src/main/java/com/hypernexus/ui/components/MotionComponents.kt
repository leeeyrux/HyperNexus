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

// ============================================================
// iNEXUS STORE — SCROLL ANIMATION SYSTEM (FÓRMULA 1:1 WEB)
// - Opacidad: 0 -> 1
// - translateY: 30px (30.dp) -> 0px
// - Duración: 600ms (0.6s)
// - Easing: easeOutQuad cubic-bezier(0.25, 0.46, 0.45, 0.94)
// - Stagger: 100ms entre hijos (0ms, 100ms, 200ms, 300ms...)
// - translateX: 0 | scale: 1 | rotateY: 0 (Movimiento vertical puro)
// ============================================================
val INexusEaseOutQuad = CubicBezierEasing(0.25f, 0.46f, 0.45f, 0.94f)

@Composable
fun CyberGlowEntranceWrapper(
    index: Int,
    modifier: Modifier = Modifier,
    itemKey: Any? = null,
    content: @Composable () -> Unit
) {
    // Equivale a: entry.target.classList.add('visible') con threshold: 0.1
    // En cuanto entra en viewport se activa y no vuelve a reiniciarse al scrollear
    var isVisible by rememberSaveable(key = "inexus_scroll_${index}_${itemKey ?: index}") {
        mutableStateOf(false)
    }

    if (isVisible) {
        Box(modifier = modifier) {
            content()
        }
        return
    }

    val density = LocalDensity.current
    // translateY(30px) exacto del CSS de tu web
    val startOffsetY = with(density) { 30.dp.toPx() }

    val animProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Stagger de 100ms exacto de tu web (.feature-pill:nth-child delay)
        val delayMs = (index * 100L).coerceAtMost(500L)
        if (delayMs > 0L) {
            delay(delayMs)
        }
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 600, // 0.6s exactos de tu web
                easing = INexusEaseOutQuad // cubic-bezier(0.25, 0.46, 0.45, 0.94)
            )
        )
        isVisible = true
    }

    // FASE DE DIBUJO PURA (Hardware RenderThread):
    // animProgress.value se lee ÚNICAMENTE dentro de graphicsLayer.
    // La CPU no recompone nodos; la GPU mueve la textura en C++ igual que en Brave.
    Box(
        modifier = modifier.graphicsLayer {
            val progress = animProgress.value
            alpha = progress // opacity: 0 -> 1
            translationY = startOffsetY * (1f - progress) // translateY: 30px -> 0
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
