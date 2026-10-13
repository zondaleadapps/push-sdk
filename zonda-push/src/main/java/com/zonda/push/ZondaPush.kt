package com.zonda.push

import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object ZondaPush {
    const val PERMISSION_REQUEST = 47021
    const val CHANNEL_ID = ZondaPushConfig.DEFAULT_CHANNEL_ID

    private var openHandler: ((Map<String, String>) -> Boolean)? = null

    fun init(context: Context, sdkKey: String, endpoint: String = ZondaPushConfig.DEFAULT_ENDPOINT) {
        init(context, ZondaPushConfig(endpoint = endpoint, ingestKey = sdkKey))
    }

    fun init(context: Context, config: ZondaPushConfig) {
        val app = context.applicationContext
        InstallStore(app).saveConfig(config)
        InstallStore(app).installId()
        NotificationPresenter.ensureChannel(app, config)
        ZondaSyncWorker.enqueueSync(app)
    }

    fun installId(context: Context): String = InstallStore(context).installId()

    fun sync(context: Context) {
        ZondaSyncWorker.enqueueSync(context.applicationContext)
    }

    fun addSegment(context: Context, name: String) {
        val clean = PushRequest.normalizeSegment(name)
        ZondaSyncWorker.enqueueSegments(context.applicationContext, listOf(clean), emptyList())
    }

    fun removeSegment(context: Context, name: String) {
        val clean = PushRequest.normalizeSegment(name)
        ZondaSyncWorker.enqueueSegments(context.applicationContext, emptyList(), listOf(clean))
    }

    fun shouldRequestPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < 33) return false
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
    }

    fun requestPermission(activity: Activity) {
        if (!shouldRequestPermission(activity)) {
            sync(activity)
            return
        }
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
            PERMISSION_REQUEST,
        )
    }

    fun onPermissionResult(context: Context, granted: Boolean) {
        if (!granted && Build.VERSION.SDK_INT >= 26) {
            val config = InstallStore(context).config()
            val manager = context.getSystemService(NotificationManager::class.java)
            val channelId = config?.channelId ?: CHANNEL_ID
            if (manager?.getNotificationChannel(channelId) == null && config != null) {
                NotificationPresenter.ensureChannel(context, config)
            }
        }
        sync(context)
    }

    fun setOpenHandler(handler: ((Map<String, String>) -> Boolean)?) {
        openHandler = handler
    }

    internal fun dispatchOpen(data: Map<String, String>): Boolean {
        return openHandler?.invoke(data) == true
    }
}
