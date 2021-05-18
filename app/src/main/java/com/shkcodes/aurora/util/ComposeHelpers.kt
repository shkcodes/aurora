package com.shkcodes.aurora.util

import androidx.annotation.PluralsRes
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
