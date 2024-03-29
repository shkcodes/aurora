package com.shkcodes.aurora.ui.tweetlist

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.google.accompanist.coil.rememberCoilPainter
import com.shkcodes.aurora.theme.Dimens
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import timber.log.Timber
import java.net.URL

private const val IMAGE_WEIGHT = 0.2F

data class MetaData(
    val title: String = "",
    val imageUrl: String = ""
)

@Composable
fun LinkPreview(
    url: String,
    cachedMetaData: MetaData?,
    onMetadataLoaded: (MetaData) -> Unit,
    onClick: (String) -> Unit
) {
    var metaData by remember { mutableStateOf(cachedMetaData ?: MetaData()) }
    if (cachedMetaData == null) {
        LaunchedEffect(Unit) {
            launch(Dispatchers.IO) {
                metaData = getMetadata(url.fixScheme())
                onMetadataLoaded(metaData)
            }
        }
    }
    Card(
        modifier = Modifier
            .height(Dimens.link_preview_height)
            .clip(RoundedCornerShape(Dimens.corner_radius))
            .padding(top = Dimens.space)
            .clickable { onClick(url) }
    ) {
        Row {
            if (metaData.imageUrl.isNotEmpty()) {
                Image(
                    painter = rememberCoilPainter(
                        request = metaData.imageUrl,
                        fadeIn = true
                    ),
                    contentScale = ContentScale.Crop,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(IMAGE_WEIGHT)
                )
            }
            TextContent(
                title = metaData.title,
                url = url,
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1 - IMAGE_WEIGHT)
                    .padding(start = Dimens.space)
            )
        }
    }
}

@Composable
private fun TextContent(title: String, url: String, modifier: Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            color = Color.White,
            style = typography.body2,
            modifier = Modifier.padding(horizontal = Dimens.space),
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        if (title.isNotEmpty()) {
            Text(
                text = URL(url).host.replace("www.", ""),
                color = Color.White,
                modifier = Modifier.padding(horizontal = Dimens.space),
                fontSize = Dimens.text_caption,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Suppress("TooGenericExceptionCaught")
private fun getMetadata(url: String): MetaData {
    return try {
        val document = Jsoup.connect(url).get()
        val title = getMetaTagContent(document, "meta[property=og:title]") ?: document.title()
        val imageUrl = getMetaTagContent(document, "meta[property=og:image]")?.fixScheme()
        MetaData(title, imageUrl.orEmpty())
    } catch (e: Exception) {
        Timber.e(e)
        MetaData(URL(url).host)
    }
}

private fun String.fixScheme(): String {
    return replace("http://", "https://")
}

private fun getMetaTagContent(document: Document, query: String): String? {
    return document.select(query).firstOrNull()?.attr("content")
}
