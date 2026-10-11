package com.zonda.push

import android.graphics.Color
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class ZondaMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        ZondaSyncWorker.enqueueSync(this)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val config = InstallStore(this).config() ?: return
        val note = message.notification
        val data = message.data
        val title = note?.title ?: data["title"] ?: return
        val body = note?.body ?: data["body"] ?: ""
        val image = note?.imageUrl?.toString() ?: data["image"]
        val color = note?.color?.let { runCatching { Color.parseColor(it) }.getOrNull() }
        NotificationPresenter.show(
            context = this,
            config = config,
            title = title,
            body = body,
            data = data,
            imageUrl = image,
            tag = note?.tag,
            color = color,
        )
    }
}
