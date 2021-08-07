package com.shkcodes.aurora.service

import com.shkcodes.aurora.cache.Authorization
import com.shkcodes.aurora.cache.PreferenceManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceService @Inject constructor(
    private val preferenceManager: PreferenceManager
) {

    val isLoggedIn: Boolean
        get() = preferenceManager.authorization != null

    var autoplayVideos: Boolean
        get() = preferenceManager.autoplayVideos
        set(value) {
            preferenceManager.autoplayVideos = value
        }

    var authorization: Authorization?
        get() = preferenceManager.authorization
        set(value) {
            preferenceManager.authorization = value
        }
}
