package com.letter.socketassistant.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import java.io.InputStream
import java.io.Reader

/**
 * 扩展方法
 *
 * @author Letter(nevermindzzt@gmail.com)
 * @since 1.0.0
 */

private const val TAG = "Utils"

/**
 * Toast
 * @receiver Context
 * @param message String 消息
 * @param duration Int 提示时长
 */
fun Context.toast(message: String, duration: Int = Toast.LENGTH_SHORT)
        = Toast.makeText(this, message, duration).show()

/**
 * Toast
 * @receiver Context
 * @param resId Int 文本资源id
 * @param duration Int 提示时长
 */
fun Context.toast(@StringRes resId: Int, duration: Int = Toast.LENGTH_SHORT)
        = Toast.makeText(this, resources.getText(resId), duration).show()

/**
 * Toast
 * @receiver AndroidViewModel
 * @param message String 消息
 * @param duration Int 提示时长
 */
fun AndroidViewModel.toast(message: String, duration: Int = Toast.LENGTH_SHORT)
        = Toast.makeText(getApplication(), message, duration).show()

/**
 * Toast
 * @receiver AndroidViewModel
 * @param resId Int 文本资源id
 * @param duration Int 提示时长
 */
fun AndroidViewModel.toast(@StringRes resId: Int, duration: Int = Toast.LENGTH_SHORT)
        = Toast.makeText(getContext(), getContext().resources.getText(resId), duration).show()

/**
 * 获取Context
 * @receiver AndroidViewModel
 * @return Context Context
 */
fun AndroidViewModel.getContext(): Context
        = getApplication()

/**
 * 获取字符串
 * @receiver AndroidViewModel
 * @param resId Int 资源id
 * @return String 字符串
 */
fun AndroidViewModel.getString(@StringRes resId: Int)
        = getContext().getString(resId)

/**
 * 尝试从InputStream中读取一定大小的数据
 * @receiver Reader
 * @param byteArray ByteArray 数据
 * @param maxLength Int 最大数据长度
 * @param timeout Long 超时时间
 * @return Int 读取到的数据长度
 */
fun InputStream.tryRead(byteArray: ByteArray, maxLength: Int = byteArray.size, timeout: Long = 100): Int {
    val time = System.currentTimeMillis()
    while (available() < maxLength
        && System.currentTimeMillis() - time < timeout) {
        Thread.sleep(1)
    }
    val length = if (available() >= maxLength) maxLength else available()
    return read(byteArray, 0, length)
}

/**
 * ByteArray 转16进制字符串
 * @receiver ByteArray
 * @return String 16进制字符串
 */
fun ByteArray.toHexString(): String {
    return joinToString(" ") {
        "%02x".format(it.toInt() and 0xFF)
    }
}

/**
 * 16进制字符串转ByteArray
 * @receiver String
 * @return ByteArray 转换后数据
 */
fun String.toHexByteArray(delimiters: String = " "): ByteArray {
    val byteList = mutableListOf<Byte>()
    val words = split(delimiters)
    words.forEach {
        if (it.isNotEmpty()) {
            try {
                byteList.add(it.toInt(16).toByte())
            } catch (e: Exception) {
                Log.w(TAG, "", e)
            }
        }
    }
    return byteList.toByteArray()
}

/**
 * 启动活动
 * @receiver Activity
 * @param clazz Class<Activity> 活动
 */
fun <T: Activity> Activity.startActivity(clazz: Class<T>) {
    startActivity(Intent(this, clazz))
}
