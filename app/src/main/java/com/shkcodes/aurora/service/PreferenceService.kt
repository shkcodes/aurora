package com.shkcodes.aurora.service

import com.shkcodes.aurora.cache.PreferenceManager
import com.shkcodes.aurora.cache.UserCredentials
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceService @Inject constructor(
    private val preferenceManager: PreferenceManager
) {

    val isLoggedIn: Boolean
        get() = preferenceManager.userCredentials != null

    var autoplayVideos: Boolean
        get() = preferenceManager.autoplayVideos
        set(value) {
            preferenceManager.autoplayVideos = value
        }

    var userCredentials: UserCredentials?
        get() = preferenceManager.userCredentials
        set(value) {
            preferenceManager.userCredentials = value
        }
}
