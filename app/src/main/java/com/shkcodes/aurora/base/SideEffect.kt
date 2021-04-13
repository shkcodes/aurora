package com.shkcodes.aurora.base

sealed class SideEffect {
    data class DisplayScreen<T>(val screen: T) : SideEffect()
}
