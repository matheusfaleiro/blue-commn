package dev.matheusfaleiro.bluecommn.data.chat

import android.annotation.SuppressLint
import android.bluetooth.BluetoothSocket
import dev.matheusfaleiro.bluecommn.data.mapper.toBluetoothMessage
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class BluetoothDataTransferService @Inject constructor(private val socket: BluetoothSocket) {

    @SuppressLint("MissingPermission")
    fun listenForIncomingMessages(): Flow<ConnectionResult> {
        return flow {
            if (!socket.isConnected) {
                return@flow
            }

            val buffer = ByteArray(1024)

            while (true) {
                val byteCount = try {
                    socket.inputStream.read(buffer)
                } catch (exception: Exception) {
                    throw TransferFailedException()
                }
                emit(
                    ConnectionResult.TransferSuccessful(
                        bluetoothMessage = buffer.decodeToString(endIndex = byteCount)
                            .toBluetoothMessage(isFromLocalUser = false)
                    )
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    suspend fun sendMessage(message: ByteArray): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                socket.outputStream.write(message)
                return@withContext true
            } catch (ioException: IOException) {
                return@withContext false
            }
        }
    }
}