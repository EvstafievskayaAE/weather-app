package com.example.weatherapp

import okhttp3.HttpUrl
import java.text.SimpleDateFormat
import java.util.*

object CommonSettings {
    val weatherMapAPI = "b5695569eeb89ee67d2b47e883e416dd"
    var isCityName = false
    var chosenCityName = ""

    fun weatherMapAPIRequestByLocation(latitude:String, longitude:String): HttpUrl {
        return HttpUrl.Builder()
            .scheme("https")
            .host("api.openweathermap.org")
            .addPathSegment("data/2.5/weather")
            .addQueryParameter("lat",latitude)
            .addQueryParameter("lon",longitude)
            .addQueryParameter("units","metric")
            .addQueryParameter("appid", weatherMapAPI)
            .build()
    }

    fun weatherMapAPIRequestByCityName(city:String):HttpUrl{
        return HttpUrl.Builder()
            .scheme("https")
            .host("api.openweathermap.org")
            .addPathSegment("data/2.5/weather")
            .addQueryParameter("q", city)
            .addQueryParameter("units","metric")
            .addQueryParameter("appid", weatherMapAPI)
            .build()
    }

    fun convertUnixTimeStampToDateTime(unixTimeStamp:Double):String{
        val dateFormat = SimpleDateFormat("HH:mm")
        val date = Date()
        date.time = unixTimeStamp.toLong()*1000
        return dateFormat.format(date)
    }

    fun getImage(icon:String):String{
        return "https://api.openweathermap.org/img/w/${icon}.png"
    }

    val currentDate:String
    get() {
        val dateFormat = SimpleDateFormat("dd MM yyyy HH:mm")
        val date = Date()
        return dateFormat.format(date)
    }
}