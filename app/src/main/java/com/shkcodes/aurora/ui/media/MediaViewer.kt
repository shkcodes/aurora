package com.shkcodes.aurora.ui.media

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltNavGraphViewModel
import com.alexvasilkov.gestures.views.GestureImageView
import com.google.accompanist.coil.rememberCoilPainter
import com.google.accompanist.imageloading.ImageLoadState.Loading
import com.google.accompanist.imageloading.ImageLoadState.Success
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.shkcodes.aurora.theme.Dimens
import com.shkcodes.aurora.ui.media.MediaViewerContract.Intent.Init
import com.shkcodes.aurora.ui.media.MediaViewerContract.State.Content
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class)
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
                val pagerState = rememberPagerState(
                    initialPage = state.initialIndex,
                    pageCount = state.media.size
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    HorizontalPager(state = pagerState) {
                        PagerItem(source = state.media[it].url)
                    }
                    Text(
                        text = "${pagerState.currentPage + 1}/${pagerState.pageCount}",
                        color = Color.White,
                        modifier = Modifier.padding(Dimens.space)
                    )
                }
            }
        }
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
