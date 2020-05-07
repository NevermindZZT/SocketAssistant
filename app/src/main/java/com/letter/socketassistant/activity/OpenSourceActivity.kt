package com.letter.socketassistant.activity

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.letter.socketassistant.LetterApplication
import com.letter.socketassistant.R
import com.letter.socketassistant.adapter.BindingViewAdapter
import com.letter.socketassistant.model.local.OpenSourceDao
import com.letter.socketassistant.presenter.ItemPresenter
import com.letter.socketassistant.viewmodel.OpenSourceViewModel
import kotlinx.android.synthetic.main.activity_open_source.*

/**
 * 开源相关活动
 * @property model OpenSourceViewModel view model
 * @property adapter BindingViewAdapter<(com.letter.socketassistant.model.local.OpenSourceDao..com.letter.socketassistant.model.local.OpenSourceDao?)>
 *     列表适配器
 *
 * @author Letter(NevermindZZT@gmail.com)
 * @since 1.0.2
 */
class OpenSourceActivity : AppCompatActivity(), ItemPresenter<OpenSourceDao> {

    private val model by lazy {
        ViewModelProvider
            .AndroidViewModelFactory(LetterApplication.instance()).create(OpenSourceViewModel::class.java)
    }
    private val adapter by lazy {
        BindingViewAdapter(this,
            R.layout.layout_open_source_item,
            model.openSourceList,
            this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_open_source)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        /* 设置ActionBar */
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        /* 设置recycler view 适配器和布局管理器 */
        openSourceView.apply {
            adapter = this@OpenSourceActivity.adapter
            layoutManager = LinearLayoutManager(this@OpenSourceActivity)
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

    /**
     * 列表项点击处理
     * @param item OpenSourceDao item
     */
    override fun onItemClick(item: OpenSourceDao) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(item.url)
        startActivity(intent)
    }
}
