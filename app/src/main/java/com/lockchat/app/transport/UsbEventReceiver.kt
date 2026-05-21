package com.lockchat.app.transport

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbManager
import android.util.Log
import com.lockchat.app.data.transport.TransportManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Receptor de eventos USB — notifica al TransportManager cuando se conecta
 * o desconecta un dispositivo USB serial (ESP32 con módulo LoRa).
 */
@AndroidEntryPoint
class UsbEventReceiver : BroadcastReceiver() {

    @Inject
    lateinit var transportManager: TransportManager

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                Log.i("UsbEventReceiver", "USB conectado — verificando LoRa")
                transportManager.onUsbDeviceAttached()
            }
            UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                Log.i("UsbEventReceiver", "USB desconectado")
                transportManager.onUsbDeviceDetached()
            }
        }
    }
}
