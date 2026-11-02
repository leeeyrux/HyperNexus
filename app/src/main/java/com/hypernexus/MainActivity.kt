package com.hypernexus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hypernexus.data.PreferencesRepository
import com.hypernexus.ui.HyperNexusViewModel
import com.hypernexus.ui.screens.*
import com.hypernexus.ui.theme.HyperNexusTheme

class MainActivity : ComponentActivity() {

    private lateinit var shizukuBridge: ShizukuBridge
    private lateinit var privilegedActions: PrivilegedActions
    private lateinit var viewModel: HyperNexusViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        shizukuBridge = ShizukuBridge(this)
        privilegedActions = PrivilegedActions(shizukuBridge)
        val repository = PreferencesRepository(applicationContext)
        viewModel = HyperNexusViewModel(privilegedActions, repository)

        setContent {
            HyperNexusTheme {
                HyperNexusApp(
                    shizukuBridge = shizukuBridge,
                    viewModel = viewModel
                )
            }
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
    viewModel: HyperNexusViewModel
) {
    var selectedTab by remember { mutableStateOf(0) }
    val isConnected by shizukuBridge.isConnected.collectAsState()
    val hasPermission by shizukuBridge.hasPermission.collectAsState()

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
            NavigationBar(
                containerColor = Color(0xFF1C1C1E),
                tonalElevation = 0.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Dashboard, "Panel") },
                    label = { Text("Panel") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF007AFF),
                        unselectedIconColor = Color(0xFF8E8E93)
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Speed, "Rendimiento") },
                    label = { Text("Rendimiento") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF007AFF),
                        unselectedIconColor = Color(0xFF8E8E93)
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.VideogameAsset, "Gráficos") },
                    label = { Text("Gráficos") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF007AFF),
                        unselectedIconColor = Color(0xFF8E8E93)
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Terminal, "Terminal") },
                    label = { Text("Terminal") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF007AFF),
                        unselectedIconColor = Color(0xFF8E8E93)
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = { Icon(Icons.Default.Science, "Experimental") },
                    label = { Text("Experimental") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF007AFF),
                        unselectedIconColor = Color(0xFF8E8E93)
                    )
                )
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
                2 -> GraphicsScreen(viewModel = viewModel)
                3 -> TerminalScreen(viewModel = viewModel)
                4 -> ExperimentalScreen(viewModel = viewModel)
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
