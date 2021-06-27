package com.shkcodes.aurora.util

import com.fueled.reclaim.BaseViewHolder
import com.fueled.reclaim.ItemsViewAdapter

class PagedAdapter(val loadMoreData: () -> Unit) : ItemsViewAdapter() {
    companion object {
        private const val PAGINATION_TRIGGER_THRESHOLD = 3
    }

    var canLoadMore: Boolean = true

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        checkForPagination(position)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int, payloads: List<Any>) {
        super.onBindViewHolder(holder, position, payloads)
        checkForPagination(position)
    }

    private fun checkForPagination(index: Int) {
        if (canLoadMore && itemCount != 0 && index > itemCount - PAGINATION_TRIGGER_THRESHOLD) {
            loadMoreData()
        }
    }
}
