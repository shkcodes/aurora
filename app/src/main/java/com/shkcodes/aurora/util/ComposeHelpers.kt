package com.shkcodes.aurora.util

import androidx.annotation.PluralsRes
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun pluralResource(
    @PluralsRes id: Int,
    quantity: Int,
    vararg args: Any? = emptyArray()
): String {
    return LocalContext.current.resources
        .getQuantityString(id, quantity, *args)
}

val LazyListState.isAtTheBottom: Boolean
    get() {
        return with(layoutInfo) {
            visibleItemsInfo.isNotEmpty() && visibleItemsInfo.last().index == totalItemsCount - 1
        }
    }
