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
    private val imageLoader: ImageLoader
) : AdapterItem<ImageItemViewHolder>() {

    override fun onCreateViewHolder(view: View) = ImageItemViewHolder(ItemImageBinding.bind(view))

    override val layoutId = R.layout.item_image

    override fun updateItemViews(viewHolder: ImageItemViewHolder) {
        with(viewHolder.binding.image) {
            load(media.url, imageLoader) { allowHardware(false) }
            transitionName = "${media.id}"
        }
    }

    override fun isTheSame(newItem: AdapterItem<*>): Boolean {
        return newItem is ImageAdapterItem && newItem.media == media
    }
}

class ImageItemViewHolder(val binding: ItemImageBinding) : BaseViewHolder(binding.root)
