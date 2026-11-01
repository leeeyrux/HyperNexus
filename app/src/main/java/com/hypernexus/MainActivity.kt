package com.hypernexus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hypernexus.ui.theme.HyperNexusTheme
import com.hypernexus.ui.screens.*

class MainActivity : ComponentActivity() {
    
    private lateinit var shizukuBridge: ShizukuBridge
    private lateinit var privilegedActions: PrivilegedActions
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        shizukuBridge = ShizukuBridge(this)
        privilegedActions = PrivilegedActions(shizukuBridge)
        
        setContent {
            HyperNexusTheme {
                HyperNexusApp(
                    shizukuBridge = shizukuBridge,
                    privilegedActions = privilegedActions
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
    privilegedActions: PrivilegedActions
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
                        Text(
                            text = "HyperNexus",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White
                        )
                        Text(
                            text = if (isConnected) "● Conectado" else "○ Desconectado",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isConnected) Color(0xFF30D158) else Color(0xFFFF9500)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Black
                )
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
                0 -> DashboardScreen(privilegedActions)
                1 -> PerformanceScreen(privilegedActions)
                2 -> GraphicsScreen(privilegedActions)
                3 -> ExperimentalScreen(privilegedActions)
            }
            
            if (!hasPermission) {
                PermissionBanner(
                    onRequestPermission = { shizukuBridge.requestPermission() },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}

@Composable
fun PermissionBanner(
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        color = Color(0xFF1C1C1E)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Permiso Shizuku requerido",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                Text(
                    text = "Para aplicar tweaks de rendimiento",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF8E8E93)
                )
            }
            Button(
                onClick = onRequestPermission,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF007AFF)
                )
            ) {
                Text("Conceder")
            }
        }
    }
}
