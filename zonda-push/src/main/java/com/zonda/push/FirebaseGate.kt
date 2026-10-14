package com.zonda.push

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import org.json.JSONObject
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

internal data class FirebaseRemote(
    val applicationId: String,
    val apiKey: String,
    val projectId: String,
    val gcmSenderId: String,
) {
    companion object {
        fun parse(raw: String): FirebaseRemote? {
            val root = JSONObject(raw)
            if (!root.has("firebase") || root.isNull("firebase")) return null
            val firebase = root.getJSONObject("firebase")
            val applicationId = firebase.optString("applicationId")
            val apiKey = firebase.optString("apiKey")
            val projectId = firebase.optString("projectId")
            val gcmSenderId = firebase.optString("gcmSenderId")
            if (applicationId.isBlank() || apiKey.isBlank() || projectId.isBlank() || gcmSenderId.isBlank()) return null
            return FirebaseRemote(applicationId, apiKey, projectId, gcmSenderId)
        }
    }
}

internal object FirebaseGate {
    fun tokenOrNull(context: Context, remote: FirebaseRemote?): String? {
        if (remote == null) return null
        return try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(
                    context,
                    FirebaseOptions.Builder()
                        .setApplicationId(remote.applicationId)
                        .setApiKey(remote.apiKey)
                        .setProjectId(remote.projectId)
                        .setGcmSenderId(remote.gcmSenderId)
                        .build(),
                )
            }
            val token = AtomicReference<String?>()
            val latch = CountDownLatch(1)
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                token.set(if (task.isSuccessful) task.result else null)
                latch.countDown()
            }
            if (!latch.await(10, TimeUnit.SECONDS)) return null
            token.get()
        } catch (_: Exception) {
            null
        }
    }
}
