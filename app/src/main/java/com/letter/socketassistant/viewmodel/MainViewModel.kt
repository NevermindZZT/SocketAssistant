package com.letter.socketassistant.viewmodel

import androidx.databinding.ObservableArrayList
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.NetworkUtils
import com.letter.socketassistant.connection.AbstractConnection
import com.letter.socketassistant.connection.TcpClientConnection
import com.letter.socketassistant.connection.TcpServerConnection
import com.letter.socketassistant.connection.UdpConnection
import com.letter.socketassistant.model.local.ConnectionParamDao
import com.letter.socketassistant.model.local.MessageDao
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
class MainViewModel : ViewModel() {

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
    private val selectedConnectionIndex = MutableLiveData(0)

    init {
        viewModelScope.launch {
            localIp.value = "Local IP: ${getLocalIp()}"
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

                    }
                }
                connection?.apply {
                    onReceivedListener = onConnectionPacketReceived
                    onConnectedListener = onConnectionConnected
                    connect()
                }
            }
        }
    }

    /**
     * 连接成功回调
     */
    private val onConnectionConnected : (AbstractConnection) -> Unit = {
        connectionList.value?.add(it)
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