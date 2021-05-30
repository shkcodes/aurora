package com.shkcodes.aurora.util

import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder

class PagedAdapter(val loadMoreData: () -> Unit) : GroupAdapter<GroupieViewHolder>() {
    companion object {
        private const val PAGINATION_TRIGGER_THRESHOLD = 3
    }

    var canLoadMore: Boolean = true

    override fun onBindViewHolder(holder: GroupieViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        checkForPagination(position)
    }

    override fun onBindViewHolder(
        holder: GroupieViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        super.onBindViewHolder(holder, position, payloads)
        checkForPagination(position)
    }

    private fun checkForPagination(index: Int) {
        if (canLoadMore && itemCount != 0 && index > itemCount - PAGINATION_TRIGGER_THRESHOLD) {
            loadMoreData()
        }
    }
}
