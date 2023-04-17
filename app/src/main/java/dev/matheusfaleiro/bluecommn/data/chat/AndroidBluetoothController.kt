package dev.matheusfaleiro.bluecommn.data.chat

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import dev.matheusfaleiro.bluecommn.BluetoothController
import dev.matheusfaleiro.bluecommn.data.mapper.toByteArray
import dev.matheusfaleiro.bluecommn.data.mapper.toDomain
import dev.matheusfaleiro.bluecommn.domain.BluetoothDeviceDomain
import dev.matheusfaleiro.bluecommn.domain.BluetoothMessage
import java.io.IOException
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


/**
 * Android implementation of the BluetoothController interface.
 *
 * This class provides an Android-specific implementation of the BluetoothController for managing
 * Bluetooth device scanning, discovery, and pairing. It uses the Android Bluetooth APIs to interact
 * with the Bluetooth adapter and leverages a BroadcastReceiver for updates on discovered devices.
 *
 * @param context The Android context required for interacting with system services and registering
 * receivers.
 */
class AndroidBluetoothController(
    private val context: Context
): BluetoothController {

    private val bluetoothManager by lazy {
        context.getSystemService(BluetoothManager::class.java)
    }
    private val bluetoothAdapter by lazy {
        bluetoothManager?.adapter
    }

    private var bluetoothDataTransferService: BluetoothDataTransferService? = null

    private val _isConnectionEstablished = MutableStateFlow(value = false)
    override val isConnectionEstablished: StateFlow<Boolean>
        get() = _isConnectionEstablished.asStateFlow()

    private val _scannedDevices = MutableStateFlow<List<BluetoothDeviceDomain>>(value = emptyList())
    override val scannedDevices: StateFlow<List<BluetoothDeviceDomain>>
        get() = _scannedDevices.asStateFlow()

    private val _pairedDevices = MutableStateFlow<List<BluetoothDeviceDomain>>(value = emptyList())
    override val pairedDevices: StateFlow<List<BluetoothDeviceDomain>>
        get() = _pairedDevices.asStateFlow()

    private val _bluetoothErrors = MutableSharedFlow<String>()
    override val bluetoothErrors: SharedFlow<String>
        get() = _bluetoothErrors.asSharedFlow()

    private val foundDeviceReceiver = FoundDeviceReceiver { device ->
        _scannedDevices.update { availableDevices ->
            if (availableDevices.find { it.address == device.address } == null) {
                availableDevices + device.toDomain()
            } else {
                availableDevices
            }
        }
    }

    @SuppressLint("MissingPermission")
    private val bluetoothStateReceiver = BluetoothStateReceiver { isConnected, bluetoothDevice ->
        if (bluetoothAdapter?.bondedDevices?.contains(bluetoothDevice) == true) {
            _isConnectionEstablished.update {
                isConnected
            }
        } else {
            CoroutineScope(Dispatchers.IO).launch {
                _bluetoothErrors.emit("Can't connect to a non-paired device.")
            }
        }
    }

    private var currentServerSocket: BluetoothServerSocket? = null
    private var currentClientSocket: BluetoothSocket? = null

    /**
     * Initializes the AndroidBluetoothController and updates the list of paired devices.
     *
     * Upon initialization, the list of paired devices is updated by fetching the bonded devices
     * from the Bluetooth adapter and mapping them to domain models.
     */
    init {
        updatePairedDevices()
        context.registerReceiver(
            bluetoothStateReceiver,
            IntentFilter().apply {
                addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
                addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
                addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            }
        )
    }


    /**
     * Starts the Bluetooth discovery process if the necessary permissions are granted.
     *
     * This method first checks if the required permission (BLUETOOTH_SCAN) is granted. If so, it
     * registers the foundDeviceReceiver BroadcastReceiver with the appropriate IntentFilter to listen
     * for discovered devices.
     */
    @SuppressLint("MissingPermission")
    override fun startDiscovery() {
        if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            return
        }

        context.registerReceiver(
            foundDeviceReceiver,
            IntentFilter(BluetoothDevice.ACTION_FOUND)
        )

        updatePairedDevices()

        bluetoothAdapter?.startDiscovery()
    }

    /**
     * Stops the Bluetooth discovery process if the necessary permissions are granted.
     *
     * This method first checks if the required permission (BLUETOOTH_SCAN) is granted. If so, it
     * cancels the ongoing discovery process using the Bluetooth adapter.
     */
    @SuppressLint("MissingPermission")
    override fun stopDiscovery() {
        if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            return
        }

        bluetoothAdapter?.cancelDiscovery()
    }

    /**
     * Starts the Bluetooth server to accept incoming connections and returns a Flow of ConnectionResult.
     * This method checks for required permissions, creates a server socket, and accepts incoming
     * connections. The Flow of ConnectionResult will emit the result of the connection attempt,
     * indicating success or providing an error message in case of failure.
     *
     * @return A Flow of ConnectionResult which represents the result of the connection attempt.
     */
    @SuppressLint("MissingPermission")
    override fun startBluetoothServer(): Flow<ConnectionResult> {
        return flow {
            if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                throw SecurityException("No BLUETOOTH_CONNECT permission")
            }

            currentServerSocket = bluetoothAdapter?.listenUsingRfcommWithServiceRecord(
                "chat_service",
                UUID.fromString(SERVICE_UUID)
            )

            var shouldLoop = true
            while (shouldLoop) {
                currentClientSocket = try {
                    currentServerSocket?.accept()
                } catch (e: IOException) {
                    shouldLoop = false
                    null
                }
                emit(ConnectionResult.ConnectionEstablished)
                currentClientSocket?.let {
                    currentServerSocket?.close()

                    val service =
                        BluetoothDataTransferService(socket = it).also { bluetoothService ->
                            bluetoothDataTransferService = bluetoothService
                        }

                    emitAll(service.listenForIncomingMessages())
                }
            }
        }.onCompletion {
            stopBluetoothServer()
        }.flowOn(Dispatchers.IO)
    }


    /**
     * Initiates a connection to the specified Bluetooth device and returns a Flow of ConnectionResult.
     * Implementations should attempt to establish a connection with the given Bluetooth device when
     * this method is called. Once a connection is established, enable bi-directional communication
     * between the devices. The Flow of ConnectionResult will emit the result of the connection attempt,
     * indicating success or providing an error message in case of failure.
     *
     * @param device The BluetoothDevice to connect to.
     * @return A Flow of ConnectionResult which represents the result of the connection attempt.
     */
    @SuppressLint("MissingPermission")
    override fun connectToDevice(device: BluetoothDeviceDomain): Flow<ConnectionResult> {
        return flow {
            if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                throw SecurityException("No BLUETOOTH_CONNECT permission")
            }

            currentClientSocket = bluetoothAdapter
                ?.getRemoteDevice(device.address)
                ?.createRfcommSocketToServiceRecord(
                    UUID.fromString(SERVICE_UUID)
                )
            stopDiscovery()

            currentClientSocket?.let { socket ->
                try {
                    socket.connect()
                    emit(value = ConnectionResult.ConnectionEstablished)

                    BluetoothDataTransferService(socket = socket).also { bluetoothService ->
                        bluetoothDataTransferService = bluetoothService

                        emitAll(flow = bluetoothService.listenForIncomingMessages())
                    }

                } catch (e: IOException) {
                    socket.close()
                    currentClientSocket = null
                    emit(value = ConnectionResult.ConnectionFailed("Connection was interrupted"))
                }
            }
        }.onCompletion {
            stopBluetoothServer()
        }.flowOn(Dispatchers.IO)
    }

    @SuppressLint("MissingPermission")
    override suspend fun sendMessageToDevice(
        device: BluetoothDeviceDomain,
        message: String
    ): BluetoothMessage? {
        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            return null
        }

        if (bluetoothDataTransferService == null) {
            return null
        }

        val bluetoothMessage = BluetoothMessage(
            message = message,
            sender = bluetoothAdapter?.name ?: "Unknown",
            isFromLocalUser = true
        )

        bluetoothDataTransferService?.sendMessage(bluetoothMessage.toByteArray())

        return bluetoothMessage
    }

    /**
     * Stops the Bluetooth server and disconnects any active connections.
     * Implementations should stop the Bluetooth server from listening for incoming connections and
     * gracefully close any ongoing connections when this method is called. Resources associated
     * with the server, such as threads or sockets, should be released.
     */
    override fun stopBluetoothServer() {
        currentClientSocket?.close()
        currentServerSocket?.close()
        currentClientSocket = null
        currentServerSocket = null
    }


    /**
     * Releases resources held by the AndroidBluetoothController instance.
     *
     * This method unregisters the foundDeviceReceiver BroadcastReceiver to release resources when
     * the AndroidBluetoothController is no longer needed. It is important to call this method
     * when the controller is no longer required to avoid potential memory leaks.
     */
    override fun release() {
        context.unregisterReceiver(foundDeviceReceiver)
        context.unregisterReceiver(bluetoothStateReceiver)
        stopBluetoothServer()
    }

    /**
     * Updates the list of paired devices.
     *
     * This method first checks if the required permission (BLUETOOTH_CONNECT) is granted. If so, it
     * fetches the list of bonded devices from the Bluetooth adapter, maps them to domain models,
     * and updates the _pairedDevices MutableStateFlow with the new list.
     */
    @SuppressLint("MissingPermission")
    private fun updatePairedDevices() {
        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            return
        }
        bluetoothAdapter
            ?.bondedDevices
            ?.map { it.toDomain() }
            ?.also { devices ->
                _pairedDevices.update { devices }
            }
    }

    private fun hasPermission(permission: String): Boolean {
        return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        const val SERVICE_UUID = "27b7d1da-08c7-4505-a6d1-2459987e5e2d"
    }
}