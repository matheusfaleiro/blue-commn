package dev.matheusfaleiro.bluecommn.data.mapper

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import dev.matheusfaleiro.bluecommn.domain.BluetoothDeviceDomain

@SuppressLint("MissingPermission")
fun BluetoothDevice.toDomain(): BluetoothDeviceDomain {
    return BluetoothDeviceDomain(
        name = name.orEmpty(),
        address = address.orEmpty())
}