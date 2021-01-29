package com.example.weatherapp.Model

class OpenWeatherMap {
    var location: Location?=null
    var weather:List<Weather>?=null
    var base:String?=null
    var main: Main?=null
    var wind: Wind?=null
    var rain: Rain?=null
    var clouds: Clouds?=null
    var dataCalculationTime:Int=0
    var sys: Sys?=null
    var cityId:Int=0
    var name:String?=null
    var cod:Int=0

    constructor(){}

    constructor(location: Location, weatherList:List<Weather>, base:String, main: Main,
                wind: Wind, rain: Rain, clouds: Clouds, dataCalculationTime:Int, name:String, cod:Int)
    {
        this.location = location
        this.weather = weather
        this.main = main
        this.base = base
        this.wind = wind
        this.rain = rain
        this.clouds = clouds
        this.dataCalculationTime = dataCalculationTime
        this.sys = sys
        this.cityId = cityId
        this.name = name
        this.cod = cod
    }

}
