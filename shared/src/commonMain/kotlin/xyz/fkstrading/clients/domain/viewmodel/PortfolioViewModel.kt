package xyz.fkstrading.clients.domain.viewmodel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xyz.fkstrading.clients.data.models.*
import xyz.fkstrading.clients.data.repository.PortfolioRepository
import xyz.fkstrading.clients.data.repository.DataRepository

/**
 * ViewModel for Portfolio Dashboard
 */
class PortfolioViewModel(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    private val portfolioRepository = PortfolioRepository()
    private val dataRepository = DataRepository()
    
    private val _portfolioValue = MutableStateFlow<PortfolioValueResponse?>(null)
    val portfolioValue: StateFlow<PortfolioValueResponse?> = _portfolioValue.asStateFlow()
    
    private val _assetPrices = MutableStateFlow<List<AssetPriceResponse>>(emptyList())
    val assetPrices: StateFlow<List<AssetPriceResponse>> = _assetPrices.asStateFlow()
    
    private val _correlations = MutableStateFlow<List<CorrelationResponse>>(emptyList())
    val correlations: StateFlow<List<CorrelationResponse>> = _correlations.asStateFlow()
    
    private val _btcPrice = MutableStateFlow<PriceResponse?>(null)
    val btcPrice: StateFlow<PriceResponse?> = _btcPrice.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    /**
     * Load all portfolio data
     */
    fun loadPortfolio() {
        scope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                _portfolioValue.value = portfolioRepository.getPortfolioValue()
                _assetPrices.value = portfolioRepository.getAssetPrices()
                _correlations.value = portfolioRepository.getCorrelations()
                _btcPrice.value = dataRepository.getBtcPrice()
            } catch (e: Exception) {
                _error.value = "Failed to load portfolio: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Refresh portfolio data
     */
    fun refresh() {
        loadPortfolio()
    }
    
    /**
     * Get rebalancing plan
     */
    fun getRebalancingPlan(targetBtcAllocation: Double, onResult: (RebalancingPlanResponse?) -> Unit) {
        scope.launch {
            try {
                val plan = portfolioRepository.getRebalancingPlan(targetBtcAllocation)
                onResult(plan)
            } catch (e: Exception) {
                _error.value = "Failed to get rebalancing plan: ${e.message}"
                onResult(null)
            }
        }
    }
    
    /**
     * Get portfolio value in USD
     */
    fun getPortfolioValueUsd(): Double? {
        val btcValue = _portfolioValue.value?.total_btc ?: return null
        val btcPriceUsd = _btcPrice.value?.price ?: return null
        return btcValue * btcPriceUsd
    }
    
    /**
     * Clear error
     */
    fun clearError() {
        _error.value = null
    }
}
