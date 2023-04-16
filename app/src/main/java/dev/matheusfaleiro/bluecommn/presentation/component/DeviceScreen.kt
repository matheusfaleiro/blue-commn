package dev.matheusfaleiro.bluecommn.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.matheusfaleiro.bluecommn.domain.BluetoothDevice
import dev.matheusfaleiro.bluecommn.presentation.state.BluetoothUiState

@Composable
fun DeviceScreen(
    bluetoothUiState: BluetoothUiState,
    onStartScan: () -> Unit,
    onStartServer: () -> Unit,
    onDeviceClicked: (BluetoothDevice) -> Unit,
    onStopScan: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        BluetoothDevices(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1F),
            pairedDevices = bluetoothUiState.pairedDevices,
            scannedDevices = bluetoothUiState.scannedDevices,
            onClick = onDeviceClicked
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            Button(onClick = onStartScan) {
                Text(text = "Start Scan")
            }

            Button(onClick = onStartServer) {
                Text(text = "Start Server")
            }

            Button(onClick = onStopScan) {
                Text(text = "Stop Scan")
            }
        }
    }
}