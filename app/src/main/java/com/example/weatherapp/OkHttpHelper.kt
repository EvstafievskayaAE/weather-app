package com.example.weatherapp

import okhttp3.*
import java.io.IOException

/** Класс для формирования запросов к серверу с помощью библиотеки OkHttp */
class OkHttpHelper {

    // GET network request
    @Throws(IOException::class)
    fun GET(client: OkHttpClient, url: HttpUrl): String? {
        val request = Request.Builder()
            .url(url)
            .build()
        val response: Response = client.newCall(request).execute()
        return response.body()?.string()
    }

}