package com.shkcodes.aurora.api

import android.content.Context
import com.shkcodes.aurora.R
import com.shkcodes.aurora.base.StringId
import com.shkcodes.aurora.base.StringProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StringProviderImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : StringProvider {

    override fun getString(stringId: StringId) = context.getString(
        when (stringId) {
            StringId.UNKNOWN -> R.string.error_unknown
            StringId.NO_INTERNET -> R.string.error_no_internet
            StringId.TIMEOUT -> R.string.error_timeout
            StringId.TOO_MANY_IMAGES -> R.string.error_too_many_images
            StringId.UNSUPPORTED_ATTACHMENT -> R.string.error_unsupported_attachment
            StringId.TOO_MANY_VIDEOS -> R.string.error_too_many_videos
            StringId.MULTIPLE_TYPES -> R.string.error_multiple_attachments_type
        }
    )
}
