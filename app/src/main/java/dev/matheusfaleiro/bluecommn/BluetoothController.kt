package dev.matheusfaleiro.bluecommn

import dev.matheusfaleiro.bluecommn.data.chat.ConnectionResult
import dev.matheusfaleiro.bluecommn.domain.BluetoothDevice
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
Interface that defines the contract for BluetoothController implementations.
This interface provides a unified way to manage Bluetooth devices' scanning and discovery
processes. It exposes StateFlows for observing scanned and paired devices and offers methods
to control the discovery process.
 */
interface BluetoothController {

    /**
     * A StateFlow that emits a boolean indicating if a connection is established.
     * This StateFlow should be updated whenever the connection status changes. Subscribers can
     * observe this StateFlow to receive updates on the connection status.
     */
    val isConnectionEstablished: StateFlow<Boolean>

    /**
    A StateFlow that emits a list of scanned Bluetooth devices.
    This StateFlow should be updated whenever new Bluetooth devices are discovered during the
    discovery process. Subscribers can observe this StateFlow to receive updates on the list of
    scanned devices.
     */
    val scannedDevices: StateFlow<List<BluetoothDevice>>

    /**
    A StateFlow that emits a list of paired Bluetooth devices.
    This StateFlow should be updated when the list of paired devices changes. Subscribers can
    observe this StateFlow to receive updates on the list of paired devices.
     */
    val pairedDevices: StateFlow<List<BluetoothDevice>>

    /**
     * A SharedFlow that emits error messages related to Bluetooth operations.
     * This SharedFlow should emit error messages when any issues arise during Bluetooth
     * operations, such as connection failures or permission issues. Subscribers can
     * observe this SharedFlow to receive updates on errors and handle them accordingly.
     */
    val bluetoothErrors: SharedFlow<String>

    /**
    Starts the Bluetooth discovery process.
    Implementations should initiate the process of discovering nearby Bluetooth devices when
    this method is called. Discovered devices should be added to the scannedDevices StateFlow.
     */
    fun startDiscovery()

    /**
    Stops the Bluetooth discovery process.
    Implementations should stop the process of discovering nearby Bluetooth devices when this
    method is called. Any ongoing scans should be terminated, and no new devices should be added
    to the scannedDevices StateFlow after this method is called.
     */
    fun stopDiscovery()

    /**
    Starts the Bluetooth server to accept incoming connections and returns a Flow of ConnectionResult.
    Implementations should initiate the process of listening for incoming Bluetooth connections
    when this method is called. The server should be set up to accept incoming connections from
    client devices, and once a connection is established, enable bi-directional communication
    between the devices. The Flow of ConnectionResult will emit the result of the connection attempt,
    indicating success or providing an error message in case of failure.

    @return A Flow of ConnectionResult which represents the result of the connection attempt.
     */
    fun startBluetoothServer(): Flow<ConnectionResult>

    /**
    Initiates a connection to the specified Bluetooth device and returns a Flow of ConnectionResult.
    Implementations should attempt to establish a connection with the given Bluetooth device when
    this method is called. Once a connection is established, enable bi-directional communication
    between the devices. The Flow of ConnectionResult will emit the result of the connection attempt,
    indicating success or providing an error message in case of failure.

    @param device The BluetoothDevice to connect to.
    @return A Flow of ConnectionResult which represents the result of the connection attempt.
     */
    fun connectToDevice(device: BluetoothDevice): Flow<ConnectionResult>

    /**
    Stops the Bluetooth server and disconnects any active connections.
    Implementations should stop the Bluetooth server from listening for incoming connections and
    gracefully close any ongoing connections when this method is called. Resources associated
    with the server, such as threads or sockets, should be released.
     */
    fun stopBluetoothServer()

    /**
    Releases any resources held by the BluetoothController.
    Implementations should release any resources or unregister any listeners held by the
    BluetoothController when this method is called. This is typically called when the
    BluetoothController is no longer needed, such as when the lifecycle of an associated component
    ends.
     */
    fun release()
}