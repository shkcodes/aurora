package com.shkcodes.aurora.base

sealed class SideEffect {
    data class DisplayScreen<T>(val screen: T) : SideEffect()
    data class Action<T>(val action: T) : SideEffect()
}
