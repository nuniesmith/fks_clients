package xyz.fkstrading.clients.web

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.web.renderComposable
import xyz.fkstrading.clients.api.FksApiClient
import xyz.fkstrading.clients.domain.viewmodel.AuthViewModel
import xyz.fkstrading.clients.ui.screens.*
import xyz.fkstrading.clients.ui.theme.*

/**
 * Web app entry point for FKS Trading Platform
 * Uses Compose Multiplatform to share code with Android, Desktop, and iOS
 */
fun main() {
    // Configure API URLs - can be set via window globals or environment
    configureApiUrls()
    
    // Render the app to the root element
    renderComposable(rootElementId = "root") {
        FksWebApp()
    }
}

fun configureApiUrls() {
    // Try to get from window globals (set in index.html script tag)
    val apiUrl = js("window.FKS_API_URL") as? String ?: "http://localhost:8001"
    val authUrl = js("window.FKS_AUTH_URL") as? String ?: "http://localhost:8009"
    val dataUrl = js("window.FKS_DATA_URL") as? String ?: "http://localhost:8003"
    val portfolioUrl = js("window.FKS_PORTFOLIO_URL") as? String ?: "http://localhost:8012"
    
    FksApiClient.instance.configure(
        apiUrl = apiUrl,
        authUrl = authUrl,
        dataUrl = dataUrl,
        portfolioUrl = portfolioUrl
    )
}

@Composable
fun FksWebApp() {
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
                    onLoginSuccess = { /* State will update automatically */ }
                )
            }
        }
    }
}

@Composable
fun MainContent(authViewModel: AuthViewModel) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Signals) }
    
    Row(modifier = Modifier.fillMaxSize()) {
        // Navigation sidebar
        NavigationSidebar(
            currentScreen = currentScreen,
            onScreenSelected = { currentScreen = it },
            onLogout = { authViewModel.logout() }
        )
        
        // Main content area - reuse existing Compose screens!
        Box(modifier = Modifier.fillMaxSize()) {
            when (currentScreen) {
                Screen.Signals -> SignalMatrixScreen()
                Screen.Portfolio -> PortfolioDashboardScreen()
                Screen.Evaluation -> EvaluationMatrixScreen()
                Screen.TradingWall -> TradingWallScreen()
            }
        }
    }
}

enum class Screen {
    Signals, Portfolio, Evaluation, TradingWall
}

@Composable
fun NavigationSidebar(
    currentScreen: Screen,
    onScreenSelected: (Screen) -> Unit,
    onLogout: () -> Unit
) {
    NavigationRail(modifier = Modifier.fillMaxHeight()) {
        NavigationRailItem(
            selected = currentScreen == Screen.Signals,
            onClick = { onScreenSelected(Screen.Signals) },
            icon = { Text("📊") },
            label = { Text("Signals") }
        )
        NavigationRailItem(
            selected = currentScreen == Screen.Portfolio,
            onClick = { onScreenSelected(Screen.Portfolio) },
            icon = { Text("💼") },
            label = { Text("Portfolio") }
        )
        NavigationRailItem(
            selected = currentScreen == Screen.Evaluation,
            onClick = { onScreenSelected(Screen.Evaluation) },
            icon = { Text("📈") },
            label = { Text("Evaluation") }
        )
        NavigationRailItem(
            selected = currentScreen == Screen.TradingWall,
            onClick = { onScreenSelected(Screen.TradingWall) },
            icon = { Text("🖥️") },
            label = { Text("Trading Wall") }
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        NavigationRailItem(
            selected = false,
            onClick = onLogout,
            icon = { Text("🚪") },
            label = { Text("Logout") }
        )
    }
}
