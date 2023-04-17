package dev.matheusfaleiro.bluecommn.presentation

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import dev.matheusfaleiro.bluecommn.presentation.component.ChatScreen
import dev.matheusfaleiro.bluecommn.presentation.component.DeviceScreen
import dev.matheusfaleiro.bluecommn.ui.theme.BlueCommnTheme

@AndroidEntryPoint
class BlueCommnActivity : ComponentActivity() {
    private val bluetoothManager by lazy {
        applicationContext.getSystemService(BluetoothManager::class.java)
    }
    private val bluetoothAdapter by lazy {
        bluetoothManager?.adapter
    }

    private val isBluetoothEnabled: Boolean
        get() = bluetoothAdapter?.isEnabled == true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val enableBluetoothLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { /* Not needed */ }

        val permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permission ->
            val canEnableBluetooth = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                permission[Manifest.permission.BLUETOOTH_CONNECT] == true
            } else true

            if (canEnableBluetooth && !isBluetoothEnabled) {
                enableBluetoothLauncher.launch(
                    Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                )
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                )
            )
        }


        setContent {
            BlueCommnTheme() {
                val viewModel = hiltViewModel<BluetoothViewModel>()

                val bluetoothUiState by viewModel.uiState.collectAsStateWithLifecycle()

                LaunchedEffect(key1 = bluetoothUiState.errorMessage) {
                    bluetoothUiState.errorMessage?.let {
                        Toast.makeText(
                            applicationContext,
                            it,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                LaunchedEffect(key1 = bluetoothUiState.isConnectionEstablished){
                    Toast.makeText(
                        applicationContext,
                        "You are connected!",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                Surface(
                    color = MaterialTheme.colorScheme.surface
                ) {
                    when {
                        bluetoothUiState.isConnecting -> {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator()
                                Text(text = "Connecting...")
                            }
                        }

                        bluetoothUiState.isConnectionEstablished -> {
                            ChatScreen(
                                uiState = bluetoothUiState,
                                onDisconnect = viewModel::disconnectFromDevice,
                                onSendMessage = viewModel::sendMessage
                            )
                        }

                        else -> {
                            DeviceScreen(
                                bluetoothUiState = bluetoothUiState,
                                onStartScan = viewModel::startScan,
                                onStartServer = viewModel::waitForIncomingConnections,
                                onDeviceClicked = viewModel::connectToDevice,
                                onStopScan = viewModel::stopScan
                            )
                        }
                    }
                }
            }
        }
    }
}