package com.example.weatherapp

object ProjectConstants {

    const val weatherMapAPI = "b5695569eeb89ee67d2b47e883e416dd"

    const val INTERVAL: Long = 10000 //время, через которое пройдет обновление координат, в мс
    const val FASTEST_INTERVAL: Long = 1000 //время обновления координат в мс
    const val REQUEST_PERMISSION_LOCATION = 10
    const val REQUEST_GPS_CODE = 11
    const val PROGRESS_BAR_DELAY:Long = 500 //задержка для прокрутки спиннера
    const val WEATHER_UPDATE_DELAY:Long = 10000 //аремя, через которое нужно обновить данные о погоде
}