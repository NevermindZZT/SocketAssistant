package com.letter.socketassistant.connection

import android.util.Log
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

/**
 * UDP 连接
 * @property remoteIp String 远端IP
 * @property remotePort Int 远端端口
 * @property maxPacketLen Int 接收最大包长
 * @property socket DatagramSocket socket连接实例
 * @constructor 构造一个UDP连接
 *
 * @author Letter(nevermindzzzt@gmail.com)
 * @since 1.0.0
 */
class UdpConnection constructor(private var remoteIp: String?,
                                private var remotePort: Int,
                                localPort: Int,
                                private val maxPacketLen: Int = 1024)
    : AbstractConnection() {

    companion object {
        private const val TAG = "UdpConnection"
    }

    private val socket = DatagramSocket(localPort)

    init {
        name = "udp: $remoteIp:$remotePort"
    }

    override fun send(connection: AbstractConnection, bytes: ByteArray?) {
        try {
            socket.send(DatagramPacket(
                bytes,
                bytes?.size ?: 0,
                InetAddress.getByName(remoteIp),
                remotePort
            ))
        } catch (e : Exception) {
            Log.e(TAG, "", e)
        }
    }

    override fun run() {
        val data = ByteArray(maxPacketLen)
        val packetReceived = DatagramPacket(data, data.size)
        while (!isInterrupted) {
            try {
                socket.receive(packetReceived)
                onReceivedListener?.invoke(this, data.sliceArray(IntRange(0, packetReceived.length - 1)))
            } catch (e : Exception) {
                Log.w(TAG, "", e)
                break
            }
        }
        onDisConnectedListener?.invoke(this)
    }

    override fun disconnect() {
        socket.close()
        super.disconnect()
    }
}