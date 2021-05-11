package com.shkcodes.aurora.ui.media

import android.net.Uri
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltNavGraphViewModel
import com.alexvasilkov.gestures.views.GestureImageView
import com.google.accompanist.coil.rememberCoilPainter
import com.google.accompanist.imageloading.ImageLoadState.Loading
import com.google.accompanist.imageloading.ImageLoadState.Success
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.shkcodes.aurora.R
import com.shkcodes.aurora.cache.entities.MediaEntity
import com.shkcodes.aurora.theme.Dimens
import com.shkcodes.aurora.ui.media.MediaViewerContract.Intent.Init
import com.shkcodes.aurora.ui.media.MediaViewerContract.State.Content
import com.shkcodes.aurora.util.inflate
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@Composable
fun MediaViewer(index: Int, tweetId: Long) {
    val viewModel = hiltNavGraphViewModel<MediaViewerViewModel>()
    LaunchedEffect(Unit) {
        viewModel.handleIntent(Init(index, tweetId))
        launch {
            viewModel.getSideEffects().collect { }
        }
    }
    Scaffold {
        when (val state = viewModel.getState().collectAsState().value) {
            is Content -> {
                with(state) {
                    if (media.first().isAnimatedMedia) {
                        VideoPlayer(media.first())
                    } else {
                        ImagesPager(initialIndex = initialIndex, media = media)
                    }
                }
            }
        }
    }
}

@Composable
private fun VideoPlayer(media: MediaEntity) {
    val context = LocalContext.current

    val exoPlayer = remember {
        SimpleExoPlayer.Builder(context).build().apply {
            val dataSourceFactory =
                DefaultDataSourceFactory(context, Util.getUserAgent(context, context.packageName))

            val source = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(Uri.parse(media.url)))

            setMediaSource(source)
            prepare()
            repeatMode = Player.REPEAT_MODE_ALL
            playWhenReady = true
        }
    }

    AndroidView(
        factory = { context ->
            context.inflate<PlayerView>(R.layout.player_view).apply {
                layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                player = exoPlayer
                showController()
            }
        }
    )
}

@OptIn(ExperimentalPagerApi::class)
@Composable
private fun ImagesPager(initialIndex: Int, media: List<MediaEntity>) {
    val pagerState = rememberPagerState(
        initialPage = initialIndex,
        pageCount = media.size
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.BottomCenter
    ) {
        HorizontalPager(state = pagerState) {
            PagerItem(source = media[it].url)
        }
        Text(
            text = "${pagerState.currentPage + 1}/${pagerState.pageCount}",
            color = Color.White,
            modifier = Modifier.padding(Dimens.space)
        )
    }
}

@Composable
private fun PagerItem(source: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        val coilPainter = rememberCoilPainter(request = source)

        when (val state = coilPainter.loadState) {
            is Success -> {
                AndroidView(factory = {
                    GestureImageView(it).apply {
                        layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                        setImageDrawable(state.result)
                    }
                })
            }
            is Loading -> {
                CircularProgressIndicator()
            }
        }
    }
}
