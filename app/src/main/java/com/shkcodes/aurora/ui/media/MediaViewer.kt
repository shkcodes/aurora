package com.shkcodes.aurora.ui.media

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.IntOffset
import androidx.hilt.navigation.compose.hiltNavGraphViewModel
import com.google.accompanist.coil.rememberCoilPainter
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.shkcodes.aurora.theme.Dimens
import com.shkcodes.aurora.ui.media.MediaViewerContract.Intent.Init
import com.shkcodes.aurora.ui.media.MediaViewerContract.State.Content
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

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
                        ZoomableImage(
                            source = state.media[it].imageUrl,
                            modifier = Modifier.fillMaxSize()
                        )
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
private fun ZoomableImage(source: String, modifier: Modifier = Modifier) {
    Box(modifier = Modifier.fillMaxSize()) {
        var angle by remember { mutableStateOf(0F) }
        var zoom by remember { mutableStateOf(1F) }
        var offsetX by remember { mutableStateOf(0F) }
        var offsetY by remember { mutableStateOf(0F) }

        Image(
            painter = rememberCoilPainter(
                request = source,
                fadeIn = true,
            ),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = modifier
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .graphicsLayer(
                    scaleX = zoom,
                    scaleY = zoom,
                    rotationZ = angle
                )
                .pointerInput(Unit) {
                    detectTransformGestures(
                        onGesture = { _, pan, gestureZoom, gestureRotate ->
                            angle += gestureRotate
                            zoom *= gestureZoom
                            val x = pan.x * zoom
                            val y = pan.y * zoom
                            offsetX += (x * cos(angle.radians) - y * sin(angle.radians)).toFloat()
                            offsetY += (x * sin(angle.radians) + y * cos(angle.radians)).toFloat()
                        }
                    )
                }
        )
    }
}

private val Float.radians: Double
    get() = Math.toRadians(toDouble())
