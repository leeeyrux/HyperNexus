package com.hypernexus.ui.components

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

// Ease-out quad function: 1 - (1 - t)^2
@kotlin.jvm.JvmInline
value class EaseOutQuadProgress(private val value: Float) {
    operator fun component1(): Float = value
    fun eased(): Float = 1f - (1f - value) * (1f - value)
}

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

    // Animation state driven by manual frame clock - BYPASSES animator_duration_scale
    var animProgress by remember { mutableStateOf(0f) }
    var startTime by remember { mutableStateOf(0L) }
    var animationJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

    LaunchedEffect(Unit) {
        // Stagger de 100ms exacto de tu web (.feature-pill:nth-child delay)
        val delayMs = (index * 100L).coerceAtMost(500L)
        if (delayMs > 0L) {
            delay(delayMs)
        }

        startTime = System.nanoTime()
        val durationNanos = 600_000_000L // 600ms en nanosegundos

        animationJob = launch {
            // Manual frame-by-frame animation loop using withFrameNanos
            // This bypasses the high-level Compose animation framework entirely
            // and drives coordinates directly on the hardware graphics layer
            while (animProgress < 1f) {
                val currentTime = withFrameNanos { it }
                val elapsed = currentTime - startTime
                val rawProgress = (elapsed.toFloat() / durationNanos.toFloat()).coerceIn(0f, 1f)
                
                // Apply easeOutQuad easing manually
                animProgress = 1f - (1f - rawProgress) * (1f - rawProgress)
                
                if (rawProgress >= 1f) {
                    animProgress = 1f
                    break
                }
            }
            isVisible = true
        }
    }

    // Cleanup on recomposition
    DisposableEffect(Unit) {
        onDispose {
            animationJob?.cancel()
        }
    }

    // FASE DE DIBUJO PURA (Hardware RenderThread):
    // animProgress se lee ÚNICAMENTE dentro de graphicsLayer.
    // La CPU no recompone nodos; la GPU mueve la textura en C++ igual que en Brave.
    // Bypass completo del animator_duration_scale del sistema.
    Box(
        modifier = modifier.graphicsLayer {
            val progress = animProgress
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
