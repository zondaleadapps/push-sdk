package com.zonda.push

import android.content.ContentProvider
import android.content.ContentValues
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri

/**
 * Starts the SDK before [android.app.Application.onCreate] when the app
 * declares `com.zonda.push.sdk_key`. Same idea as OneSignal's app id.
 */
class ZondaPushInitProvider : ContentProvider() {
    override fun onCreate(): Boolean {
        val app = context?.applicationContext ?: return false
        val meta = app.packageManager
            .getApplicationInfo(app.packageName, PackageManager.GET_META_DATA)
            .metaData
        val key = meta?.getString("com.zonda.push.sdk_key")
            ?: meta?.getString("com.zonda.push.ingest_key")
        if (key.isNullOrBlank()) return true
        val endpoint = meta?.getString("com.zonda.push.endpoint") ?: ZondaPushConfig.DEFAULT_ENDPOINT
        ZondaPush.init(app, key, endpoint)
        return true
    }

    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? = null
    override fun getType(uri: Uri): String? = null
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0
}
