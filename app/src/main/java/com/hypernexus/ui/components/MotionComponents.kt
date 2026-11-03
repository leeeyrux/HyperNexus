package com.hypernexus.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun StaggeredEntranceCard(
    index: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val animAlpha = remember { Animatable(0f) }
    val animOffsetY = remember { Animatable(50f) }

    LaunchedEffect(Unit) {
        val delayMs = (index * 30L).coerceAtMost(300L)
        delay(delayMs)
        launch {
            animAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 380, easing = FastOutSlowInEasing)
            )
        }
        launch {
            animOffsetY.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 380, easing = FastOutSlowInEasing)
            )
        }
    }

    Box(
        modifier = modifier.graphicsLayer {
            alpha = animAlpha.value
            translationY = animOffsetY.value
        }
    ) {
        content()
    }
}
