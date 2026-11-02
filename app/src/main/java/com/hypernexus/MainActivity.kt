package com.hypernexus

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hypernexus.data.PreferencesRepository
import com.hypernexus.services.HyperNexusVpnService
import com.hypernexus.ui.HyperNexusViewModel
import com.hypernexus.ui.screens.*
import com.hypernexus.ui.theme.HyperNexusTheme

data class NavItem(val title: String, val icon: ImageVector)

class MainActivity : ComponentActivity() {

    private lateinit var shizukuBridge: ShizukuBridge
    private lateinit var privilegedActions: PrivilegedActions
    private lateinit var viewModel: HyperNexusViewModel

    private var pendingVpnTargetPackage: String? = null

    // Contrato oficial para interceptar la autorización del sistema VpnService sin caídas
    private val vpnPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val target = pendingVpnTargetPackage ?: ""
            executeVpnStart(target)
        } else {
            Toast.makeText(this, "Permiso de túnel VPN cancelado", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Fijación a 60Hz para evitar vibraciones en recomposiciones
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val params = window.attributes
                params.preferredRefreshRate = 60f
                window.attributes = params
            } catch (_: Exception) {}
        }

        shizukuBridge = ShizukuBridge(this)
        privilegedActions = PrivilegedActions(shizukuBridge)
        val repository = PreferencesRepository(applicationContext)
        viewModel = HyperNexusViewModel(privilegedActions, repository)

        setContent {
            HyperNexusTheme {
                HyperNexusApp(
                    shizukuBridge = shizukuBridge,
                    viewModel = viewModel,
                    onRequestVpnStart = { pkg -> requestVpnActivation(pkg) },
                    onRequestVpnStop = { executeVpnStop() }
                )
            }
        }
    }

    private fun requestVpnActivation(targetPackage: String) {
        pendingVpnTargetPackage = targetPackage
        try {
            val prepareIntent = VpnService.prepare(this)
            if (prepareIntent != null) {
                vpnPermissionLauncher.launch(prepareIntent)
            } else {
                executeVpnStart(targetPackage)
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error al preparar VpnService", e)
            executeVpnStart(targetPackage)
        }
    }

    private fun executeVpnStart(targetPackage: String) {
        try {
            viewModel.toggleVpnTunnel(this, targetPackage, true)
            Toast.makeText(this, "Gaming VPN Conectado", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("MainActivity", "Error al iniciar servicio VPN", e)
        }
    }

    private fun executeVpnStop() {
        try {
            viewModel.toggleVpnTunnel(this, "", false)
            Toast.makeText(this, "Túnel VPN Desconectado", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("MainActivity", "Error al detener servicio VPN", e)
        }
    }

    override fun onResume() {
        super.onResume()
        shizukuBridge.bindService()
    }

    override fun onPause() {
        super.onPause()
        shizukuBridge.unbindService()
    }

    override fun onDestroy() {
        super.onDestroy()
        shizukuBridge.cleanup()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HyperNexusApp(
    shizukuBridge: ShizukuBridge,
    viewModel: HyperNexusViewModel,
    onRequestVpnStart: (String) -> Unit,
    onRequestVpnStop: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val isConnected by shizukuBridge.isConnected.collectAsState()
    val hasPermission by shizukuBridge.hasPermission.collectAsState()

    val navTabs = remember {
        listOf(
            NavItem("Panel", Icons.Default.Dashboard),
            NavItem("Rendimiento", Icons.Default.Speed),
            NavItem("Gráficos", Icons.Default.VideogameAsset),
            NavItem("Seguridad", Icons.Default.Shield),
            NavItem("Módulos", Icons.Default.Extension),
            NavItem("Terminal", Icons.Default.Terminal),
            NavItem("Experimental", Icons.Default.Science)
        )
    }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("HyperNexus", style = MaterialTheme.typography.titleLarge, color = Color.White)
                        Text(
                            text = if (isConnected) "● Shizuku Conectado" else "○ Shizuku Desconectado",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isConnected) Color(0xFF30D158) else Color(0xFFFF9500)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Black)
            )
        },
        bottomBar = {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF141416),
                contentColor = Color(0xFF007AFF),
                edgePadding = 12.dp,
                indicator = { tabPositions ->
                    if (tabPositions.isNotEmpty() && selectedTab in tabPositions.indices) {
                        TabRowDefaults.Indicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = Color(0xFF007AFF),
                            height = 3.dp
                        )
                    }
                },
                divider = {}
            ) {
                navTabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = tab.title,
                                fontSize = 11.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        selectedContentColor = Color(0xFF007AFF),
                        unselectedContentColor = Color(0xFF8E8E93)
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.Black)
        ) {
            when (selectedTab) {
                0 -> PanelScreen(viewModel = viewModel)
                1 -> RendimientoScreen(viewModel = viewModel)
                2 -> GraficosScreen(viewModel = viewModel)
                3 -> SeguridadScreen(
                    viewModel = viewModel,
                    onRequestVpnStart = onRequestVpnStart,
                    onRequestVpnStop = onRequestVpnStop
                )
                4 -> ModulosScreen(viewModel = viewModel)
                5 -> TerminalScreen(viewModel = viewModel)
                6 -> ExperimentalScreen(viewModel = viewModel)
            }

            if (!hasPermission) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF1C1C1E)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Permiso Shizuku Requerido", style = MaterialTheme.typography.titleMedium, color = Color.White)
                            Text("Indispensable para aplicar optimizaciones", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF8E8E93))
                        }
                        Button(onClick = { shizukuBridge.requestPermission() }) {
                            Text("Conceder")
                        }
                    }
                }
            }
        }
    }
}
