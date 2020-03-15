package com.letter.socketassistant.model.local

/**
 * Esp Touch参数数据
 * @property ssid String ssid
 * @property bssid String bssid
 * @property password String 密码
 * @property deviceCount String 设备数量
 * @property isBroadCast Boolean 是否广播
 * @constructor 构造一个Esp Touch 参数
 *
 * @author Letter(nevermindzzt@gamil.com)
 * @since 1.0.0
 */
data class EspTouchParamDao(var ssid: String ?= null,
                            var bssid: String ?= null,
                            var password: String ?= null,
                            var deviceCount: String ?= "1",
                            var isBroadCast: Boolean = true)