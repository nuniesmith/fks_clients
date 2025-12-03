package xyz.fkstrading.clients.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * FKS Trading Theme Colors
 */
object FksColors {
    // Brand colors
    val Primary = Color(0xFFF7931A) // Bitcoin orange
    val PrimaryVariant = Color(0xFFE07800)
    val Secondary = Color(0xFF4CAF50) // Green for positive
    val SecondaryVariant = Color(0xFF388E3C)
    val Error = Color(0xFFE53935) // Red for negative
    
    // Signal quality colors
    val SignalStrong = Color(0xFF2E7D32)
    val SignalMedium = Color(0xFFFFA000)
    val SignalWeak = Color(0xFFD32F2F)
    
    // Chart colors
    val BuyColor = Color(0xFF4CAF50)
    val SellColor = Color(0xFFF44336)
    val NeutralColor = Color(0xFF9E9E9E)
    
    // Background colors
    val DarkBackground = Color(0xFF121212)
    val DarkSurface = Color(0xFF1E1E1E)
    val LightBackground = Color(0xFFF5F5F5)
    val LightSurface = Color(0xFFFFFFFF)
}

/**
 * Dark color scheme for FKS
 */
val FksDarkColorScheme = darkColorScheme(
    primary = FksColors.Primary,
    secondary = FksColors.Secondary,
    error = FksColors.Error,
    background = FksColors.DarkBackground,
    surface = FksColors.DarkSurface
)

/**
 * Light color scheme for FKS
 */
val FksLightColorScheme = lightColorScheme(
    primary = FksColors.Primary,
    secondary = FksColors.Secondary,
    error = FksColors.Error,
    background = FksColors.LightBackground,
    surface = FksColors.LightSurface
)
