package com.letter.socketassistant.activity

import android.os.Bundle
import android.view.MenuItem
import com.letter.socketassistant.R
import kotlinx.android.synthetic.main.activity_setting.*

/**
 * 设置活动
 *
 * @author Letter(zhagnkeqaing@gmail.com)
 * @since 1.0.4
 */
class SettingActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    /**
     * 菜单选项选择处理
     * @param item 被选中的选项
     * @return Boolean 动作是否被处理
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return true
    }
}
