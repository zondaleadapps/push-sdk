package com.zonda.push

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PushRequestTest {
    @Test
    fun userBodyKeepsTokenOutWhenMissing() {
        val json = PushRequest.userBody(
            installId = "11111111-1111-4111-8111-111111111111",
            token = null,
            permission = "denied",
            subscribed = false,
            language = "ru",
            country = "ua",
            timezone = "Europe/Kyiv",
            appVersion = "1.2",
            packageName = "com.example.app",
        )
        assertTrue(json.contains("\"install_id\":\"11111111-1111-4111-8111-111111111111\""))
        assertTrue(json.contains("\"permission\":\"denied\""))
        assertTrue(json.contains("\"subscribed\":false"))
        assertTrue(json.contains("\"country\":\"UA\""))
        assertFalse(json.contains("fcm_token"))
    }

    @Test
    fun userBodyIncludesToken() {
        val json = PushRequest.userBody(
            installId = "11111111-1111-4111-8111-111111111111",
            token = "abcdefghijklmnopqrst",
            permission = "granted",
            subscribed = true,
            language = "en",
            country = "",
            timezone = "UTC",
            appVersion = "1",
            packageName = "com.example.app",
        )
        assertTrue(json.contains("\"fcm_token\":\"abcdefghijklmnopqrst\""))
        assertTrue(json.contains("\"subscribed\":true"))
        assertFalse(json.contains("\"country\""))
    }

    @Test
    fun segmentsRejectsBadName() {
        try {
            PushRequest.normalizeSegment("Bad Name")
            throw AssertionError("expected invalid_segment")
        } catch (err: IllegalArgumentException) {
            assertEquals("invalid_segment", err.message)
        }
    }

    @Test
    fun segmentsBodyListsAddAndRemove() {
        val json = PushRequest.segmentsBody(
            "11111111-1111-4111-8111-111111111111",
            listOf("Payers"),
            listOf("qa-ui"),
        )
        assertTrue(json.contains("\"add\":[\"payers\"]"))
        assertTrue(json.contains("\"remove\":[\"qa-ui\"]"))
    }

    @Test
    fun jsonEscapesQuotes() {
        assertEquals("\"a\\\"b\"", jsonString("a\"b"))
    }
}
