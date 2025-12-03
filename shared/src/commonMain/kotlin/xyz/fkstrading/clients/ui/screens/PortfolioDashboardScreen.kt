package xyz.fkstrading.clients.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import xyz.fkstrading.clients.domain.viewmodel.PortfolioViewModel
import xyz.fkstrading.clients.domain.viewmodel.WebSocketViewModel
import xyz.fkstrading.clients.ui.components.*
import xyz.fkstrading.clients.ui.theme.FksColors

/**
 * Portfolio Dashboard Screen
 */
@Composable
fun PortfolioDashboardScreen(
    viewModel: PortfolioViewModel = remember { PortfolioViewModel() },
    webSocketViewModel: WebSocketViewModel = remember { WebSocketViewModel() }
) {
    val portfolioValue by viewModel.portfolioValue.collectAsState()
    val assetPrices by viewModel.assetPrices.collectAsState()
    val correlations by viewModel.correlations.collectAsState()
    val btcPrice by viewModel.btcPrice.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    val isConnected by webSocketViewModel.isConnected.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadPortfolio()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Portfolio Dashboard",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ConnectionIndicator(isConnected = isConnected)
                Button(onClick = { viewModel.refresh() }) {
                    Text("Refresh")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Error display
        error?.let {
            ErrorCard(message = it, onRetry = { viewModel.refresh() })
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Loading indicator
        if (isLoading) {
            LoadingIndicator(message = "Loading portfolio...")
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // BTC Price card
        btcPrice?.let { price ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "₿ Bitcoin",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Provider: ${price.provider}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Text(
                        text = "$${String.format("%,.2f", price.price)}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = FksColors.Primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Portfolio Value Card
        portfolioValue?.let { value ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Portfolio Value",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${String.format("%.8f", value.total_btc)} BTC",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    viewModel.getPortfolioValueUsd()?.let { usd ->
                        Text(
                            text = "$${String.format("%,.2f", usd)} USD",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LabeledProgressBar(
                        label = "BTC Allocation",
                        progress = value.btc_allocation.toFloat()
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Asset Prices
        if (assetPrices.isNotEmpty()) {
            SectionHeader(title = "Asset Prices")
            
            assetPrices.forEach { asset ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = asset.symbol,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            asset.price_usd?.let {
                                Text(
                                    text = "$${String.format("%.2f", it)}",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            asset.price_btc?.let {
                                Text(
                                    text = "${String.format("%.8f", it)} BTC",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                        asset.change_24h?.let { change ->
                            Text(
                                text = "${if (change >= 0) "+" else ""}${String.format("%.2f", change)}%",
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (change >= 0) FksColors.BuyColor else FksColors.SellColor
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Correlations
        if (correlations.isNotEmpty()) {
            SectionHeader(title = "BTC Correlations")
            
            correlations.forEach { correlation ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = correlation.symbol,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = String.format("%.3f", correlation.correlation_to_btc),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
