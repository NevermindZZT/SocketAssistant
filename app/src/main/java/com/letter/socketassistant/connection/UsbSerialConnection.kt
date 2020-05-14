package com.letter.socketassistant.connection

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbManager
import android.util.Log
import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber

/**
 * USB转串口连接
 * @property context Context context
 * @property driver UsbSerialDriver USB串口driver
 * @property baudRate Int 波特率
 * @property dataBits Int 数据位
 * @property parity Int 校验
 * @property stopBits Int 停止位
 * @property maxPacketLen Int 最大包长度
 * @property packetTimeOut Long 包超时
 * @property port UsbSerialPort? USB串口端口
 * @constructor 构造一个USB串口连接
 *
 * @author Letter(NevermindZZT@gmail.com)
 * @since 1.0.1
 */
class UsbSerialConnection constructor(private val context: Context,
                                      private val driver: UsbSerialDriver,
                                      private val baudRate: Int = 115200,
                                      private val dataBits: Int = UsbSerialPort.DATABITS_8,
                                      private val parity: Int = UsbSerialPort.PARITY_NONE,
                                      private val stopBits: Int = UsbSerialPort.STOPBITS_1,
                                      private val maxPacketLen: Int = 1024,
                                      private val packetTimeOut: Long = 100)
    : AbstractConnection() {

    companion object {
        private const val TAG = "UsbSerialConnection"

        private const val ACTION_USB_PERMISSION = "com.letter.socketassistant.connection.USB_PERMISSION"

        /**
         * 获取所有USB 串口设备
         * @param context Context context
         * @return List<UsbSerialDriver> 设备列表
         */
        fun getDrivers(context: Context): List<UsbSerialDriver> {
            val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
            return UsbSerialProber.getDefaultProber().findAllDrivers(usbManager)
        }
    }

    private var port: UsbSerialPort ?= null

    private val usbPermissionReceiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (ACTION_USB_PERMISSION == intent?.action) {
                context?.unregisterReceiver(this)
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    this@UsbSerialConnection.start()
                    this@UsbSerialConnection.onConnectedListener?.invoke(this@UsbSerialConnection)
                } else {
                    this@UsbSerialConnection.onDisConnectedListener?.invoke(this@UsbSerialConnection)
                }
            }
        }

    }

    init {
        name = "usb serial: ${driver.device.productName}(${driver.device.deviceName})"
    }

    override fun send(connection: AbstractConnection, bytes: ByteArray?) {
        port?.write(bytes, packetTimeOut.toInt())
    }

    override fun run() {
        val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        if (!usbManager.hasPermission(driver.device)) {
            onDisConnectedListener?.invoke(this)
            return
        }
        val connection = usbManager.openDevice(driver.device)
        if (connection == null) {
            onDisConnectedListener?.invoke(this)
            return
        }
        port = driver.ports[0]
        port?.open(connection)
        port?.setParameters(baudRate, dataBits, stopBits, parity)
        val data = ByteArray(maxPacketLen)
        val buffer = ByteArray(maxPacketLen)
        var time: Long
        while (!isInterrupted) {
            try {
                var length = 0
                time = System.currentTimeMillis()
                while (System.currentTimeMillis() - time < packetTimeOut) {
                    val len = port?.read(data, packetTimeOut.toInt()) ?: 0
                    if (len > 0) {
                        for (i in 0 until len) {
                            buffer[length++] = data[i]
                            if (length >= maxPacketLen) {
                                onReceivedListener?.invoke(this, buffer.sliceArray(IntRange(0, length - 1)))
                                length = 0
                            }
                        }
                        time = System.currentTimeMillis()
                    }
                }
                if (length > 0) {
                    onReceivedListener?.invoke(this, buffer.sliceArray(IntRange(0, length - 1)))
                }
            } catch (e: Exception) {
                Log.w(TAG, "", e)
                break
            }
        }
        onDisConnectedListener?.invoke(this)
    }

    override fun connect() {
        val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        if (!usbManager.hasPermission(driver.device)) {
            val intentFilter = IntentFilter(ACTION_USB_PERMISSION)
            context.registerReceiver(usbPermissionReceiver, intentFilter)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                Intent(ACTION_USB_PERMISSION),
                0)
            usbManager.requestPermission(driver.device, pendingIntent)
        } else {
            super.connect()
        }
    }

    override fun disconnect() {
        super.disconnect()
        port?.close()
    }
}