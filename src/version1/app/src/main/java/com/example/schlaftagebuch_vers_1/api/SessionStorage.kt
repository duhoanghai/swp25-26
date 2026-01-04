package com.example.schlaftagebuch_vers_1.api

import android.content.Context

object SessionStorage {
    private const val PREF = "session"
    private const val KEY_JWT = "jwt"

    fun saveJwt(context: Context, jwt: String) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_JWT, jwt)
            .apply()
    }

    fun loadJwt(context: Context): String? =
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getString(KEY_JWT, null)

    fun clear(context: Context) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_JWT)
            .apply()
    }
}
