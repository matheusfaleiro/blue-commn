package dev.matheusfaleiro.bluecommn.presentation.state

import dev.matheusfaleiro.bluecommn.domain.BluetoothDevice
import dev.matheusfaleiro.bluecommn.domain.BluetoothMessage

data class BluetoothUiState(
    val isConnecting: Boolean = false,
    val isConnectionEstablished: Boolean = false,
    val scannedDevices: List<BluetoothDevice> = emptyList(),
    val pairedDevices: List<BluetoothDevice> = emptyList(),
    val message: List<BluetoothMessage> = emptyList(),
    val errorMessage:String? = null,
)