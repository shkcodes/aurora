package com.shkcodes.aurora.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Movie
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.hilt.navigation.compose.hiltNavGraphViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.navigate
import com.google.accompanist.coil.rememberCoilPainter
import com.shkcodes.aurora.R
import com.shkcodes.aurora.base.SideEffect
import com.shkcodes.aurora.theme.Dimens
import com.shkcodes.aurora.ui.Screen
import com.shkcodes.aurora.ui.common.TerminalError
import com.shkcodes.aurora.ui.profile.ProfileContract.Intent.TweetContentClick
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
import com.shkcodes.aurora.util.lerp
import kotlinx.coroutines.flow.collect
import kotlin.math.roundToInt

private const val TAB_SCRIM_OPACITY = 0.65F

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

    val minHeight =
        with(LocalDensity.current) { (Dimens.banner_height - Dimens.tab_height).toPx() }
    var bannerOffset by remember { mutableStateOf(0f) }
    var animationProgress by remember { mutableStateOf(1F) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                val newOffset = bannerOffset + delta
                bannerOffset = newOffset.coerceIn(-minHeight, 0F)
                animationProgress = (bannerOffset + minHeight) / minHeight
                return Offset.Zero
            }
        }
    }
    Box(
        Modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection)
    ) {
        Box(contentAlignment = Alignment.BottomCenter) {
            TweetList(
                state = TweetListState(
                    state.tweets,
                    autoplayVideos = state.autoplayVideos,
                    isPaginatedError = state.isPaginatedError
                ),
                listState = listState,
                handler = tweetsHandler,
                contentPadding = Dimens.banner_height
            )
            if (state.isPaginatedLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }

        Header(bannerOffset = bannerOffset, animationProgress = animationProgress, state = state)
    }
}

@Composable
private fun Header(state: State, bannerOffset: Float, animationProgress: Float) {
    val bottomClip = with(LocalDensity.current) { Dimens.banner_bottom_clip.toPx() }

    val acrRadius =
        with(LocalDensity.current) { lerp(Dimens.banner_arc_radius, animationProgress).toPx() }
    val arcShape = ArcShape(radius = acrRadius, bottomClip = bottomClip)
    val tabBarOffset = with(LocalDensity.current) { Dimens.tab_bar_offset.toPx() }
    val scrimOffset = with(LocalDensity.current) { Dimens.scrim_offset.toPx() }
    val profileImageSize = lerp(Dimens.profile_image_radius, animationProgress)
    val tabColor =
        lerp(colors.background, colors.background.copy(TAB_SCRIM_OPACITY), animationProgress)
    val profileImageOffset =
        with(LocalDensity.current) { lerp(Dimens.profile_image_offset, animationProgress).toPx() }

    Box(modifier = Modifier
        .fillMaxWidth()
        .offset { IntOffset(x = 0, y = bannerOffset.roundToInt()) }
        .height(Dimens.banner_height)
        .clip(arcShape)
        .background(colors.background),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = rememberCoilPainter(request = state.user!!.profileBannerUrl),
            contentDescription = stringResource(id = R.string.accessibility_user_profile_banner),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        TabBar(scrimOffset = scrimOffset, offset = tabBarOffset, color = tabColor)
        Image(
            painter = rememberCoilPainter(request = state.user.profileImageUrlLarge),
            contentDescription = null,
            modifier = Modifier
                .graphicsLayer { translationY = profileImageOffset }
                .size(profileImageSize)
                .clip(CircleShape)
        )
    }
}

@Composable
private fun TabBar(scrimOffset: Float, offset: Float, color: Color) {
    Box(modifier = Modifier
        .graphicsLayer { translationY = scrimOffset }
        .fillMaxWidth()
        .background(color)
        .height(Dimens.scrim_height))
    Row(
        modifier = Modifier.graphicsLayer { translationY = offset },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Home,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.weight(1F)
        )
        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.weight(1F)
        )
        Spacer(modifier = Modifier.weight(1F))
        Icon(
            imageVector = Icons.Default.Movie,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.weight(1F)
        )
        Icon(
            imageVector = Icons.Default.Inbox,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.weight(1F)
        )
    }
}

@Composable
private fun ArcShape(radius: Float, bottomClip: Float): Shape {
    return GenericShape { size, _ ->
        with(size) {
            moveTo(0F, 0F)
            lineTo(width, 0F)
            lineTo(width, height - bottomClip)
            lineTo(width / 2 + radius, height - bottomClip)
            cubicTo(
                width / 2 + radius,
                height - bottomClip,
                width / 2,
                height + radius - bottomClip,
                width / 2 - radius,
                height - bottomClip
            )
            lineTo(0F, height - bottomClip)
        }
    }
}

private fun tweetListHandler(viewModel: ProfileViewModel): TweetListHandler {
    return object : TweetListHandler {

        override fun annotationClick(annotation: String) {
            viewModel.handleIntent(TweetContentClick(annotation))
        }

        override fun paginatedErrorAction() {
            viewModel.handleIntent(LoadNextPage)
        }

        override fun mediaClick(index: Int, id: Long) {
            viewModel.handleIntent(MediaClick(index, id))
        }

        override fun showProfile(userHandle: String) {
            viewModel.handleIntent(TweetContentClick(userHandle))
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
