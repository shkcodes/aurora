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
import com.shkcodes.aurora.databinding.TweetMediaBinding

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
    fun show(media: List<MediaEntity>, imageLoader: ImageLoader) {
        isVisible = media.isNotEmpty()
        with(binding) {
            row1.isVisible = media.isNotEmpty()
            row2.isVisible = media.size > 2

            image2.isVisible = media.size > 1
            image4.isVisible = media.size == 4

            row1.layoutParams.height =
                resources.getDimensionPixelSize(
                    if (media.size <= 2) {
                        R.dimen.single_row_media_height
                    } else {
                        R.dimen.multi_row_media_height
                    }
                )

            image1.loadMedia(media.getUrl(0), imageLoader)
            image2.loadMedia(media.getUrl(1), imageLoader)
            image3.loadMedia(media.getUrl(2), imageLoader)
            image4.loadMedia(media.getUrl(3), imageLoader)
        }
    }

    private fun ImageView.loadMedia(url: String?, imageLoader: ImageLoader) {
        isVisible = url != null
        load(url, imageLoader)
    }

    private fun List<MediaEntity>.getUrl(index: Int): String? {
        return getOrNull(index)?.thumbnail
    }
}
