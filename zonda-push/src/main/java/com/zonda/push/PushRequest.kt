package com.zonda.push

internal object PushRequest {
    private val segmentPattern = Regex("^[a-z0-9][a-z0-9_-]{0,39}$")
    private val installPattern = Regex("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")

    fun normalizeSegment(name: String): String {
        val clean = name.trim().lowercase()
        if (!segmentPattern.matches(clean)) {
            throw IllegalArgumentException("invalid_segment")
        }
        return clean
    }

    fun requireInstallId(id: String): String {
        val clean = id.trim().lowercase()
        if (!installPattern.matches(clean)) {
            throw IllegalArgumentException("invalid_install_id")
        }
        return clean
    }

    fun userBody(
        installId: String,
        token: String?,
        permission: String,
        subscribed: Boolean,
        language: String,
        country: String,
        timezone: String,
        appVersion: String,
        packageName: String,
    ): String {
        val fields = linkedMapOf(
            "install_id" to jsonString(requireInstallId(installId)),
            "permission" to jsonString(permission),
            "subscribed" to if (subscribed) "true" else "false",
            "language" to jsonString(language),
            "timezone" to jsonString(timezone),
            "app_version" to jsonString(appVersion),
            "package" to jsonString(packageName),
        )
        if (!token.isNullOrBlank()) fields["fcm_token"] = jsonString(token)
        if (country.length == 2) fields["country"] = jsonString(country.uppercase())
        return fields.entries.joinToString(prefix = "{", postfix = "}") { (key, value) ->
            "${jsonString(key)}:$value"
        }
    }

    fun segmentsBody(installId: String, add: List<String>, remove: List<String>): String {
        val addJson = add.map { jsonString(normalizeSegment(it)) }.joinToString(prefix = "[", postfix = "]")
        val removeJson = remove.map { jsonString(normalizeSegment(it)) }.joinToString(prefix = "[", postfix = "]")
        return "{" +
            "${jsonString("install_id")}:${jsonString(requireInstallId(installId))}," +
            "${jsonString("add")}:$addJson," +
            "${jsonString("remove")}:$removeJson" +
            "}"
    }
}

internal fun jsonString(value: String): String {
    val out = StringBuilder(value.length + 2)
    out.append('"')
    for (ch in value) {
        when (ch) {
            '\\' -> out.append("\\\\")
            '"' -> out.append("\\\"")
            '\n' -> out.append("\\n")
            '\r' -> out.append("\\r")
            '\t' -> out.append("\\t")
            else -> if (ch.code < 0x20) out.append("\\u%04x".format(ch.code)) else out.append(ch)
        }
    }
    out.append('"')
    return out.toString()
}
