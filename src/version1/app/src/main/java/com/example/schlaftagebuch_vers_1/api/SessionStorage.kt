package com.example.schlaftagebuch_vers_1.api

import android.content.Context

/**
 * Persistenter Speicher für Session-Daten (SharedPreferences).
 *
 * Zweck:
 * - JWT zwischen App-Starts speichern/laden
 * - Logout/Reset ermöglichen (clear)
 */

object SessionStorage {
    //Name der SharedPreferences-Datei.
    private const val PREF = "session"
    //Key unter dem das JWT gespeichert wird.
    private const val KEY_JWT = "jwt"

    //Speichert JWT persistent auf dem Gerät.
    fun saveJwt(context: Context, jwt: String) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_JWT, jwt)
            .apply()
    }

    //Lädt JWT (oder null, falls nicht vorhanden).
    fun loadJwt(context: Context): String? =
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getString(KEY_JWT, null)

    //Entfernt JWT (z.B. beim Logout).
    fun clear(context: Context) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_JWT)
            .apply()
    }
}
