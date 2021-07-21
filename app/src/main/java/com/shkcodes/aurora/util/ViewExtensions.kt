package com.shkcodes.aurora.util

import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.DimenRes
import androidx.core.view.get
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener

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

fun AppBarLayout.observeOffsetChanges(lifecycle: Lifecycle, onOffsetChange: (Int) -> Unit) {
    val listener = OnOffsetChangedListener { _, offset -> onOffsetChange(offset) }

    lifecycle.onDestroy { removeOnOffsetChangedListener(listener) }
    addOnOffsetChangedListener(listener)
}

val ViewPager2.recyclerView: RecyclerView
    get() = this[0] as RecyclerView

fun EditText.observeTextChanges(lifecycle: Lifecycle, onTextChange: (String) -> Unit) {
    val listener = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) = Unit

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            onTextChange(s.toString())
        }

        override fun afterTextChanged(s: Editable) = Unit
    }
    lifecycle.onDestroy { removeTextChangedListener(listener) }
    addTextChangedListener(listener)
}
