package com.zonda.push

import android.content.ContentProvider
import android.content.ContentValues
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.os.Looper

/**
 * Starts the SDK when the app declares `com.zonda.push.sdk_key`.
 * Work is posted to the main looper so it runs after Application.onCreate:
 * some apps remove WorkManagerInitializer and initialize WorkManager there.
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
        Handler(Looper.getMainLooper()).post {
            ZondaPush.init(app, key, endpoint)
        }
        return true
    }

    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? = null
    override fun getType(uri: Uri): String? = null
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0
}
