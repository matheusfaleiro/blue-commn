package dev.matheusfaleiro.bluecommn.domain

/**
 * Represents a message exchanged between Bluetooth devices.
 *
 * @property message The content of the message being sent or received.
 * @property sender The identifier (e.g., MAC address or device name) of the device that sent the message.
 * @property isFromLocalUser A boolean indicating whether the message was sent by the local user (true) or received from a remote user (false).
 */
data class BluetoothMessage(
    val message: String,
    val sender: String,
    val isFromLocalUser: Boolean
)