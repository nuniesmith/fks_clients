package xyz.fkstrading.clients.data.repository

import xyz.fkstrading.clients.api.FksApiClient
import xyz.fkstrading.clients.api.TokenManager
import xyz.fkstrading.clients.data.models.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * Repository for FKS Auth service
 */
class AuthRepository {
    private val apiClient = FksApiClient.instance
    
    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
                encodeDefaults = true
            })
        }
    }
    
    /**
     * Login with username and password
     */
    suspend fun login(username: String, password: String): TokenResponse {
        val request = LoginRequest(username, password)
        val response = apiClient.post<TokenResponse, LoginRequest>(
            "/api/v1/auth/login", 
            request, 
            apiClient.authUrl
        )
        
        // Store token and start auto-refresh
        apiClient.setAuthToken(response.access_token)
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
            httpClient.post("${apiClient.authUrl}/api/v1/auth/logout") {
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
        val response = httpClient.post("${apiClient.authUrl}/api/v1/auth/refresh") {
            contentType(ContentType.Application.Json)
        }.body<TokenResponse>()
        apiClient.setAuthToken(response.access_token)
        return response
    }
    
    /**
     * Get current user profile
     */
    suspend fun getProfile(): UserProfile {
        return apiClient.get("/api/v1/auth/me", apiClient.authUrl)
    }
    
    /**
     * Check auth service health
     */
    suspend fun getHealth(): HealthResponse {
        return apiClient.get("/health", apiClient.authUrl)
    }
}
