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
import com.shkcodes.aurora.ui.tweetlist.TweetItem
import com.shkcodes.aurora.util.toPrettyTime
import com.xwray.groupie.Item
import com.xwray.groupie.viewbinding.BindableItem

class TweetAdapterItem(
    private val tweetContent: SpannableStringBuilder,
    private val tweetItem: TweetItem,
    private val imageLoader: ImageLoader
) : BindableItem<ItemTweetBinding>() {

    val tweet = tweetItem.tweet
    val quoteTweet = tweetItem.quoteTweet
    val media = tweetItem.tweetMedia
    val quoteTweetMedia = tweetItem.quoteTweetMedia

    override fun initializeViewBinding(view: View): ItemTweetBinding {
        return ItemTweetBinding.bind(view)
    }

    override fun getLayout() = R.layout.item_tweet

    override fun bind(binding: ItemTweetBinding, position: Int) {
        with(binding) {
            val context = root.context
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
            retweetIndicator.isVisible = tweetItem.isRetweet
            retweeter.isVisible = tweetItem.isRetweet
            retweeter.text =
                context.getString(R.string.retweet_indicator_placeholder, tweetItem.retweeter)
        }
    }

    override fun isSameAs(other: Item<*>): Boolean {
        return other is TweetAdapterItem && other.tweetItem.tweetId == tweetItem.tweetId
    }

    override fun hasSameContentAs(other: Item<*>): Boolean {
        return (other as TweetAdapterItem).tweetItem == tweetItem
    }
}
