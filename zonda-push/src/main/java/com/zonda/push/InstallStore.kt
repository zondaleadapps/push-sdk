package com.zonda.push

import android.content.Context
import java.util.UUID

internal class InstallStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun installId(): String {
        val existing = prefs.getString(KEY_INSTALL, null)
        if (!existing.isNullOrBlank()) return existing
        val created = UUID.randomUUID().toString()
        prefs.edit().putString(KEY_INSTALL, created).apply()
        return created
    }

    fun saveConfig(config: ZondaPushConfig) {
        prefs.edit()
            .putString(KEY_ENDPOINT, config.endpointBase())
            .putString(KEY_INGEST, config.ingestKey)
            .putString(KEY_CHANNEL, config.channelId)
            .putString(KEY_CHANNEL_NAME, config.channelName)
            .putInt(KEY_ICON, config.smallIconRes)
            .apply()
    }

    fun config(): ZondaPushConfig? {
        val endpoint = prefs.getString(KEY_ENDPOINT, null) ?: return null
        val key = prefs.getString(KEY_INGEST, null) ?: return null
        return ZondaPushConfig(
            endpoint = endpoint,
            ingestKey = key,
            channelId = prefs.getString(KEY_CHANNEL, null) ?: ZondaPushConfig.DEFAULT_CHANNEL_ID,
            channelName = prefs.getString(KEY_CHANNEL_NAME, null),
            smallIconRes = prefs.getInt(KEY_ICON, R.drawable.ic_stat_zonda),
        )
    }

    companion object {
        private const val PREFS = "zonda_push"
        private const val KEY_INSTALL = "install_id"
        private const val KEY_ENDPOINT = "endpoint"
        private const val KEY_INGEST = "ingest_key"
        private const val KEY_CHANNEL = "channel_id"
        private const val KEY_CHANNEL_NAME = "channel_name"
        private const val KEY_ICON = "small_icon"
    }
}
