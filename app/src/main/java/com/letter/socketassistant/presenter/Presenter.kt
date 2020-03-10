package com.letter.socketassistant.presenter

import android.view.View

/**
 * 点击Presenter
 *
 * @author Letter(nevermindzzt@gmail.com)
 * @since 1.0.0
 */
interface Presenter : View.OnClickListener {

    /**
     * View点击回调
     * @param view View 被点击的View
     */
    override fun onClick(view: View?)
}