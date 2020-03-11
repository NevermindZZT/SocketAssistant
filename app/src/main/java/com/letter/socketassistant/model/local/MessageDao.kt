package com.letter.socketassistant.model.local

/**
 * 消息实例
 * @property type Type 消息类型
 * @property msg String? 消息内容
 * @constructor 构造一个消息实例
 *
 * @author Letter(nevermindzzt@gmail.com)
 * @since 1.0.0
 */
data class MessageDao constructor(var type: Type,
                                  var msg: String?,
                                  var name: String ?= null,
                                  var time: String ?= null) {

    enum class Type {
        SEND,
        RECEIVED
    }
}