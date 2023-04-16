package dev.matheusfaleiro.bluecommn.domain

typealias BluetoothDeviceDomain = BluetoothDevice

/**
 * Represents a Bluetooth device in the application domain.
 *
 * This data class is used to store information about a Bluetooth device within the application.
 * It is separate from the platform-specific Bluetooth device representation to allow for easier
 * manipulation and testing within the application, as well as potential future support for
 * multiple platforms.
 *
 * @property name The name of the Bluetooth device.
 * @property address The hardware address of the Bluetooth device, typically in the format
 *                   "XX:XX:XX:XX:XX:XX", where each X is a hexadecimal digit.
 */
data class BluetoothDevice(
    val name: String,
    val address: String
)