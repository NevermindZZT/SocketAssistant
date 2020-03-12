package com.letter.socketassistant.activity

import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableList
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.google.android.material.navigation.NavigationView
import com.letter.socketassistant.LetterApplication
import com.letter.socketassistant.R
import com.letter.socketassistant.adapter.BindingViewAdapter
import com.letter.socketassistant.presenter.Presenter
import com.letter.socketassistant.databinding.ActivityMainBinding
import com.letter.socketassistant.databinding.LayoutMainNavHeaderBinding
import com.letter.socketassistant.databinding.LayoutMainNetConnectionParamBinding
import com.letter.socketassistant.databinding.LayoutMainSerialConnectionParamBinding
import com.letter.socketassistant.model.local.ConnectionParamDao
import com.letter.socketassistant.model.local.MessageDao
import com.letter.socketassistant.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*

/**
 * 主界面Activity
 * @property binding ActivityMainBinding DataBinding实例
 * @property model MainViewModel View Model
 * @property adapter BindingViewAdapter<MessageDao> 消息列表适配器
 * @property netParamBinding LayoutMainNetConnectionParamBinding 网络配置参数DataBinding
 *
 * @author Letter(nevermindzzt@gmail.com)
 * @since 1.0.0
 */
class MainActivity : AppCompatActivity(), Presenter,
    NavigationView.OnNavigationItemSelectedListener,
    View.OnFocusChangeListener {

    private lateinit var binding : ActivityMainBinding
    private val model by lazy {
        ViewModelProvider
            .AndroidViewModelFactory(LetterApplication.instance()).create(MainViewModel::class.java)
    }
    private val adapter by lazy {
        BindingViewAdapter(this, R.layout.layout_main_message_item, model.messageList)
    }
    private lateinit var netParamBinding : LayoutMainNetConnectionParamBinding
    private lateinit var serialParamBinding : LayoutMainSerialConnectionParamBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        /* 绑定视图 */
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        /* 设置ActionBar */
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_main_menu)

        /* 设置数据 */
        binding.let {
            it.lifecycleOwner = this@MainActivity
            it.vm = model
            it.presenter = this
        }

        /* navigation view header layout 数据绑定 */
        DataBindingUtil.bind<LayoutMainNavHeaderBinding>(navigationView.getHeaderView(0)).let {
            it?.lifecycleOwner = this@MainActivity
            it?.vm = model
        }

        /* navigation view 菜单点击监听设置 */
        navigationView.setNavigationItemSelectedListener(this)

        /* 设置编辑框焦点改变监听 */
        editText.onFocusChangeListener = this

        /* 网路配置参数View 数据绑定 */
        netParamBinding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.layout_main_net_connection_param,
            null,
            false
        )

        /* 串口配置参数View 数据绑定 */
        serialParamBinding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.layout_main_serial_connection_param,
            null,
            false
        )

        /* ViewModel */
        model.apply {
            messageList.addOnListChangedCallback(
                object : ObservableList.OnListChangedCallback<ObservableList<MessageDao>>() {
                    override fun onChanged(sender: ObservableList<MessageDao>?) = Unit

                    override fun onItemRangeRemoved(sender: ObservableList<MessageDao>?,
                        positionStart: Int, itemCount: Int) = Unit

                    override fun onItemRangeMoved(sender: ObservableList<MessageDao>?,
                        fromPosition: Int, toPosition: Int, itemCount: Int) = Unit

                    override fun onItemRangeInserted(sender: ObservableList<MessageDao>?,
                        positionStart: Int, itemCount: Int
                    ) {
                        recyclerView.scrollToPosition(messageList.size - 1)
                    }

                    override fun onItemRangeChanged(sender: ObservableList<MessageDao>?,
                        positionStart: Int, itemCount: Int) = Unit
                })
        }

        /* 设置recycler view 适配器和布局管理器 */
        recyclerView.apply {
            adapter = this@MainActivity.adapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
    }

    /**
     * View 点击处理
     * @param view View? view
     */
    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.addExtraButton -> {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE)
                if (imm is InputMethodManager) {
                    imm.hideSoftInputFromWindow(window.decorView.windowToken, 0)
                }
                extraLayout.visibility = if (extraLayout.visibility == View.VISIBLE) View.GONE
                    else View.VISIBLE
            }
            R.id.sendButton -> model.sendMessage()
            R.id.atButton -> showConnectionListDialog()
            R.id.disconnectButton -> model.disconnect()
        }
    }

    /**
     * 菜单选项点击处理
     * @param item MenuItem 菜单选项
     * @return Boolean 事件是否被处理
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> drawerLayout.openDrawer(GravityCompat.START)
        }
        return true
    }

    /**
     * navigation view 菜单点击处理
     * @param item MenuItem 菜单选项
     * @return Boolean 事件是否被处理
     */
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.main_nav_tcp_server ->
                showParamDialog(ConnectionParamDao.Type.TCP_SERVER, R.string.main_activity_nav_tcp_server)
            R.id.main_nav_tcp_client ->
                showParamDialog(ConnectionParamDao.Type.TCP_CLIENT, R.string.main_activity_nav_tcp_server)
            R.id.main_nav_udp ->
                showParamDialog(ConnectionParamDao.Type.UDP, R.string.main_activity_nav_udp)
            R.id.main_nav_serial ->
                showParamDialog(ConnectionParamDao.Type.SERIAL, R.string.main_activity_nav_serial)
        }
        drawerLayout.closeDrawers()
        return true
    }

    /**
     * 焦点改变监听
     * @param view View view
     * @param hasFocus Boolean 是否得到焦点
     */
    override fun onFocusChange(view: View?, hasFocus: Boolean) {
        when (view?.id) {
            R.id.editText -> {
                if (hasFocus && extraLayout.visibility == View.VISIBLE) {
                    extraLayout.visibility = View.GONE
                }
            }
        }
    }

    /**
     * 显示连接参数对话框
     * @param type Type 连接类型
     * @param titleRes Int 标题资源 id
     */
    private fun showParamDialog(type: ConnectionParamDao.Type, titleRes: Int) {
        model.connectionParamDao.value?.type = type
        netParamBinding.vm = model
        serialParamBinding.vm = model
        MaterialDialog(this).show {
            title(titleRes)
            customView(view = if (type != ConnectionParamDao.Type.SERIAL)
                netParamBinding.root else serialParamBinding.root)
            positiveButton(R.string.main_activity_dialog_confirm) {
                it.dismiss()
                model.connect()
            }
        }
    }

    /**
     * 显示连接列表
     */
    private fun showConnectionListDialog() {
        MaterialDialog(this).show {
            listItemsSingleChoice(
                items = model.getConnectionNameList(),
                initialSelection = model.selectedConnectionIndex.value ?: 0) {
                dialog, index, _ ->
                model.selectedConnectionIndex.value = index
                dialog.dismiss()
            }
        }
    }
}
