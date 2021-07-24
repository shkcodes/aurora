package com.shkcodes.aurora.ui.create.items

import android.net.Uri
import android.view.View
import coil.ImageLoader
import coil.load
import com.fueled.reclaim.AdapterItem
import com.fueled.reclaim.BaseViewHolder
import com.shkcodes.aurora.R
import com.shkcodes.aurora.databinding.ItemImageAttachmentBinding

class ImageAttachmentAdapterItem(
    private val uri: Uri,
    private val imageLoader: ImageLoader,
    private val removeAction: (Uri) -> Unit
) : AdapterItem<ImageAttachmentViewHolder>() {

    override fun onCreateViewHolder(view: View) =
        ImageAttachmentViewHolder(ItemImageAttachmentBinding.bind(view))

    override val layoutId = R.layout.item_image_attachment

    override fun updateItemViews(viewHolder: ImageAttachmentViewHolder) {
        with(viewHolder.binding) {
            image.load(uri, imageLoader)
            remove.setOnClickListener { removeAction(uri) }
        }
    }

    override fun isTheSame(newItem: AdapterItem<*>): Boolean {
        return newItem is ImageAttachmentAdapterItem && newItem.uri.path == uri.path
    }
}

class ImageAttachmentViewHolder(val binding: ItemImageAttachmentBinding) : BaseViewHolder(binding.root)
