package com.shkcodes.aurora.ui.timeline

import androidx.compose.foundation.Image
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
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltNavGraphViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.navigate
import coil.transform.CircleCropTransformation
import com.google.accompanist.coil.rememberCoilPainter
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.shkcodes.aurora.R
import com.shkcodes.aurora.base.SideEffect
import com.shkcodes.aurora.cache.entities.TweetEntity
import com.shkcodes.aurora.theme.Dimens
import com.shkcodes.aurora.ui.Screen
import com.shkcodes.aurora.ui.common.TerminalError
import com.shkcodes.aurora.ui.timeline.TimelineContract.Intent.Init
import com.shkcodes.aurora.ui.timeline.TimelineContract.Intent.LoadNextPage
import com.shkcodes.aurora.ui.timeline.TimelineContract.Intent.MediaClick
import com.shkcodes.aurora.ui.timeline.TimelineContract.Intent.Refresh
import com.shkcodes.aurora.ui.timeline.TimelineContract.Intent.Retry
import com.shkcodes.aurora.ui.timeline.TimelineContract.Screen.MediaViewer
import com.shkcodes.aurora.ui.timeline.TimelineContract.State.Content
import com.shkcodes.aurora.ui.timeline.TimelineContract.State.Error
import com.shkcodes.aurora.util.toPrettyTime
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@Composable
fun TweetsTimeline(navController: NavController) {
    val viewModel = hiltNavGraphViewModel<TimelineViewModel>()

    LaunchedEffect(Unit) {
        viewModel.handleIntent(Init)
        launch {
            viewModel.getSideEffects().collect { handleActions(it, navController) }
        }
    }

    Scaffold {
        when (val state = viewModel.getState().collectAsState().value) {
            is Content -> {
                val listState = rememberLazyListState()
                val shouldLoadMore =
                    listState.isAtTheBottom && !state.isPaginatedError && state.items.isNotEmpty()

                if (shouldLoadMore) {
                    viewModel.handleIntent(LoadNextPage(state))
                }

                val urlsMetaData = remember { mutableMapOf<String, MetaData>() }
                Box(contentAlignment = Alignment.BottomCenter) {
                    TweetsList(state, urlsMetaData, listState, viewModel)
                    if (state.isPaginatedLoading) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }
            }
            is Error -> {
                TerminalError(message = state.message) {
                    viewModel.handleIntent(Retry)
                }
            }
        }
    }
}

@Composable
private fun TweetsList(
    state: Content,
    urlsMetaData: MutableMap<String, MetaData>,
    listState: LazyListState,
    viewModel: TimelineViewModel
) {
    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshing = state.isLoading),
        onRefresh = { viewModel.handleIntent(Refresh(state)) },
        indicator = { swipeRefreshState, trigger ->
            SwipeRefreshIndicator(
                state = swipeRefreshState,
                refreshTriggerDistance = trigger,
                contentColor = colors.secondary,
            )
        }) {
        LazyColumn(state = listState, modifier = Modifier.fillMaxWidth()) {
            items(state.items) {
                TweetItem(it, urlsMetaData, viewModel)
            }
            if (state.isPaginatedError) {
                item {
                    PaginatedError {
                        viewModel.handleIntent(LoadNextPage(state))
                    }
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
    viewModel: TimelineViewModel
) {
    val tweet = timelineItem.tweet
    val quoteTweet = timelineItem.quoteTweet
    val media = timelineItem.tweetMedia
    val quoteTweetMedia = timelineItem.quoteTweetMedia

    val uriHandler = LocalUriHandler.current

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
                RepliedToUsers(tweet.repliedToUsers)
            }
            if (tweet.content.isNotEmpty()) RichContent(tweet, uriHandler)
            QuoteTweet(quoteTweet, quoteTweetMedia, uriHandler, viewModel)
            if (tweet.sharedUrls.isNotEmpty() && media.isEmpty() && quoteTweet == null) {
                val url = tweet.sharedUrls.first().url
                LinkPreview(
                    url,
                    urlsMetaData[url],
                    { urlsMetaData[url] = it }, { uriHandler.openUri(it) })
            }

            TweetMedia(media) { index ->
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

private fun handleActions(sideEffect: SideEffect, navController: NavController) {
    when (sideEffect) {
        is SideEffect.DisplayScreen<*> -> {
            when (val screen = sideEffect.screen) {
                is MediaViewer -> {
                    val route = Screen.MEDIA_VIEWER.createRoute(screen.tweetId, screen.index)
                    navController.navigate(route)
                }
            }
        }
    }
}

private val LazyListState.isAtTheBottom: Boolean
    get() {
        return with(layoutInfo) {
            visibleItemsInfo.isNotEmpty() && visibleItemsInfo.last().index == totalItemsCount - 1
        }
    }
