package com.example.weatherapp

import java.text.SimpleDateFormat
import java.util.*

object CommonSettings {
    val weatherMapAPI = "b5695569eeb89ee67d2b47e883e416dd"
    val weatherMapLink = "https://api.openweathermap.org/data/2.5/weather"

    fun weatherMapAPIRequest(latitude:String, longitude:String):String{
        var requestStringBuilder = StringBuilder(weatherMapLink)
        requestStringBuilder.append("?lat=${latitude}&lon=${longitude}&units=metric&appid=${weatherMapAPI}")
        return requestStringBuilder.toString()
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