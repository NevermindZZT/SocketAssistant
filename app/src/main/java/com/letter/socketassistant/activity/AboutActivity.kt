package com.letter.socketassistant.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import com.letter.socketassistant.BuildConfig
import com.letter.socketassistant.R
import com.letter.socketassistant.utils.startActivity
import kotlinx.android.synthetic.main.activity_about.*

/**
 * 关于活动
 *
 * @author Letter(nevermindzzt@gmail.com)
 * @since 1.0.0
 */
class AboutActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        versionText.text = BuildConfig.VERSION_NAME

        githubText.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(getString(R.string.about_activity_open_source_address))
            startActivity(intent)
        }
        openSourceText.setOnClickListener {
            startActivity(OpenSourceActivity::class.java)
        }
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
