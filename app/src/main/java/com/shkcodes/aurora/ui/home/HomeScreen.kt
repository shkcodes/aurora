package com.shkcodes.aurora.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltNavGraphViewModel
import coil.transform.CircleCropTransformation
import coil.transform.RoundedCornersTransformation
import com.google.accompanist.coil.CoilImage
import com.shkcodes.aurora.R
import com.shkcodes.aurora.cache.entities.MediaEntity
import com.shkcodes.aurora.cache.entities.TweetEntity
import com.shkcodes.aurora.theme.Dimens
import com.shkcodes.aurora.ui.common.TerminalError
import com.shkcodes.aurora.ui.home.HomeContract.Intent.Init
import com.shkcodes.aurora.ui.home.HomeContract.Intent.LoadNextPage
import com.shkcodes.aurora.ui.home.HomeContract.Intent.Retry
import com.shkcodes.aurora.ui.home.HomeContract.State
import com.shkcodes.aurora.util.contentFormatter
import com.shkcodes.aurora.util.toPrettyTime
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

private const val USER_HANDLE_OPACITY = 0.5F

@Composable
fun HomeScreen() {
    val viewModel = hiltNavGraphViewModel<HomeViewModel>()

    LaunchedEffect(Unit) {
        viewModel.handleIntent(Init)
        launch {
            viewModel.getSideEffects().collect { }
        }
    }

    Scaffold {
        when (val state = viewModel.getState().collectAsState().value) {
            is State.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is State.Content -> {
                val listState = rememberLazyListState()
                val shouldLoadMore = listState.isAtTheBottom && !state.isPaginatedError

                if (shouldLoadMore) {
                    viewModel.handleIntent(LoadNextPage(state))
                }
                Box(contentAlignment = Alignment.BottomCenter) {
                    TweetsList(state, listState) {
                        viewModel.handleIntent(
                            LoadNextPage(
                                State.Content(
                                    state.tweets,
                                    false
                                )
                            )
                        )
                    }
                    if (state.isLoadingNextPage) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }
            }
            is State.Error -> {
                TerminalError(message = state.message) {
                    viewModel.handleIntent(Retry)
                }
            }
        }
    }
}

@Composable
private fun TweetsList(state: State.Content, listState: LazyListState, retryAction: () -> Unit) {
    val scope = rememberCoroutineScope()
    LazyColumn(state = listState) {
        items(state.tweets) {
            TweetItem(it)
        }
        if (state.isPaginatedError) {
            item { PaginatedError(retryAction) }
        }
    }
    LaunchedEffect(state.isPaginatedError) {
        if (state.isPaginatedError) scope.launch {
            listState.animateScrollToItem(state.tweets.size - 1)
        }
    }
}

@Composable
private fun TweetItem(timelineTweet: TimelineTweetItem) {
    val tweet = timelineTweet.retweetedTweet ?: timelineTweet.primaryTweet
    Row(modifier = Modifier.padding(Dimens.keyline_1)) {
        CoilImage(
            data = tweet.userProfileImageUrl,
            contentDescription = stringResource(id = R.string.accessibility_user_profile_image),
            fadeIn = true,
            requestBuilder = {
                transformations(CircleCropTransformation())
            },
            modifier = Modifier.size(Dimens.tweet_profile_pic)
        )
        Column(
            modifier = Modifier
                .padding(start = Dimens.space)
        ) {
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
            RichContent(tweet.content)
            timelineTweet.quotedTweet?.let {
                Card(
                    modifier = Modifier
                        .padding(top = Dimens.space, start = Dimens.space)
                        .fillMaxWidth()
                ) {
                    Column(Modifier.padding(Dimens.keyline_1)) {
                        UserInfo(
                            it.userName,
                            it.userHandle
                        )
                        Text(
                            text = it.content,
                            style = typography.body2
                        )
                        SharedUrl(tweet = it)
                    }
                }
            }
            SharedUrl(tweet = tweet)
            TweetMedia(timelineTweet.media)
            if (timelineTweet.retweetedTweet != null) {
                RetweetIndicator(timelineTweet.primaryTweet.userName)
            }
        }
    }
}

private const val MEDIA_CORNER_RADIUS = 4F

@Composable
private fun TweetMedia(media: List<MediaEntity>) {
    if (media.isNotEmpty()) {
        CoilImage(
            data = media.first().imageUrl,
            contentDescription = null,
            fadeIn = true,
            contentScale = ContentScale.FillWidth,
            requestBuilder = {
                transformations(RoundedCornersTransformation(radius = MEDIA_CORNER_RADIUS))
            },
            modifier = Modifier
                .padding(top = Dimens.space)
                .fillMaxWidth()
                .height(Dimens.tweet_single_media_height)
        )
    }
}

@Composable
private fun SharedUrl(tweet: TweetEntity) {
    val uriHandler = LocalUriHandler.current
    if (tweet.displayableSharedUrl != null) {
        Text(
            text = tweet.displayableSharedUrl,
            style = typography.body2.copy(color = colors.primary),
            modifier = Modifier.clickable {
                uriHandler.openUri(tweet.sharedUrl.orEmpty())
            }
        )
    }
}

@Composable
private fun RetweetIndicator(userName: String) {
    Row(modifier = Modifier.padding(top = Dimens.space)) {
        Image(
            imageVector = Icons.Default.Repeat,
            contentDescription = stringResource(id = R.string.accessibility_retweet_icon),
            modifier = Modifier.size(Dimens.keyline_1),
            colorFilter = ColorFilter.tint(
                colors.secondary
            )
        )
        Text(
            text = stringResource(id = R.string.retweet_indicator_placeholder, userName),
            modifier = Modifier.padding(start = Dimens.space),
            style = typography.caption,
            color = colors.secondary
        )
    }
}

@Composable
private fun RichContent(content: String) {
    val styledContent = contentFormatter(text = content)
    ClickableText(
        text = styledContent,
        style = typography.body2.copy(color = LocalContentColor.current),
        modifier = Modifier
            .padding(top = Dimens.space)
    ) {
        styledContent
            .getStringAnnotations(start = it, end = it)
            .firstOrNull()
            ?.let { annotation ->
                println("clicked ${annotation.item}")
            }
    }
}

@Composable
private fun UserInfo(name: String, handle: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            style = typography.subtitle2.copy(fontSize = Dimens.text_body),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = stringResource(
                id = R.string.user_handle_placeholder,
                handle
            ),
            style = typography.body2.copy(fontSize = Dimens.text_caption),
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            modifier = Modifier
                .alpha(USER_HANDLE_OPACITY)
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

private val LazyListState.isAtTheBottom: Boolean
    get() {
        return with(layoutInfo) {
            visibleItemsInfo.isNotEmpty() && visibleItemsInfo.last().index == totalItemsCount - 1
        }
    }
