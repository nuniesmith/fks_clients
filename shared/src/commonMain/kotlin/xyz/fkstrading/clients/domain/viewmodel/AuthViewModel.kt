package xyz.fkstrading.clients.domain.viewmodel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xyz.fkstrading.clients.data.models.*
import xyz.fkstrading.clients.data.repository.AuthRepository

/**
 * ViewModel for Authentication
 */
class AuthViewModel(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    private val authRepository = AuthRepository()
    
    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()
    
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    /**
     * Login with username and password
     */
    fun login(username: String, password: String) {
        scope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val tokenResponse = authRepository.login(username, password)
                _isAuthenticated.value = true
                
                // Load user profile
                _userProfile.value = authRepository.getProfile()
            } catch (e: Exception) {
                _error.value = "Login failed: ${e.message}"
                _isAuthenticated.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Logout and clear session
     */
    fun logout() {
        scope.launch {
            _isLoading.value = true
            try {
                authRepository.logout()
            } catch (e: Exception) {
                // Ignore logout errors
            } finally {
                _isAuthenticated.value = false
                _userProfile.value = null
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Refresh authentication token
     */
    fun refreshToken() {
        scope.launch {
            try {
                authRepository.refreshToken()
            } catch (e: Exception) {
                _error.value = "Token refresh failed: ${e.message}"
                _isAuthenticated.value = false
                _userProfile.value = null
            }
        }
    }
    
    /**
     * Check if user is authenticated by loading profile
     */
    fun checkAuthentication() {
        scope.launch {
            try {
                val profile = authRepository.getProfile()
                _userProfile.value = profile
                _isAuthenticated.value = true
            } catch (e: Exception) {
                _isAuthenticated.value = false
                _userProfile.value = null
            }
        }
    }
    
    /**
     * Clear error state
     */
    fun clearError() {
        _error.value = null
    }
}
