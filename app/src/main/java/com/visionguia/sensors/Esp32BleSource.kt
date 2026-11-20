package com.visionguia.sensors

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.ParcelUuid
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import java.util.UUID

class Esp32BleSource(
    private val context: Context,
    private val parser: Esp32ProtocolParser = Esp32ProtocolParser(),
) : SensorSource {
    companion object {
        val SERVICE_UUID: UUID = UUID.fromString("7e400001-b5a3-f393-e0a9-e50e24dcca9e")
        val SENSOR_UUID: UUID = UUID.fromString("7e400003-b5a3-f393-e0a9-e50e24dcca9e")
        private val CCCD_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
    }
    private val mutable = MutableSharedFlow<com.visionguia.core.model.SensorReading>(extraBufferCapacity = 32)
    override val readings: SharedFlow<com.visionguia.core.model.SensorReading> = mutable
    private val adapter: BluetoothAdapter? get() = context.getSystemService(BluetoothManager::class.java)?.adapter
    private var gatt: BluetoothGatt? = null

    @SuppressLint("MissingPermission")
    override suspend fun start() {
        check(hasPermission()) { "Falta permiso Bluetooth" }
        val scanner = adapter?.bluetoothLeScanner ?: error("Bluetooth LE no disponible")
        val filter = ScanFilter.Builder().setServiceUuid(ParcelUuid(SERVICE_UUID)).build()
        scanner.startScan(listOf(filter), ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build(), scanCallback)
    }

    @SuppressLint("MissingPermission")
    override suspend fun stop() {
        adapter?.bluetoothLeScanner?.stopScan(scanCallback)
        gatt?.disconnect(); gatt?.close(); gatt = null
    }

    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            if (!hasPermission() || gatt != null) return
            adapter?.bluetoothLeScanner?.stopScan(this)
            gatt = result.device.connectGatt(context, false, gattCallback, BluetoothDeviceTransport.LE)
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(
            gatt: BluetoothGatt,
            status: Int,
            newState: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS &&
                newState == BluetoothProfile.STATE_CONNECTED
            ) {
                val requested = gatt.requestMtu(185)

                if (!requested) {
                    gatt.discoverServices()
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                gatt.close()
                this@Esp32BleSource.gatt = null
            }
        }

        @SuppressLint("MissingPermission")
        override fun onMtuChanged(
            gatt: BluetoothGatt,
            mtu: Int,
            status: Int
        ) {
            gatt.discoverServices()
        }
        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS) return
            val characteristic = gatt.getService(SERVICE_UUID)?.getCharacteristic(SENSOR_UUID) ?: return
            gatt.setCharacteristicNotification(characteristic, true)
            val descriptor = characteristic.getDescriptor(CCCD_UUID) ?: return
            if (Build.VERSION.SDK_INT >= 33) gatt.writeDescriptor(descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
            else writeLegacyDescriptor(gatt, descriptor)
        }
        @Suppress("DEPRECATION")
        @Deprecated("Deprecated in Java") override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            consume(characteristic.value)
        }
        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, value: ByteArray) { consume(value) }
    }

    private fun consume(bytes: ByteArray) { parser.parse(bytes.toString(Charsets.UTF_8), "esp32-ble").getOrNull()?.let(mutable::tryEmit) }
    @SuppressLint("MissingPermission")
    @Suppress("DEPRECATION")
    private fun writeLegacyDescriptor(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor) {
        if (!hasPermission()) return
        descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
        gatt.writeDescriptor(descriptor)
    }
    private fun hasPermission(): Boolean = Build.VERSION.SDK_INT < 31 || ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
}

private object BluetoothDeviceTransport { const val LE = 2 }
