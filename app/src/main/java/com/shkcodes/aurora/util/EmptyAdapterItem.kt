package com.shkcodes.aurora.util

import android.view.View
import com.shkcodes.aurora.R
import com.shkcodes.aurora.databinding.ItemEmptyBinding
import com.xwray.groupie.Item
import com.xwray.groupie.viewbinding.BindableItem

class EmptyAdapterItem(
    private val content: String
) : BindableItem<ItemEmptyBinding>() {

    override fun initializeViewBinding(view: View): ItemEmptyBinding {
        return ItemEmptyBinding.bind(view)
    }

    override fun getLayout() = R.layout.item_empty

    override fun bind(binding: ItemEmptyBinding, position: Int) {
        binding.text.text = content
    }

    override fun isSameAs(other: Item<*>): Boolean {
        return other is EmptyAdapterItem && other.content == content
    }

    override fun hasSameContentAs(other: Item<*>): Boolean {
        return isSameAs(other)
    }
}
