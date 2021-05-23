package com.shkcodes.aurora.ui.tweetlist

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.transform.CircleCropTransformation
import com.google.accompanist.coil.rememberCoilPainter
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.shkcodes.aurora.R
import com.shkcodes.aurora.cache.entities.TweetEntity
import com.shkcodes.aurora.theme.Dimens
import com.shkcodes.aurora.util.toPrettyTime
import kotlin.math.abs

data class TweetListState(
    val items: TweetItems = emptyList(),
    val autoplayVideos: Boolean = false,
    val isPaginatedError: Boolean = false
)

interface TweetListHandler {
    fun annotationClick(annotation: String)
    fun paginatedErrorAction()
    fun mediaClick(index: Int, id: Long)
    fun showProfile(userHandle: String)
}

@Composable
fun TweetList(state: TweetListState, listState: LazyListState, handler: TweetListHandler) {

    val context = LocalContext.current
    val exoPlayer = remember {
        SimpleExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ALL
        }
    }

    val currentlyPlayingItem = if (state.autoplayVideos) {
        getCurrentlyPlayingItem(listState, state.items)
    } else {
        null
    }
    val urlsMetaData = remember { mutableMapOf<String, MetaData>() }

    VideoPlayer(exoPlayer, currentlyPlayingItem)

    LazyColumn(state = listState, modifier = Modifier.fillMaxWidth()) {
        items(state.items) {
            TweetItem(it, urlsMetaData, exoPlayer, currentlyPlayingItem == it, handler)
        }
        if (state.isPaginatedError) {
            item {
                PaginatedError { handler.paginatedErrorAction() }
            }
        }
    }
    LaunchedEffect(state.isPaginatedError) {
        if (state.isPaginatedError) listState.animateScrollToItem(state.items.size - 1)
    }
}

@Composable
private fun TweetItem(
    tweetItem: TweetItem,
    urlsMetaData: MutableMap<String, MetaData>,
    exoPlayer: SimpleExoPlayer,
    isVideoPlaying: Boolean,
    handler: TweetListHandler
) {
    val tweet = tweetItem.tweet
    val quoteTweet = tweetItem.quoteTweet
    val media = tweetItem.tweetMedia
    val quoteTweetMedia = tweetItem.quoteTweetMedia

    Row(modifier = Modifier.padding(Dimens.keyline_1)) {
        Image(
            painter = rememberCoilPainter(
                request = tweet.userProfileImageUrl, fadeIn = true,
                requestBuilder = {
                    transformations(CircleCropTransformation())
                },
            ),
            contentDescription = stringResource(id = R.string.accessibility_user_profile_image),
            modifier = Modifier
                .size(Dimens.tweet_profile_pic)
                .clickable { handler.showProfile("@${tweet.userHandle}") }
        )
        Column(
            modifier = Modifier
                .padding(start = Dimens.space)
        ) {
            TweetItemHeader(tweet)
            if (tweet.repliedToUsers.isNotEmpty()) {
                RepliedToUsers(tweet.repliedToUsers) {
                    handler.annotationClick(it)
                }
            }
            if (tweet.content.isNotEmpty()) RichContent(tweet) {
                handler.annotationClick(it)
            }
            QuoteTweet(
                quoteTweet,
                quoteTweetMedia,
                handler,
                exoPlayer
            )
            if (tweet.sharedUrls.isNotEmpty() && media.isEmpty() && quoteTweet == null) {
                val url = tweet.sharedUrls.first().url
                LinkPreview(
                    url,
                    urlsMetaData[url],
                    { urlsMetaData[url] = it },
                    { handler.annotationClick(it) })
            }

            TweetMedia(media, exoPlayer, isVideoPlaying) { index ->
                handler.mediaClick(index, tweet.id)
            }
            if (tweetItem.isRetweet) {
                RetweetIndicator(tweetItem.retweeter)
            }
        }
    }
}

@Composable
private fun TweetItemHeader(tweet: TweetEntity) {
    Row {
        UserInfo(tweet.userName, tweet.userHandle, modifier = Modifier.weight(1F))
        Text(
            text = tweet.createdAt.toPrettyTime(),
            style = typography.overline,
            modifier = Modifier.padding(
                start = Dimens.space_small,
                top = Dimens.space_small
            )
        )
    }
}

@Composable
private fun PaginatedError(action: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Divider(
            color = colors.error,
            modifier = Modifier.padding(horizontal = Dimens.space_large, vertical = Dimens.space)
        )
        Text(
            text = stringResource(id = R.string.tweets_pagination_error),
            style = typography.caption.copy(
                fontSize = Dimens.text_large,
                fontWeight = FontWeight.Medium
            ),
            textAlign = TextAlign.Center
        )
        Button(
            onClick = action, modifier = Modifier.padding(Dimens.space),
            colors = ButtonDefaults.buttonColors(backgroundColor = colors.error)
        ) {
            Text(text = stringResource(id = R.string.retry))
        }
    }
}

@Composable
private fun VideoPlayer(exoPlayer: SimpleExoPlayer, tweet: TweetItem?) {
    val context = LocalContext.current
    val dataSourceFactory = remember {
        DefaultDataSourceFactory(
            context,
            Util.getUserAgent(context, context.packageName)
        )
    }

    val lifecycleOwner by rememberUpdatedState(LocalLifecycleOwner.current)

    LaunchedEffect(tweet) {
        exoPlayer.apply {
            if (tweet != null && tweet.tweetMedia.isNotEmpty()) {
                val source = ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(MediaItem.fromUri(Uri.parse(tweet.tweetMedia.first().url)))

                setMediaSource(source)
                prepare()
                playWhenReady = true
            } else {
                stop()
            }
        }
    }
    DisposableEffect(lifecycleOwner) {
        val lifecycle = lifecycleOwner.lifecycle
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    exoPlayer.playWhenReady = false
                }
                Lifecycle.Event.ON_RESUME -> {
                    exoPlayer.playWhenReady = true
                }
                Lifecycle.Event.ON_DESTROY -> {
                    exoPlayer.run {
                        stop()
                        release()
                    }
                }
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }
}

private fun getCurrentlyPlayingItem(listState: LazyListState, items: TweetItems): TweetItem? {
    val layoutInfo = listState.layoutInfo
    val animatedMediaTweets =
        layoutInfo.visibleItemsInfo.map { items[it.index] }.filter { it.hasAnimatedMedia }
    return if (animatedMediaTweets.size == 1) {
        animatedMediaTweets.first()
    } else {
        val midPoint = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
        val itemsFromCenter =
            layoutInfo.visibleItemsInfo.sortedBy { abs((it.offset + it.size / 2) - midPoint) }
        itemsFromCenter.map { items[it.index] }.firstOrNull { it.hasAnimatedMedia }
    }
}
