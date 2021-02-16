package com.example.weatherapp

import android.content.ContentValues
import android.content.Context
import android.database.Cursor


class CacheDataClass(context: Context) :DatabaseHelper(context) {

    var cityName:String? = null
    var country:String? = null
    lateinit var currentDate:String
    var skyDescription:String? = null
    var temperature:Double = 0.0
    var feelsLike:Double = 0.0
    var minTemperature:Double = 0.0
    var maxTemperature:Double = 0.0
    lateinit var sunrise:String
    lateinit var sunset:String
    var windSpeed:Double = 0.0

    /*constructor(cityName:String?, country:String?, currentDate:String, skyDescription:String?,
                temperature:Double, feelsLike:Double, minTemperature:Double, maxTemperature:Double,
                sunrise:String, sunset:String, windSpeed:Double ){
        this.cityName = cityName
        this.country = country
        this.currentDate = currentDate
        this.skyDescription = skyDescription
        this.temperature = temperature
        this.feelsLike = feelsLike
        this.minTemperature = minTemperature
        this.maxTemperature = maxTemperature
        this.sunrise = sunrise
        this.sunset = sunset
        this.windSpeed = windSpeed

    }*/

    /** Обновление кэшированных данных о погоде для последнего отображенного города */
   fun updateCacheTable(
        cityName: String?,
        country: String?,
        currentDate: String,
        skyDescription: String?,
        temperature: Double,
        feelsLike: Double,
        minTemperature: Double,
        maxTemperature: Double,
        sunrise: String,
        sunset: String,
        windSpeed: Double
    ) {
        val database = this.writableDatabase
        val values = ContentValues()

        values.put(Companion.COLUMN_NAME, cityName)
        values.put(Companion.COLUMN_COUNTRY, country)
        values.put(COLUMN_CURRENT_DATE, currentDate)
        values.put(COLUMN_SKY_DESCRIPTION, skyDescription)
        values.put(COLUMN_TEMPERATURE, temperature)
        values.put(COLUMN_FEELS_LIKE, feelsLike)
        values.put(COLUMN_MIN_TEMPERATURE, minTemperature)
        values.put(COLUMN_MAX_TEMPERATURE, maxTemperature)
        values.put(COLUMN_SUNRISE, sunrise)
        values.put(COLUMN_SUNSET, sunset)
        values.put(COLUMN_WIND_SPEED, windSpeed)

        database?.update(Companion.CACHE_TABLE_NAME, values, "${COLUMN_ID}=1", arrayOf())

    }

    /** Получение закэшированных данных из БД */
    fun getCacheDataFromDB(){
        val query: String
        val cursor: Cursor

        val database = this.readableDatabase
        query = "SELECT  * FROM $CACHE_TABLE_NAME"
        if (database != null) {
            cursor = database.rawQuery(query, null)

        while (cursor.moveToNext()) {
            WeatherDataForDisplay.cityName = cursor.getString(cursor.getColumnIndex(Companion.COLUMN_NAME))
            WeatherDataForDisplay.country = cursor.getString(cursor.getColumnIndex(Companion.COLUMN_COUNTRY))
            WeatherDataForDisplay.currentDate = cursor.getString(cursor.getColumnIndex(COLUMN_CURRENT_DATE))
            WeatherDataForDisplay.skyDescription = cursor.getString(cursor.getColumnIndex(COLUMN_SKY_DESCRIPTION))
            WeatherDataForDisplay.temperature = cursor.getDouble(cursor.getColumnIndex(COLUMN_TEMPERATURE))
            WeatherDataForDisplay.feelsLike = cursor.getDouble(cursor.getColumnIndex(COLUMN_FEELS_LIKE))
            WeatherDataForDisplay.minTemperature = cursor.getDouble(cursor.getColumnIndex(COLUMN_MIN_TEMPERATURE))
            WeatherDataForDisplay.maxTemperature = cursor.getDouble(cursor.getColumnIndex(COLUMN_MAX_TEMPERATURE))
            WeatherDataForDisplay.sunrise = cursor.getString(cursor.getColumnIndex(COLUMN_SUNRISE))
            WeatherDataForDisplay.sunset = cursor.getString(cursor.getColumnIndex(COLUMN_SUNSET))
            WeatherDataForDisplay.windSpeed = cursor.getDouble(cursor.getColumnIndex(COLUMN_WIND_SPEED))

        }}
    }

    companion object {
        const val CACHE_TABLE_NAME = "cacheTable"
        const val COLUMN_ID = "cityId"
        const val COLUMN_NAME = "cityName"
        const val COLUMN_COUNTRY = "countryName"
        const val COLUMN_LATITUDE = "latitude"
        const val COLUMN_LONGITUDE = "longitude"
        const val COLUMN_CURRENT_DATE = "currentDate"
        const val COLUMN_SKY_DESCRIPTION = "skyDescription"
        const val COLUMN_TEMPERATURE = "temperature"
        const val COLUMN_FEELS_LIKE = "feelsLike"
        const val COLUMN_MIN_TEMPERATURE = "minTemperature"
        const val COLUMN_MAX_TEMPERATURE = "maxTemperature"
        const val COLUMN_SUNRISE = "sunrise"
        const val COLUMN_SUNSET = "sunset"
        const val COLUMN_WIND_SPEED = "windSpeed"


        const val CREATE_CACHE_TABLE_QUERY =
            "CREATE TABLE IF NOT EXISTS " + CACHE_TABLE_NAME + " (" + COLUMN_ID +
                    " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_NAME + " TEXT," +
                    COLUMN_COUNTRY + " TEXT," + COLUMN_CURRENT_DATE + " TEXT," +
                    COLUMN_SKY_DESCRIPTION + " TEXT," + COLUMN_TEMPERATURE + " DOUBLE," +
                    COLUMN_FEELS_LIKE + " DOUBLE," + COLUMN_MIN_TEMPERATURE + " DOUBLE," +
                    COLUMN_MAX_TEMPERATURE + " DOUBLE," + COLUMN_SUNRISE + " DOUBLE," +
                    COLUMN_SUNSET + " DOUBLE," + COLUMN_WIND_SPEED + " DOUBLE);"

        const val INSERT_EMPTY_ROW_QUERY = "INSERT INTO $CACHE_TABLE_NAME DEFAULT VALUES"
    }
}