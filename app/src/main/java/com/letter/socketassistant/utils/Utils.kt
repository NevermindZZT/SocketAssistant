package com.letter.socketassistant.utils

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel

/**
 * 扩展方法
 *
 * @author Letter(nevermindzzt@gmail.com)
 * @since 1.0.0
 */

/**
 * Toast
 * @receiver Context
 * @param message String 消息
 * @param duration Int 提示时长
 */
fun Context.toast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

/**
 * Toast
 * @receiver Context
 * @param resId Int 文本资源id
 * @param duration Int 提示时长
 */
fun Context.toast(@StringRes resId: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, resources.getText(resId), duration).show()
}

/**
 * Toast
 * @receiver AndroidViewModel
 * @param message String 消息
 * @param duration Int 提示时长
 */
fun AndroidViewModel.toast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(getApplication(), message, duration).show()
}

/**
 * Toast
 * @receiver AndroidViewModel
 * @param resId Int 文本资源id
 * @param duration Int 提示时长
 */
fun AndroidViewModel.toast(@StringRes resId: Int, duration: Int = Toast.LENGTH_SHORT) {
    val context = getApplication<Application>()
    Toast.makeText(context, context.resources.getText(resId), duration).show()
}
