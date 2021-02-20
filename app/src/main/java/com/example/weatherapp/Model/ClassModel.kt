package com.example.weatherapp.Model

class Weather(var cityId:Int, var main:String?, var description:String?, var icon:String?)

class Sys (var type:Int, var cityId:Int, var message:Double, var country:String?,
           var sunrise:Double, var sunset:Double)

class Location(var latitude:Double, var longitude:Double)

class Clouds(var all:Int)

class Rain ()

class Wind(var speed:Double, var deg:Double, var gust:Double)

class Main(
        var temp: Double, var feels_like: Double, var pressure: Double,
        var humidity: Int, var temp_min: Double, var temp_max: Double)



