package xyz.fkstrading.clients

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import xyz.fkstrading.clients.api.FksApiClient
import xyz.fkstrading.clients.domain.viewmodel.AuthViewModel
import xyz.fkstrading.clients.ui.screens.*
import xyz.fkstrading.clients.ui.theme.FksDarkColorScheme

fun main() = application {
    // Configure API URLs from environment
    FksApiClient.instance.configure(
        apiUrl = System.getenv("FKS_API_URL") ?: "http://localhost:8001",
        authUrl = System.getenv("FKS_AUTH_URL") ?: "http://localhost:8009",
        dataUrl = System.getenv("FKS_DATA_URL") ?: "http://localhost:8003",
        portfolioUrl = System.getenv("FKS_PORTFOLIO_URL") ?: "http://localhost:8012"
    )
    
    Window(
        onCloseRequest = ::exitApplication,
        title = "FKS Trading",
        state = rememberWindowState(width = 1200.dp, height = 800.dp)
    ) {
        FksDesktopApp()
    }
}

@Composable
fun FksDesktopApp() {
    val authViewModel = remember { AuthViewModel() }
    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()
    
    MaterialTheme(colorScheme = FksDarkColorScheme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            if (isAuthenticated) {
                MainContent(authViewModel)
            } else {
                LoginScreen(
                    viewModel = authViewModel,
                    onLoginSuccess = { /* State handles navigation */ }
                )
            }
        }
    }
}

@Composable
fun MainContent(authViewModel: AuthViewModel) {
    var currentScreen by remember { mutableStateOf("signals") }
    
    Row(modifier = Modifier.fillMaxSize()) {
        // Sidebar
        NavigationRail(
            modifier = Modifier.fillMaxHeight()
        ) {
            NavigationRailItem(
                selected = currentScreen == "signals",
                onClick = { currentScreen = "signals" },
                icon = { Text("📊") },
                label = { Text("Signals") }
            )
            NavigationRailItem(
                selected = currentScreen == "portfolio",
                onClick = { currentScreen = "portfolio" },
                icon = { Text("💼") },
                label = { Text("Portfolio") }
            )
            NavigationRailItem(
                selected = currentScreen == "evaluation",
                onClick = { currentScreen = "evaluation" },
                icon = { Text("📈") },
                label = { Text("Evaluation") }
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            NavigationRailItem(
                selected = false,
                onClick = { authViewModel.logout() },
                icon = { Text("🚪") },
                label = { Text("Logout") }
            )
        }
        
        // Main content
        Box(modifier = Modifier.fillMaxSize()) {
            when (currentScreen) {
                "signals" -> SignalMatrixScreen()
                "portfolio" -> PortfolioDashboardScreen()
                "evaluation" -> EvaluationMatrixScreen()
            }
        }
    }
}
