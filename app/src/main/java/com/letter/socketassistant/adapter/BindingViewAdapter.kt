package com.letter.socketassistant.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableList
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.letter.socketassistant.BR
import com.letter.socketassistant.presenter.ItemPresenter

/**
 * 数据绑定List Adapter
 * @param T 数据类型
 * @property context Context context
 * @property layoutRes Int 布局资源id
 * @property list ObservableList<T> 数据
 * @property presenter ItemPresenter<T>? item点击Presenter
 * @constructor 构造一个adapter
 *
 * @author Letter(nevermindzzt@gmail.com)
 * @since 1.0.0
 */
class BindingViewAdapter<T>
constructor(private val context: Context,
            private val layoutRes: Int,
            private val list: ObservableList<T>,
            private val presenter: ItemPresenter<T>? = null)
    :RecyclerView.Adapter<BindingViewAdapter.BindingViewHolder<ViewDataBinding>>() {

    class BindingViewHolder<out T : ViewDataBinding>
        constructor(val binding : T)
            : RecyclerView.ViewHolder(binding.root)

    init {
        list.addOnListChangedCallback(object : ObservableList.OnListChangedCallback<ObservableList<T>>() {
            override fun onChanged(sender: ObservableList<T>?) {
                notifyDataSetChanged()
            }

            override fun onItemRangeRemoved(
                sender: ObservableList<T>?,
                positionStart: Int,
                itemCount: Int
            ) {
                notifyItemRangeRemoved(positionStart, itemCount)
            }

            override fun onItemRangeMoved(
                sender: ObservableList<T>?,
                fromPosition: Int,
                toPosition: Int,
                itemCount: Int
            ) {
                notifyItemMoved(fromPosition, toPosition)
            }

            override fun onItemRangeInserted(
                sender: ObservableList<T>?,
                positionStart: Int,
                itemCount: Int
            ) {
                notifyItemRangeInserted(positionStart, itemCount)
            }

            override fun onItemRangeChanged(
                sender: ObservableList<T>?,
                positionStart: Int,
                itemCount: Int
            ) {
                notifyItemRangeChanged(positionStart, itemCount)
            }

        })
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) = BindingViewHolder(
        DataBindingUtil.inflate<ViewDataBinding>(
            LayoutInflater.from(context),
            layoutRes,
            parent,
            false
        )
    )

    override fun onBindViewHolder(
        holder: BindingViewHolder<ViewDataBinding>,
        position: Int
    ) {
        val item = list[position]
        holder.binding.setVariable(BR.item, item)
        if (presenter != null) {
            holder.binding.setVariable(BR.presenter, presenter)
        }
        holder.binding.executePendingBindings()
    }

    override fun getItemCount() = list.size
}