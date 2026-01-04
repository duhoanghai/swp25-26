package com.example.schlaftagebuch_vers_1.api

import com.example.schlaftagebuch_vers_1.api.protocol.ProtocolApi
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.schlaftagebuch_vers_1.api.auth.AuthApi

object ApiClient {

    // Для эмулятора Android: 10.0.2.2
    // Для реального телефона в одной Wi-Fi сети: IP твоего ПК, например http://192.168.0.10:8080
    private const val BASE_URL = "http://10.0.2.2:8080/"

    private val httpLogger = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    private val okHttp = OkHttpClient.Builder()
        .addInterceptor(httpLogger)
        .addInterceptor { chain ->
            val token = Session.jwt
            val req = if (!token.isNullOrBlank()) {
                chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
            } else chain.request()
            chain.proceed(req)
        }
        .build()

    val protocolApi: ProtocolApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ProtocolApi::class.java)
    }

    val authApi: AuthApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApi::class.java)
    }
}