package com.letter.socketassistant.connection

/**
 * Connection 抽象类
 * @property onReceivedListener Function2<AbstractConnection, ByteArray, Unit>? 数据接收监听
 * @property onDisConnectedListener Function1<AbstractConnection, Unit>? 连接断开监听
 * @property onConnectedListener Function1<AbstractConnection, Unit>? 连接完成监听
 *
 * @author Letter(nevermindzzt@gmail.com)
 * @since 1.0.0
 */
abstract class AbstractConnection : Thread() {

    var onReceivedListener : ((AbstractConnection, ByteArray) -> Unit) ?= null

    var onDisConnectedListener : ((AbstractConnection) -> Unit) ?= null

    var onConnectedListener : ((AbstractConnection) -> Unit) ?= null

    /**
     * 数据发送
     * @param connection AbstractConnection 连接实例
     * @param bytes ByteArray 数据内容
     */
    abstract fun send(connection: AbstractConnection, bytes: ByteArray?)

    /**
     * 数据发送
     * @param bytes ByteArray 数据内容
     */
    open fun send(bytes: ByteArray?) {
        send(this, bytes)
    }

    /**
     * 建立连接
     */
    open fun connect() {
        start()
        onConnectedListener?.invoke(this)
    }

    /**
     * 断开连接
     */
    open fun disconnect() {
        interrupt()
        join()
    }

}