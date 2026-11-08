package com.hypernexus.ui.components

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class NavNode(val title: String, val icon: ImageVector)

val NavigationSwipeEasing = CubicBezierEasing(0.25f, 0.46f, 0.45f, 0.94f)

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

    // Geometría de curvatura ovalada absoluta
    val capsuleShape = RoundedCornerShape(100.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .shadow(
                elevation = 16.dp,
                shape = capsuleShape,
                ambientColor = Color.Black.copy(alpha = 0.6f),
                spotColor = Color.Black.copy(alpha = 0.8f)
            )
    ) {
        // CAPA 1: GLASS PURO CERO BLUR (SUPERFICIE OSCURA TRANSLÚCIDA ULTRA LIGERA)
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(capsuleShape)
                .background(Color(0xD9121216))
        )

        // CAPA 2: RESALTE DE BORDE 3D (DESTELLO CRISTAL REFRACTIVO EN EXTREMOS)
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(capsuleShape)
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        listOf(
                            Color(0x52FFFFFF),
                            Color(0x0FFFFFFF),
                            Color(0x52FFFFFF)
                        )
                    ),
                    shape = capsuleShape
                )
        )

        // CAPA 3: SWIPE TRACKER HORIZONTAL CON SELECCIÓN MONOCROMÁTICA BOLD-WHITE
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = Color.White,
            divider = {},
            indicator = {},
            edgePadding = 16.dp,
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center)
        ) {
            navNodes.forEachIndexed { index, node ->
                val isSelected = selectedTab == index

                val animScale by animateFloatAsState(
                    targetValue = if (isSelected) 1.0f else 0.92f,
                    animationSpec = tween(durationMillis = 350, easing = NavigationSwipeEasing),
                    label = "capsuleScale"
                )

                val animAlpha by animateFloatAsState(
                    targetValue = if (isSelected) 1.0f else 0.45f,
                    animationSpec = tween(durationMillis = 350, easing = NavigationSwipeEasing),
                    label = "capsuleAlpha"
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
                                },
                            tint = Color.White.copy(alpha = animAlpha)
                        )
                    }
                )
            }
        }
    }
}
