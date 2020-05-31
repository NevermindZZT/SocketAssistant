package com.letter.socketassistant.activity

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.*
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.letter.socketassistant.LetterApplication
import com.letter.socketassistant.R
import com.letter.socketassistant.databinding.ActivityEspTouchBinding
import com.letter.socketassistant.presenter.Presenter
import com.letter.socketassistant.viewmodel.EspTouchViewModel
import kotlinx.android.synthetic.main.activity_esp_touch.*

/**
 * Esp Touch 活动
 * @property binding ActivityEspTouchBinding bingding
 * @property model EspTouchViewModel View Model
 * @property progressDialog MaterialDialog 加载对话框
 * @property messageDialog MaterialDialog 消息对话框
 *
 * @author Letter(nevermindzzt@gmail.com)lk;
 * @since 1.0.0
 */
class EspTouchActivity : BaseActivity(), Presenter {

    companion object {
        private const val TAG = "EspTouchActivity"
    }

    private lateinit var binding: ActivityEspTouchBinding
    private val model by lazy {
        ViewModelProvider
            .AndroidViewModelFactory(LetterApplication.instance()).create(EspTouchViewModel::class.java)
    }
    private val progressDialog by lazy {
        MaterialDialog(this).apply {
            customView(R.layout.layout_esp_touch_configuring)
            cancelOnTouchOutside(false)
            negativeButton(R.string.esp_touch_activity_dialog_negative_button) {
                model.cancelEspTouch()
                it.dismiss()
            }
        }
    }
    private val messageDialog by lazy {
        MaterialDialog(this).apply {
            negativeButton(R.string.esp_touch_activity_dialog_positive_button) {
                it.dismiss()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /* 绑定视图 */
        binding = DataBindingUtil.setContentView(this, R.layout.activity_esp_touch)

        /* 设置ActionBar */
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        /* 设置数据 */
        binding.let {
            it.lifecycleOwner = this
            it.vm = model
            it.presenter = this
        }

        model.apply {
            isConfiguring.observe(this@EspTouchActivity) {
                progressDialog.apply {
                    if (it) show() else dismiss()
                }
            }
            configMessage.observe(this@EspTouchActivity) {
                if (it.isNotEmpty()) {
                    messageDialog.show {
                        message(text = it)
                    }
                } else {
                    messageDialog.dismiss()
                }
            }
        }

        lifecycle.addObserver(BroadcastObserver())
    }


    /**
     * View 点击处理
     * @param view View? view
     */
    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.confirmButton -> model.startEspTouch()
        }
    }

    /**
     * navigation view 菜单点击处理
     * @param item MenuItem 菜单选项
     * @return Boolean 事件是否被处理
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return true
    }

    inner class BroadcastObserver: LifecycleObserver {

        @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
        fun connectListener() {
            model.registerBroadcastReceiver(this@EspTouchActivity)
            model.checkPermission {}
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        fun disconnectListener() {
            model.unregisterBroadcastReceiver(this@EspTouchActivity)
        }
    }
}
