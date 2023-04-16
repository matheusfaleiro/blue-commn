package dev.matheusfaleiro.bluecommn.presentation.state

import dev.matheusfaleiro.bluecommn.domain.BluetoothDevice

data class BluetoothUiState(
    val isConnecting: Boolean = false,
    val isConnectionEstablished: Boolean = false,
    val scannedDevices: List<BluetoothDevice> = emptyList(),
    val pairedDevices: List<BluetoothDevice> = emptyList(),
    val errorMessage:String? = null
)