package com.zonda.push

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import java.net.HttpURLConnection
import java.net.URL

internal object NotificationPresenter {
    fun ensureChannel(context: Context, config: ZondaPushConfig) {
        if (Build.VERSION.SDK_INT < 26) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        if (manager.getNotificationChannel(config.channelId) != null) return
        val channel = NotificationChannel(
            config.channelId,
            config.channelName ?: context.getString(R.string.zonda_push_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        )
        manager.createNotificationChannel(channel)
    }

    fun show(
        context: Context,
        config: ZondaPushConfig,
        title: String,
        body: String,
        data: Map<String, String>,
        imageUrl: String?,
        tag: String?,
        color: Int?,
    ) {
        ensureChannel(context, config)
        val open = Intent(context, ZondaPushOpenActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            data.forEach { (key, value) -> putExtra(key, value) }
        }
        val requestCode = (tag ?: title).hashCode()
        val pending = PendingIntent.getActivity(
            context,
            requestCode,
            open,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val builder = NotificationCompat.Builder(context, config.channelId)
            .setSmallIcon(config.smallIconRes)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pending)
        if (color != null) builder.color = color
        val bitmap = imageUrl?.let { download(it) }
        if (bitmap != null) {
            builder.setStyle(NotificationCompat.BigPictureStyle().bigPicture(bitmap).setSummaryText(body))
        } else if (body.isNotBlank()) {
            builder.setStyle(NotificationCompat.BigTextStyle().bigText(body))
        }
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        manager.notify(tag, requestCode, builder.build())
    }

    private fun download(url: String): android.graphics.Bitmap? {
        if (!url.startsWith("https://")) return null
        return try {
            val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                connectTimeout = 5_000
                readTimeout = 5_000
                instanceFollowRedirects = true
            }
            try {
                if (connection.responseCode !in 200..299) return null
                connection.inputStream.use { BitmapFactory.decodeStream(it) }
            } finally {
                connection.disconnect()
            }
        } catch (_: Exception) {
            null
        }
    }
}
