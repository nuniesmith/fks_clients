package xyz.fkstrading.clients.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import xyz.fkstrading.clients.data.models.SignalResponse
import xyz.fkstrading.clients.ui.theme.FksColors

/**
 * Card for displaying a trading signal
 */
@Composable
fun SignalCard(
    signal: SignalResponse,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (signal.signal_type.uppercase()) {
                "BUY" -> MaterialTheme.colorScheme.primaryContainer
                "SELL" -> MaterialTheme.colorScheme.errorContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        onClick = { onClick?.invoke() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Symbol and Signal Type
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = signal.symbol,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                SignalTypeBadge(signalType = signal.signal_type)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Price targets
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Entry: ${formatPrice(signal.entry_price)}", 
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "TP: ${formatPrice(signal.take_profit)} (${formatPercent(signal.take_profit_pct)})", 
                        style = MaterialTheme.typography.bodyMedium,
                        color = FksColors.BuyColor
                    )
                    Text(
                        "SL: ${formatPrice(signal.stop_loss)} (${formatPercent(signal.stop_loss_pct)})", 
                        style = MaterialTheme.typography.bodyMedium,
                        color = FksColors.SellColor
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Confidence: ${(signal.confidence * 100).toInt()}%", 
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "Strength: ${signal.strength}", 
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "R:R: ${String.format("%.2f", signal.risk_reward_ratio)}", 
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Footer: Category and validity
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Category: ${signal.category}", 
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    if (signal.is_valid) "✓ Valid" else "✗ Invalid",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (signal.is_valid) FksColors.BuyColor else FksColors.SellColor
                )
            }
        }
    }
}

/**
 * Badge for signal type (BUY/SELL)
 */
@Composable
fun SignalTypeBadge(signalType: String) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = when (signalType.uppercase()) {
            "BUY" -> FksColors.BuyColor
            "SELL" -> FksColors.SellColor
            else -> FksColors.NeutralColor
        }
    ) {
        Text(
            text = signalType.uppercase(),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

/**
 * Confidence indicator bar
 */
@Composable
fun ConfidenceIndicator(
    confidence: Double,
    modifier: Modifier = Modifier
) {
    val color = when {
        confidence >= 0.8 -> FksColors.SignalStrong
        confidence >= 0.6 -> FksColors.SignalMedium
        else -> FksColors.SignalWeak
    }
    
    Column(modifier = modifier) {
        Text(
            text = "${(confidence * 100).toInt()}%",
            style = MaterialTheme.typography.labelSmall
        )
        LinearProgressIndicator(
            progress = { confidence.toFloat() },
            modifier = Modifier.fillMaxWidth().height(4.dp),
            color = color
        )
    }
}

/**
 * WebSocket connection indicator
 */
@Composable
fun ConnectionIndicator(
    isConnected: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = if (isConnected) 
            MaterialTheme.colorScheme.primaryContainer
        else 
            MaterialTheme.colorScheme.errorContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = if (isConnected) FksColors.BuyColor else FksColors.SellColor,
                modifier = Modifier.size(8.dp)
            ) {}
            
            Text(
                text = if (isConnected) "Live" else "Offline",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

// Helper functions
private fun formatPrice(price: Double): String {
    return if (price >= 1000) {
        String.format("%.2f", price)
    } else if (price >= 1) {
        String.format("%.4f", price)
    } else {
        String.format("%.8f", price)
    }
}

private fun formatPercent(pct: Double): String {
    return String.format("%.2f%%", pct)
}
