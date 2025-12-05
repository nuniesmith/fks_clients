package xyz.fkstrading.clients.domain.viewmodel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import xyz.fkstrading.clients.data.models.*
import xyz.fkstrading.clients.data.repository.DataRepository
import xyz.fkstrading.clients.api.FksApiClient
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * ViewModel for Trading Wall Dashboard
 */
class TradingWallViewModel(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    private val apiClient = FksApiClient.instance
    private val dataRepository = DataRepository()
    
    // World clocks
    private val _worldClocks = MutableStateFlow<List<WorldClock>>(emptyList())
    val worldClocks: StateFlow<List<WorldClock>> = _worldClocks.asStateFlow()
    
    // Ticker prices
    private val _tickerPrices = MutableStateFlow<List<TickerPrice>>(emptyList())
    val tickerPrices: StateFlow<List<TickerPrice>> = _tickerPrices.asStateFlow()
    
    // Metrics
    private val _metrics = MutableStateFlow<TimeframeMetricsResponse?>(null)
    val metrics: StateFlow<TimeframeMetricsResponse?> = _metrics.asStateFlow()
    
    private val _portfolioMetrics = MutableStateFlow<PortfolioMetricsResponse?>(null)
    val portfolioMetrics: StateFlow<PortfolioMetricsResponse?> = _portfolioMetrics.asStateFlow()
    
    // Status
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _connectionStatus = MutableStateFlow("Connected")
    val connectionStatus: StateFlow<String> = _connectionStatus.asStateFlow()
    
    // Update jobs
    private var clockUpdateJob: Job? = null
    private var tickerUpdateJob: Job? = null
    private var metricsUpdateJob: Job? = null
    
    // Configuration
    val tickerSymbols = listOf(
        "EURUSD", "GBPUSD", "USDJPY", "USDCAD", "AUDUSD",
        "BTCUSDT", "ETHUSDT", "SPX", "NDX", "GOLD", "OIL"
    )
    
    /**
     * Start all updates
     */
    fun start() {
        updateClocks()
        updateTicker()
        updateMetrics()
        
        // Start periodic updates
        clockUpdateJob = scope.launch {
            while (true) {
                delay(1000) // Update every second
                updateClocks()
            }
        }
        
        tickerUpdateJob = scope.launch {
            while (true) {
                delay(8000) // Update every 8 seconds
                updateTicker()
            }
        }
        
        metricsUpdateJob = scope.launch {
            while (true) {
                delay(5000) // Update every 5 seconds
                updateMetrics()
            }
        }
    }
    
    /**
     * Stop all updates
     */
    fun stop() {
        clockUpdateJob?.cancel()
        tickerUpdateJob?.cancel()
        metricsUpdateJob?.cancel()
    }
    
    /**
     * Update world clocks
     */
    private fun updateClocks() {
        scope.launch {
            try {
                val clocks = buildWorldClocks()
                _worldClocks.value = clocks
            } catch (e: Exception) {
                // Silently fail - clocks should still update
            }
        }
    }
    
    /**
     * Update ticker prices
     */
    private fun updateTicker() {
        scope.launch {
            try {
                val prices = mutableListOf<TickerPrice>()
                var successCount = 0
                
                for (symbol in tickerSymbols) {
                    try {
                        val priceResponse = dataRepository.getPrice(symbol, useCache = true)
                        prices.add(
                            TickerPrice(
                                symbol = symbol,
                                price = priceResponse.price,
                                timestamp = priceResponse.timestamp
                            )
                        )
                        successCount++
                    } catch (e: Exception) {
                        // Skip failed symbols
                    }
                }
                
                _tickerPrices.value = prices
                _connectionStatus.value = if (successCount > 0) {
                    "Connected (${successCount}/${tickerSymbols.size} symbols)"
                } else {
                    "No data available"
                }
            } catch (e: Exception) {
                _connectionStatus.value = "Connection error"
            }
        }
    }
    
    /**
     * Update metrics
     */
    private fun updateMetrics() {
        scope.launch {
            try {
                // Fetch timeframe metrics
                try {
                    val metricsJson = apiClient.get<JsonObject>(
                        "/api/v1/timeframes/metrics",
                        apiClient.apiUrl
                    )
                    _metrics.value = parseMetricsResponse(metricsJson)
                } catch (e: Exception) {
                    // Metrics are optional
                }
                
                // Fetch portfolio metrics (if available)
                try {
                    val portfolioJson = apiClient.get<JsonObject>(
                        "/api/dashboard/overview",
                        apiClient.apiUrl
                    )
                    _portfolioMetrics.value = parsePortfolioMetrics(portfolioJson)
                } catch (e: Exception) {
                    // Portfolio metrics are optional
                }
            } catch (e: Exception) {
                // Silently fail - metrics are optional
            }
        }
    }
    
    /**
     * Build world clocks for major trading centers
     */
    private fun buildWorldClocks(): List<WorldClock> {
        val now = System.currentTimeMillis()
        
        val cities = listOf(
            "Sydney" to "Australia/Sydney",
            "Tokyo" to "Asia/Tokyo",
            "Hong Kong" to "Asia/Hong_Kong",
            "London" to "Europe/London",
            "Frankfurt" to "Europe/Berlin",
            "New York" to "America/New_York"
        )
        
        return cities.map { (city, timezone) ->
            // For web, we'll use JavaScript Date API via expect/actual
            // For now, create placeholder - will be implemented with actual timezone logic
            WorldClock(
                city = city,
                timezone = timezone,
                time = formatTime(now, timezone),
                isOpen = isMarketOpen(city, now),
                isPreMarket = isPreMarket(city, now),
                isOverlap = isOverlapPeriod(city, now)
            )
        }
    }
    
    /**
     * Format time for timezone (simplified - timezone formatting done in UI for KMP compatibility)
     */
    private fun formatTime(timestamp: Long, timezone: String): String {
        // For KMP compatibility, format time on client side using JS Date API
        // This is a placeholder - actual formatting happens in the UI layer
        val hours = (timestamp / 3600000) % 24
        val minutes = (timestamp / 60000) % 60
        val h = hours.toInt()
        val m = minutes.toInt()
        return "${if (h < 10) "0" else ""}$h:${if (m < 10) "0" else ""}$m"
    }
    
    /**
     * Check if market is open (simplified - actual logic would use market calendar)
     */
    private fun isMarketOpen(city: String, timestamp: Long): Boolean {
        // Simplified market hours - in production use proper market calendar
        // For now, return true as placeholder - actual logic in UI layer
        return true
    }
    
    private fun isPreMarket(city: String, timestamp: Long): Boolean = false
    private fun isOverlapPeriod(city: String, timestamp: Long): Boolean = false
    
    /**
     * Parse metrics response from JSON
     */
    private fun parseMetricsResponse(json: JsonObject): TimeframeMetricsResponse {
        return TimeframeMetricsResponse(
            timestamp = json["timestamp"]?.jsonPrimitive?.content,
            signal_generation = json["signal_generation"]?.jsonObject?.let {
                SignalGenerationMetrics(
                    total = it["total"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0,
                    by_timeframe_strategy = emptyMap(), // Parse if needed
                    avg_times = emptyMap()
                )
            },
            cache = json["cache"]?.jsonObject?.let {
                CacheMetrics(
                    hits = emptyMap(),
                    misses = emptyMap(),
                    total_operations = emptyMap(),
                    hit_rates = it["hit_rates"]?.jsonObject?.let { rates ->
                        rates.entries.associate { (k, v) ->
                            k to (v.jsonPrimitive.content.toDoubleOrNull() ?: 0.0)
                        }
                    } ?: emptyMap()
                )
            }
        )
    }
    
    /**
     * Parse portfolio metrics from JSON
     */
    private fun parsePortfolioMetrics(json: JsonObject): PortfolioMetricsResponse {
        return PortfolioMetricsResponse(
            pnl = json["pnl"]?.jsonPrimitive?.content?.toDoubleOrNull(),
            exposure = json["exposure"]?.jsonPrimitive?.content?.toDoubleOrNull(),
            risk = json["risk"]?.jsonPrimitive?.content?.toDoubleOrNull(),
            timestamp = json["timestamp"]?.jsonPrimitive?.content
        )
    }
    
    /**
     * Clear error
     */
    fun clearError() {
        _error.value = null
    }
    
    /**
     * Refresh all data
     */
    fun refresh() {
        updateClocks()
        updateTicker()
        updateMetrics()
    }
}
