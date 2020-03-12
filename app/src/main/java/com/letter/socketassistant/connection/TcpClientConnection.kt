package com.letter.socketassistant.connection

import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
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
class TcpClientConnection constructor(private val socket: Socket,
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
            : this(Socket(remoteIp, remotePort), maxPacketLen, packetTimeOut)

    init {
        name = "tcp client: ${socket.port}"
    }

    override fun send(connection: AbstractConnection, bytes: ByteArray?) {
        if (bytes == null) {
            return
        }
        val outputStream = socket.getOutputStream()
        try {
            outputStream.write(bytes)
            outputStream.flush()
        } catch (e : Exception) {
            Log.e(TAG, "", e)
        }
    }

    override fun run() {
        Log.d(TAG, "tcp client run: ${socket.port}")
        val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
        val data = CharArray(maxPacketLen)
        while (!isInterrupted) {
            try {
                if (reader.ready()) {
                    val length = reader.read(data)
                    if (length > 0) {
                        val bytes = ByteArray(
                            length
                        ) {
                            data[it].toByte()
                        }
                        onReceivedListener?.invoke(this, bytes)
                    }
                }
                sleep(packetTimeOut)
            } catch (e: Exception) {
                Log.w(TAG, "", e)
                break
            }
        }
        onDisConnectedListener?.invoke(this)
    }

    override fun disconnect() {
        super.disconnect()
        socket.getOutputStream()?.close()
        socket.close()
        Log.d(TAG, "connection disconnect")
    }
}