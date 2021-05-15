package com.shkcodes.aurora.service

import com.shkcodes.aurora.cache.PreferenceManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesService @Inject constructor(
    private val preferenceManager: PreferenceManager
) {

    var autoplayVideos: Boolean
        get() = preferenceManager.autoplayVideos
        set(value) {
            preferenceManager.autoplayVideos = value
        }
}
