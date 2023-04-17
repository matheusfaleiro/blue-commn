package dev.matheusfaleiro.bluecommn.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import dev.matheusfaleiro.bluecommn.presentation.state.BluetoothUiState

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    uiState: BluetoothUiState,
    onDisconnect: () -> Unit,
    onSendMessage: (String) -> Unit
) {

    var message = rememberSaveable {
        mutableStateOf("")
    }

    var keyboardController = LocalSoftwareKeyboardController.current

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier
                .fillMaxWidth()
                .padding(all = 16.dp)
        ) {
            Text(text = "Messages", modifier = Modifier.weight(1F))

            IconButton(onClick = onDisconnect) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = Icons.Default.Close.name
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1F)
        ) {
            items(uiState.message) { bluetoothMessage ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    ChatMessage(
                        modifier = Modifier.align(
                            alignment = if (bluetoothMessage.isFromLocalUser) {
                                Alignment.End
                            } else {
                                Alignment.Start
                            }
                        ),
                        bluetoothMessage = bluetoothMessage
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                modifier = Modifier.weight(1F),
                value = message.value,
                onValueChange = {
                    message.value = it
                }, placeholder = {
                    Text(text = "Type your message")
                }
            )

            IconButton(onClick = {
                onSendMessage(message.value)

                keyboardController?.hide()
            }) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = Icons.Default.Send.name
                )
            }
        }
    }
}