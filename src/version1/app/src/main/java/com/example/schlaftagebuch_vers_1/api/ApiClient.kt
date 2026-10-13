package com.example.schlaftagebuch_vers_1.api

import android.content.Context
import com.example.schlaftagebuch_vers_1.api.protocol.ProtocolApi
import com.example.schlaftagebuch_vers_1.api.report.ReportApi
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.schlaftagebuch_vers_1.api.auth.AuthApi

/**
 * Zentrale Retrofit-/HTTP-Konfiguration der App.
 *
 * Aufgaben:
 * - BASE_URL definieren (Backend-Adresse)
 * - OkHttpClient konfigurieren (Logging + Authorization-Header)
 * - Retrofit-Instanzen für verschiedene APIs bereitstellen (AuthApi, ProtocolApi)
 */

object ApiClient {

    /**
     * Basis-URL des Backends.
     *
     * Hinweis:
     * - Android Emulator: 10.0.2.2 verweist auf den Host-PC (localhost)
     * - echtes Gerät im gleichen WLAN: IP-Adresse des PCs verwenden (z.B. http://192.168.x.x:8080/)
     */

    private const val BASE_URL = "http://10.0.2.2:8080/"

    private lateinit var appContext: Context
    fun init(context: Context) {
        appContext = context.applicationContext
    }

    // HTTP-Logger (nur für Entwicklung sinnvoll)
    private val httpLogger = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    /**
     * OkHttpClient mit:
     * - Logging
     * - Interceptor, der automatisch "Authorization: Bearer <JWT>" hinzufügt,
     *   wenn Session.jwt gesetzt ist.
     */

    private val okHttp: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val token = Session.jwt
                android.util.Log.d("AUTH", "tokenFromStorage=${token?.take(15)} len=${token?.length}")

                val req = if (!token.isNullOrBlank()) {
                    chain.request().newBuilder()
                        .addHeader("Authorization", "Bearer $token")
                        .build()
                } else chain.request()

                android.util.Log.d("AUTH", "outAuthHeader=${req.header("Authorization")}")
                chain.proceed(req)
            }
            .addInterceptor(httpLogger)
            .build()
    }

    // API für Protokoll-Endpoints (Submission usw.).
    val protocolApi: ProtocolApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ProtocolApi::class.java)
    }

    // API für Auth/First-Login-Endpoints.
    val authApi: AuthApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApi::class.java)
    }

    val reportApi: ReportApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ReportApi::class.java)
    }
}