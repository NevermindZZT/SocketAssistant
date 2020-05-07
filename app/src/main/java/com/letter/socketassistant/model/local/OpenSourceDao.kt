package com.letter.socketassistant.model.local

/**
 * 开源项目数据
 * @property name String 项目名
 * @property author String 作者
 * @property url String 地址
 * @property desc String? 描述
 * @constructor 构造一个开源项目数据
 *
 * @author Letter(NevermindZZT@gmail.com)
 * @since 1.0.2
 */
data class OpenSourceDao constructor(var name: String,
                                     var author: String,
                                     var url: String,
                                     var desc: String? = null)
