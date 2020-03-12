package com.letter.socketassistant.connection

import android.util.Log
import com.letter.serialport.SerialPort

/**
 * 串口连接
 * @property path String 串口路径
 * @property baudRate Int 波特率
 * @property dataBits Int 数据位
 * @property parity String 校验
 * @property stopBits Int 停止位
 * @property maxPacketLen Int 接收最大包
 * @property packetTimeOut Long 超时
 * @property serialPort SerialPort 串口
 * @constructor 构造一个连接
 *
 * @author Letter(nevermindzt@gmail.com)
 * @since 1.0.0
 */
class SerialConnection constructor(private val path: String,
                                   private val baudRate: Int = 115200,
                                   private val dataBits: Int = 8,
                                   private val parity: String = "N",
                                   private val stopBits: Int = 1,
                                   private val maxPacketLen: Int = 1024,
                                   private val packetTimeOut: Long = 100)
    : AbstractConnection() {

    companion object {
        private const val TAG = "SerialConnection"
    }

    private val serialPort = SerialPort()

    init {
        name = "serial: $path"
    }

    override fun send(connection: AbstractConnection, bytes: ByteArray?) {
        if (bytes != null) {
            serialPort.outputStream?.write(bytes)
        }
    }

    override fun run() {
        if (serialPort.open(path,
                baudRate,
                dataBits = dataBits,
                parity = parity,
                stopBits = stopBits)) {
            val inputStream = serialPort.inputStream
            val data = ByteArray(maxPacketLen)
            while (!isInterrupted) {
                try {
                    val length = inputStream?.read(data) ?: 0
                    if (length > 0) {
                        onReceivedListener?.invoke(this, data.sliceArray(IntRange(0, length - 1)))
                    }
                    sleep(packetTimeOut)
                } catch (e: Exception) {
                    Log.w(TAG, "", e)
                    break
                }
            }
        }
        onDisConnectedListener?.invoke(this)
    }

    override fun disconnect() {
        super.disconnect()
        serialPort.close()
    }
}