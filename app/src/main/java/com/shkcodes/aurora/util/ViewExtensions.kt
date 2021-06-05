package com.shkcodes.aurora.util

import android.graphics.Color
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.TextView
import androidx.annotation.DimenRes
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

fun View.setSize(@DimenRes dimen: Int) {
    layoutParams.height = context.pixelSize(dimen)
    layoutParams.width = context.pixelSize(dimen)
}

fun TextView.handleClickableSpans() {
    movementMethod = LinkMovementMethod.getInstance()
    highlightColor = Color.TRANSPARENT
}

val RecyclerView.linearLayoutManager: LinearLayoutManager
    get() = layoutManager as LinearLayoutManager

fun RecyclerView.observeScrolling(lifecycle: Lifecycle, onScroll: () -> Unit) {
    val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            onScroll()
        }
    }
    lifecycle.onDestroy { removeOnScrollListener(scrollListener) }
    addOnScrollListener(scrollListener)
}

fun ViewPager2.observePageChanges(lifecycle: Lifecycle, onPageChange: (Int) -> Unit) {
    val callback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            onPageChange(position)
        }
    }
    lifecycle.onDestroy { unregisterOnPageChangeCallback(callback) }
    registerOnPageChangeCallback(callback)
}
