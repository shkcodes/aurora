package com.shkcodes.aurora.ui.timeline.items

import android.view.View
import com.fueled.reclaim.AdapterItem
import com.fueled.reclaim.BaseViewHolder
import com.shkcodes.aurora.R
import com.shkcodes.aurora.databinding.ItemPaginatedErrorBinding

class PaginatedErrorItem(
    private val retryAction: () -> Unit
) : AdapterItem<PaginatedErrorViewHolder>() {

    override fun onCreateViewHolder(view: View) =
        PaginatedErrorViewHolder(ItemPaginatedErrorBinding.bind(view))

    override val layoutId = R.layout.item_paginated_error

    override fun updateItemViews(viewHolder: PaginatedErrorViewHolder) {
        viewHolder.binding.retry.setOnClickListener { retryAction() }
    }
}

class PaginatedErrorViewHolder(val binding: ItemPaginatedErrorBinding) : BaseViewHolder(binding.root)
