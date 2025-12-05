package xyz.fkstrading.clients.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.websocket.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json

/**
 * FKS API Client for all backend services
 */
class FksApiClient {
    // Base URLs - configurable via environment
    var apiBaseUrl = "http://localhost:8001"
        private set
    var dataBaseUrl = "http://localhost:8003"
        private set
    var authBaseUrl = "http://localhost:8009"
        private set
    var portfolioBaseUrl = "http://localhost:8012"
        private set
    
    // JWT token storage
    var authToken: String? = null
        private set
    
    /**
     * Configure base URLs from environment or explicit values
     */
    fun configure(
        apiUrl: String? = null,
        dataUrl: String? = null,
        authUrl: String? = null,
        portfolioUrl: String? = null
    ) {
        apiUrl?.let { apiBaseUrl = it }
        dataUrl?.let { dataBaseUrl = it }
        authUrl?.let { authBaseUrl = it }
        portfolioUrl?.let { portfolioBaseUrl = it }
    }
    
    /**
     * Service URL accessors
     */
    val apiUrl: String get() = apiBaseUrl
    val dataUrl: String get() = dataBaseUrl
    val authUrl: String get() = authBaseUrl
    val portfolioUrl: String get() = portfolioBaseUrl
    
    /**
     * Configured HTTP client with JSON serialization and logging
     */
    val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
                encodeDefaults = true
            })
        }
        
        install(Logging) {
            level = LogLevel.INFO
        }
    }
    
    /**
     * WebSocket client for real-time updates
     */
    val webSocketClient = HttpClient {
        install(WebSockets)
        install(Logging) {
            level = LogLevel.INFO
        }
    }
    
    /**
     * Set authentication token
     */
    fun setAuthToken(token: String) {
        authToken = token
    }
    
    /**
     * Clear authentication token
     */
    fun clearAuthToken() {
        authToken = null
    }
    
    /**
     * Check if authenticated
     */
    fun isAuthenticated(): Boolean = authToken != null
    
    /**
     * Generic GET request
     */
    suspend inline fun <reified T> get(
        endpoint: String,
        baseUrl: String = apiBaseUrl,
        queryParams: Map<String, String>? = null
    ): T {
        val url = buildString {
            append(baseUrl)
            if (!endpoint.startsWith("/")) append("/")
            append(endpoint)
            if (queryParams != null && queryParams.isNotEmpty()) {
                append("?")
                append(queryParams.entries.joinToString("&") { "${it.key}=${it.value}" })
            }
        }
        
        return httpClient.get(url) {
            authToken?.let { token ->
                header(HttpHeaders.Authorization, "Bearer $token")
            }
        }.body()
    }
    
    /**
     * Generic POST request
     */
    suspend inline fun <reified T, reified B> post(
        endpoint: String,
        body: B,
        baseUrl: String = apiBaseUrl
    ): T {
        val url = buildString {
            append(baseUrl)
            if (!endpoint.startsWith("/")) append("/")
            append(endpoint)
        }
        
        return httpClient.post(url) {
            contentType(ContentType.Application.Json)
            authToken?.let { token ->
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            setBody(body)
        }.body()
    }
    
    /**
     * Generic PUT request
     */
    suspend inline fun <reified T, reified B> put(
        endpoint: String,
        body: B,
        baseUrl: String = apiBaseUrl
    ): T {
        val url = buildString {
            append(baseUrl)
            if (!endpoint.startsWith("/")) append("/")
            append(endpoint)
        }
        
        return httpClient.put(url) {
            contentType(ContentType.Application.Json)
            authToken?.let { token ->
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            setBody(body)
        }.body()
    }
    
    /**
     * Generic DELETE request
     */
    suspend inline fun <reified T> delete(
        endpoint: String,
        baseUrl: String = apiBaseUrl
    ): T {
        val url = buildString {
            append(baseUrl)
            if (!endpoint.startsWith("/")) append("/")
            append(endpoint)
        }
        
        return httpClient.delete(url) {
            authToken?.let { token ->
                header(HttpHeaders.Authorization, "Bearer $token")
            }
        }.body()
    }
    
    /**
     * WebSocket connection for real-time updates
     */
    suspend fun connectWebSocket(
        endpoint: String,
        baseUrl: String = apiBaseUrl
    ): Flow<String> = flow {
        val url = buildString {
            append(baseUrl.replace("http://", "ws://").replace("https://", "wss://"))
            if (!endpoint.startsWith("/")) append("/")
            append(endpoint)
        }
        
        webSocketClient.webSocket(url) {
            for (frame in incoming) {
                if (frame is Frame.Text) {
                    emit(frame.readText())
                }
            }
        }
    }
    
    companion object {
        val instance: FksApiClient = FksApiClient()
    }
}
