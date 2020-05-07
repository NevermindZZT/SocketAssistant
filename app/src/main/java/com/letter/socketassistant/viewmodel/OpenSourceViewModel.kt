package com.letter.socketassistant.viewmodel

import android.app.Application
import androidx.databinding.ObservableArrayList
import androidx.lifecycle.AndroidViewModel
import com.letter.socketassistant.model.local.OpenSourceDao

/**
 * 开源相关View Model
 * @property openSourceList ObservableArrayList<OpenSourceDao> 开源项目列表
 * @constructor 构造一个View Model
 *
 * @author Letter(NevermindZZT@gmail.com)
 * @since 1.0.2
 */
class OpenSourceViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "OpenSourceViewModel"
    }

    val openSourceList = ObservableArrayList<OpenSourceDao>()

    init {
        initData()
    }

    private fun initData() {
        openSourceList.add(OpenSourceDao("AndroidUtilCode",
            "Blankj",
            "https://github.com/Blankj/AndroidUtilCode",
            "AndroidUtilCode is a powerful & easy to use library for Android."))
        openSourceList.add(OpenSourceDao("material-dialogs",
            "afollestad",
            "https://github.com/afollestad/material-dialogs",
            "A beautiful, fluid, and extensible dialogs API for Kotlin & Android."))
        openSourceList.add(OpenSourceDao("usb-serial-for-android",
            "mik3y",
            "https://github.com/mik3y/usb-serial-for-android",
            "Android USB host serial driver library for CDC, FTDI, Arduino and other devices."))
    }
}