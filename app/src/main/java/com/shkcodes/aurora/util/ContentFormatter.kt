package com.shkcodes.aurora.util

import android.content.Context
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import com.shkcodes.aurora.R
import com.shkcodes.aurora.api.response.Url
import com.shkcodes.aurora.cache.entities.TweetEntity
import com.shkcodes.aurora.ui.tweetlist.TweetItem
import org.jsoup.Jsoup

// from https://github.com/android/compose-samples/blob/main/Jetchat/app/src/main/java/com/example/compose/jetchat/conversation/MessageFormatter.kt

// Regex containing the syntax tokens
val symbolPattern by lazy {
    Regex("""(https?://[^\s\t\n]+)|(@\w+)|(#\w+)|(\*[\w]+\*)|(_[\w]+_)|(~[\w]+~)""")
}

// Accepted annotations for the ClickableTextWrapper
sealed class SymbolAnnotationType {
    abstract val data: String

    data class Person(override val data: String) : SymbolAnnotationType()
    data class HashTag(override val data: String) : SymbolAnnotationType()
    data class Link(val displayableUrl: String, override val data: String) : SymbolAnnotationType()
}

private fun symbolAnnotationFor(
    result: String,
    urls: List<Url>,
    hashtags: List<String>
): SymbolAnnotationType? {
    return when (result.first()) {
        '@' -> SymbolAnnotationType.Person(result)
        '#' -> {
            if (hashtags.contains(result.substring(1))) {
                SymbolAnnotationType.HashTag(result)
            } else null
        }
        'h' -> {
            val relevantUrl = urls.first { result.contains(it.shortenedUrl) }
            SymbolAnnotationType.Link(relevantUrl.displayUrl, relevantUrl.url)
        }
        else -> null
    }
}

typealias StringAnnotation = AnnotatedString.Range<String>
// Pair returning styled content and annotation for ClickableText when matching syntax token
typealias SymbolAnnotation = Pair<AnnotatedString, StringAnnotation?>

/**
 * Format a tweet following Markdown-lite syntax
 * | @username -> bold, primary color and clickable element
 * | http(s)://... -> clickable link, opening it into the browser
 *
 * @param text contains tweet content to be parsed
 * @return AnnotatedString with annotations used inside the ClickableText wrapper
 */
@Composable
fun contentFormatter(
    text: String,
    urls: List<Url> = emptyList(),
    hashtags: List<String> = emptyList()
): AnnotatedString {
    val cleanedText = Jsoup.parse(text).wholeText()
    val tokens = symbolPattern.findAll(cleanedText)

    return buildAnnotatedString {

        var cursorPosition = 0

        for (token in tokens) {
            append(cleanedText.slice(cursorPosition until token.range.first))

            val (annotatedString, stringAnnotation) = getSymbolAnnotation(
                matchResult = token,
                colors = MaterialTheme.colors,
                urls = urls,
                hashtags = hashtags
            )
            append(annotatedString)

            if (stringAnnotation != null) {
                val (item, start, end, tag) = stringAnnotation
                addStringAnnotation(tag = tag, start = start, end = end, annotation = item)
            }

            cursorPosition = token.range.last + 1
        }

        if (!tokens.none()) {
            append(cleanedText.slice(cursorPosition..cleanedText.lastIndex))
        } else {
            append(cleanedText)
        }
    }
}

/**
 * Map regex matches found in a tweet with supported syntax symbols
 *
 * @param matchResult is a regex result matching our syntax symbols
 * @return pair of AnnotatedString with annotation (optional) used inside the ClickableText wrapper
 */
private fun getSymbolAnnotation(
    matchResult: MatchResult,
    urls: List<Url>,
    hashtags: List<String>,
    colors: Colors
): SymbolAnnotation {
    val type = symbolAnnotationFor(matchResult.value, urls, hashtags)
    return if (type != null) {
        annotationForType(type, matchResult, colors.primary)
    } else {
        SymbolAnnotation(AnnotatedString(matchResult.value), null)
    }
}

private fun annotationForType(
    type: SymbolAnnotationType,
    result: MatchResult,
    highlightColor: Color
): SymbolAnnotation {
    val value = if (type is SymbolAnnotationType.Link) {
        type.displayableUrl
    } else {
        result.value
    }
    return SymbolAnnotation(
        AnnotatedString(
            text = value,
            spanStyle = SpanStyle(
                color = highlightColor
            )
        ),
        StringAnnotation(
            item = value,
            start = result.range.first,
            end = result.range.last,
            tag = type.data
        )
    )
}

private class TweetClickableSpan(
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
    return context.contentFormatter2(content, sharedUrls, hashtags, handler)
}

private fun TweetEntity.repliedUsers(
    context: Context,
    handler: (String) -> Unit
): SpannableStringBuilder {
    return context.contentFormatter2(
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

private fun Context.contentFormatter2(
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
            val clickableSpan = TweetClickableSpan(getColor(R.color.colorPrimary)) {
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
