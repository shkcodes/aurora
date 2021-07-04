package com.shkcodes.aurora.ui.profile.items

import android.view.View
import coil.ImageLoader
import coil.load
import com.fueled.reclaim.AdapterItem
import com.fueled.reclaim.BaseViewHolder
import com.shkcodes.aurora.R
import com.shkcodes.aurora.cache.entities.MediaEntity
import com.shkcodes.aurora.databinding.ItemGridMediaBinding

class GridMediaItem(
    private val media: MediaEntity,
    private val imageLoader: ImageLoader
) : AdapterItem<GridMediaViewHolder>() {

    override val layoutId = R.layout.item_grid_media

    override fun onCreateViewHolder(view: View) = GridMediaViewHolder(ItemGridMediaBinding.bind(view))

    override fun updateItemViews(viewHolder: GridMediaViewHolder) {
        with(viewHolder.binding.image) {
            load(media.thumbnail, imageLoader) { allowHardware(false) }
        }
    }

    override fun isTheSame(newItem: AdapterItem<*>): Boolean {
        return newItem is GridMediaItem && newItem.media == media
    }
}

class GridMediaViewHolder(val binding: ItemGridMediaBinding) : BaseViewHolder(binding.root)
