package dev.matheusfaleiro.bluecommn.data.mapper

import dev.matheusfaleiro.bluecommn.domain.BluetoothMessage

/**
 * Converts the input string into a BluetoothMessage object.
 *
 * The input string is expected to have two parts separated by a "|" delimiter:
 * - The first part is the message content.
 * - The second part is the sender name.
 *
 * If the input string is not properly formatted, contains an empty message or sender name,
 * the function returns null.
 *
 * @param isFromLocalUser A boolean flag indicating if the message is from the local user.
 * @return A BluetoothMessage object if the input string is properly formatted, or null otherwise.
 */
fun String.toBluetoothMessage(isFromLocalUser: Boolean): BluetoothMessage? {
    val parts = split("␟", limit = 2)

    val message = parts.getOrNull(0)?.trim() ?: return null

    val senderName = parts.getOrNull(1)?.trim() ?: return null

    if (message.isEmpty() || senderName.isEmpty()) {
        return null
    }

    return BluetoothMessage(message, senderName, isFromLocalUser)
}


/**
 * Converts the BluetoothMessage object into a ByteArray.
 *
 * The function concatenates the message and sender properties using a less common delimiter (in this case, "␟"),
 * and then converts the resulting string into a ByteArray.
 *
 * @return A ByteArray representation of the BluetoothMessage object.
 */
fun BluetoothMessage.toByteArray(): ByteArray {
    val delimiter = "␟"
    return "$message$delimiter$sender".toByteArray()
}