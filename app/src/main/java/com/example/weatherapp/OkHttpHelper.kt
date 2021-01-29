package com.example.weatherapp

import okhttp3.*
import java.io.IOException


class OkHttpHelper {
    //GET network request
    @Throws(IOException::class)
    fun GET(client: OkHttpClient, url: HttpUrl): String? {
        val request = Request.Builder()
            .url(url)
            .build()
        val response: Response = client.newCall(request).execute()
        return response.body()?.string()
    }

    //POST network request
    @Throws(IOException::class)
    fun POST(client: OkHttpClient, url: HttpUrl?, body: RequestBody?): String? {
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()
        val response = client.newCall(request).execute()
        return response.body()!!.string()
    }
}