package com.shkcodes.aurora.ui.profile.items

import android.view.View
import coil.ImageLoader
import com.shkcodes.aurora.R
import com.shkcodes.aurora.databinding.ItemTweetListBinding
import com.shkcodes.aurora.ui.profile.ProfileTweetListHandler
import com.shkcodes.aurora.ui.timeline.UrlMetadataHandler
import com.shkcodes.aurora.ui.timeline.items.PaginatedErrorItem
import com.shkcodes.aurora.ui.timeline.items.TweetAdapterItem
import com.shkcodes.aurora.ui.tweetlist.TweetItems
import com.shkcodes.aurora.util.PagedAdapter
import com.shkcodes.aurora.util.annotatedContent
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import com.xwray.groupie.viewbinding.BindableItem

class PagerTweetListItem(
    private val tweets: TweetItems,
    private val handler: ProfileTweetListHandler,
    private val urlMetadataHandler: UrlMetadataHandler,
    private val imageLoader: ImageLoader,
    private val isPaginatedError: Boolean
) : Item<PagerListViewHolder>() {

    override fun createViewHolder(view: View) = PagerListViewHolder(view) { handler.loadNextPage() }

    override fun getLayout() = R.layout.item_tweet_list

    override fun bind(viewHolder: PagerListViewHolder, position: Int) {
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
            val items = mutableListOf<BindableItem<*>>().apply {
                addAll(tweetItems)
                if (isPaginatedError) {
                    add(PaginatedErrorItem { handler.loadNextPage() })
                }
            }
            viewHolder.adapter.update(items)
            val state = handler.getState(position)
            if (state != null) {
                list.layoutManager?.onRestoreInstanceState(state)
                handler.saveState(null)
            }
        }
    }

    override fun onViewDetachedFromWindow(viewHolder: PagerListViewHolder) {
        super.onViewDetachedFromWindow(viewHolder)
        handler.saveState(viewHolder.binding.list.layoutManager?.onSaveInstanceState())
    }

    override fun isSameAs(other: Item<*>): Boolean {
        return other is PagerTweetListItem && other.tweets == tweets && other.isPaginatedError == isPaginatedError
    }

    override fun hasSameContentAs(other: Item<*>): Boolean {
        return isSameAs(other)
    }
}

class PagerListViewHolder(view: View, paginationAction: () -> Unit) : GroupieViewHolder(view) {
    val binding = ItemTweetListBinding.bind(view)
    val adapter = PagedAdapter(paginationAction)

    init {
        binding.list.adapter = adapter
    }
}
