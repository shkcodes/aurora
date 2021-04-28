package com.shkcodes.aurora.util

import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import com.shkcodes.aurora.api.response.Url

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
    urls: List<Url>,
    hashtags: List<String>
): AnnotatedString {
    val cleanedText = text.replace("&amp;", "&")
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
