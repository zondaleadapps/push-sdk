package com.zonda.push

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle

class ZondaPushOpenActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val data = linkedMapOf<String, String>()
        intent?.extras?.keySet()?.forEach { key ->
            intent.extras?.getString(key)?.let { data[key] = it }
        }
        val handled = ZondaPush.dispatchOpen(data)
        if (!handled) {
            val url = data["url"]
            if (!url.isNullOrBlank() && (url.startsWith("https://") || url.startsWith("http://"))) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            } else {
                packageManager.getLaunchIntentForPackage(packageName)?.let { startActivity(it) }
            }
        }
        finish()
    }
}
