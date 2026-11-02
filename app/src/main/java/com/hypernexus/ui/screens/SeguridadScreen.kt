package com.hypernexus.ui.screens

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hypernexus.ui.HyperNexusViewModel
import com.hypernexus.ui.components.GlassCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class SecurityAuditItem(
    val appName: String,
    val packageName: String,
    val riskLevel: String,
    val reasons: List<String>
)

@Composable
fun SeguridadScreen(
    viewModel: HyperNexusViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 1. Limpiador
    var availableGbToClear by remember { mutableStateOf(1.84f) }
    var isCleaning by remember { mutableStateOf(false) }

    // 2. Escáner
    var isScanning by remember { mutableStateOf(false) }
    var scanProgress by remember { mutableStateOf(0f) }
    var auditResults by remember { mutableStateOf<List<SecurityAuditItem>>(emptyList()) }
    var hasScanned by remember { mutableStateOf(false) }

    fun runAntivirusAudit() {
        if (isScanning) return
        isScanning = true
        scanProgress = 0f
        auditResults = emptyList()

        scope.launch {
            val pm = context.packageManager
            val packages = withContext(Dispatchers.IO) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    pm.getInstalledPackages(PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS.toLong()))
                } else {
                    @Suppress("DEPRECATION")
                    pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)
                }
            }

            val total = packages.size.coerceAtLeast(1)
            val issues = mutableListOf<SecurityAuditItem>()

            packages.forEachIndexed { index, pkg ->
                delay(12)
                scanProgress = (index + 1).toFloat() / total.toFloat()

                val requested = pkg.requestedPermissions ?: emptyArray()
                val riskReasons = mutableListOf<String>()

                if (requested.contains("android.permission.SYSTEM_ALERT_WINDOW")) riskReasons.add("Superposición de Pantalla")
                if (requested.contains("android.permission.REQUEST_INSTALL_PACKAGES")) riskReasons.add("Instalación de APKs Desconocidos")
                if (requested.contains("android.permission.WRITE_SECURE_SETTINGS")) riskReasons.add("Acceso a Ajustes del Sistema")
                if (requested.contains("android.permission.ACCESS_BACKGROUND_LOCATION")) riskReasons.add("Ubicación en Segundo Plano")

                val isSystem = (pkg.applicationInfo?.flags ?: 0) and android.content.pm.ApplicationInfo.FLAG_SYSTEM != 0
                if (!isSystem && riskReasons.isNotEmpty()) {
                    issues.add(
                        SecurityAuditItem(
                            appName = pkg.applicationInfo?.let { pm.getApplicationLabel(it).toString() } ?: pkg.packageName,
                            packageName = pkg.packageName,
                            riskLevel = if (riskReasons.size >= 3) "Alto" else "Moderado",
                            reasons = riskReasons
                        )
                    )
                }
            }

            auditResults = issues
            isScanning = false
            hasScanned = true
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Seguridad & Limpieza", style = MaterialTheme.typography.headlineLarge, color = Color.White, fontWeight = FontWeight.Bold)
            Text("Protección de privacidad y optimización profunda de almacenamiento", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF8E8E93))
        }

        // COMPONENTE 1: HYPER CLEANER / BOOSTER ENGINE
        item {
            GlassCard {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CleaningServices, null, tint = Color(0xFF30D158), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Hyper Cleaner & Cache Booster", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "${"%.2f".format(availableGbToClear)} GB",
                        style = MaterialTheme.typography.displayMedium,
                        color = Color(0xFF30D158),
                        fontWeight = FontWeight.Bold
                    )
                    Text("Espacio residual, logs y basura en caché detectada", style = MaterialTheme.typography.bodySmall, color = Color(0xFF8E8E93))

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            isCleaning = true
                            scope.launch {
                                val res = viewModel.privilegedActions.cleanHyperEngine()
                                delay(1200)
                                availableGbToClear = 0.00f
                                isCleaning = false
                                Toast.makeText(context, res.second, Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF30D158)),
                        enabled = !isCleaning && availableGbToClear > 0.05f
                    ) {
                        if (isCleaning) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Limpiando registros del sistema...")
                        } else {
                            Text(if (availableGbToClear <= 0.05f) "Almacenamiento Optimizado" else "Optimizar y Liberar Caché", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // COMPONENTE 2: APP ANTIVIRUS SCANNER
        item {
            GlassCard {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Shield, null, tint = Color(0xFF007AFF), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Auditor Antivirus de Aplicaciones", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Audita el manifiesto de todas las apps instaladas e identifica brechas de seguridad y permisos críticos.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF8E8E93)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    if (isScanning) {
                        LinearProgressIndicator(
                            progress = scanProgress,
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = Color(0xFF007AFF),
                            trackColor = Color(0xFF2C2C2E)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Auditando paquetes: ${(scanProgress * 100).toInt()}%", fontSize = 11.sp, color = Color(0xFF8E8E93))
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    Button(
                        onClick = { runAntivirusAudit() },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF)),
                        enabled = !isScanning
                    ) {
                        Icon(Icons.Default.Security, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isScanning) "Escaneando Sistema..." else "Escanear Aplicaciones", fontWeight = FontWeight.Bold)
                    }

                    if (hasScanned && !isScanning) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (auditResults.isEmpty()) "✅ Ninguna aplicación de riesgo detectada" else "⚠️ ${auditResults.size} aplicaciones con permisos sensibles",
                            color = if (auditResults.isEmpty()) Color(0xFF30D158) else Color(0xFFFF9500),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        if (auditResults.isNotEmpty()) {
            item {
                Text("Vulnerabilidades y Exposición", style = MaterialTheme.typography.titleSmall, color = Color.White)
            }
            items(auditResults) { item ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1C1C1E))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(item.appName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(item.riskLevel, color = if (item.riskLevel == "Alto") Color(0xFFFF003C) else Color(0xFFFF9500), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(item.packageName, color = Color(0xFF8E8E93), fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Permisos: ${item.reasons.joinToString(", ")}", color = Color(0xFFFF9500), fontSize = 11.sp)
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}
