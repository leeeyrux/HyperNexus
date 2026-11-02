package com.hypernexus.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun GraphicsRendererSelector(
    selectedRenderer: String,
    onRendererSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val renderers = listOf(
        "vulkan" to "Vulkan (Alto Desempeño)",
        "vulkan_lite" to "Vulkan Lite (Bajo Consumo)",
        "opengl" to "OpenGL SkiaGL",
        "opengl_lite" to "OpenGL Lite (Estabilidad)"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Motor de Renderizado Gráfico",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(10.dp))

        renderers.forEach { (key, label) ->
            val isSelected = selectedRenderer.equals(key, ignoreCase = true)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) Color(0xFF007AFF).copy(alpha = 0.15f) else Color(0xFF1C1C1E))
                    .clickable { onRendererSelected(key) }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = isSelected,
                    onClick = { onRendererSelected(key) },
                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF007AFF))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isSelected) Color.White else Color(0xFF8E8E93),
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}
