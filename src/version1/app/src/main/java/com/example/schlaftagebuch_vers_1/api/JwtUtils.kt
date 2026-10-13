package com.example.schlaftagebuch_vers_1.api

import android.util.Base64
import org.json.JSONObject

/**
 * Hilfsfunktionen für JWT (lokales Auslesen von Claims).
 *
 * Achtung:
 * - JWT wird hier NICHT verifiziert (keine Signaturprüfung),
 *   es wird nur das Payload decodiert, um Felder (Claims) auszulesen.
 */

object JwtUtils {

    /**
     * Liest "username" aus dem JWT-Payload (Claim).
     *
     * Rückgabe:
     * - username als String
     * - null, wenn Token nicht lesbar ist oder Claim fehlt
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

    fun isExpired(jwt: String): Boolean {
        return try {
            val parts = jwt.split(".")
            if (parts.size < 2) return true
            val payloadJson = String(
                android.util.Base64.decode(parts[1], android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP)
            )
            val obj = org.json.JSONObject(payloadJson)
            val exp = obj.optLong("exp", 0L)
            if (exp <= 0L) return true
            val nowSec = System.currentTimeMillis() / 1000
            nowSec >= exp
        } catch (e: Exception) {
            true
        }
    }
}
