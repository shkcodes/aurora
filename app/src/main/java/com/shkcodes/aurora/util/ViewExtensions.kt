package com.shkcodes.aurora.util

import android.view.View
import androidx.annotation.DimenRes

fun View.setSize(@DimenRes dimen: Int) {
    layoutParams.height = context.pixelSize(dimen)
    layoutParams.width = context.pixelSize(dimen)
}
