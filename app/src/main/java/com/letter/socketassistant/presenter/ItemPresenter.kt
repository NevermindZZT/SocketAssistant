package com.letter.socketassistant.presenter

/**
 * item点击Presenter
 * @param T item类型
 *
 * @author Letter(NevermindZZT@gmail.com)
 * @since 1.0.2
 */
interface ItemPresenter<T> {
    fun onItemClick(item: T)
}