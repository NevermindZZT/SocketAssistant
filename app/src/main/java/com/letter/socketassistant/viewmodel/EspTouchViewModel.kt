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
import com.espressif.iot.esptouch.IEsptouchResult
import com.letter.socketassistant.R
import com.letter.socketassistant.model.local.EspTouchParamDao
import com.letter.socketassistant.utils.getContext
import com.letter.socketassistant.utils.getString
import com.letter.socketassistant.utils.toHexByteArray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.charset.Charset

/**
 * Esp Touch view model
 * @property espTouchParamDao MutableLiveData<(com.letter.socketassistant.model.local.EspTouchParamDao..com.letter.socketassistant.model.local.EspTouchParamDao?)>
 *     Esp Touch 参数
 * @property normalMessage MutableLiveData<(kotlin.String..kotlin.String?)> 界面提示
 * @property isConfiguring MutableLiveData<(kotlin.Boolean..kotlin.Boolean?)> Esp Touch配置中标志
 * @property configMessage MutableLiveData<(kotlin.String..kotlin.String?)> Esp Touch消息
 * @property receiver EspTouchReceiver 广播接收器
 * @property espTouchTask EsptouchTask? Esp Touch任务
 * @constructor 构造一个ViewModel
 *
 * @author Letter(nevermindzzt@gmail.com)
 * @since 1.0.0
 */
class EspTouchViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "EspTouchViewModel"
    }

    val espTouchParamDao = MutableLiveData(EspTouchParamDao())
    val normalMessage = MutableLiveData("")
    val isConfiguring = MutableLiveData(false)
    val configMessage = MutableLiveData("")
    private val receiver = EspTouchReceiver()
    private var espTouchTask : EsptouchTask ?= null

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

    /**
     * 检查权限
     * @param func Function0<Unit>? 获取权限后执行的动作
     */
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

    /**
     * 启动 Esp Touch
     */
    private fun launchEspTouch() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                espTouchTask = EsptouchTask(
                    espTouchParamDao.value?.ssid?.toByteArray(Charset.defaultCharset()),
                    espTouchParamDao.value?.bssid?.toHexByteArray(":"),
                    espTouchParamDao.value?.password?.toByteArray(Charset.defaultCharset()),
                    getContext()
                )
                withContext(Dispatchers.Main) {
                    isConfiguring.value = true
                }
//                espTouchTask?.setEsptouchListener(onEspTouchResult)
                val result = espTouchTask
                    ?.executeForResults(espTouchParamDao.value?.deviceCount?.toInt() ?: 0)
                withContext(Dispatchers.Main) {
                    isConfiguring.value = false
                    onEspTouchResult(result)
                }
            }
        }
    }

    /**
     * 取消 Esp Touch
     */
    fun cancelEspTouch() {
        espTouchTask?.interrupt()
    }

    /**
     * Esp 配置结果
     * @param result List<IEsptouchResult>?
     */
    private fun onEspTouchResult(result: List<IEsptouchResult>?) {
        if (result == null) {
            configMessage.value = getString(R.string.esp_touch_activity_dialog_start_fail)
        } else {
            if (result[0].isCancelled) {
                return
            }
            if (result[0].isSuc) {
                configMessage.value = getString(R.string.esp_touch_activity_dialog_config_fail)
                return
            }
            configMessage.value = result.joinToString {
                getString(R.string.esp_touch_activity_dialog_success_item)
                    .format(it.bssid, it.inetAddress?.hostAddress)
            }
        }
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
        espTouchParamDao.postValue(espTouchParamDao.value)
    }

    /**
     * 广播接收器
     */
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