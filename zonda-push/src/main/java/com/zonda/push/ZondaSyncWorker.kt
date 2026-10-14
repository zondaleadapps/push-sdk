package com.zonda.push

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import java.io.IOException
import java.net.URLEncoder
import java.util.Locale
import java.util.TimeZone

internal class ZondaSyncWorker(
    context: Context,
    params: WorkerParameters,
) : Worker(context, params) {
    override fun doWork(): Result {
        val config = InstallStore(applicationContext).config() ?: return Result.failure()
        return try {
            if (inputData.getString(KEY_JOB) == JOB_SEGMENTS) {
                val add = inputData.getString(KEY_ADD).orEmpty().split(',').filter { it.isNotBlank() }
                val remove = inputData.getString(KEY_REMOVE).orEmpty().split(',').filter { it.isNotBlank() }
                val body = PushRequest.segmentsBody(InstallStore(applicationContext).installId(), add, remove)
                PushApi.post(config, "/v1/users/segments", body)
            } else {
                val token = resolveToken(applicationContext, config)
                PushApi.post(config, "/v1/users", userBody(applicationContext, token))
            }
            Result.success()
        } catch (_: IOException) {
            Result.retry()
        } catch (_: IllegalArgumentException) {
            Result.failure()
        }
    }

    companion object {
        private const val KEY_JOB = "job"
        private const val KEY_ADD = "add"
        private const val KEY_REMOVE = "remove"
        private const val JOB_SYNC = "sync"
        private const val JOB_SEGMENTS = "segments"

        fun enqueueSync(context: Context) {
            val request = OneTimeWorkRequestBuilder<ZondaSyncWorker>()
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .setInputData(workDataOf(KEY_JOB to JOB_SYNC))
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork("zonda-push-sync", ExistingWorkPolicy.REPLACE, request)
        }

        fun enqueueSegments(context: Context, add: List<String>, remove: List<String>) {
            val request = OneTimeWorkRequestBuilder<ZondaSyncWorker>()
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .setInputData(
                    workDataOf(
                        KEY_JOB to JOB_SEGMENTS,
                        KEY_ADD to add.joinToString(","),
                        KEY_REMOVE to remove.joinToString(","),
                    ),
                )
                .build()
            WorkManager.getInstance(context).enqueue(request)
        }

        fun userBody(context: Context, token: String?): String {
            val appContext = context.applicationContext
            val granted = notificationsAllowed(appContext)
            val info = appContext.packageManager.getPackageInfo(appContext.packageName, 0)
            val version = if (Build.VERSION.SDK_INT >= 28) info.longVersionCode.toString() else @Suppress("DEPRECATION") info.versionCode.toString()
            val versionName = info.versionName ?: version
            val locale = Locale.getDefault()
            return PushRequest.userBody(
                installId = InstallStore(appContext).installId(),
                token = token,
                permission = if (granted) "granted" else "denied",
                subscribed = granted,
                language = locale.language.orEmpty(),
                country = locale.country.orEmpty(),
                timezone = TimeZone.getDefault().id,
                appVersion = versionName,
                packageName = appContext.packageName,
            )
        }

        private fun resolveToken(context: Context, config: ZondaPushConfig): String? {
            val packageName = URLEncoder.encode(context.packageName, Charsets.UTF_8.name())
            val raw = PushApi.get(config, "/v1/config?package=$packageName")
            return FirebaseGate.tokenOrNull(context, FirebaseRemote.parse(raw))
        }

        private fun notificationsAllowed(context: Context): Boolean {
            if (Build.VERSION.SDK_INT >= 33) {
                val granted = context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED
                if (!granted) return false
            }
            return NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }
}
