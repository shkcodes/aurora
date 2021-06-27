package com.shkcodes.aurora.ui.profile.items

import android.view.View
import coil.ImageLoader
import com.fueled.reclaim.AdapterItem
import com.fueled.reclaim.BaseViewHolder
import com.shkcodes.aurora.R
import com.shkcodes.aurora.databinding.ItemTweetListBinding
import com.shkcodes.aurora.ui.profile.ProfileTweetListHandler
import com.shkcodes.aurora.ui.timeline.UrlMetadataHandler
import com.shkcodes.aurora.ui.timeline.items.PaginatedErrorItem
import com.shkcodes.aurora.ui.timeline.items.TweetAdapterItem
import com.shkcodes.aurora.ui.tweetlist.TweetItems
import com.shkcodes.aurora.util.PagedAdapter
import com.shkcodes.aurora.util.annotatedContent

class PagerTweetListItem(
    private val tweets: TweetItems,
    private val handler: ProfileTweetListHandler,
    private val urlMetadataHandler: UrlMetadataHandler,
    private val imageLoader: ImageLoader,
    private val isPaginatedError: Boolean
) : AdapterItem<PagerListViewHolder>() {

    override fun onCreateViewHolder(view: View) =
        PagerListViewHolder(ItemTweetListBinding.bind(view)) { handler.loadNextPage() }

    override val layoutId = R.layout.item_tweet_list

    override fun updateItemViews(viewHolder: PagerListViewHolder) {
        with(viewHolder.binding) {
            val context = root.context
            val tweetItems = tweets.map { tweetItem ->
                val annotatedContent =
                    tweetItem.annotatedContent(context) { handler.onAnnotationClick(it) }
                TweetAdapterItem(
                    annotatedContent,
                    tweetItem,
                    urlMetadataHandler,
                    imageLoader,
                    handler,
                    list.layoutManager
                )
            }
            val items = mutableListOf<AdapterItem<*>>().apply {
                addAll(tweetItems)
                if (isPaginatedError) {
                    add(PaginatedErrorItem { handler.loadNextPage() })
                }
            }
            viewHolder.adapter.replaceItems(items)
            val state = handler.getState(viewHolder.adapterPosition)
            if (state != null) {
                list.layoutManager?.onRestoreInstanceState(state)
                handler.saveState(null)
            }
        }
    }

    override fun isTheSame(newItem: AdapterItem<*>): Boolean {
        return newItem is PagerTweetListItem && newItem.tweets == tweets && newItem.isPaginatedError == isPaginatedError
    }
}

class PagerListViewHolder(val binding: ItemTweetListBinding, paginationAction: () -> Unit) :
    BaseViewHolder(binding.root) {
    val adapter = PagedAdapter(paginationAction)

    init {
        binding.list.adapter = adapter
    }
}
