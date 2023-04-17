package dev.matheusfaleiro.bluecommn.data.chat

import dev.matheusfaleiro.bluecommn.domain.BluetoothMessage

sealed interface ConnectionResult {
    object ConnectionEstablished : ConnectionResult

    data class TransferSuccessful(val bluetoothMessage: BluetoothMessage?) : ConnectionResult

    data class ConnectionFailed(val error: String) : ConnectionResult
}