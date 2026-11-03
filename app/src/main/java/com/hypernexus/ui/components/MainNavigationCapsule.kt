package com.hypernexus.ui.components

import android.os.Build
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class NavNode(val title: String, val icon: ImageVector)

val CubeSwipeEasing = CubicBezierEasing(0.2f, 0.8f, 0.2f, 1.0f)

@Composable
fun MainNavigationCapsule(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val navNodes = remember {
        listOf(
            NavNode("Panel", Icons.Default.Dashboard),
            NavNode("Rendimiento", Icons.Default.Speed),
            NavNode("Gráficos", Icons.Default.VideogameAsset),
            NavNode("Seguridad", Icons.Default.Shield),
            NavNode("Módulos", Icons.Default.Extension),
            NavNode("Terminal", Icons.Default.Terminal),
            NavNode("Experimental", Icons.Default.Science)
        )
    }

    val capsuleShape = RoundedCornerShape(28.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .shadow(
                elevation = 16.dp,
                shape = capsuleShape,
                ambientColor = Color.Black.copy(alpha = 0.6f),
                spotColor = Color.Black.copy(alpha = 0.8f)
            )
    ) {
        // Capa 1: Efecto Liquid Glass Blur por Hardware
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(capsuleShape)
                    .blur(radius = 12.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
                    .background(Color(0x991A1A1A))
            )
        } else {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(capsuleShape)
                    .background(Color(0xCC1A1A1A))
            )
        }

        // Capa 2: Borde ultrafino neón plateado
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(capsuleShape)
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.2f),
                    shape = capsuleShape
                )
        )

        // Capa 3: Swipe horizontal no-lineal 3D-Cube (Monochrome Bold-White)
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = Color.White,
            divider = {},
            indicator = {},
            edgePadding = 12.dp,
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center)
        ) {
            navNodes.forEachIndexed { index, node ->
                val isSelected = selectedTab == index

                val animScale by animateFloatAsState(
                    targetValue = if (isSelected) 1.0f else 0.92f,
                    animationSpec = tween(durationMillis = 350, easing = CubeSwipeEasing),
                    label = "cubeScale"
                )

                val animAlpha by animateFloatAsState(
                    targetValue = if (isSelected) 1.0f else 0.45f,
                    animationSpec = tween(durationMillis = 350, easing = CubeSwipeEasing),
                    label = "cubeAlpha"
                )

                val animShiftX by animateFloatAsState(
                    targetValue = if (isSelected) 0f else if (selectedTab > index) -3f else 3f,
                    animationSpec = tween(durationMillis = 350, easing = CubeSwipeEasing),
                    label = "cubeShift"
                )

                Tab(
                    selected = isSelected,
                    onClick = { onTabSelected(index) },
                    modifier = Modifier.padding(horizontal = 4.dp),
                    text = {
                        Text(
                            text = node.title,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = Color.White.copy(alpha = animAlpha),
                            letterSpacing = if (isSelected) 0.5.sp else 0.sp
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = node.icon,
                            contentDescription = node.title,
                            modifier = Modifier
                                .size(20.dp)
                                .graphicsLayer {
                                    scaleX = animScale
                                    scaleY = animScale
                                    translationX = animShiftX
                                },
                            tint = Color.White.copy(alpha = animAlpha)
                        )
                    }
                )
            }
        }
    }
}
