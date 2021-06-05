package com.shkcodes.aurora.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.view.isVisible
import coil.ImageLoader
import coil.load
import com.shkcodes.aurora.R
import com.shkcodes.aurora.cache.entities.MediaEntity
import com.shkcodes.aurora.cache.entities.MediaType
import com.shkcodes.aurora.databinding.TweetMediaBinding
import com.shkcodes.aurora.util.pixelSize
import java.time.Duration

private const val SECONDS_PER_MINUTE = 60

class TweetMedia @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    val binding: TweetMediaBinding

    init {
        val view = inflate(context, R.layout.tweet_media, this)
        binding = TweetMediaBinding.bind(view)
        orientation = VERTICAL
    }

    @Suppress("MagicNumber")
    fun show(
        media: List<MediaEntity>,
        imageLoader: ImageLoader,
        onClick: (ImageView, Int) -> Unit,
        duration: Long? = null
    ) {
        isVisible = media.isNotEmpty()
        with(binding) {
            row1.isVisible = media.isNotEmpty()
            row2.isVisible = media.size > 2

            val isVideo = media.any { it.mediaType == MediaType.VIDEO }
            playIcon.isVisible = isVideo
            gifIndicator.isVisible = media.any { it.mediaType == MediaType.GIF }
            videoDuration.isVisible = isVideo
            videoDuration.text = formatDuration(duration ?: media.firstOrNull()?.duration)

            row1.layoutParams.height =
                context.pixelSize(
                    if (media.size <= 2) {
                        R.dimen.single_row_media_height
                    } else {
                        R.dimen.multi_row_media_height
                    }
                )

            listOf(image1, image2, image3, image4).forEachIndexed { index, imageView ->
                with(imageView) {
                    loadMedia(media.getUrl(index), imageLoader)
                    setOnClickListener {
                        onClick(imageView, index)
                    }
                    transitionName = "${media.getOrNull(index)?.id}"
                }
            }
        }
    }

    private fun formatDuration(millis: Long?): String {
        val duration = Duration.ofMillis(millis ?: 0)
        return "${"%02d".format(duration.toMinutes())}:${"%02d".format((duration.seconds % SECONDS_PER_MINUTE))}"
    }

    private fun ImageView.loadMedia(url: String?, imageLoader: ImageLoader) {
        isVisible = url != null
        load(url, imageLoader) {
            allowHardware(false)
        }
    }

    private fun List<MediaEntity>.getUrl(index: Int): String? {
        return getOrNull(index)?.thumbnail
    }
}
