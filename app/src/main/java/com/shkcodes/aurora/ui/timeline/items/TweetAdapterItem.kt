package com.shkcodes.aurora.ui.timeline.items

import android.graphics.Color
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.core.view.isVisible
import coil.ImageLoader
import coil.load
import coil.transform.CircleCropTransformation
import com.shkcodes.aurora.R
import com.shkcodes.aurora.databinding.ItemTweetBinding
import com.shkcodes.aurora.databinding.LayoutTweetSkeletonBinding
import com.shkcodes.aurora.ui.tweetlist.TweetItem
import com.shkcodes.aurora.util.setSize
import com.shkcodes.aurora.util.toPrettyTime
import com.xwray.groupie.Item
import com.xwray.groupie.viewbinding.BindableItem

class TweetAdapterItem(
    private val tweetContent: SpannableStringBuilder,
    private val quoteTweetContent: SpannableStringBuilder?,
    private val tweetItem: TweetItem,
    private val imageLoader: ImageLoader
) : BindableItem<ItemTweetBinding>() {

    private val tweet = tweetItem.tweet
    private val quoteTweet = tweetItem.quoteTweet
    private val media = tweetItem.tweetMedia
    private val quoteTweetMedia = tweetItem.quoteTweetMedia

    override fun initializeViewBinding(view: View): ItemTweetBinding {
        return ItemTweetBinding.bind(view)
    }

    override fun getLayout() = R.layout.item_tweet

    override fun bind(binding: ItemTweetBinding, position: Int) {
        with(binding) {
            val context = root.context
            with(primaryTweet) {
                with(content) {
                    text = tweetContent
                    movementMethod = LinkMovementMethod.getInstance()
                    highlightColor = Color.TRANSPARENT
                }
                content.isVisible = tweet.content.isNotEmpty()
                profilePic.load(tweet.userProfileImageUrl, imageLoader) {
                    transformations(CircleCropTransformation())
                }
                userName.text = tweet.userName
                userHandle.text =
                    context.getString(R.string.user_handle_placeholder, tweet.userHandle)
                time.text = tweet.createdAt.toPrettyTime()
                tweetMedia.show(media, imageLoader)
            }
            retweetIndicator.isVisible = tweetItem.isRetweet
            retweeter.isVisible = tweetItem.isRetweet
            retweeter.text =
                context.getString(R.string.retweet_indicator_placeholder, tweetItem.retweeter)
            quoteTweetCard.isVisible = tweetItem.quoteTweet != null
            renderQuoteTweet(binding.quoteTweet)
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
            with(content) {
                text = quoteTweetContent
                movementMethod = LinkMovementMethod.getInstance()
                highlightColor = Color.TRANSPARENT
            }
            tweetMedia.show(quoteTweetMedia, imageLoader)
        }
    }

    override fun isSameAs(other: Item<*>): Boolean {
        return other is TweetAdapterItem && other.tweetItem.tweetId == tweetItem.tweetId
    }

    override fun hasSameContentAs(other: Item<*>): Boolean {
        return (other as TweetAdapterItem).tweetItem == tweetItem
    }
}