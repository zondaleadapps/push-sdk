package com.zonda.push

import android.content.Context

class ZondaPushConfig(
    val endpoint: String,
    val ingestKey: String,
    val channelId: String = DEFAULT_CHANNEL_ID,
    val channelName: String? = null,
    val smallIconRes: Int = R.drawable.ic_stat_zonda,
) {
    init {
        require(endpoint.startsWith("https://") || endpoint.startsWith("http://")) {
            "endpoint must be an http(s) URL"
        }
        require(ingestKey.startsWith("zp_")) { "ingestKey must be the SDK key from Zonda Push" }
    }

    fun endpointBase(): String = endpoint.trimEnd('/')

    companion object {
        const val DEFAULT_CHANNEL_ID = "zonda_default"

        fun fromMetaData(context: Context, fallback: ZondaPushConfig? = null): ZondaPushConfig {
            val meta = context.packageManager
                .getApplicationInfo(context.packageName, android.content.pm.PackageManager.GET_META_DATA)
                .metaData
            val endpoint = meta?.getString("com.zonda.push.endpoint") ?: fallback?.endpoint
            val key = meta?.getString("com.zonda.push.ingest_key") ?: fallback?.ingestKey
            require(!endpoint.isNullOrBlank() && !key.isNullOrBlank()) {
                "Set com.zonda.push.endpoint and com.zonda.push.ingest_key, or call ZondaPush.init"
            }
            return ZondaPushConfig(
                endpoint = endpoint,
                ingestKey = key,
                channelId = meta?.getString("com.zonda.push.channel_id") ?: DEFAULT_CHANNEL_ID,
                smallIconRes = meta?.getInt("com.zonda.push.small_icon")?.takeIf { it != 0 }
                    ?: R.drawable.ic_stat_zonda,
            )
        }
    }
}
