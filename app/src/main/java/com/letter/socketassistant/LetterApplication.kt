package com.letter.socketassistant

import android.app.Application

/**
 * Application
 *
 * @author Letter(nevermindzt@gmail.com)
 * @since 1.0.0
 */
class LetterApplication : Application() {

    companion object {
        /**
         * Application 单例
         */
        private var instance: LetterApplication ?= null

        /**
         * 获取Application实例
         * @return ScheduleApplication Application实例
         */
        fun instance() = instance!!
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

}