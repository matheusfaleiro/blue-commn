package dev.matheusfaleiro.bluecommn.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.matheusfaleiro.bluecommn.BluetoothController
import dev.matheusfaleiro.bluecommn.data.chat.ConnectionResult
import dev.matheusfaleiro.bluecommn.domain.BluetoothDevice
import dev.matheusfaleiro.bluecommn.presentation.state.BluetoothUiState
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class BluetoothViewModel @Inject constructor(
    private val bluetoothController: BluetoothController
) : ViewModel() {

    private val _uiState = MutableStateFlow(BluetoothUiState())

    val uiState = combine(
        bluetoothController.scannedDevices,
        bluetoothController.pairedDevices,
        _uiState
    ) { scannedDevices, pairedDevices, state ->
        state.copy(
            scannedDevices = scannedDevices,
            pairedDevices = pairedDevices,
            message = if (state.isConnectionEstablished) {
                state.message
            } else {
                emptyList()
            }
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        _uiState.value
    )

    private var deviceConnectionJob: Job? = null

    init {
        bluetoothController.isConnectionEstablished.onEach { isConnected ->
            _uiState.update { it.copy(isConnectionEstablished = isConnected) }
        }.launchIn(viewModelScope)

        bluetoothController.bluetoothErrors.onEach { error ->
            _uiState.update {
                it.copy(
                    errorMessage = error
                )
            }
        }.launchIn(viewModelScope)
    }

    fun connectToDevice(device: BluetoothDevice) {
        _uiState.update { it.copy(isConnecting = true) }
        deviceConnectionJob = bluetoothController
            .connectToDevice(device)
            .listen()
    }

    fun sendMessage(message: String) = viewModelScope.launch {
        val sentMessage = bluetoothController.sendMessageToDevice(
            device = BluetoothDevice(
                name = "Lacy Matthews",
                address = "agam"
            ), message = message
        )

        _uiState.update {
            it.copy(message = (it.message + sentMessage).filterNotNull())
        }
    }

    fun disconnectFromDevice() {
        deviceConnectionJob?.cancel()
        bluetoothController.stopBluetoothServer()
        _uiState.update {
            it.copy(
                isConnecting = false,
                isConnectionEstablished = false
            )
        }
    }

    fun waitForIncomingConnections() {
        _uiState.update { it.copy(isConnecting = true) }
        deviceConnectionJob = bluetoothController
            .startBluetoothServer()
            .listen()
    }

    fun startScan() {
        bluetoothController.startDiscovery()
    }

    fun stopScan() {
        bluetoothController.stopDiscovery()
    }

    private fun Flow<ConnectionResult>.listen(): Job {
        return onEach { result ->
            when (result) {
                ConnectionResult.ConnectionEstablished -> {
                    _uiState.update {
                        it.copy(
                            isConnectionEstablished = true,
                            isConnecting = false,
                            errorMessage = null
                        )
                    }
                }

                is ConnectionResult.ConnectionFailed -> {
                    _uiState.update {
                        it.copy(
                            isConnectionEstablished = false,
                            isConnecting = false,
                            errorMessage = result.error
                        )
                    }
                }

                is ConnectionResult.TransferSuccessful -> {
                    _uiState.update {
                        it.copy(
                            message = (it.message + result.bluetoothMessage).filterNotNull()
                        )
                    }
                }
            }
        }.catch { _ ->
            bluetoothController.stopBluetoothServer()
            _uiState.update {
                it.copy(
                    isConnectionEstablished = false,
                    isConnecting = false,
                )
            }
        }
            .launchIn(viewModelScope)
    }

    override fun onCleared() {
        super.onCleared()
        bluetoothController.release()
    }
}