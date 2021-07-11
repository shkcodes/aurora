package com.shkcodes.aurora.ui.media

import android.view.View
import coil.ImageLoader
import coil.load
import com.fueled.reclaim.AdapterItem
import com.fueled.reclaim.BaseViewHolder
import com.shkcodes.aurora.R
import com.shkcodes.aurora.cache.entities.MediaEntity
import com.shkcodes.aurora.databinding.ItemImageBinding

class ImageAdapterItem(
    private val media: MediaEntity,
    private val imageLoader: ImageLoader,
    private val onImageLoaded: () -> Unit = {},
    private val isGridItem: Boolean = false
) : AdapterItem<ImageItemViewHolder>() {

    override fun onCreateViewHolder(view: View) = ImageItemViewHolder(ItemImageBinding.bind(view))

    override val layoutId = R.layout.item_image

    override fun updateItemViews(viewHolder: ImageItemViewHolder) {
        val postFix = if (isGridItem) "_grid" else ""
        with(viewHolder.binding.image) {
            load(media.thumbnail, imageLoader) {
                listener(onSuccess = { _, _ -> onImageLoaded() },
                    onError = { _, _ -> onImageLoaded() })
                allowHardware(false)
            }
            transitionName = "${media.id}$postFix"
        }
    }

    override fun isTheSame(newItem: AdapterItem<*>): Boolean {
        return newItem is ImageAdapterItem && newItem.media == media
    }
}

class ImageItemViewHolder(val binding: ItemImageBinding) : BaseViewHolder(binding.root)
