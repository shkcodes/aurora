package com.shkcodes.aurora.api.response

import com.shkcodes.aurora.cache.entities.Url
import com.shkcodes.aurora.cache.entities.toUrl
import twitter4j.URLEntity

data class User(
    val id: Long,
    val name: String,
    val screenName: String,
    val location: String?,
    val description: String,
    val verified: Boolean,
    val contributorsEnabled: Boolean,
    val createdAt: String,
    val favouritesCount: Int,
    val followRequestSent: Boolean,
    val followersCount: Int,
    val friendsCount: Int,
    val profileBackgroundColor: String?,
    val profileBackgroundImageUrl: String?,
    val profileBackgroundTile: Boolean,
    val profileBannerUrl: String?,
    val profileImageUrl: String,
    val profileLinkColor: String,
    val profileSidebarBorderColor: String,
    val profileSidebarFillColor: String,
    val profileTextColor: String,
    val profileUseBackgroundImage: Boolean,
    val statusesCount: Int,
    val url: Url?,
    val descriptionUrls: List<Url>
) {
    val profileImageUrlLarge = profileImageUrl.replace("_normal", "")
}

fun twitter4j.User.toUser(): User {
    return User(
        id,
        name,
        screenName,
        location,
        description,
        isVerified,
        isContributorsEnabled,
        createdAt.toString(),
        favouritesCount,
        isFollowRequestSent,
        followersCount,
        friendsCount,
        profileBackgroundColor,
        profileBackgroundImageUrlHttps,
        isProfileBackgroundTiled,
        profileBannerURL,
        profileImageURLHttps,
        profileLinkColor,
        profileSidebarBorderColor,
        profileSidebarFillColor,
        profileTextColor,
        isProfileUseBackgroundImage,
        statusesCount,
        urlEntity.toUrl(),
        descriptionURLEntities?.map(URLEntity::toUrl).orEmpty()
    )
}
