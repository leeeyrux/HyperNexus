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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hypernexus.data.PreferencesRepository
import com.hypernexus.services.HyperNexusVpnService
import com.hypernexus.ui.HyperNexusViewModel
import com.hypernexus.ui.components.MainNavigationCapsule
import com.hypernexus.ui.screens.*
import com.hypernexus.ui.theme.HyperNexusTheme

class MainActivity : ComponentActivity() {

    private lateinit var shizukuBridge: ShizukuBridge
    private lateinit var privilegedActions: PrivilegedActions
    private lateinit var viewModel: HyperNexusViewModel

    private var pendingVpnTargetPackage: String? = null

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

        // Lock a 60Hz para asegurar fluidez sin tirones
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
                val nexusState by viewModel.uiState.collectAsState()
                androidx.compose.runtime.CompositionLocalProvider(
                    com.hypernexus.ui.components.LocalGlassEffectEnabled provides nexusState.glassEffectEnabled
                ) {
                    HyperNexusApp(
                        shizukuBridge = shizukuBridge,
                        viewModel = viewModel,
                        onRequestVpnStart = { pkg -> requestVpnActivation(pkg) },
                        onRequestVpnStop = { executeVpnStop() }
                    )
                }
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

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            if (!hasPermission) shizukuBridge.requestPermission()
                        }
                    ) {
                        Text("HyperNexus", style = MaterialTheme.typography.titleLarge, color = Color.White)
                        Text(
                            text = if (isConnected) "● Shizuku Conectado" else "○ Shizuku Desconectado (Toca para conceder)",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isConnected) Color(0xFF30D158) else Color(0xFFFF9500)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Black)
            )
        }
    ) { scaffoldPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = scaffoldPadding.calculateTopPadding())
                .background(Color.Black)
        ) {
            // Contenido completo pasando libremente detrás de la cápsula flotante
            Box(modifier = Modifier.fillMaxSize()) {
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
            }

            // Cápsula Liquid Glass Flotante (Desacoplada 20dp lateral y 16dp inferior)
            MainNavigationCapsule(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(start = 20.dp, end = 20.dp, bottom = 16.dp)
            )
        }
    }
}
