package com.letter.socketassistant.viewmodel

import android.Manifest
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.constant.PermissionConstants
import com.blankj.utilcode.util.PermissionUtils
import com.espressif.iot.esptouch.EsptouchTask
import com.espressif.iot.esptouch.IEsptouchListener
import com.letter.socketassistant.R
import com.letter.socketassistant.model.local.EspTouchParamDao
import com.letter.socketassistant.utils.getContext
import com.letter.socketassistant.utils.getString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.charset.Charset

class EspTouchViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "EspTouchViewModel"
    }

    val espTouchParamDao = MutableLiveData(EspTouchParamDao())
    val normalMessage = MutableLiveData("")
    val isConfiguring = MutableLiveData(false)
    private val receiver = EspTouchReceiver()
    private var espTouchTask : EsptouchTask ?= null

    private val onEepTouchResult = IEsptouchListener {

    }

    /**
     * 注册广播
     * @param context Context context
     */
    fun registerBroadcastReceiver(context: Context?) {
        val filter = IntentFilter()
        filter.apply {
            addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
        }
        context?.registerReceiver(receiver, filter)
        Log.d(TAG, "broadcast register")
    }

    /**
     * 注销广播
     * @param context Context context
     */
    fun unregisterBroadcastReceiver(context: Context?) {
        context?.unregisterReceiver(receiver)
        Log.d(TAG, "broadcast unregister")
    }

    fun checkPermission(func: (() -> Unit)?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
            && !PermissionUtils.isGranted(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            PermissionUtils
                .permission(PermissionConstants.LOCATION)
                .callback(object : PermissionUtils.SimpleCallback {
                    override fun onGranted() {
                        func?.invoke()
                    }

                    override fun onDenied() = Unit
                })
                .request()
        } else {
            func?.invoke()
        }
    }

    /**
     * 启动Esp Touch
     */
    fun startEspTouch() {
        checkPermission {
            launchEspTouch()
        }
    }

    private fun launchEspTouch() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                espTouchTask = EsptouchTask(
                    espTouchParamDao.value?.ssid?.toByteArray(Charset.defaultCharset()),
                    espTouchParamDao.value?.bssid?.toByteArray(Charset.defaultCharset()),
                    espTouchParamDao.value?.password?.toByteArray(Charset.defaultCharset()),
                    getContext()
                )
                espTouchTask?.setEsptouchListener(onEepTouchResult)
                val result = espTouchTask
                    ?.executeForResults(espTouchParamDao.value?.deviceCount?.toInt() ?: 0)
            }
        }
    }

    fun cancelEspTouch() {

    }

    /**
     * WiFi改变处理
     * @param wifiInfo WifiInfo WiFi信息
     */
    private fun onWifiChanged(wifiInfo: WifiInfo) {
        Log.d(TAG, "net: ${wifiInfo.networkId}, ssid: ${wifiInfo.ssid}")
        if (wifiInfo.networkId == -1) {
            espTouchParamDao.value?.ssid = ""
            espTouchParamDao.value?.bssid = ""
            normalMessage.value = null
        } else {

            espTouchParamDao.value?.ssid =
                if (wifiInfo.ssid.startsWith("\"") && wifiInfo.ssid.endsWith("\""))
                    wifiInfo.ssid.substring(1, wifiInfo.ssid.length - 1)
                else
                    wifiInfo.ssid
            espTouchParamDao.value?.bssid = wifiInfo.bssid
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (wifiInfo.frequency in 4900..5900) {
                    normalMessage.value = getString(R.string.esp_touch_activity_5G_wifi_not_supported)
                }
            }
            Log.d(TAG, "ssid: ${espTouchParamDao.value?.ssid}")
        }
    }

    inner class EspTouchReceiver: BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, "broadcast ${intent?.action}")
            when (intent?.action) {
                WifiManager.NETWORK_STATE_CHANGED_ACTION -> {
                    val wifiManager = context?.applicationContext
                        ?.getSystemService(Context.WIFI_SERVICE) as WifiManager
                    onWifiChanged(wifiManager.connectionInfo)
                }
            }
        }

    }
}