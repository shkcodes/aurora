package com.shkcodes.aurora.ui.timeline

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltNavGraphViewModel
import androidx.lifecycle.Lifecycle.Event
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import androidx.navigation.compose.navigate
import coil.transform.CircleCropTransformation
import com.google.accompanist.coil.rememberCoilPainter
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.shkcodes.aurora.R
import com.shkcodes.aurora.base.SideEffect
import com.shkcodes.aurora.cache.entities.TweetEntity
import com.shkcodes.aurora.theme.Dimens
import com.shkcodes.aurora.ui.Screen
import com.shkcodes.aurora.ui.common.TerminalError
import com.shkcodes.aurora.ui.timeline.TimelineContract.Intent.HandleAnnotationClick
import com.shkcodes.aurora.ui.timeline.TimelineContract.Intent.LoadNextPage
import com.shkcodes.aurora.ui.timeline.TimelineContract.Intent.MarkItemsAsSeen
import com.shkcodes.aurora.ui.timeline.TimelineContract.Intent.MediaClick
import com.shkcodes.aurora.ui.timeline.TimelineContract.Intent.Refresh
import com.shkcodes.aurora.ui.timeline.TimelineContract.Intent.Retry
import com.shkcodes.aurora.ui.timeline.TimelineContract.Intent.ScrollIndexChange
import com.shkcodes.aurora.ui.timeline.TimelineContract.Screen.MediaViewer
import com.shkcodes.aurora.ui.timeline.TimelineContract.Screen.UserProfile
import com.shkcodes.aurora.ui.timeline.TimelineContract.State
import com.shkcodes.aurora.ui.timeline.TimelineContract.TimelineSideEffect.OpenUrl
import com.shkcodes.aurora.ui.timeline.TimelineContract.TimelineSideEffect.RetainScrollState
import com.shkcodes.aurora.ui.timeline.TimelineContract.TimelineSideEffect.ScrollToTop
import com.shkcodes.aurora.util.pluralResource
import com.shkcodes.aurora.util.toPrettyTime
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlin.math.abs

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun TweetsTimeline(navController: NavController) {
    val viewModel = hiltNavGraphViewModel<TimelineViewModel>()
    val state = viewModel.composableState()
    val listState = rememberLazyListState()

    val newItemsCount = state.newItems.size
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(Unit) {
        launch {
            viewModel.getSideEffects().collect {
                when (it) {
                    is SideEffect.Action<*> -> {
                        when (val action = it.action) {
                            is RetainScrollState -> {
                                listState.scrollToItem(action.newItemsCount)
                            }
                            is ScrollToTop -> {
                                listState.animateScrollToItem(0)
                            }
                            is OpenUrl -> {
                                uriHandler.openUri(action.url)
                            }
                        }
                    }
                    is SideEffect.DisplayScreen<*> -> handleNavigation(it, navController)
                }
            }
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .collect { viewModel.handleIntent(ScrollIndexChange(it)) }
    }

    Scaffold {
        if (state.isTerminalError) {
            TerminalError(message = state.errorMessage) {
                viewModel.handleIntent(Retry)
            }
        } else {
            val shouldLoadMore =
                listState.isAtTheBottom && !state.isPaginatedError && state.items.isNotEmpty()

            if (shouldLoadMore) {
                viewModel.handleIntent(LoadNextPage)
            }

            val urlsMetaData = remember { mutableMapOf<String, MetaData>() }
            Box(contentAlignment = Alignment.TopCenter) {
                TweetsList(state, urlsMetaData, listState, viewModel)
                AnimatedVisibility(
                    visible = newItemsCount != 0 && !listState.isScrollInProgress,
                    enter = slideInVertically(animationSpec = tween()),
                    exit = slideOutVertically(animationSpec = tween())
                ) {
                    NewTweetsIndicator(newItemsCount, viewModel)
                }
            }
        }
    }
}

@Composable
private fun TweetsList(
    state: State,
    urlsMetaData: MutableMap<String, MetaData>,
    listState: LazyListState,
    viewModel: TimelineViewModel
) {

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

    VideoPlayer(exoPlayer, currentlyPlayingItem)

    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshing = state.isLoading),
        onRefresh = { viewModel.handleIntent(Refresh) },
        indicator = { swipeRefreshState, trigger ->
            SwipeRefreshIndicator(
                state = swipeRefreshState,
                refreshTriggerDistance = trigger,
                contentColor = colors.secondary,
            )
        }) {
        LazyColumn(state = listState, modifier = Modifier.fillMaxWidth()) {
            items(state.items) {
                TweetItem(it, urlsMetaData, viewModel, exoPlayer, currentlyPlayingItem == it)
            }
            if (state.isPaginatedError) {
                item {
                    PaginatedError { viewModel.handleIntent(LoadNextPage) }
                }
            }
        }
    }
    LaunchedEffect(state.isPaginatedError) {
        if (state.isPaginatedError) listState.animateScrollToItem(state.items.size - 1)
    }
}

@Composable
private fun TweetItem(
    timelineItem: TimelineItem,
    urlsMetaData: MutableMap<String, MetaData>,
    viewModel: TimelineViewModel,
    exoPlayer: SimpleExoPlayer,
    isVideoPlaying: Boolean
) {
    val tweet = timelineItem.tweet
    val quoteTweet = timelineItem.quoteTweet
    val media = timelineItem.tweetMedia
    val quoteTweetMedia = timelineItem.quoteTweetMedia

    Row(modifier = Modifier.padding(Dimens.keyline_1)) {
        Image(
            painter = rememberCoilPainter(
                request = tweet.userProfileImageUrl, fadeIn = true,
                requestBuilder = {
                    transformations(CircleCropTransformation())
                },
            ),
            contentDescription = stringResource(id = R.string.accessibility_user_profile_image),
            modifier = Modifier.size(Dimens.tweet_profile_pic)
        )
        Column(
            modifier = Modifier
                .padding(start = Dimens.space)
        ) {
            TweetItemHeader(tweet)
            if (tweet.repliedToUsers.isNotEmpty()) {
                RepliedToUsers(tweet.repliedToUsers) {
                    viewModel.handleIntent(HandleAnnotationClick(it))
                }
            }
            if (tweet.content.isNotEmpty()) RichContent(tweet) {
                viewModel.handleIntent(HandleAnnotationClick(it))
            }
            QuoteTweet(
                quoteTweet,
                quoteTweetMedia,
                viewModel,
                exoPlayer
            )
            if (tweet.sharedUrls.isNotEmpty() && media.isEmpty() && quoteTweet == null) {
                val url = tweet.sharedUrls.first().url
                LinkPreview(
                    url,
                    urlsMetaData[url],
                    { urlsMetaData[url] = it },
                    { viewModel.handleIntent(HandleAnnotationClick(it)) })
            }

            TweetMedia(media, exoPlayer, isVideoPlaying) { index ->
                viewModel.handleIntent(MediaClick(index, tweet.id))
            }
            if (timelineItem.isRetweet) {
                RetweetIndicator(timelineItem.retweeter)
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
private fun VideoPlayer(exoPlayer: SimpleExoPlayer, tweet: TimelineItem?) {
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
                Event.ON_PAUSE -> {
                    exoPlayer.playWhenReady = false
                }
                Event.ON_RESUME -> {
                    exoPlayer.playWhenReady = true
                }
                Event.ON_DESTROY -> {
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

private fun getCurrentlyPlayingItem(listState: LazyListState, items: TimelineItems): TimelineItem? {
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

@Composable
private fun NewTweetsIndicator(newItemsCount: Int, viewModel: TimelineViewModel) {
    Box(
        modifier = Modifier
            .padding(Dimens.keyline_1)
            .clip(CircleShape)
            .background(colors.secondary)
            .clickable { viewModel.handleIntent(MarkItemsAsSeen) }
    ) {
        Text(
            text = if (newItemsCount == 0) {
                stringResource(R.string.new_tweets_caught_up)
            } else {
                pluralResource(
                    id = R.plurals.new_tweets_placeholder,
                    newItemsCount, newItemsCount
                )
            },
            color = colors.surface,
            textAlign = TextAlign.Center,
            style = typography.caption,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(Dimens.space)
        )
    }
}

private fun handleNavigation(
    sideEffect: SideEffect.DisplayScreen<*>,
    navController: NavController
) {
    when (val screen = sideEffect.screen) {
        is MediaViewer -> {
            val route = Screen.MEDIA_VIEWER.createRoute(screen.tweetId, screen.index)
            navController.navigate(route)
        }
        is UserProfile -> {
            val route = Screen.PROFILE.createRoute(screen.userHandle)
            navController.navigate(route)
        }
    }
}

private val LazyListState.isAtTheBottom: Boolean
    get() {
        return with(layoutInfo) {
            visibleItemsInfo.isNotEmpty() && visibleItemsInfo.last().index == totalItemsCount - 1
        }
    }
