package com.shkcodes.aurora.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltNavGraphViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.navigate
import coil.transform.CircleCropTransformation
import com.google.accompanist.coil.rememberCoilPainter
import com.shkcodes.aurora.R
import com.shkcodes.aurora.api.response.User
import com.shkcodes.aurora.base.SideEffect
import com.shkcodes.aurora.theme.Dimens
import com.shkcodes.aurora.ui.Screen
import com.shkcodes.aurora.ui.common.TerminalError
import com.shkcodes.aurora.ui.profile.ProfileContract.Intent.HandleAnnotationClick
import com.shkcodes.aurora.ui.profile.ProfileContract.Intent.Init
import com.shkcodes.aurora.ui.profile.ProfileContract.Intent.LoadNextPage
import com.shkcodes.aurora.ui.profile.ProfileContract.Intent.MediaClick
import com.shkcodes.aurora.ui.profile.ProfileContract.Intent.Retry
import com.shkcodes.aurora.ui.profile.ProfileContract.ProfileSideEffect.OpenUrl
import com.shkcodes.aurora.ui.profile.ProfileContract.Screen.MediaViewer
import com.shkcodes.aurora.ui.profile.ProfileContract.Screen.UserProfile
import com.shkcodes.aurora.ui.profile.ProfileContract.State
import com.shkcodes.aurora.ui.tweetlist.TweetList
import com.shkcodes.aurora.ui.tweetlist.TweetListHandler
import com.shkcodes.aurora.ui.tweetlist.TweetListState
import com.shkcodes.aurora.util.isAtTheBottom
import kotlinx.coroutines.flow.collect

@Composable
fun ProfileScreen(userHandle: String, navController: NavController) {
    val viewModel = hiltNavGraphViewModel<ProfileViewModel>()
    val state = viewModel.composableState()

    val uriHandler = LocalUriHandler.current

    LaunchedEffect(Unit) {
        viewModel.handleIntent(Init(userHandle))
        viewModel.getSideEffects().collect {
            when (it) {
                is SideEffect.Action<*> -> {
                    when (val action = it.action) {
                        is OpenUrl -> {
                            uriHandler.openUri(action.url)
                        }
                    }
                }
                is SideEffect.DisplayScreen<*> -> handleNavigation(it, navController)
            }
        }
    }

    when {
        state.isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        state.user != null -> {
            Content(state, viewModel)
        }
        state.isTerminalError -> {
            TerminalError(message = state.errorMessage) {
                viewModel.handleIntent(Retry(userHandle))
            }
        }
    }
}

@Composable
private fun Content(state: State, viewModel: ProfileViewModel) {
    val listState = rememberLazyListState()
    val tweetsHandler = remember { tweetListHandler(viewModel) }
    val shouldLoadMore =
        listState.isAtTheBottom && !state.isPaginatedError && state.tweets.isNotEmpty()

    if (shouldLoadMore) {
        viewModel.handleIntent(LoadNextPage)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        with(state) {
            Header(user!!)
            Box(
                contentAlignment = Alignment.BottomCenter,
                modifier = Modifier.padding(top = Dimens.banner_height)
            ) {
                TweetList(
                    state = TweetListState(
                        tweets,
                        autoplayVideos = state.autoplayVideos,
                        isPaginatedError = state.isPaginatedError
                    ),
                    listState = listState,
                    handler = tweetsHandler,
                )
                if (state.isPaginatedLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}

private fun tweetListHandler(viewModel: ProfileViewModel): TweetListHandler {
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

@Composable
private fun Header(user: User) {
    Box(
        modifier = Modifier
            .height(Dimens.banner_height)
            .fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Image(
            painter = rememberCoilPainter(request = user.profileBannerUrl),
            contentDescription = stringResource(id = R.string.accessibility_user_profile_banner),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Image(
            painter = rememberCoilPainter(request = user.profileImageUrlLarge,
                requestBuilder = {
                    transformations(CircleCropTransformation())
                }),
            contentDescription = stringResource(id = R.string.accessibility_user_profile_image),
            modifier = Modifier.size(Dimens.profile_pic_size)
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
