package com.shkcodes.aurora.ui.profile.items

import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import coil.ImageLoader
import com.fueled.reclaim.AdapterItem
import com.fueled.reclaim.BaseViewHolder
import com.shkcodes.aurora.R
import com.shkcodes.aurora.cache.entities.MediaEntity
import com.shkcodes.aurora.databinding.ItemMediaGridBinding
import com.shkcodes.aurora.ui.profile.ProfileTweetListHandler
import com.shkcodes.aurora.util.GridSpacingItemDecoration
import com.shkcodes.aurora.util.PagedAdapter

class PagerMediaGridItem(
    private val media: List<MediaEntity>,
    private val imageLoader: ImageLoader,
    private val handler: ProfileTweetListHandler
) : AdapterItem<PagerMediaGridViewHolder>() {

    override fun onCreateViewHolder(view: View) =
        PagerMediaGridViewHolder(ItemMediaGridBinding.bind(view)) { handler.loadNextPage() }

    override val layoutId = R.layout.item_media_grid

    override fun updateItemViews(viewHolder: PagerMediaGridViewHolder) {
        val mediaItems = media.map {
            GridMediaItem(it, imageLoader)
        }
        viewHolder.gridAdapter.replaceItems(mediaItems)
    }

    override fun isTheSame(newItem: AdapterItem<*>): Boolean {
        return newItem is PagerMediaGridItem && newItem.media == media
    }
}

class PagerMediaGridViewHolder(
    val binding: ItemMediaGridBinding,
    paginationAction: () -> Unit
) : BaseViewHolder(binding.root) {
    val gridAdapter = PagedAdapter(paginationAction)

    init {
        with(binding.grid) {
            adapter = gridAdapter
            val gridLayoutManager = GridLayoutManager(binding.root.context, 2)
            layoutManager = gridLayoutManager
            val spacing = binding.root.resources.getDimensionPixelSize(R.dimen.space_small)
            addItemDecoration(GridSpacingItemDecoration(spacing))
        }
    }
}
