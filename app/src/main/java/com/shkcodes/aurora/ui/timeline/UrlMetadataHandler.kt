package com.shkcodes.aurora.ui.timeline

import androidx.core.view.doOnAttach
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleCoroutineScope
import coil.ImageLoader
import coil.clear
import coil.load
import com.shkcodes.aurora.databinding.LayoutLinkPreviewBinding
import com.shkcodes.aurora.ui.tweetlist.MetaData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import timber.log.Timber
import java.net.URL

class UrlMetadataHandler(private val scope: LifecycleCoroutineScope, private val imageLoader: ImageLoader) {

    private val urlsMetaData = mutableMapOf<String, MetaData>()

    fun get(binding: LayoutLinkPreviewBinding, url: String) {
        if (urlsMetaData[url] == null) {
            scope.launchWhenResumed {
                binding.content.isVisible = false
                val metaData = withContext(Dispatchers.IO) {
                    getMetadata(url).also {
                        urlsMetaData[url] = it
                    }
                }
                showData(binding, metaData, url)
            }
        } else {
            showData(binding, urlsMetaData.getValue(url), url)
        }
    }

    private fun showData(binding: LayoutLinkPreviewBinding, metaData: MetaData, url: String) {
        with(binding) {
            root.doOnAttach {
                if (binding.root.tag == url) {
                    content.isVisible = true
                    previewImage.clear()
                    previewImage.load(metaData.imageUrl, imageLoader) {
                        crossfade(true)
                    }
                    previewImage.isVisible = metaData.imageUrl.isNotEmpty()
                    title.text = metaData.title
                    subtitle.text = URL(url).host.replace("www.", "")
                }
            }
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private fun getMetadata(url: String): MetaData {
        return try {
            val document = Jsoup.connect(url.fixScheme()).get()
            val title = getMetaTagContent(document, "meta[property=og:title]") ?: document.title()
            val imageUrl = getMetaTagContent(document, "meta[property=og:image]")?.fixScheme()
            MetaData(title, imageUrl.orEmpty())
        } catch (e: Exception) {
            Timber.e(e)
            MetaData(URL(url).host)
        }
    }

    private fun getMetaTagContent(document: Document, query: String): String? {
        return document.select(query).firstOrNull()?.attr("content")
    }

    private fun String.fixScheme(): String {
        return replace("http://", "https://")
    }
}
