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
import xyz.fkstrading.clients.domain.viewmodel.WebSocketViewModel
import xyz.fkstrading.clients.ui.components.*
import xyz.fkstrading.clients.ui.theme.FksColors

/**
 * Signal Matrix Screen - displays trading signals
 */
@Composable
fun SignalMatrixScreen(
    viewModel: SignalViewModel = remember { SignalViewModel() },
    webSocketViewModel: WebSocketViewModel = remember { WebSocketViewModel() }
) {
    val signals by viewModel.signals.collectAsState()
    val signalSummary by viewModel.signalSummary.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    
    // WebSocket state
    val isConnected by webSocketViewModel.isConnected.collectAsState()
    val latestSignal by webSocketViewModel.latestSignal.collectAsState()
    
    // Load signals on first composition
    LaunchedEffect(Unit) {
        viewModel.loadSignals()
        webSocketViewModel.connect()
    }
    
    // Collect all signals including WebSocket updates
    val allSignals = remember(signals, latestSignal) {
        if (latestSignal != null && !signals.any { it.symbol == latestSignal!!.symbol && it.timestamp == latestSignal!!.timestamp }) {
            listOf(latestSignal!!) + signals
        } else {
            signals
        }
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
                text = "Signal Matrix",
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
        
        // Category selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("swing", "scalp", "position", "bitcoin").forEach { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { viewModel.loadSignals(category) },
                    label = { Text(category.replaceFirstChar { it.uppercase() }) }
                )
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
            LoadingIndicator(message = "Loading signals...")
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Signal summary
        signalSummary?.let { summary ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "📊 Summary",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Total Signals: ${summary.total_signals}")
                    Text("By Category: ${summary.by_category}")
                    Text("By Strength: ${summary.by_strength}")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Latest WebSocket signal
        latestSignal?.let { signal ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "🔄 Latest Update",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    SignalCard(signal = signal)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        // Signals List
        if (allSignals.isNotEmpty()) {
            SectionHeader(
                title = "Signals (${allSignals.size})${if (isConnected) " - Live" else ""}"
            )
            
            allSignals.forEach { signal ->
                SignalCard(signal = signal)
                Spacer(modifier = Modifier.height(8.dp))
            }
        } else if (!isLoading) {
            EmptyState(
                title = "No Signals",
                message = "No signals available for the selected category",
                icon = "📉"
            )
        }
    }
}
