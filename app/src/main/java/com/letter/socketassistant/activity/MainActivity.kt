package com.letter.socketassistant.activity

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
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
import com.letter.socketassistant.utils.startActivity
import com.letter.socketassistant.utils.toast
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
class MainActivity : BaseActivity(), Presenter,
    NavigationView.OnNavigationItemSelectedListener,
    View.OnFocusChangeListener {

    companion object {
        private const val TAG = "MainActivity"
    }

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
    private lateinit var serialPortAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /* 绑定视图 */
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        /* 设置ActionBar */
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_main_menu)

        /* 设置数据 */
        binding.let {
            it.lifecycleOwner = this
            it.vm = model
            it.presenter = this
        }

        /* navigation view header layout 数据绑定 */
        DataBindingUtil.bind<LayoutMainNavHeaderBinding>(navigationView.getHeaderView(0)).let {
            it?.lifecycleOwner = this
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

        /* 初始化串口端口下拉框 */
        val serialPortSpinner = serialParamBinding.root.findViewById<Spinner>(R.id.serialPortSpinner)
        serialPortAdapter = ArrayAdapter(
            this@MainActivity,
            R.layout.layout_main_spinner_select,
            model.serialPortList
        )
        serialPortAdapter.setDropDownViewResource(R.layout.layout_main_spinner_drop)
        serialPortSpinner.adapter = serialPortAdapter
        serialPortSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                model.connectionParamDao.value?.serialConnectionParam?.port = model.serialPortList[position]
            }
        }

        /* 初始化串口端口下拉框 */
        val serialParitySpinner = serialParamBinding.root.findViewById<Spinner>(R.id.serialParitySpinner)
        val serialParityAdapter = ArrayAdapter(
            this@MainActivity,
            R.layout.layout_main_spinner_select,
            model.serialParityList
        )
        serialParityAdapter.setDropDownViewResource(R.layout.layout_main_spinner_drop)
        serialParitySpinner.adapter = serialParityAdapter
        serialParitySpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                model.connectionParamDao.value?.serialConnectionParam?.parity = model.serialParityList[position]
            }
        }

        /* 初始化字符集下拉框 */
        val charsetAdapter = ArrayAdapter(
            this,
            R.layout.layout_main_spinner_select,
            MainViewModel.CHARSET_LIST
        )
        charsetAdapter.setDropDownViewResource(R.layout.layout_main_spinner_drop)
        charsetSpinner.adapter = charsetAdapter
        charsetSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                model.charSet.value = MainViewModel.CHARSET_LIST[position]
            }
        }


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
     * 创建选项菜单
     * @param menu Menu 菜单
     * @return Boolean
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main_toolbar, menu)
        return true
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
            R.id.disconnect -> model.disconnect()
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
            R.id.main_nav_esp_touch ->
                startActivity(EspTouchActivity::class.java)
            R.id.main_nav_setting ->
                startActivity(SettingActivity::class.java)
            R.id.main_nav_about ->
                startActivity(AboutActivity::class.java)
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
        model.refreshSerialPort()
        serialPortAdapter.notifyDataSetChanged()
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
        if (model.getConnectionNameList().isNotEmpty()) {
            MaterialDialog(this).show {
                listItemsSingleChoice(
                    items = model.getConnectionNameList(),
                    initialSelection = model.selectedConnectionIndex.value ?: 0) {
                        dialog, index, _ ->
                    model.selectedConnectionIndex.value = index
                    dialog.dismiss()
                }
            }
        } else {
            toast(R.string.main_activity_toast_no_connection)
        }
    }
}
