package com.shkcodes.aurora.ui.auth

import android.graphics.Bitmap
import android.webkit.WebView
import android.webkit.WebViewClient
import com.shkcodes.aurora.BuildConfig

class AuthWebViewClient(private val listener: (String) -> Unit) : WebViewClient() {
    override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
        if (url.startsWith(BuildConfig.CALLBACK_URL)) {
            listener(url)
        }
    }
}
