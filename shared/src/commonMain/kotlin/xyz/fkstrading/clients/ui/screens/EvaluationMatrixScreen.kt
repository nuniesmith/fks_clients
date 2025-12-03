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
import xyz.fkstrading.clients.domain.viewmodel.SignalViewModel
import xyz.fkstrading.clients.ui.components.*
import xyz.fkstrading.clients.ui.theme.FksColors

/**
 * Evaluation Matrix Screen - displays signal performance and metrics
 */
@Composable
fun EvaluationMatrixScreen(
    viewModel: SignalViewModel = remember { SignalViewModel() }
) {
    val signals by viewModel.signals.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadSignals()
    }
    
    // Calculate metrics
    val totalSignals = signals.size
    val buySignals = signals.count { it.signal_type.uppercase() == "BUY" }
    val sellSignals = signals.count { it.signal_type.uppercase() == "SELL" }
    val avgConfidence = viewModel.getAverageConfidence()
    val validSignals = signals.count { it.is_valid }
    val strongSignals = viewModel.getStrongSignals().size
    
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
                text = "Evaluation Matrix",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Button(onClick = { viewModel.refresh() }) {
                Text("Refresh")
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
            LoadingIndicator(message = "Loading evaluation data...")
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Metrics Row 1
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricCard(
                title = "Total Signals",
                value = totalSignals.toString(),
                modifier = Modifier.weight(1f),
                icon = "📊"
            )
            MetricCard(
                title = "Buy Signals",
                value = buySignals.toString(),
                modifier = Modifier.weight(1f),
                color = FksColors.BuyColor.copy(alpha = 0.2f),
                icon = "📈"
            )
            MetricCard(
                title = "Sell Signals",
                value = sellSignals.toString(),
                modifier = Modifier.weight(1f),
                color = FksColors.SellColor.copy(alpha = 0.2f),
                icon = "📉"
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Metrics Row 2
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricCard(
                title = "Avg Confidence",
                value = "${(avgConfidence * 100).toInt()}%",
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.secondaryContainer
            )
            MetricCard(
                title = "Valid Signals",
                value = "$validSignals / $totalSignals",
                modifier = Modifier.weight(1f),
                color = if (validSignals.toFloat() / totalSignals.coerceAtLeast(1) > 0.8) {
                    FksColors.BuyColor.copy(alpha = 0.2f)
                } else {
                    FksColors.SellColor.copy(alpha = 0.2f)
                }
            )
            MetricCard(
                title = "Strong Signals",
                value = strongSignals.toString(),
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.tertiaryContainer
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Signal Quality Distribution
        if (signals.isNotEmpty()) {
            SectionHeader(title = "Signal Quality Distribution")
            
            val qualityRanges = listOf(
                Triple("High (80-100%)", signals.count { it.confidence >= 0.8 }, FksColors.SignalStrong),
                Triple("Medium (60-80%)", signals.count { it.confidence in 0.6..0.79 }, FksColors.SignalMedium),
                Triple("Low (<60%)", signals.count { it.confidence < 0.6 }, FksColors.SignalWeak)
            )
            
            qualityRanges.forEach { (range, count, color) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = range, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                text = "$count (${if (totalSignals > 0) (count.toFloat() / totalSignals * 100).toInt() else 0}%)",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { if (totalSignals > 0) count.toFloat() / totalSignals else 0f },
                            modifier = Modifier.fillMaxWidth().height(8.dp),
                            color = color
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Strength Distribution
            SectionHeader(title = "Strength Distribution")
            
            val strengthGroups = signals.groupBy { it.strength.lowercase() }
            strengthGroups.forEach { (strength, signalList) ->
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
                            text = strength.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "${signalList.size} signals",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        } else if (!isLoading) {
            EmptyState(
                title = "No Data",
                message = "No signals available for evaluation",
                icon = "📊"
            )
        }
    }
}
