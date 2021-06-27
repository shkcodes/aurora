package com.shkcodes.aurora.util

import android.view.View
import com.fueled.reclaim.AdapterItem
import com.fueled.reclaim.BaseViewHolder
import com.shkcodes.aurora.R
import com.shkcodes.aurora.databinding.ItemEmptyBinding

class EmptyAdapterItem(
    private val content: String
) : AdapterItem<EmptyItemViewHolder>() {

    override fun onCreateViewHolder(view: View) = EmptyItemViewHolder(ItemEmptyBinding.bind(view))

    override val layoutId = R.layout.item_empty

    override fun updateItemViews(viewHolder: EmptyItemViewHolder) {
        viewHolder.binding.text.text = content
    }

    override fun isTheSame(newItem: AdapterItem<*>): Boolean {
        return newItem is EmptyAdapterItem && newItem.content == content
    }
}

class EmptyItemViewHolder(val binding: ItemEmptyBinding) : BaseViewHolder(binding.root)
