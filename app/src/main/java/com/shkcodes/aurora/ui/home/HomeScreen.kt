package com.shkcodes.aurora.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltNavGraphViewModel
import coil.transform.CircleCropTransformation
import com.google.accompanist.coil.CoilImage
import com.shkcodes.aurora.R
import com.shkcodes.aurora.cache.entities.TweetEntity
import com.shkcodes.aurora.theme.Dimens
import com.shkcodes.aurora.theme.typography
import com.shkcodes.aurora.ui.common.TerminalError
import com.shkcodes.aurora.ui.home.HomeContract.Intent.Init
import com.shkcodes.aurora.ui.home.HomeContract.Intent.Retry
import com.shkcodes.aurora.ui.home.HomeContract.State
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
                LazyColumn {
                    items(state.tweets) {
                        TweetItem(it)
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
private fun TweetItem(tweet: TweetEntity) {
    Row(modifier = Modifier.padding(Dimens.keyline_1)) {
        CoilImage(
            data = tweet.user.profileImageUrl,
            contentDescription = null,
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
                Row(
                    modifier = Modifier.weight(1F),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = tweet.user.name,
                        style = typography.subtitle2.copy(fontSize = Dimens.text_body),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = stringResource(
                            id = R.string.user_handle_placeholder,
                            tweet.user.screenName
                        ),
                        style = typography.body2.copy(fontSize = Dimens.text_caption),
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        modifier = Modifier
                            .alpha(USER_HANDLE_OPACITY)
                    )
                }
                Text(
                    text = tweet.createdAt.toPrettyTime(),
                    style = typography.overline,
                    modifier = Modifier.padding(
                        start = Dimens.space_small,
                        top = Dimens.space_small
                    )
                )
            }
            Text(
                text = tweet.content,
                style = typography.body2,
                modifier = Modifier
                    .padding(top = Dimens.space)

            )
        }
    }
}
