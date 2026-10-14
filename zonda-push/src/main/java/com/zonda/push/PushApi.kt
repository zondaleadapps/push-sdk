package com.zonda.push

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

internal object PushApi {
    fun post(config: ZondaPushConfig, path: String, json: String) {
        val connection = (URL(config.endpointBase() + path).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 15_000
            readTimeout = 15_000
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            setRequestProperty("Authorization", "Bearer ${config.ingestKey}")
        }
        try {
            connection.outputStream.use { it.write(json.toByteArray(Charsets.UTF_8)) }
            val code = connection.responseCode
            if (code !in 200..299) {
                throw IOException("zonda push HTTP $code")
            }
        } finally {
            connection.disconnect()
        }
    }

    fun get(config: ZondaPushConfig, path: String): String {
        val connection = (URL(config.endpointBase() + path).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 15_000
            readTimeout = 15_000
            setRequestProperty("Authorization", "Bearer ${config.ingestKey}")
        }
        try {
            val code = connection.responseCode
            if (code !in 200..299) throw IOException("zonda push HTTP $code")
            return connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }
}
