package dev.matheusfaleiro.bluecommn.presentation.component

import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.dp
import dev.matheusfaleiro.bluecommn.domain.BluetoothDevice
import dev.matheusfaleiro.bluecommn.ui.theme.BlueCommnTheme
import java.util.UUID

@Composable
fun BluetoothDevice(
    modifier: Modifier = Modifier,
    device: BluetoothDevice,
    onClick: (BluetoothDevice) -> Unit
) {

    val accentColor = MaterialTheme.colorScheme.primary

    Row(
        modifier = modifier
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Bluetooth,
            contentDescription = Icons.Default.Bluetooth.name,
            modifier = Modifier
                .size(18.dp)
                .drawBehind {
                    drawCircle(
                        color = accentColor,
                        radius = this.size.maxDimension
                    )
                },
            tint = MaterialTheme.colorScheme.onTertiary,
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp).clickable {
                    onClick(device)
                }
        ) {
            Text(
                text = device.name,
                color  = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = device.address,
                color  = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5F),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BluetoothDevices(
    modifier: Modifier = Modifier,
    pairedDevices: List<BluetoothDevice>,
    scannedDevices: List<BluetoothDevice>,
    onClick: (BluetoothDevice) -> Unit
) {

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(vertical = 8.dp),
    ) {
        stickyHeader {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.secondary)
            ) {
                Text(
                    text = "Paired Devices",
                    modifier = Modifier.padding(horizontal = 8.dp),
                    color  = MaterialTheme.colorScheme.onSecondary,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }

        items(pairedDevices) { device ->
            BluetoothDevice(device = device, onClick = onClick)
        }

        stickyHeader {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.secondary)
            ) {
                Text(
                    text = "Scanned Devices",
                    modifier = Modifier.padding(horizontal = 8.dp),
                    color  = MaterialTheme.colorScheme.onSecondary,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }

        items(scannedDevices) { device ->
            BluetoothDevice(device = device, onClick = onClick)
        }
    }
}


@PreviewScreen
@Composable
fun PreviewBluetoothDevice() {
    BlueCommnTheme {
        Surface {
            BluetoothDevices(
                pairedDevices = listOf(
                    BluetoothDevice(name = "Device 1", address = "00:00:00:00:00:00"),
                    BluetoothDevice(name = "Device 2", address = "00:00:00:00:00:00"),
                    BluetoothDevice(name = "Device 3", address = "00:00:00:00:00:00"),
                    BluetoothDevice(name = "Device 4", address = "00:00:00:00:00:00"),
                    BluetoothDevice(name = "Device 5", address = "00:00:00:00:00:00"),
                ),
                scannedDevices = listOf(
                    BluetoothDevice(name = "Device 1", address = "00:00:00:00:00:00"),
                    BluetoothDevice(name = "Device 2", address = "00:00:00:00:00:00"),
                    BluetoothDevice(name = "Device 3", address = "00:00:00:00:00:00"),
                    BluetoothDevice(name = "Device 4", address = "00:00:00:00:00:00"),
                    BluetoothDevice(name = "Device 5", address = "00:00:00:00:00:00"),
                ),
                onClick = {})
        }
    }
}

@Preview(
    name = "Dark Mode - Screen Preview",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    device = "id:pixel",
    apiLevel = 26,
    wallpaper = Wallpapers.BLUE_DOMINATED_EXAMPLE
)
@Preview(
    name = "Light Mode - Screen Preview",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    device = "id:pixel_6_pro",
    wallpaper = Wallpapers.RED_DOMINATED_EXAMPLE
)
annotation class PreviewScreen