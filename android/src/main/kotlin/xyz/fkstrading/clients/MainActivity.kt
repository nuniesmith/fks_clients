package xyz.fkstrading.clients

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import xyz.fkstrading.clients.api.FksApiClient
import xyz.fkstrading.clients.domain.viewmodel.AuthViewModel
import xyz.fkstrading.clients.ui.screens.LoginScreen
import xyz.fkstrading.clients.ui.screens.SignalMatrixScreen
import xyz.fkstrading.clients.ui.theme.FksDarkColorScheme

/**
 * Main Activity for FKS Trading Android App
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Configure API client (use environment or BuildConfig)
        FksApiClient.configure(
            apiUrl = BuildConfig.FKS_API_URL,
            authUrl = BuildConfig.FKS_AUTH_URL,
            dataUrl = BuildConfig.FKS_DATA_URL,
            portfolioUrl = BuildConfig.FKS_PORTFOLIO_URL
        )
        
        setContent {
            FksApp()
        }
    }
}

@Composable
fun FksApp() {
    val authViewModel = remember { AuthViewModel() }
    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()
    
    MaterialTheme(colorScheme = FksDarkColorScheme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            if (isAuthenticated) {
                // Main app content
                MainNavigation()
            } else {
                // Login screen
                LoginScreen(
                    viewModel = authViewModel,
                    onLoginSuccess = {
                        // Navigation handled by state change
                    }
                )
            }
        }
    }
}

@Composable
fun MainNavigation() {
    var currentScreen by remember { mutableStateOf("signals") }
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentScreen == "signals",
                    onClick = { currentScreen = "signals" },
                    icon = { Text("📊") },
                    label = { Text("Signals") }
                )
                NavigationBarItem(
                    selected = currentScreen == "portfolio",
                    onClick = { currentScreen = "portfolio" },
                    icon = { Text("💼") },
                    label = { Text("Portfolio") }
                )
                NavigationBarItem(
                    selected = currentScreen == "settings",
                    onClick = { currentScreen = "settings" },
                    icon = { Text("⚙️") },
                    label = { Text("Settings") }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (currentScreen) {
                "signals" -> SignalMatrixScreen()
                "portfolio" -> xyz.fkstrading.clients.ui.screens.PortfolioDashboardScreen()
                "settings" -> SettingsScreen()
            }
        }
    }
}

@Composable
fun SettingsScreen() {
    val authViewModel = remember { AuthViewModel() }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = { authViewModel.logout() },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text("Logout")
        }
    }
}
