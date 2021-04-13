package com.shkcodes.aurora.base

interface StringProvider {
    fun getString(stringId: StringId): String
}

enum class StringId {
    UNKNOWN, NO_INTERNET, TIMEOUT
}
