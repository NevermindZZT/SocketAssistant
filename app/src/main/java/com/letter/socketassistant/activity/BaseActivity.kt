package com.letter.socketassistant.activity

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.letter.socketassistant.utils.isDarkTheme

/**
 * 基础活动
 *
 * @author Letter(nevermindzzt@gmail.com)
 * @since 1.0.5
 */
open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!isDarkTheme()) {
            /* Android O以上支持，设置浅色状态栏 */
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
    }
}
