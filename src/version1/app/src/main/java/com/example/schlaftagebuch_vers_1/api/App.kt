package com.example.schlaftagebuch_vers_1.api

import android.app.Application
import android.util.Log

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        ApiClient.init(this)
        Log.d("ApiClient", "INIT called")
    }
}

