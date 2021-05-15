package com.shkcodes.aurora.ui.timeline

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.google.android.exoplayer2.SimpleExoPlayer
import com.shkcodes.aurora.R
import com.shkcodes.aurora.cache.entities.MediaEntity
import com.shkcodes.aurora.cache.entities.TweetEntity
import com.shkcodes.aurora.theme.Dimens
import com.shkcodes.aurora.util.contentFormatter

@Composable
fun RepliedToUsers(users: List<String>) {
    val content = when (users.size) {
        1 -> stringResource(R.string.single_replied_placeholder, users.single())
        2 -> stringResource(
            id = R.string.two_users_replied_placeholder,
            users.first(),
            users.last()
        )
        else -> stringResource(
            id = R.string.multiple_users_replied_placeholder,
            users[0], users[1], users.size - 2
        )
    }
    val styledContent =
        contentFormatter(text = stringResource(id = R.string.users_replied_placeholder, content))

    CustomClickableText(
        text = styledContent,
        style = MaterialTheme.typography.caption.copy(color = LocalContentColor.current),
        maxLines = 2,
    ) {
        styledContent
            .getStringAnnotations(start = it, end = it)
            .firstOrNull()
            ?.let { annotation ->
                println("clicked replied to ${annotation.tag}")
            }
    }
}

@Composable
fun QuoteTweet(
    tweet: TweetEntity?,
    media: List<MediaEntity>,
    uriHandler: UriHandler,
    viewModel: TimelineViewModel,
    exoPlayer: SimpleExoPlayer,
) {
    tweet?.let {
        Box(
            modifier = Modifier
                .padding(top = Dimens.space)
                .clip(RoundedCornerShape(Dimens.default_corner_radius))
                .fillMaxWidth()
                .background(colors.surface)
        ) {
            Column(Modifier.padding(Dimens.keyline_1)) {
                UserInfo(
                    it.userName,
                    it.userHandle
                )
                RichContent(it, uriHandler)
                TweetMedia(media, exoPlayer, false) { index ->
                    viewModel.handleIntent(TimelineContract.Intent.MediaClick(index, it.id))
                }
            }
        }
    }
}

@Composable
fun RetweetIndicator(userName: String) {
    Row(modifier = Modifier.padding(top = Dimens.keyline_1)) {
        Image(
            imageVector = Icons.Default.Repeat,
            contentDescription = stringResource(id = R.string.accessibility_retweet_icon),
            modifier = Modifier.size(Dimens.keyline_1),
            colorFilter = ColorFilter.tint(
                MaterialTheme.colors.secondary
            )
        )
        Text(
            text = stringResource(id = R.string.retweet_indicator_placeholder, userName),
            modifier = Modifier.padding(start = Dimens.space),
            style = MaterialTheme.typography.caption,
            color = MaterialTheme.colors.secondary
        )
    }
}

@Composable
fun RichContent(tweet: TweetEntity, uriHandler: UriHandler) {
    val styledContent = contentFormatter(tweet.content, tweet.sharedUrls, tweet.hashtags)
    CustomClickableText(
        text = styledContent,
        style = MaterialTheme.typography.body2.copy(color = LocalContentColor.current),
        modifier = Modifier
            .padding(top = Dimens.space)
    ) {
        styledContent
            .getStringAnnotations(start = it, end = it)
            .firstOrNull()
            ?.let { annotation ->
                uriHandler.openUri(annotation.tag)
                println("clicked ${annotation.tag}")
            }
    }
}

private const val USER_HANDLE_OPACITY = 0.5F

@Composable
fun UserInfo(name: String, handle: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.subtitle2.copy(fontSize = Dimens.text_body),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = stringResource(
                id = R.string.user_handle_placeholder,
                handle
            ),
            style = MaterialTheme.typography.body2.copy(fontSize = Dimens.text_caption),
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            modifier = Modifier
                .alpha(USER_HANDLE_OPACITY)
        )
    }
}
