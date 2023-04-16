package dev.matheusfaleiro.bluecommn.data.chat

sealed interface ConnectionResult {
    object ConnectionEstablished : ConnectionResult
    data class ConnectionFailed(val error: String) : ConnectionResult
}