package com.letter.socketassistant.viewmodel

import android.app.Application
import android.util.Log
import androidx.databinding.ObservableArrayList
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.NetworkUtils
import com.letter.socketassistant.connection.*
import com.letter.socketassistant.model.local.ConnectionParamDao
import com.letter.socketassistant.model.local.MessageDao
import com.letter.socketassistant.utils.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.charset.Charset

/**
 * Main view model
 * @property title MutableLiveData<String> 界面标题
 * @property userName MutableLiveData<String> 用户名
 * @property localIp MutableLiveData<String> 本地Ip
 * @property inputText MutableLiveData<String> 输入内容
 * @property messageList ObservableList<MessageDao> 消息列表
 * @property connectionParamDao MutableLiveData<ConnectionDao> 连接配置参数
 * @property connectionList MutableLiveData<MutableList<AbstractConnection>> 连接列表
 * @property selectedConnectionIndex MutableLiveData<Int> 选中的连接索引
 *
 * @author Letter(nevermindzzt@gmail.com)
 * @since 1.0.0
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "MainViewModel"
    }

    val title = MutableLiveData("SocketAssistant")
    val userName = MutableLiveData("Letter")
    val localIp = MutableLiveData("")
    val inputText  = MutableLiveData("")
    val messageList = ObservableArrayList<MessageDao>()
    val connectionParamDao  = MutableLiveData(ConnectionParamDao())
    private val connectionList by lazy {
        MutableLiveData<MutableList<AbstractConnection>>(mutableListOf())
    }
    val selectedConnectionIndex = MutableLiveData(0)

    init {
        viewModelScope.launch {
            localIp.value = "Local IP: ${getLocalIp()}"
        }
        selectedConnectionIndex.observeForever {
            title.value =
                if (connectionList.value?.size ?: 0 > 0)
                    connectionList.value?.get(it)?.name
                else
                    "SocketAssistant"
        }
    }

    /**
     * 发送消息
     */
    fun sendMessage() {
        if (inputText.value != null && inputText.value?.isNotEmpty()!!) {
            val connection = if (connectionList.value?.size ?: 0 > 0)
                connectionList.value?.get(selectedConnectionIndex.value ?: 0) else null
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    connection?.send(
                        inputText.value?.toByteArray(Charset.defaultCharset())
                    )
                }
            }
            messageList.add(
                MessageDao(
                    MessageDao.Type.SEND,
                    inputText.value,
                    connection?.name
                )
            )
            inputText.value = null
        }
    }

    /**
     * 获取本地IP地址
     * @return (kotlin.String..kotlin.String?) Ip地址
     */
    private suspend fun getLocalIp() = withContext(Dispatchers.IO) {
        return@withContext when (NetworkUtils.getNetworkType()) {
                NetworkUtils.NetworkType.NETWORK_ETHERNET,
                NetworkUtils.NetworkType.NETWORK_2G,
                NetworkUtils.NetworkType.NETWORK_3G,
                NetworkUtils.NetworkType.NETWORK_4G -> NetworkUtils.getIPAddress(true)
                NetworkUtils.NetworkType.NETWORK_WIFI -> NetworkUtils.getIpAddressByWifi()
                else -> null
            }
    }

    /**
     * 建立连接
     */
    fun connect() {
        Log.d(TAG, "param: ${connectionParamDao.value?.serialConnectionParam}")
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                var connection: AbstractConnection ?= null
                when (connectionParamDao.value?.type) {
                    ConnectionParamDao.Type.TCP_SERVER -> {
                        connection = TcpServerConnection(
                            connectionParamDao.value?.netConnectionParam?.localPort?.toInt() ?: 0
                        )
                    }
                    ConnectionParamDao.Type.TCP_CLIENT -> {
                        connection = TcpClientConnection(
                            connectionParamDao.value?.netConnectionParam?.remoteIp,
                            connectionParamDao.value?.netConnectionParam?.remotePort?.toInt() ?: 0
                        )
                    }
                    ConnectionParamDao.Type.UDP -> {
                        connection = UdpConnection(
                            connectionParamDao.value?.netConnectionParam?.remoteIp,
                            connectionParamDao.value?.netConnectionParam?.remotePort?.toInt() ?: 0,
                            connectionParamDao.value?.netConnectionParam?.localPort?.toInt() ?: 0
                        )
                    }
                    ConnectionParamDao.Type.SERIAL -> {
                        connection = SerialConnection(
                            connectionParamDao.value?.serialConnectionParam?.port ?: "",
                            connectionParamDao.value?.serialConnectionParam?.baudRate?.toInt() ?: 0,
                            connectionParamDao.value?.serialConnectionParam?.dataBits?.toInt() ?: 0,
                            connectionParamDao.value?.serialConnectionParam?.parity ?: "",
                            connectionParamDao.value?.serialConnectionParam?.stopBits?.toInt() ?: 0
                        )
                    }
                }
                connection?.apply {
                    onReceivedListener = onConnectionPacketReceived
                    onConnectedListener = onConnectionConnected
                    onDisConnectedListener = onConnectionDisconnected
                    connect()
                }
            }
        }
    }

    /**
     * 断开连接
     * @param index Int 连接索引
     */
    fun disconnect(index: Int = selectedConnectionIndex.value ?: 0) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                if (index < connectionList.value?.size ?: 0) {
                    connectionList.value?.get(index)?.disconnect()
                }
            }
        }
    }

    /**
     * 获取连接名字列表
     * @return List<String> 列表
     */
    fun getConnectionNameList() = List (connectionList.value?.size ?: 0) {
        connectionList.value?.get(it)?.name ?: ""
    }

    /**
     * 连接成功回调
     */
    private val onConnectionConnected : (AbstractConnection) -> Unit = {
        viewModelScope.launch {
            withContext(Dispatchers.Main) {
                connectionList.value?.add(it)
                toast("${it.name} connected")
            }
        }
    }

    private val onConnectionDisconnected : (AbstractConnection) -> Unit = {
        viewModelScope.launch {
            withContext(Dispatchers.Main) {
                if (selectedConnectionIndex.value ?: 0 == connectionList.value?.size ?: 0 - 1) {
                    selectedConnectionIndex.value = selectedConnectionIndex.value ?: 0 - 1
                }
                toast("${it.name} disconnected")
                connectionList.value?.remove(it)
            }
        }
    }

    /**
     * 连接数据接收回调
     */
    private val onConnectionPacketReceived : (AbstractConnection, ByteArray) -> Unit = {
        abstractConnection, bytes ->
        viewModelScope.launch {
            withContext(Dispatchers.Main) {
                messageList.add(
                    MessageDao(
                        MessageDao.Type.RECEIVED,
                        bytes.toString(Charset.defaultCharset()),
                        abstractConnection.name
                    )
                )
            }
        }
    }
}