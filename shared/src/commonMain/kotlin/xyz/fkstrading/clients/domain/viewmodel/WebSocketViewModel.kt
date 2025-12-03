package xyz.fkstrading.clients.domain.viewmodel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect
import kotlinx.serialization.json.Json
import xyz.fkstrading.clients.data.models.SignalResponse
import xyz.fkstrading.clients.data.repository.SignalRepository

/**
 * ViewModel for WebSocket real-time updates
 */
class WebSocketViewModel(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    private val signalRepository = SignalRepository()
    private var webSocketJob: Job? = null
    
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()
    
    private val _latestSignal = MutableStateFlow<SignalResponse?>(null)
    val latestSignal: StateFlow<SignalResponse?> = _latestSignal.asStateFlow()
    
    private val _signalUpdates = MutableStateFlow<List<SignalResponse>>(emptyList())
    val signalUpdates: StateFlow<List<SignalResponse>> = _signalUpdates.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    /**
     * Connect to WebSocket for real-time signal updates
     */
    fun connect() {
        if (_isConnected.value) {
            return // Already connected
        }
        
        webSocketJob = scope.launch {
            try {
                _isConnected.value = true
                _error.value = null
                
                signalRepository.connectSignalWebSocket()
                    .catch { e: Throwable ->
                        _error.value = "WebSocket error: ${e.message}"
                        _isConnected.value = false
                    }
                    .collect { message: String ->
                        try {
                            val signal = json.decodeFromString<SignalResponse>(message)
                            _latestSignal.value = signal
                            
                            // Keep last 50 signals
                            val updated = (_signalUpdates.value + signal).takeLast(50)
                            _signalUpdates.value = updated
                        } catch (e: Exception) {
                            // Parse error - could be different message type
                            _error.value = "Failed to parse: ${e.message}"
                        }
                    }
            } catch (e: Exception) {
                _error.value = "Connection failed: ${e.message}"
                _isConnected.value = false
            }
        }
    }
    
    /**
     * Disconnect from WebSocket
     */
    fun disconnect() {
        webSocketJob?.cancel()
        webSocketJob = null
        _isConnected.value = false
        _error.value = null
    }
    
    /**
     * Clear signal updates
     */
    fun clearUpdates() {
        _signalUpdates.value = emptyList()
        _latestSignal.value = null
    }
    
    /**
     * Get number of updates received
     */
    fun getUpdateCount(): Int = _signalUpdates.value.size
}
