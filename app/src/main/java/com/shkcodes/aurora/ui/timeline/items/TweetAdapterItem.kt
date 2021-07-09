package com.shkcodes.aurora.ui.timeline.items

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import coil.ImageLoader
import coil.load
import coil.transform.CircleCropTransformation
import com.fueled.reclaim.AdapterItem
import com.fueled.reclaim.BaseViewHolder
import com.shkcodes.aurora.R
import com.shkcodes.aurora.databinding.ItemTweetBinding
import com.shkcodes.aurora.databinding.LayoutTweetSkeletonBinding
import com.shkcodes.aurora.service.SharedElementTransitionHelper
import com.shkcodes.aurora.ui.timeline.TweetListHandler
import com.shkcodes.aurora.ui.timeline.UrlMetadataHandler
import com.shkcodes.aurora.ui.tweetlist.TweetItem
import com.shkcodes.aurora.util.AnnotatedContent
import com.shkcodes.aurora.util.handleClickableSpans
import com.shkcodes.aurora.util.setSize
import com.shkcodes.aurora.util.toPrettyTime

class TweetAdapterItem(
    private val annotatedContent: AnnotatedContent,
    private val tweetItem: TweetItem,
    private val urlMetadataHandler: UrlMetadataHandler,
    private val imageLoader: ImageLoader,
    private val handler: TweetListHandler,
    private val animationHelper: SharedElementTransitionHelper,
    private val layoutManager: LayoutManager? = null
) : AdapterItem<TweetItemViewHolder>() {

    private val tweet = tweetItem.tweet
    private val quoteTweet = tweetItem.quoteTweet
    private val media = tweetItem.tweetMedia
    private val quoteTweetMedia = tweetItem.quoteTweetMedia

    override fun onCreateViewHolder(view: View) = TweetItemViewHolder(ItemTweetBinding.bind(view))

    override val layoutId = R.layout.item_tweet

    override fun updateItemViews(viewHolder: TweetItemViewHolder) {
        with(viewHolder.binding) {
            val context = root.context
            with(primaryTweet) {
                content.text = annotatedContent.primaryContent
                content.handleClickableSpans()
                content.isVisible = tweet.content.isNotEmpty()
                profilePic.load(tweet.userProfileImageUrl, imageLoader) {
                    transformations(CircleCropTransformation())
                }
                userName.text = tweet.userName
                userHandle.text =
                    context.getString(R.string.user_handle_placeholder, tweet.userHandle)
                time.text = tweet.createdAt.toPrettyTime()
                tweetMedia.show(media, imageLoader, { imageView, index ->
                    handler.saveState(layoutManager?.onSaveInstanceState())
                    animationHelper.tweetIndex = positionInAdapter
                    animationHelper.mediaIndex = index
                    handler.onMediaClick(media[index], index, imageView)
                })
                repliedUsers.isVisible = tweet.repliedToUsers.isNotEmpty()
                repliedUsers.text = annotatedContent.repliedUsers
                repliedUsers.handleClickableSpans()
            }
            retweetIndicator.isVisible = tweetItem.isRetweet
            retweeter.isVisible = tweetItem.isRetweet
            retweeter.text =
                context.getString(R.string.retweet_indicator_placeholder, tweetItem.retweeter)
            quoteTweetCard.isVisible = tweetItem.quoteTweet != null
            with(linkPreview) {
                root.isVisible =
                    tweet.sharedUrls.isNotEmpty() && media.isEmpty() && tweetItem.quoteTweet == null
                val url = tweet.sharedUrls.firstOrNull()?.url
                if (url != null) {
                    root.tag = url
                    urlMetadataHandler.get(this, url)
                    root.setOnClickListener {
                        handler.onAnnotationClick(url)
                    }
                }
                renderQuoteTweet(quoteTweet)
            }
            setupClickHandling(this)
        }
    }

    private fun renderQuoteTweet(binding: LayoutTweetSkeletonBinding) {
        with(binding) {
            val context = root.context
            profilePic.setSize(R.dimen.quote_tweet_profile_pic)
            profilePic.load(quoteTweet?.userProfileImageUrl, imageLoader) {
                transformations(CircleCropTransformation())
            }
            userName.text = quoteTweet?.userName
            userHandle.text =
                context.getString(R.string.user_handle_placeholder, quoteTweet?.userHandle)
            content.text = annotatedContent.quotedContent
            content.handleClickableSpans()
            tweetMedia.show(quoteTweetMedia, imageLoader, { imageView, index ->
                handler.saveState(layoutManager?.onSaveInstanceState())
                handler.onMediaClick(quoteTweetMedia[index], index, imageView)
            })
        }
    }

    private fun setupClickHandling(binding: ItemTweetBinding) {
        val context = binding.root.context
        val primaryTweetUser = context.getString(R.string.user_handle_placeholder, tweet.userHandle).trim()
        val quoteTweetUser =
            context.getString(R.string.user_handle_placeholder, quoteTweet?.userHandle).trim()
        with(binding.primaryTweet) {
            userInfo.setOnClickListener { handler.onProfileClick(primaryTweetUser) }
            profilePic.setOnClickListener { handler.onProfileClick(primaryTweetUser) }
        }
        with(binding.quoteTweet) {
            userInfo.setOnClickListener { handler.onProfileClick(quoteTweetUser) }
            profilePic.setOnClickListener { handler.onProfileClick(quoteTweetUser) }
        }
    }

    override fun isTheSame(newItem: AdapterItem<*>): Boolean {
        return newItem is TweetAdapterItem && newItem.tweetItem.tweetId == tweetItem.tweetId
    }

    override fun isContentsTheSame(newItem: AdapterItem<*>): Boolean {
        return (newItem as TweetAdapterItem).tweetItem == tweetItem
    }
}

class TweetItemViewHolder(val binding: ItemTweetBinding) : BaseViewHolder(binding.root)
