package com.letter.socketassistant.viewmodel

import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.NetworkUtils
import com.letter.socketassistant.model.local.MessageDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Main view model
 * @property title MutableLiveData<String> 界面标题
 * @property userName MutableLiveData<String> 用户名
 * @property localIp MutableLiveData<String> 本地Ip
 * @property inputText MutableLiveData<String> 输入内容
 * @property messageList ObservableList<MessageDao> 消息列表
 *
 * @author Letter(nevermindzzt@gmail.com)
 * @since 1.0.0
 */
class MainViewModel : ViewModel() {
    val title : MutableLiveData<String> = MutableLiveData("SocketAssistant")
    val userName : MutableLiveData<String> = MutableLiveData("Letter")
    val localIp : MutableLiveData<String> = MutableLiveData("")
    val inputText : MutableLiveData<String> = MutableLiveData("")
    val messageList : ObservableList<MessageDao> = ObservableArrayList()

    init {
//        viewModelScope.launch {
//            localIp.value = getLocalIp()
//        }
    }

    /**
     * 发送消息
     */
    fun sendMessage() {
        if (inputText.value != null && inputText.value?.isNotEmpty()!!) {
            messageList.add(MessageDao(MessageDao.Type.SEND, inputText.value))
            inputText.value = null
        }
    }

    /**
     * 获取本地IP地址
     * @return (kotlin.String..kotlin.String?) Ip地址
     */
    private suspend fun getLocalIp() = withContext(Dispatchers.IO) {
        return@withContext NetworkUtils.getIPAddress(true)
    }
}