package com.shkcodes.aurora.base

interface StringProvider {
    fun getString(stringId: StringId): String
}

enum class StringId {
    UNKNOWN, NO_INTERNET, TIMEOUT, TOO_MANY_IMAGES, UNSUPPORTED_ATTACHMENT, TOO_MANY_VIDEOS, MULTIPLE_TYPES
}
