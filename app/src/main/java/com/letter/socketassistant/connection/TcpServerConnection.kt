package com.letter.socketassistant.connection

import android.util.Log
import java.net.ServerSocket

/**
 * Tcp 服务连接
 * @property localPort Int 本地端口
 * @property socket ServerSocket socket
 * @property clientList MutableList<TcpClientConnection> 客户列表
 * @constructor 构建一个TCP服务连接
 *
 * @author Letter(nevermindzzt@gmail.com)
 * @since 1.0.0
 */
class TcpServerConnection constructor(private var localPort: Int,
                                      private val maxPacketLen: Int = 1024)
    : AbstractConnection() {

    companion object {
        private const val TAG = "TcpServerConnection"
    }

    private val socket = ServerSocket(localPort)
    private val clientList = mutableListOf<TcpClientConnection>()

    init {
        name = "tcp server: $localPort"
    }

    override fun send(connection: AbstractConnection, bytes: ByteArray?) {
        if (connection != this) {
            connection.send(bytes)
        }
    }

    override fun run() {
        while (!isInterrupted) {
            try {
                val client = socket.accept()
                val connection = TcpClientConnection(client, maxPacketLen = maxPacketLen)
                connection.apply {
                    onReceivedListener = this@TcpServerConnection.onReceivedListener
                    onConnectedListener = this@TcpServerConnection.onConnectedListener
                    onDisConnectedListener = this@TcpServerConnection.onDisConnectedListener
                    connect()
                }
                clientList.add(connection)
            } catch (e: Exception) {
                Log.w(TAG, "", e)
                break
            }
        }
        onDisConnectedListener?.invoke(this)
    }

    override fun disconnect() {
        for (client in clientList) {
            client.disconnect()
        }
        super.disconnect()
        socket.close()
    }
}