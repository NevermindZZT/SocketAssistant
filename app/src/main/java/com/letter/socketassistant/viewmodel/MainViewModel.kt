package com.letter.socketassistant.viewmodel

import android.app.Application
import android.util.Log
import androidx.databinding.ObservableArrayList
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.DeviceUtils
import com.blankj.utilcode.util.NetworkUtils
import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.letter.serialport.SerialPortFinder
import com.letter.socketassistant.R
import com.letter.socketassistant.connection.*
import com.letter.socketassistant.model.local.ConnectionParamDao
import com.letter.socketassistant.model.local.MessageDao
import com.letter.socketassistant.utils.getContext
import com.letter.socketassistant.utils.toHexByteArray
import com.letter.socketassistant.utils.toHexString
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
        val CHARSET_LIST = listOf("utf-8", "gbk", "unicode")
    }

    val title = MutableLiveData("SocketAssistant")
    val userName by lazy {
        MutableLiveData(DeviceUtils.getModel())
    }
    val localIp = MutableLiveData("")
    val inputText  = MutableLiveData("")
    val messageList = ObservableArrayList<MessageDao>()
    val connectionParamDao  = MutableLiveData(ConnectionParamDao())
    private val connectionList by lazy {
        MutableLiveData<MutableList<AbstractConnection>>(mutableListOf())
    }
    private lateinit var usbSerialDrivers: List<UsbSerialDriver>
    val serialPortList = mutableListOf<String>()
    val serialParityList = listOf("NONE", "ODD", "EVEN")
    val selectedConnectionIndex = MutableLiveData(-1)
    val hexTransmit = MutableLiveData(false)
    val hexReceive = MutableLiveData(false)
    val clearWhenSend = MutableLiveData(true)
    val charSet = MutableLiveData(CHARSET_LIST[0])

    init {
        viewModelScope.launch {
            localIp.value = "Local IP: ${getLocalIp()}"
        }
        selectedConnectionIndex.observeForever {
            title.value =
                if (it >= 0)
                    connectionList.value?.get(it)?.name
                else
                    "SocketAssistant"
        }
    }

    /**
     * 刷新串口列表
     */
    fun refreshSerialPort() {
        usbSerialDrivers = UsbSerialConnection.getDrivers(getContext())
        serialPortList.clear()
        serialPortList.addAll(SerialPortFinder().deviceNameList)
        for (device in usbSerialDrivers) {
            serialPortList.add(device.device.deviceName)
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
                        if (hexTransmit.value != true)
                            inputText.value?.toByteArray(Charset.forName(charSet.value))
                        else
                            inputText.value?.toHexByteArray()
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
            if (clearWhenSend.value == true) {
                inputText.value = null
            }
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
                        val portName = connectionParamDao.value?.serialConnectionParam?.port
                        if (portName != null) {
                            for (port in SerialPortFinder().deviceNameList) {
                                if (portName == port) {
                                    connection = SerialConnection(
                                        portName,
                                        connectionParamDao.value?.serialConnectionParam?.baudRate?.toInt() ?: 0,
                                        connectionParamDao.value?.serialConnectionParam?.dataBits?.toInt() ?: 0,
                                        parserParity(connectionParamDao.value?.serialConnectionParam?.parity),
                                        connectionParamDao.value?.serialConnectionParam?.stopBits?.toInt() ?: 0
                                    )
                                    break
                                }
                            }
                            for (driver in usbSerialDrivers) {
                                if (portName == driver.device.deviceName) {
                                    connection = UsbSerialConnection(
                                        getContext(),
                                        driver,
                                        connectionParamDao.value?.serialConnectionParam?.baudRate?.toInt() ?: 0,
                                        connectionParamDao.value?.serialConnectionParam?.dataBits?.toInt() ?: 0,
                                        parserParity(connectionParamDao.value?.serialConnectionParam?.parity),
                                        connectionParamDao.value?.serialConnectionParam?.stopBits?.toInt() ?: 0
                                    )
                                }
                                break
                            }
                        }
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
        if (index < 0) {
            toast(R.string.main_activity_toast_no_connection)
            return
        }
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
                if (selectedConnectionIndex.value == -1) {
                    selectedConnectionIndex.value = 0;
                }
            }
        }
    }

    /**
     * 连接断开回调
     */
    private val onConnectionDisconnected : (AbstractConnection) -> Unit = {
        viewModelScope.launch {
            withContext(Dispatchers.Main) {
                if (selectedConnectionIndex.value ?: -1 == (connectionList.value?.size ?: 0) - 1) {
                    selectedConnectionIndex.value = (selectedConnectionIndex.value ?: 0) - 1
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
                        if (hexReceive.value != true)
                            bytes.toString(Charset.forName(charSet.value))
                        else
                            bytes.toHexString(),
                        abstractConnection.name
                    )
                )
            }
        }
    }
}

/**
 * 解析串口校验参数
 * @param parity String 参数
 * @return Int 数值
 */
private fun parserParity(parity: String?) = when (parity) {
    "ODD" -> 1
    "EVEN" -> 2
    else -> 0
}