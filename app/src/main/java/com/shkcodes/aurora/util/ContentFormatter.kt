@file:Suppress("TooManyFunctions")

package com.shkcodes.aurora.util

import android.content.Context
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View
import com.shkcodes.aurora.R
import com.shkcodes.aurora.api.response.Url
import com.shkcodes.aurora.api.response.User
import com.shkcodes.aurora.cache.entities.TweetEntity
import com.shkcodes.aurora.ui.tweetlist.TweetItem
import org.jsoup.Jsoup

// Regex containing the syntax tokens
val symbolPattern by lazy {
    Regex("""(https?://[^\s\t\n]+)|(@\w+)|(#\w+)|(\*[\w]+\*)|(_[\w]+_)|(~[\w]+~)""")
}

class ColoredClickableSpan(
    private val color: Int,
    private val handler: () -> Unit
) : ClickableSpan() {

    override fun onClick(widget: View) {
        handler()
    }

    override fun updateDrawState(ds: TextPaint) {
        ds.color = color
    }
}

data class AnnotatedContent(
    val primaryContent: SpannableStringBuilder,
    val quotedContent: SpannableStringBuilder?,
    val repliedUsers: SpannableStringBuilder
)

fun User.annotatedLink(context: Context, handler: (String) -> Unit): SpannableStringBuilder? {
    return url?.let {
        val span = ColoredClickableSpan(context.getColor(R.color.colorPrimary)) { handler(it.second) }
        SpannableStringBuilder().apply {
            append(it.first)
            setSpan(span, 0, it.first.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }
}

fun User.annotatedDescription(context: Context, handler: (String) -> Unit): SpannableStringBuilder {
    return context.contentFormatter(description, descriptionUrls, handler = handler)
}

fun TweetItem.annotatedContent(context: Context, handler: (String) -> Unit): AnnotatedContent {
    val primaryContent =
        tweet.formattedContent(context, handler)
    val quoteContent =
        quoteTweet?.formattedContent(context, handler)
    val repliedUsers =
        tweet.repliedUsers(context, handler)
    return AnnotatedContent(primaryContent, quoteContent, repliedUsers)
}

private fun TweetEntity.formattedContent(
    context: Context,
    handler: (String) -> Unit
): SpannableStringBuilder {
    return context.contentFormatter(content, sharedUrls, hashtags, handler)
}

private fun TweetEntity.repliedUsers(
    context: Context,
    handler: (String) -> Unit
): SpannableStringBuilder {
    return context.contentFormatter(
        context.getString(
            R.string.users_replied_placeholder,
            getRepliedUsers(context, repliedToUsers)
        ), handler = handler
    )
}

private fun getRepliedUsers(context: Context, users: List<String>): String {
    return with(context) {
        when (users.size) {
            0 -> ""
            1 -> getString(R.string.single_replied_placeholder, users.single())
            2 -> getString(
                R.string.two_users_replied_placeholder,
                users.first(),
                users.last()
            )
            else -> getString(
                R.string.multiple_users_replied_placeholder,
                users[0],
                users[1],
                users.size - 2
            )
        }
    }
}

private fun Context.contentFormatter(
    text: String,
    urls: List<Url> = emptyList(),
    hashtags: List<String> = emptyList(),
    handler: (String) -> Unit
): SpannableStringBuilder {
    val cleanedText = Jsoup.parse(text).wholeText()
    val tokens = symbolPattern.findAll(cleanedText)
    var cursorPosition = 0
    val output = SpannableStringBuilder("")
    for (token in tokens) {
        output.append(cleanedText.slice(cursorPosition until token.range.first))

        val result = token.value

        val (isValidSpan, clickableContent, displayableContent) = when (result.first()) {
            '#' -> {
                Triple(hashtags.contains(result.substring(1)), result, result)
            }
            'h' -> {
                val relevantUrl = urls.first { result.contains(it.shortenedUrl) }
                Triple(true, relevantUrl.url, relevantUrl.displayUrl)
            }
            else -> {
                Triple(true, result, result)
            }
        }
        if (isValidSpan) {
            val clickableSpan = ColoredClickableSpan(getColor(R.color.colorPrimary)) {
                handler(clickableContent)
            }
            output.withSpan(clickableSpan) { append(displayableContent) }
        } else {
            output.append(result)
        }
        cursorPosition = token.range.last + 1
    }
    output.append(cleanedText.slice(cursorPosition..cleanedText.lastIndex))
    return output
}

private inline fun SpannableStringBuilder.withSpan(
    span: Any,
    action: SpannableStringBuilder.() -> Unit
): SpannableStringBuilder {
    val from = length
    action()
    setSpan(span, from, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    return this
}
