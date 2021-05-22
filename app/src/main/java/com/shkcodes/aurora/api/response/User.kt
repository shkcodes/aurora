package com.shkcodes.aurora.api.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class User(
    @Json(name = "id")
    val id: Long,
    @Json(name = "name")
    val name: String,
    @Json(name = "screen_name")
    val screenName: String,
    @Json(name = "location")
    val location: String,
    @Json(name = "description")
    val description: String,
    @Json(name = "url")
    val url: String?,
    @Json(name = "verified")
    val verified: Boolean,
    @Json(name = "contributors_enabled")
    val contributorsEnabled: Boolean,
    @Json(name = "created_at")
    val createdAt: String,
    @Json(name = "favourites_count")
    val favouritesCount: Int,
    @Json(name = "follow_request_sent")
    val followRequestSent: Boolean,
    @Json(name = "followers_count")
    val followersCount: Int,
    @Json(name = "following")
    val following: Boolean,
    @Json(name = "friends_count")
    val friendsCount: Int,
    @Json(name = "profile_background_color")
    val profileBackgroundColor: String?,
    @Json(name = "profile_background_image_url_https")
    val profileBackgroundImageUrl: String?,
    @Json(name = "profile_background_tile")
    val profileBackgroundTile: Boolean,
    @Json(name = "profile_banner_url")
    val profileBannerUrl: String?,
    @Json(name = "profile_image_url_https")
    val profileImageUrl: String,
    @Json(name = "profile_link_color")
    val profileLinkColor: String,
    @Json(name = "profile_sidebar_border_color")
    val profileSidebarBorderColor: String,
    @Json(name = "profile_sidebar_fill_color")
    val profileSidebarFillColor: String,
    @Json(name = "profile_text_color")
    val profileTextColor: String,
    @Json(name = "profile_use_background_image")
    val profileUseBackgroundImage: Boolean,
    @Json(name = "statuses_count")
    val statusesCount: Int
) {
    val profileImageUrlLarge = profileImageUrl.replace("_normal", "")
}
