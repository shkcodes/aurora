package com.shkcodes.aurora.api.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MediaUploadResponse(@Json(name = "media_id_string") val mediaId: String)
