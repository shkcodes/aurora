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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltNavGraphViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.navigate
import com.shkcodes.aurora.R
import com.shkcodes.aurora.base.SideEffect
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
import com.shkcodes.aurora.ui.timeline.TimelineContract.TimelineSideEffect.OpenUrl
import com.shkcodes.aurora.ui.timeline.TimelineContract.TimelineSideEffect.RetainScrollState
import com.shkcodes.aurora.ui.timeline.TimelineContract.TimelineSideEffect.ScrollToTop
import com.shkcodes.aurora.ui.tweetlist.TweetList
import com.shkcodes.aurora.ui.tweetlist.TweetListHandler
import com.shkcodes.aurora.ui.tweetlist.TweetListState
import com.shkcodes.aurora.util.pluralResource
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

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
            val tweetsHandler = remember { tweetListHandler(viewModel) }

            Box(contentAlignment = Alignment.TopCenter) {
                with(state) {
                    TweetList(
                        state = TweetListState(items, autoplayVideos, isLoading, isPaginatedError),
                        handler = tweetsHandler,
                        listState = listState
                    )
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
    viewModel: TimelineViewModel,
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

private fun tweetListHandler(viewModel: TimelineViewModel): TweetListHandler {
    return object : TweetListHandler {
        override fun refresh() {
            viewModel.handleIntent(Refresh)
        }

        override fun annotationClick(annotation: String) {
            viewModel.handleIntent(HandleAnnotationClick(annotation))
        }

        override fun paginatedErrorAction() {
            viewModel.handleIntent(LoadNextPage)
        }

        override fun mediaClick(index: Int, id: Long) {
            viewModel.handleIntent(MediaClick(index, id))
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

private val LazyListState.isAtTheBottom: Boolean
    get() {
        return with(layoutInfo) {
            visibleItemsInfo.isNotEmpty() && visibleItemsInfo.last().index == totalItemsCount - 1
        }
    }
