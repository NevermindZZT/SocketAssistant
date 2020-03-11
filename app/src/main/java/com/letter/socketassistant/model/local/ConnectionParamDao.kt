package com.letter.socketassistant.model.local

/**
 * 连接配置参数
 * @property type Type 连接类型
 * @property netConnectionParam NetConnectionParam 网络连接参数
 * @constructor 构造一个参数数据
 *
 * @author Letter(nevermindzzt@gmail.com)
 * @since 1.0.0
 */
data class ConnectionParamDao constructor(var type: Type = Type.TCP_SERVER) {

    var netConnectionParam = NetConnectionParam()

    /**
     * 连接类型
     */
    enum class Type {
        TCP_SERVER,
        TCP_CLIENT,
        UDP,
        SERIAL
    }

    /**
     * 网络连接参数
     * @property remoteIp String? 远端IP
     * @property remotePort String? 远端端口
     * @property localPort String? 本地端口
     * @constructor 构造一个网络连接参数数据
     *
     * @author Letter(nevermindzzt@gmail.com)
     * @since 1.0.0
     */
    data class NetConnectionParam constructor(var remoteIp: String ?= null,
                                         var remotePort: String ?= null,
                                         var localPort: String ?= null)
}