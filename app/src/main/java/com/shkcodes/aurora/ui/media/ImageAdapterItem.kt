package com.shkcodes.aurora.ui.media

import android.view.View
import coil.ImageLoader
import coil.load
import com.shkcodes.aurora.R
import com.shkcodes.aurora.cache.entities.MediaEntity
import com.shkcodes.aurora.databinding.ItemImageBinding
import com.xwray.groupie.viewbinding.BindableItem

class ImageAdapterItem(
    private val media: MediaEntity,
    private val imageLoader: ImageLoader
) : BindableItem<ItemImageBinding>() {

    override fun initializeViewBinding(view: View): ItemImageBinding {
        return ItemImageBinding.bind(view)
    }

    override fun getLayout() = R.layout.item_image

    override fun bind(binding: ItemImageBinding, position: Int) {
        with(binding.image) {
            load(media.url, imageLoader) { allowHardware(false) }
            transitionName = "${media.id}"
        }
    }
}
