package com.teampower.cicerone

import okhttp3.Interceptor
import okhttp3.Response


class FoursquareRequestInterceptor(private val client_id: String, private val client_secret: String, private val version: String, private val cacheDuration: Int): Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val url = request.url.newBuilder()
            .addQueryParameter("client_id", client_id)
            .addQueryParameter("client_secret", client_secret)
            .addQueryParameter("v", version)
            .build()

        val newRequest = request.newBuilder()
            .url(url)
            .addHeader("Cache-Control", "public, max-age=$cacheDuration")
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .build()

        return chain.proceed(newRequest)
    }
}