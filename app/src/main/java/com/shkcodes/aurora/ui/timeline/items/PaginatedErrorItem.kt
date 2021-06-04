package com.shkcodes.aurora.ui.timeline.items

import android.view.View
import com.shkcodes.aurora.R
import com.shkcodes.aurora.databinding.ItemPaginatedErrorBinding
import com.xwray.groupie.viewbinding.BindableItem

class PaginatedErrorItem(private val retryAction: () -> Unit) : BindableItem<ItemPaginatedErrorBinding>() {

    override fun initializeViewBinding(view: View): ItemPaginatedErrorBinding {
        return ItemPaginatedErrorBinding.bind(view)
    }

    override fun getLayout() = R.layout.item_paginated_error

    override fun bind(binding: ItemPaginatedErrorBinding, position: Int) {
        binding.retry.setOnClickListener { retryAction() }
    }
}
