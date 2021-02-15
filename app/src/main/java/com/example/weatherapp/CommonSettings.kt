package com.example.weatherapp

import com.example.weatherapp.ProjectConstants.weatherMapAPI
import okhttp3.HttpUrl
import java.text.SimpleDateFormat
import java.util.*

object CommonSettings {
    var isCityNameChosen = false // Значение флага "Город не выбран"
    var chosenCityName = "" // Название города, выбранного из списка
    var newCityName = "" // Название нового города, добавленного в список

    /** Построение ссылки запроса к серверу погоды по координатам местоположения */
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

    /** Построение ссылки запроса к серверу погоды по названию города */
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

    /** Преобразование формата даты */
    fun convertUnixTimeStampToDateTime(unixTimeStamp:Double):String{
        val dateFormat = SimpleDateFormat("HH:mm")
        val date = Date()
        date.time = unixTimeStamp.toLong()*1000
        return dateFormat.format(date)
    }

    /** Адрес получения иконок weathermap*/
    fun getImage(icon:String):String{
        return "https://api.openweathermap.org/img/w/${icon}.png"
    }

    /** Формат отображение текущей даты получения данных о погоде*/
    val currentDate:String
    get() {
        val dateFormat = SimpleDateFormat("dd MMMM yyyy HH:mm", Locale.ENGLISH)
        val date = Date()
        return dateFormat.format(date)
    }
}