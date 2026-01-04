package com.example.schlaftagebuch_vers_1.api

import android.util.Base64
import org.json.JSONObject

object JwtUtils {

    /**
     * Извлекает username из JWT (payload).
     * Возвращает null, если что-то пошло не так.
     */
    fun extractUsername(jwt: String): String? {
        return try {
            val parts = jwt.split(".")
            if (parts.size < 2) return null

            val payload = parts[1]
            val decoded = Base64.decode(
                payload,
                Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
            )

            val json = JSONObject(String(decoded))
            json.optString("username", null)

        } catch (e: Exception) {
            null
        }
    }
}
