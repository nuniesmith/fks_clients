package xyz.fkstrading.clients.data.repository

import xyz.fkstrading.clients.api.FksApiClient
import xyz.fkstrading.clients.data.models.*

/**
 * Repository for market data service
 */
object DataRepository {
    
    /**
     * Get current price for a symbol
     * @param symbol Trading symbol (e.g., "BTC/USD")
     * @param provider Data provider (optional)
     * @param useCache Use cached data if available
     */
    suspend fun getPrice(
        symbol: String,
        provider: String? = null,
        useCache: Boolean = true
    ): PriceResponse {
        val params = buildMap {
            put("symbol", symbol)
            provider?.let { put("provider", it) }
            put("use_cache", useCache.toString())
        }
        
        return FksApiClient.get("/api/v1/data/price", FksApiClient.dataUrl, params)
    }
    
    /**
     * Get OHLCV (candlestick) data
     * @param symbol Trading symbol
     * @param interval Timeframe (1m, 5m, 15m, 1h, 4h, 1d)
     * @param provider Data provider (optional)
     * @param useCache Use cached data if available
     */
    suspend fun getOHLCV(
        symbol: String,
        interval: String = "1h",
        provider: String? = null,
        useCache: Boolean = true
    ): OHLCVResponse {
        val params = buildMap {
            put("symbol", symbol)
            put("interval", interval)
            provider?.let { put("provider", it) }
            put("use_cache", useCache.toString())
        }
        
        return FksApiClient.get("/api/v1/data/ohlcv", FksApiClient.dataUrl, params)
    }
    
    /**
     * Get BTC/USD price
     */
    suspend fun getBtcPrice(): PriceResponse {
        return getPrice("BTC/USD")
    }
    
    /**
     * Get BTC 1-hour candles
     */
    suspend fun getBtcHourlyCandles(): OHLCVResponse {
        return getOHLCV("BTC/USD", "1h")
    }
    
    /**
     * Get BTC daily candles
     */
    suspend fun getBtcDailyCandles(): OHLCVResponse {
        return getOHLCV("BTC/USD", "1d")
    }
    
    /**
     * Check data service health
     */
    suspend fun getHealth(): HealthResponse {
        return FksApiClient.get("/health", FksApiClient.dataUrl)
    }
}
