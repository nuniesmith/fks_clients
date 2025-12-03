package xyz.fkstrading.clients.data.repository

import xyz.fkstrading.clients.api.FksApiClient
import xyz.fkstrading.clients.api.TokenManager
import xyz.fkstrading.clients.data.models.*
import io.ktor.client.request.*
import io.ktor.http.*

/**
 * Repository for FKS Auth service
 */
object AuthRepository {
    
    /**
     * Login with username and password
     */
    suspend fun login(username: String, password: String): TokenResponse {
        val request = LoginRequest(username, password)
        val response = FksApiClient.post<TokenResponse, LoginRequest>(
            "/api/v1/auth/login", 
            request, 
            FksApiClient.authUrl
        )
        
        // Store token and start auto-refresh
        FksApiClient.setAuthToken(response.access_token)
        TokenManager.markTokenValid()
        TokenManager.setRefreshCallback { refreshToken() }
        TokenManager.startAutoRefresh(response.expires_in?.let { it / 60 } ?: 15)
        
        return response
    }
    
    /**
     * Logout and clear session
     */
    suspend fun logout() {
        try {
            FksApiClient.httpClient.post("${FksApiClient.authUrl}/api/v1/auth/logout") {
                contentType(ContentType.Application.Json)
            }
        } catch (e: Exception) {
            // Ignore logout errors
        } finally {
            TokenManager.invalidateToken()
        }
    }
    
    /**
     * Refresh JWT token
     */
    suspend fun refreshToken(): TokenResponse {
        val response = FksApiClient.httpClient.post("${FksApiClient.authUrl}/api/v1/auth/refresh") {
            contentType(ContentType.Application.Json)
        }.body<TokenResponse>()
        FksApiClient.setAuthToken(response.access_token)
        return response
    }
    
    /**
     * Get current user profile
     */
    suspend fun getProfile(): UserProfile {
        return FksApiClient.get("/api/v1/auth/me", FksApiClient.authUrl)
    }
    
    /**
     * Check auth service health
     */
    suspend fun getHealth(): HealthResponse {
        return FksApiClient.get("/health", FksApiClient.authUrl)
    }
}
