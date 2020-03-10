package com.letter.socketassistant.connection

/**
 * Connection 抽象类
 * @property onReceivedListener Function2<AbstractConnection, ByteArray, Unit>? 数据接收监听
 *
 * @author Letter(nevermindzzt@gmail.com)
 * @since 1.0.0
 */
abstract class AbstractConnection : Thread() {

    var onReceivedListener : ((AbstractConnection, ByteArray) -> Unit) ?= null

    /**
     * 数据发送
     * @param connection AbstractConnection 连接实例
     * @param bytes ByteArray 数据内容
     */
    abstract fun send(connection: AbstractConnection, bytes: ByteArray)
}