package xyz.fkstrading.clients.domain.viewmodel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xyz.fkstrading.clients.data.models.*
import xyz.fkstrading.clients.data.repository.SignalRepository

/**
 * ViewModel for Signal management and display
 */
class SignalViewModel(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    private val _signals = MutableStateFlow<List<SignalResponse>>(emptyList())
    val signals: StateFlow<List<SignalResponse>> = _signals.asStateFlow()
    
    private val _signalSummary = MutableStateFlow<SignalSummaryResponse?>(null)
    val signalSummary: StateFlow<SignalSummaryResponse?> = _signalSummary.asStateFlow()
    
    private val _selectedCategory = MutableStateFlow<String>("swing")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    /**
     * Load signals for selected category
     */
    fun loadSignals(
        category: String = _selectedCategory.value, 
        symbols: String? = null, 
        aiEnhanced: Boolean = false
    ) {
        scope.launch {
            _isLoading.value = true
            _error.value = null
            _selectedCategory.value = category
            
            try {
                _signals.value = SignalRepository.generateSignals(category, symbols, aiEnhanced)
                _signalSummary.value = SignalRepository.getSignalSummary()
            } catch (e: Exception) {
                _error.value = "Failed to load signals: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Load Bitcoin-specific signals
     */
    fun loadBitcoinSignals() {
        scope.launch {
            _isLoading.value = true
            _error.value = null
            _selectedCategory.value = "bitcoin"
            
            try {
                _signals.value = SignalRepository.getBitcoinSignals(aiEnhanced = true)
                _signalSummary.value = SignalRepository.getSignalSummary()
            } catch (e: Exception) {
                _error.value = "Failed to load Bitcoin signals: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Refresh signals
     */
    fun refresh() {
        loadSignals()
    }
    
    /**
     * Get signals by type
     */
    fun getSignalsByType(type: String): List<SignalResponse> {
        return _signals.value.filter { it.signal_type.uppercase() == type.uppercase() }
    }
    
    /**
     * Get buy signals
     */
    fun getBuySignals(): List<SignalResponse> = getSignalsByType("BUY")
    
    /**
     * Get sell signals
     */
    fun getSellSignals(): List<SignalResponse> = getSignalsByType("SELL")
    
    /**
     * Get signals by strength
     */
    fun getSignalsByStrength(strength: String): List<SignalResponse> {
        return _signals.value.filter { it.strength.lowercase() == strength.lowercase() }
    }
    
    /**
     * Get strong signals only
     */
    fun getStrongSignals(): List<SignalResponse> = getSignalsByStrength("strong")
    
    /**
     * Get high confidence signals
     */
    fun getHighConfidenceSignals(minConfidence: Double = 0.8): List<SignalResponse> {
        return _signals.value.filter { it.confidence >= minConfidence }
    }
    
    /**
     * Calculate average confidence
     */
    fun getAverageConfidence(): Double {
        return if (_signals.value.isNotEmpty()) {
            _signals.value.map { it.confidence }.average()
        } else 0.0
    }
    
    /**
     * Clear error
     */
    fun clearError() {
        _error.value = null
    }
}
