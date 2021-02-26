package com.example.weatherapp

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

/** Класс для работы с интернет-соединением */
class InternetConnection(var context: Context) {

    /** Функция проверки наличия интернет-соединения */
    fun isNetworkConnected(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        /** Проверка на устройствах с API < 23 */
        if (connectivityManager != null) {
            if (Build.VERSION.SDK_INT < 23) {
                val networkInfo = connectivityManager.activeNetworkInfo
                if (networkInfo != null) {
                    return networkInfo.isConnected &&
                            (networkInfo.type == ConnectivityManager.TYPE_WIFI ||
                                    networkInfo.type == ConnectivityManager.TYPE_MOBILE)
                }
            } else { /** Проверка на устройствах с API от 23 */
                val network = connectivityManager.activeNetwork
                if (network != null) {
                    val networkCapabilities =
                        connectivityManager.getNetworkCapabilities(network)
                    return networkCapabilities!!.hasTransport(
                        NetworkCapabilities.TRANSPORT_CELLULAR) || networkCapabilities.hasTransport(
                        NetworkCapabilities.TRANSPORT_WIFI
                    )
                }
            }
        }
        return false
    }
}