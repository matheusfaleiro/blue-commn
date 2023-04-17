package dev.matheusfaleiro.bluecommn.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.matheusfaleiro.bluecommn.domain.BluetoothMessage
import dev.matheusfaleiro.bluecommn.ui.theme.BlueCommnTheme

@Composable
fun ChatMessage(modifier: Modifier = Modifier, bluetoothMessage: BluetoothMessage) {
    Column(
        modifier = modifier
            .clip(
                RoundedCornerShape(
                    topStart = if (bluetoothMessage.isFromLocalUser) 16.dp else 0.dp,
                    topEnd = 16.dp,
                    bottomStart = 16.dp,
                    bottomEnd = if (bluetoothMessage.isFromLocalUser) 0.dp else 16.dp,
                )
            )
            .background(
                color = if (bluetoothMessage.isFromLocalUser) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.secondary
                }
            )
            .padding(all = 8.dp)
    ) {
        Text(
            text = bluetoothMessage.message,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onPrimary
        )

        Spacer(modifier = Modifier.height(height = 8.dp))

        Text(
            text = bluetoothMessage.sender,
            modifier = Modifier.align(
                alignment = if (bluetoothMessage.isFromLocalUser) {
                    androidx.compose.ui.Alignment.End
                } else {
                    androidx.compose.ui.Alignment.Start
                }
            ),
            fontSize = 8.sp,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Preview
@Composable
fun PreviewChatMessageFromLocalUser() {
    BlueCommnTheme {
        ChatMessage(
            bluetoothMessage = BluetoothMessage(
                message = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.",
                sender = "Matheus",
                isFromLocalUser = true
            )
        )
    }
}

@Preview
@Composable
fun PreviewChatMessageFromOtherDevice() {
    BlueCommnTheme {
        ChatMessage(
            bluetoothMessage = BluetoothMessage(
                message = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.",
                sender = "Débora",
                isFromLocalUser = false
            )
        )
    }
}