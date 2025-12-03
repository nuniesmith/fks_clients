package xyz.fkstrading.clients.api

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Token Manager for automatic JWT token refresh
 * Handles token expiration and automatic refresh
 */
object TokenManager {
    private var refreshJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    private val _isTokenValid = MutableStateFlow(false)
    val isTokenValid: StateFlow<Boolean> = _isTokenValid.asStateFlow()
    
    private var onTokenRefresh: (suspend () -> Unit)? = null
    
    /**
     * Set the refresh callback
     */
    fun setRefreshCallback(callback: suspend () -> Unit) {
        onTokenRefresh = callback
    }
    
    /**
     * Start automatic token refresh
     * @param refreshIntervalMinutes Refresh interval (default: 15 minutes)
     */
    fun startAutoRefresh(refreshIntervalMinutes: Int = 15) {
        stopAutoRefresh()
        
        _isTokenValid.value = true
        
        refreshJob = scope.launch {
            while (true) {
                delay(refreshIntervalMinutes * 60 * 1000L)
                try {
                    onTokenRefresh?.invoke()
                    _isTokenValid.value = true
                } catch (e: Exception) {
                    _isTokenValid.value = false
                    break
                }
            }
        }
    }
    
    /**
     * Stop automatic token refresh
     */
    fun stopAutoRefresh() {
        refreshJob?.cancel()
        refreshJob = null
    }
    
    /**
     * Mark token as valid after successful login/refresh
     */
    fun markTokenValid() {
        _isTokenValid.value = true
    }
    
    /**
     * Invalidate token (e.g., on 401 response)
     */
    fun invalidateToken() {
        _isTokenValid.value = false
        stopAutoRefresh()
        FksApiClient.instance.clearAuthToken()
    }
}
