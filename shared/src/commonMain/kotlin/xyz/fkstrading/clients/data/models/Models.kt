package xyz.fkstrading.clients.data.models

import kotlinx.serialization.Serializable

/**
 * Data models matching FKS backend Pydantic models
 */

// ==================== Authentication ====================

@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class TokenResponse(
    val access_token: String,
    val token_type: String = "bearer",
    val expires_in: Int? = null
)

@Serializable
data class UserProfile(
    val id: String? = null,
    val username: String,
    val email: String? = null,
    val is_active: Boolean = true
)

// ==================== Market Data ====================

@Serializable
data class PriceResponse(
    val symbol: String,
    val price: Double,
    val timestamp: Long,
    val provider: String,
    val cached: Boolean = false
)

@Serializable
data class OHLCVDataPoint(
    val timestamp: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Double
)

@Serializable
data class OHLCVResponse(
    val symbol: String,
    val interval: String,
    val data: List<OHLCVDataPoint>,
    val provider: String,
    val cached: Boolean = false
)

// ==================== Signals ====================

@Serializable
data class SignalResponse(
    val symbol: String,
    val signal_type: String,
    val category: String,
    val entry_price: Double,
    val take_profit: Double,
    val stop_loss: Double,
    val take_profit_pct: Double,
    val stop_loss_pct: Double,
    val risk_reward_ratio: Double,
    val position_size_pct: Double,
    val strength: String,
    val confidence: Double,
    val timestamp: String,
    val is_valid: Boolean,
    val metadata: Map<String, String>? = null
)

@Serializable
data class SignalSummaryResponse(
    val total_signals: Int,
    val by_category: Map<String, Int>,
    val by_strength: Map<String, Int>,
    val timestamp: String
)

// ==================== Portfolio ====================

@Serializable
data class PortfolioValueResponse(
    val total_btc: Double,
    val total_usd: Double? = null,
    val holdings_btc: Map<String, Double>,
    val btc_allocation: Double,
    val timestamp: String
)

@Serializable
data class AssetPriceResponse(
    val symbol: String,
    val price_usd: Double? = null,
    val price_btc: Double? = null,
    val change_24h: Double? = null,
    val market_cap: Double? = null
)

@Serializable
data class CorrelationResponse(
    val symbol: String,
    val correlation_to_btc: Double,
    val timeframe: String? = null
)

@Serializable
data class RebalancingAction(
    val symbol: String,
    val action: String, // "buy" or "sell"
    val amount: Double,
    val current_amount: Double
)

@Serializable
data class RebalancingPlanResponse(
    val target_btc_allocation: Double,
    val current_btc_allocation: Double,
    val actions: List<RebalancingAction>
)

// ==================== Health ====================

@Serializable
data class HealthResponse(
    val status: String,
    val service: String? = null,
    val version: String? = null,
    val port: Int? = null
)

// ==================== Tasks (Gamification) ====================

@Serializable
data class Task(
    val id: String,
    val title: String,
    val description: String? = null,
    val xp: Int = 10,
    val completed: Boolean = false,
    val category: String = "daily",
    val deadline: String? = null
)

@Serializable
data class UserProgress(
    val current_xp: Int = 0,
    val level: Int = 1,
    val streak: Int = 0,
    val hardware_wallet_progress: Double = 0.0,
    val completed_tasks: List<String> = emptyList()
)
