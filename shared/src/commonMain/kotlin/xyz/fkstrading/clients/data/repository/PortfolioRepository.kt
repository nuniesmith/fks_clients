package xyz.fkstrading.clients.data.repository

import xyz.fkstrading.clients.api.FksApiClient
import xyz.fkstrading.clients.data.models.*

/**
 * Repository for portfolio management
 */
object PortfolioRepository {
    
    /**
     * Get portfolio value in BTC and USD
     */
    suspend fun getPortfolioValue(): PortfolioValueResponse {
        return FksApiClient.get("/api/portfolio/value", FksApiClient.portfolioUrl)
    }
    
    /**
     * Get asset prices
     * @param symbols Optional comma-separated symbol list
     */
    suspend fun getAssetPrices(symbols: String? = null): List<AssetPriceResponse> {
        val params = symbols?.let { mapOf("symbols" to it) } ?: emptyMap()
        return FksApiClient.get("/api/assets/prices", FksApiClient.portfolioUrl, params)
    }
    
    /**
     * Get BTC price specifically
     */
    suspend fun getBtcPrice(): AssetPriceResponse {
        val prices = getAssetPrices("BTC")
        return prices.firstOrNull() ?: AssetPriceResponse(
            symbol = "BTC",
            price_usd = 0.0,
            price_btc = 1.0
        )
    }
    
    /**
     * Get correlations to BTC
     * @param symbols Optional comma-separated symbol list
     */
    suspend fun getCorrelations(symbols: String? = null): List<CorrelationResponse> {
        val params = symbols?.let { mapOf("symbols" to it) } ?: emptyMap()
        return FksApiClient.get("/api/portfolio/correlations", FksApiClient.portfolioUrl, params)
    }
    
    /**
     * Get rebalancing plan
     * @param targetBtcAllocation Target BTC allocation (0.0-1.0)
     */
    suspend fun getRebalancingPlan(targetBtcAllocation: Double): RebalancingPlanResponse {
        val params = mapOf("target_btc_allocation" to targetBtcAllocation.toString())
        return FksApiClient.get("/api/portfolio/rebalancing", FksApiClient.portfolioUrl, params)
    }
    
    /**
     * Check portfolio service health
     */
    suspend fun getHealth(): HealthResponse {
        return FksApiClient.get("/health", FksApiClient.portfolioUrl)
    }
}
