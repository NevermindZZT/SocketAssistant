package com.letter.socketassistant.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableList
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.letter.socketassistant.BR

/**
 * @author Letter(zhangkeqiang@ut.cn)
 * @version 1.0
 */
class BindingViewAdapter<T>
constructor(private val context: Context, private val layoutRes: Int, val list: ObservableList<T>)
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
        holder.binding.executePendingBindings()
    }

    override fun getItemCount() = list.size
}