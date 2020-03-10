package com.letter.socketassistant.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.letter.socketassistant.R
import com.letter.socketassistant.adapter.BindingViewAdapter
import com.letter.socketassistant.presenter.Presenter
import com.letter.socketassistant.databinding.ActivityMainBinding
import com.letter.socketassistant.databinding.LayoutMainNavHeaderBinding
import com.letter.socketassistant.model.local.MessageDao
import com.letter.socketassistant.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_main_nav_header.view.*

/**
 * 主界面Activity
 * @property binding ActivityMainBinding DataBinding实例
 * @property model MainViewModel View Model
 * @property adapter BindingViewAdapter<MessageDao> 消息列表适配器
 *
 * @author Letter(nevermindzzt@gmail.com)
 * @since 1.0.0
 */
class MainActivity : AppCompatActivity(), Presenter {

    private lateinit var binding : ActivityMainBinding
    private val model by lazy {
        ViewModelProvider(this).get(MainViewModel::class.java)
    }
    private val adapter by lazy {
        BindingViewAdapter(this, R.layout.layout_main_message_item, model.messageList)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setSupportActionBar(toolbar)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.let {
            it.lifecycleOwner = this@MainActivity
            it.vm = model
            it.presenter = this
        }
        val navHeaderBinding = DataBindingUtil.inflate<LayoutMainNavHeaderBinding>(
            layoutInflater,
            R.layout.layout_main_nav_header,
            null,
            false
        )
        navHeaderBinding.let {
            it.lifecycleOwner = this@MainActivity
            it.vm = model
        }
        navigationView?.addHeaderView(navHeaderBinding.root)
        model.apply {
        }

        recyclerView.apply {
            adapter = this@MainActivity.adapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.addExtraButton -> {

            }
            R.id.sendButton -> {
                model.sendMessage()
            }
        }
    }
}
