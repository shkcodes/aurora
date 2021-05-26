package com.shkcodes.aurora.ui.timeline

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltNavGraphViewModel
import androidx.navigation.NavController
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.shkcodes.aurora.R
import com.shkcodes.aurora.base.SideEffect
import com.shkcodes.aurora.theme.Dimens
import com.shkcodes.aurora.ui.Screen
import com.shkcodes.aurora.ui.common.TerminalError
import com.shkcodes.aurora.ui.timeline.HomeTimelineContract.Intent.HandleAnnotationClick
import com.shkcodes.aurora.ui.timeline.HomeTimelineContract.Intent.LoadNextPage
import com.shkcodes.aurora.ui.timeline.HomeTimelineContract.Intent.MarkItemsAsSeen
import com.shkcodes.aurora.ui.timeline.HomeTimelineContract.Intent.MediaClick
import com.shkcodes.aurora.ui.timeline.HomeTimelineContract.Intent.Refresh
import com.shkcodes.aurora.ui.timeline.HomeTimelineContract.Intent.Retry
import com.shkcodes.aurora.ui.timeline.HomeTimelineContract.Intent.ScrollIndexChange
import com.shkcodes.aurora.ui.timeline.HomeTimelineContract.Screen.MediaViewer
import com.shkcodes.aurora.ui.timeline.HomeTimelineContract.Screen.UserProfile
import com.shkcodes.aurora.ui.timeline.HomeTimelineContract.TimelineSideEffect.OpenUrl
import com.shkcodes.aurora.ui.timeline.HomeTimelineContract.TimelineSideEffect.RetainScrollState
import com.shkcodes.aurora.ui.timeline.HomeTimelineContract.TimelineSideEffect.ScrollToTop
import com.shkcodes.aurora.ui.tweetlist.TweetList
import com.shkcodes.aurora.ui.tweetlist.TweetListHandler
import com.shkcodes.aurora.ui.tweetlist.TweetListState
import com.shkcodes.aurora.util.isAtTheBottom
import com.shkcodes.aurora.util.pluralResource
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun HomeTimeline(navController: NavController) {
    val viewModel = hiltNavGraphViewModel<HomeTimelineViewModel>()
    val state = viewModel.composableState()
    val listState = rememberLazyListState()

    val newItemsCount = state.newTweets.size
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(Unit) {
        viewModel.getSideEffects().collect {
            when (it) {
                is SideEffect.Action<*> -> handleSideEffects(it, uriHandler, listState)
                is SideEffect.DisplayScreen<*> -> handleNavigation(it, navController)
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
                listState.isAtTheBottom && !state.isPaginatedError && state.tweets.isNotEmpty()

            if (shouldLoadMore) {
                viewModel.handleIntent(LoadNextPage)
            }
            val tweetsHandler = remember { tweetListHandler(viewModel) }

            Box(contentAlignment = Alignment.TopCenter) {
                with(state) {
                    SwipeRefresh(
                        state = rememberSwipeRefreshState(isRefreshing = state.isLoading),
                        onRefresh = { viewModel.handleIntent(Refresh) },
                        indicator = { swipeRefreshState, trigger ->
                            SwipeRefreshIndicator(
                                state = swipeRefreshState,
                                refreshTriggerDistance = trigger,
                                contentColor = colors.secondary,
                            )
                        }
                    ) {
                        TweetList(
                            state = TweetListState(tweets, autoplayVideos, isPaginatedError),
                            handler = tweetsHandler,
                            listState = listState
                        )
                    }
                }
                NewTweetsIndicator(newItemsCount, viewModel, listState)
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun NewTweetsIndicator(
    newItemsCount: Int,
    viewModel: HomeTimelineViewModel,
    listState: LazyListState
) {
    AnimatedVisibility(
        visible = newItemsCount != 0 && !listState.isScrollInProgress,
        enter = slideInVertically(animationSpec = tween()),
        exit = slideOutVertically(animationSpec = tween())
    ) {
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
}

private fun tweetListHandler(viewModel: HomeTimelineViewModel): TweetListHandler {
    return object : TweetListHandler {
        override fun annotationClick(annotation: String) {
            viewModel.handleIntent(HandleAnnotationClick(annotation))
        }

        override fun paginatedErrorAction() {
            viewModel.handleIntent(LoadNextPage)
        }

        override fun mediaClick(index: Int, id: Long) {
            viewModel.handleIntent(MediaClick(index, id))
        }

        override fun showProfile(userHandle: String) {
            viewModel.handleIntent(HandleAnnotationClick(userHandle))
        }
    }
}

private suspend fun handleSideEffects(
    sideEffect: SideEffect.Action<*>,
    uriHandler: UriHandler,
    listState: LazyListState
) {
    when (val action = sideEffect.action) {
        is RetainScrollState -> {
            listState.scrollToItem(action.newTweetsCount)
        }
        is ScrollToTop -> {
            listState.animateScrollToItem(0)
        }
        is OpenUrl -> {
            uriHandler.openUri(action.url)
        }
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
