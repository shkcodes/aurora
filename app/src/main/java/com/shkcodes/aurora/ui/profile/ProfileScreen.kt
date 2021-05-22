package com.shkcodes.aurora.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltNavGraphViewModel
import coil.transform.CircleCropTransformation
import com.google.accompanist.coil.rememberCoilPainter
import com.shkcodes.aurora.R
import com.shkcodes.aurora.api.response.User
import com.shkcodes.aurora.theme.Dimens
import com.shkcodes.aurora.ui.common.TerminalError
import com.shkcodes.aurora.ui.profile.ProfileContract.Intent.Init
import com.shkcodes.aurora.ui.profile.ProfileContract.Intent.Retry
import com.shkcodes.aurora.ui.profile.ProfileContract.State

@Composable
fun ProfileScreen(userHandle: String) {
    val viewModel = hiltNavGraphViewModel<ProfileViewModel>()
    val state = viewModel.composableState()

    LaunchedEffect(Unit) {
        viewModel.handleIntent(Init(userHandle))
    }

    when {
        state.isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        state.user != null -> {
            Content(state)
        }
        state.isTerminalError -> {
            TerminalError(message = state.errorMessage) {
                viewModel.handleIntent(Retry(userHandle))
            }
        }
    }
}

@Composable
private fun Content(state: State) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Header(state.user!!)
        }
        items(state.items) {
            Text(text = it.tweet.content, modifier = Modifier.padding(Dimens.keyline_1))
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
            contentScale = ContentScale.FillBounds
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
