package xyz.fkstrading.clients.web

import org.jetbrains.compose.web.renderComposable
import xyz.fkstrading.clients.api.FksApiClient
import xyz.fkstrading.clients.ui.screens.TradingWallScreen
import xyz.fkstrading.clients.ui.theme.FksDarkColorScheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize

/**
 * Kiosk mode entry point for Trading Wall Dashboard
 * Designed for full-screen display on large TVs (55-65" 4K)
 * No authentication required - public display mode
 */
fun main() {
    // Configure API URLs - can be set via window globals or environment
    configureApiUrls()
    
    // Render directly to root - no navigation, no auth
    renderComposable(rootElementId = "root") {
        MaterialTheme(colorScheme = FksDarkColorScheme) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                TradingWallScreen()
            }
        }
    }
}
