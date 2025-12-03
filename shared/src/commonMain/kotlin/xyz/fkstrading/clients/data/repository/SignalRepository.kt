package xyz.fkstrading.clients.data.repository

import xyz.fkstrading.clients.api.FksApiClient
import xyz.fkstrading.clients.data.models.*
import kotlinx.coroutines.flow.Flow

/**
 * Repository for trading signals
 */
object SignalRepository {
    
    /**
     * Generate trading signals
     * @param category Signal category (swing, scalp, position, day, crypto, forex, stocks, futures, bitcoin)
     * @param symbols Comma-separated symbol list (optional)
     * @param aiEnhanced Use AI-enhanced analysis
     */
    suspend fun generateSignals(
        category: String = "swing",
        symbols: String? = null,
        aiEnhanced: Boolean = false
    ): List<SignalResponse> {
        val params = buildMap {
            put("category", category)
            symbols?.let { put("symbols", it) }
            put("ai_enhanced", aiEnhanced.toString())
        }
        
        return FksApiClient.get("/api/signals/generate", FksApiClient.portfolioUrl, params)
    }
    
    /**
     * Get Bitcoin-specific signals
     */
    suspend fun getBitcoinSignals(aiEnhanced: Boolean = true): List<SignalResponse> {
        return generateSignals(category = "bitcoin", symbols = "BTC/USD,BTC/USDT", aiEnhanced = aiEnhanced)
    }
    
    /**
     * Get signal summary
     */
    suspend fun getSignalSummary(): SignalSummaryResponse {
        return FksApiClient.get("/api/signals/summary", FksApiClient.portfolioUrl)
    }
    
    /**
     * Connect to WebSocket for real-time signal updates
     */
    suspend fun connectSignalWebSocket(): Flow<String> {
        return FksApiClient.connectWebSocket("/api/signals/stream", FksApiClient.portfolioUrl)
    }
    
    /**
     * Get signals by strength
     */
    suspend fun getStrongSignals(): List<SignalResponse> {
        val all = generateSignals()
        return all.filter { it.strength.lowercase() == "strong" }
    }
    
    /**
     * Get high-confidence signals (>80%)
     */
    suspend fun getHighConfidenceSignals(minConfidence: Double = 0.8): List<SignalResponse> {
        val all = generateSignals()
        return all.filter { it.confidence >= minConfidence }
    }
}
