package com.shkcodes.aurora.util

import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString

// from https://github.com/android/compose-samples/blob/main/Jetchat/app/src/main/java/com/example/compose/jetchat/conversation/MessageFormatter.kt

// Regex containing the syntax tokens
val symbolPattern by lazy {
    Regex("""(https?://[^\s\t\n]+)|(`[^`]+`)|(@\w+)|(#\w+)|(\*[\w]+\*)|(_[\w]+_)|(~[\w]+~)""")
}

// Accepted annotations for the ClickableTextWrapper
enum class SymbolAnnotationType {
    PERSON, HASHTAG, LINK
}

private fun symbolAnnotationFor(char: Char): SymbolAnnotationType? {
    return when (char) {
        '@' -> SymbolAnnotationType.PERSON
        '#' -> SymbolAnnotationType.HASHTAG
        'h' -> SymbolAnnotationType.LINK
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
    text: String
): AnnotatedString {
    val tokens = symbolPattern.findAll(text)

    return buildAnnotatedString {

        var cursorPosition = 0

        for (token in tokens) {
            append(text.slice(cursorPosition until token.range.first))

            val (annotatedString, stringAnnotation) = getSymbolAnnotation(
                matchResult = token,
                colors = MaterialTheme.colors,
            )
            append(annotatedString)

            if (stringAnnotation != null) {
                val (item, start, end, tag) = stringAnnotation
                addStringAnnotation(tag = tag, start = start, end = end, annotation = item)
            }

            cursorPosition = token.range.last + 1
        }

        if (!tokens.none()) {
            append(text.slice(cursorPosition..text.lastIndex))
        } else {
            append(text)
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
    colors: Colors
): SymbolAnnotation {
    val type = symbolAnnotationFor(matchResult.value.first())
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
    val value = if (type == SymbolAnnotationType.LINK) {
        result.value
    } else {
        result.value.substring(1)
    }
    return SymbolAnnotation(
        AnnotatedString(
            text = result.value,
            spanStyle = SpanStyle(
                color = highlightColor
            )
        ),
        StringAnnotation(
            item = value,
            start = result.range.first,
            end = result.range.last,
            tag = type.name
        )
    )
}
