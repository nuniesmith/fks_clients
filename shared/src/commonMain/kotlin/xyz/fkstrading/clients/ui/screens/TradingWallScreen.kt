package xyz.fkstrading.clients.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xyz.fkstrading.clients.domain.viewmodel.TradingWallViewModel
import xyz.fkstrading.clients.ui.theme.FksColors
import kotlin.math.abs

/**
 * Trading Wall Dashboard Screen
 * Designed for large displays (55-65" 4K TVs) with world clocks, price ticker, and metrics
 */
@Composable
fun TradingWallScreen(
    viewModel: TradingWallViewModel = remember { TradingWallViewModel() }
) {
    val worldClocks by viewModel.worldClocks.collectAsState()
    val tickerPrices by viewModel.tickerPrices.collectAsState()
    val metrics by viewModel.metrics.collectAsState()
    val portfolioMetrics by viewModel.portfolioMetrics.collectAsState()
    val connectionStatus by viewModel.connectionStatus.collectAsState()
    
    // Start updates on first composition
    LaunchedEffect(Unit) {
        viewModel.start()
    }
    
    // Cleanup on dispose
    DisposableEffect(Unit) {
        onDispose {
            viewModel.stop()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Main content area - world clocks
            WorldClocksSection(
                clocks = worldClocks,
                modifier = Modifier.weight(1f)
            )
            
            // Price ticker at bottom
            PriceTickerSection(
                prices = tickerPrices,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )
        }
        
        // Metrics panel on the right
        MetricsPanel(
            metrics = metrics,
            portfolioMetrics = portfolioMetrics,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .width(300.dp)
        )
        
        // Status indicator at bottom left
        ConnectionStatus(
            status = connectionStatus,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        )
    }
}

/**
 * World Clocks Section - 6 major trading centers
 */
@Composable
private fun WorldClocksSection(
    clocks: List<xyz.fkstrading.clients.data.models.WorldClock>,
    modifier: Modifier = Modifier
) {
    if (clocks.isEmpty()) {
        // Show placeholder while loading
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = FksColors.Primary)
        }
        return
    }
    
    // Grid layout: 3 columns, 2 rows
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Row 1: Sydney, Tokyo, Hong Kong
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            clocks.take(3).forEach { clock ->
                WorldClockCard(
                    clock = clock,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        // Row 2: London, Frankfurt, New York
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            clocks.drop(3).take(3).forEach { clock ->
                WorldClockCard(
                    clock = clock,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * World Clock Card
 */
@Composable
private fun WorldClockCard(
    clock: xyz.fkstrading.clients.data.models.WorldClock,
    modifier: Modifier = Modifier
) {
    // Determine color based on market status
    val cardColor = when {
        clock.isOverlap -> FksColors.Primary.copy(alpha = 0.3f) // Yellow/orange for overlap
        clock.isOpen -> Color(0xFF00FF00).copy(alpha = 0.2f) // Green for open
        clock.isPreMarket -> Color(0xFFFFA500).copy(alpha = 0.2f) // Orange for pre-market
        else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.5f) // Gray for closed
    }
    
    val textColor = when {
        clock.isOverlap -> FksColors.Primary // Yellow/orange
        clock.isOpen -> Color(0xFF00FF00) // Green
        clock.isPreMarket -> Color(0xFFFFA500) // Orange
        else -> Color.Gray // Gray
    }
    
    // Pulsing animation for overlap periods
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    
    Card(
        modifier = modifier
            .fillMaxHeight(),
        colors = CardDefaults.cardColors(
            containerColor = if (clock.isOverlap) {
                cardColor.copy(alpha = alpha)
            } else {
                cardColor
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // City name
            Text(
                text = clock.city,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = textColor,
                fontSize = 32.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Time
            Text(
                text = clock.time,
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Black,
                color = textColor,
                fontSize = 96.sp,
                letterSpacing = (-2).sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Status indicator
            Text(
                text = when {
                    clock.isOverlap -> "OVERLAP"
                    clock.isOpen -> "OPEN"
                    clock.isPreMarket -> "PRE-MARKET"
                    else -> "CLOSED"
                },
                style = MaterialTheme.typography.bodyLarge,
                color = textColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Price Ticker Section - Scrolling ticker at bottom
 */
@Composable
private fun PriceTickerSection(
    prices: List<xyz.fkstrading.clients.data.models.TickerPrice>,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = Color(0xFF111111),
        tonalElevation = 4.dp
    ) {
        if (prices.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Loading prices...",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            // Horizontal scrolling ticker
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Repeat prices for seamless scrolling effect
                (1..3).forEach { _ ->
                    prices.forEach { price ->
                        TickerItem(price = price)
                    }
                }
            }
        }
    }
}

/**
 * Ticker Item - Individual price display
 */
@Composable
private fun TickerItem(
    price: xyz.fkstrading.clients.data.models.TickerPrice,
    modifier: Modifier = Modifier
) {
    val changePercent = price.changePercent ?: 0.0
    val priceColor = when {
        changePercent > 0 -> Color(0xFF00FF00) // Green
        changePercent < 0 -> Color(0xFFFF0000) // Red
        else -> Color.White // Neutral
    }
    
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Symbol
        Text(
            text = price.symbol,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontSize = 24.sp
        )
        
        // Price
        Text(
            text = formatPrice(price.price),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = priceColor,
            fontSize = 24.sp
        )
        
        // Change percentage if available
        if (price.changePercent != null) {
            val sign = if (changePercent > 0) "+" else ""
            Text(
                text = "$sign${String.format("%.2f", changePercent)}%",
                style = MaterialTheme.typography.bodyLarge,
                color = priceColor,
                fontSize = 20.sp
            )
        }
    }
}

/**
 * Metrics Panel - Trading metrics sidebar
 */
@Composable
private fun MetricsPanel(
    metrics: xyz.fkstrading.clients.data.models.TimeframeMetricsResponse?,
    portfolioMetrics: xyz.fkstrading.clients.data.models.PortfolioMetricsResponse?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E1E).copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title
            Text(
                text = "FKS METRICS",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = FksColors.Primary,
                modifier = Modifier.fillMaxWidth()
            )
            
            Divider(color = Color.Gray.copy(alpha = 0.3f))
            
            // P&L
            MetricRow(
                label = "P&L",
                value = portfolioMetrics?.pnl?.let { formatCurrency(it) } ?: "–––",
                isPositive = portfolioMetrics?.pnl?.let { it >= 0 } ?: null
            )
            
            // Exposure
            MetricRow(
                label = "Exposure",
                value = portfolioMetrics?.exposure?.let { formatCurrency(it) } ?: "–––",
                isPositive = null
            )
            
            // Risk
            MetricRow(
                label = "Risk",
                value = portfolioMetrics?.risk?.let { formatCurrency(it) } ?: "–––",
                isPositive = portfolioMetrics?.risk?.let { abs(it) < 0.1 } ?: null
            )
            
            // Signals count
            MetricRow(
                label = "Signals",
                value = metrics?.signal_generation?.total?.toString() ?: "–––",
                isPositive = metrics?.signal_generation?.total?.let { it > 0 } ?: null
            )
            
            // Cache hit rate
            val avgHitRate = metrics?.cache?.hit_rates?.values?.average() ?: 0.0
            MetricRow(
                label = "Cache Hit",
                value = if (avgHitRate > 0) "${String.format("%.1f", avgHitRate)}%" else "–––",
                isPositive = avgHitRate > 60
            )
        }
    }
}

/**
 * Metric Row in metrics panel
 */
@Composable
private fun MetricRow(
    label: String,
    value: String,
    isPositive: Boolean?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 18.sp
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = when {
                isPositive == true -> Color(0xFF00FF00)
                isPositive == false -> Color(0xFFFF0000)
                else -> Color.White
            },
            fontSize = 18.sp
        )
    }
}

/**
 * Connection Status Indicator
 */
@Composable
private fun ConnectionStatus(
    status: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Status dot
        val dotColor = if (status.contains("Connected", ignoreCase = true)) {
            Color(0xFF00FF00)
        } else {
            Color(0xFFFF0000)
        }
        
        // Animated pulsing dot
        val infiniteTransition = rememberInfiniteTransition(label = "blink")
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "alpha"
        )
        
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(dotColor.copy(alpha = alpha), shape = androidx.compose.foundation.shape.CircleShape)
        )
        
        Text(
            text = status,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 14.sp
        )
    }
}

/**
 * Format price for display
 */
private fun formatPrice(price: Double): String {
    return when {
        price >= 1000000 -> String.format("$%.2fM", price / 1000000)
        price >= 1000 -> String.format("$%.2fK", price / 1000)
        price >= 1 -> String.format("$%.2f", price)
        else -> String.format("$%.4f", price)
    }
}

/**
 * Format currency for display
 */
private fun formatCurrency(value: Double): String {
    return when {
        abs(value) >= 1000000 -> String.format("$%.2fM", value / 1000000)
        abs(value) >= 1000 -> String.format("$%.2fK", value / 1000)
        else -> String.format("$%.2f", value)
    }
}
