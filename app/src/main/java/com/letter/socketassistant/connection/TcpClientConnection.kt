package com.letter.socketassistant.connection

import android.util.Log
import com.letter.socketassistant.utils.tryRead
import java.net.Socket

/**
 * Tcp 客户端连接
 * @property socket Socket socket
 * @property maxPacketLen Int 单个数据包最大长度
 * @property packetTimeOut Long 数据包接收超时
 * @constructor 构造一个连接
 *
 * @author Letter(nevermindzzt@gmail.com)
 * @since 1.0.0
 */
class TcpClientConnection constructor(private var socket: Socket? = null,
                                      private val maxPacketLen: Int = 1024,
                                      private val packetTimeOut: Long = 100)
    : AbstractConnection() {

    companion object {
        private const val TAG = "TcpClientConnection"
    }

    constructor(remoteIp: String?,
                remotePort: Int,
                maxPacketLen: Int = 1024,
                packetTimeOut: Long = 100)
            : this(maxPacketLen=maxPacketLen, packetTimeOut=packetTimeOut) {
        try {
            socket = Socket(remoteIp, remotePort)
            name = "tcp client: ${socket?.inetAddress?.hostAddress}:${socket?.port}"
        } catch (e: Exception) {
            onDisConnectedListener?.invoke(this)
        }
    }

    init {
        name = "tcp client: ${socket?.inetAddress?.hostAddress}:${socket?.port}"
    }

    override fun send(connection: AbstractConnection, bytes: ByteArray?) {
        if (bytes == null) {
            return
        }
        val outputStream = socket?.getOutputStream() !!
        try {
            outputStream.write(bytes)
            outputStream.flush()
        } catch (e : Exception) {
            Log.e(TAG, "", e)
        }
    }

    override fun run() {
        Log.d(TAG, "tcp client run: ${socket?.port}")
        val inputStream = socket?.getInputStream()
        val data = ByteArray(maxPacketLen)
        while (!isInterrupted) {
            try {
                while (inputStream?.available() == 0) {
                    sleep(10)
                }
                val length = inputStream?.tryRead(data, maxPacketLen, packetTimeOut)
                if (length != null) {
                    onReceivedListener?.invoke(this, data.sliceArray(IntRange(0, length - 1)))
                }
            } catch (e: Exception) {
                Log.w(TAG, "", e)
                break
            }
        }
        onDisConnectedListener?.invoke(this)
    }

    override fun disconnect() {
        super.disconnect()
        socket?.getOutputStream()?.close()
        socket?.close()
        Log.d(TAG, "connection disconnect")
    }
}