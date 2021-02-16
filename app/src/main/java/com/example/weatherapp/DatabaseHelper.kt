package com.example.weatherapp

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper


open class DatabaseHelper(private var context: Context) : SQLiteOpenHelper(
    context, DATABASE_NAME, null, DATABASE_VERSION
) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "WEATHER DATABASE"

        private const val TABLE_NAME = "cities"
        private const val COLUMN_ID = "cityId"
        private const val COLUMN_NAME = "cityName"

        /*private const val CACHE_TABLE_NAME = "cacheTable"

        private const val COLUMN_COUNTRY = "countryName"
        private const val COLUMN_LATITUDE = "latitude"
        private const val COLUMN_LONGITUDE = "longitude"
        private const val COLUMN_CURRENT_DATE = "currentDate"
        private const val COLUMN_SKY_DESCRIPTION = "skyDescription"
        private const val COLUMN_TEMPERATURE = "temperature"
        private const val COLUMN_FEELS_LIKE = "feelsLike"
        private const val COLUMN_MIN_TEMPERATURE = "minTemperature"
        private const val COLUMN_MAX_TEMPERATURE = "maxTemperature"
        private const val COLUMN_SUNRISE = "sunrise"
        private const val COLUMN_SUNSET = "sunset"
        private const val COLUMN_WIND_SPEED = "windSpeed"*/

        private const val CREATE_TABLE_QUERY =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" + COLUMN_ID +
                    " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_NAME + " TEXT NOT NULL);"



        private const val DROP_TABLE_QUERY = "DROP TABLE IF EXISTS $TABLE_NAME"

        private const val SELECT_QUERY = "SELECT * FROM $TABLE_NAME ORDER BY $COLUMN_NAME"

    }

    /** Создание базы данных. Заполнение таблицы списка городов исходными значениями */
    override fun onCreate(database: SQLiteDatabase?) {
        database!!.execSQL(CREATE_TABLE_QUERY) // Создание таблицы хранимого списка городов
        database.execSQL(CacheDataClass.CREATE_CACHE_TABLE_QUERY) // Создание таблицы для закэшированных данных
        database.execSQL(CacheDataClass.INSERT_EMPTY_ROW_QUERY) // Вставка пустой строки в таблицу закэшированных данных
        putCitiesListToDB(database)  // Заполнение таблицы хранимого списка городов
    }

    /** Обновление базы данных */
    override fun onUpgrade(database: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        database!!.execSQL(DROP_TABLE_QUERY)
        onCreate(database)
    }

    /**Загрузка исходного списка городов из ресурсов в базу данных */
    private fun putCitiesListToDB(database: SQLiteDatabase?) {
        val contentValues = ContentValues()
        val sourceCitiesList: MutableList<String> =
            context.resources.getStringArray(R.array.cities).toMutableList()
        for (i in 0 until sourceCitiesList.size) {
            contentValues.put(COLUMN_NAME, sourceCitiesList[i])
            database!!.insert(TABLE_NAME, null, contentValues)
        }
    }

    /** Добавление нового города в базу данных */
    fun addNewCityToDb(cityName: String) {
        val database = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COLUMN_NAME, cityName)
        database.insert(TABLE_NAME, null, contentValues)
    }

    /** Получение списка всех городов из базы данных */
    fun getCitiesListFromDb(): MutableList<String> {
        val citiesList: MutableList<String> = ArrayList()
        val database = this.readableDatabase
        val cursor = database.rawQuery(SELECT_QUERY, null)
        if (cursor.moveToFirst()) {
            do {
                val cityName = cursor.getString(cursor.getColumnIndex(COLUMN_NAME))
                citiesList.add(cityName)
            } while (cursor.moveToNext())
        }
        return citiesList
    }

    fun openDatabaseForWriting(): SQLiteDatabase? {
        return writableDatabase
    }
 /*   *//** Обновление кэшированных данных о погоде для последнего отображенного города *//*
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

        values.put(COLUMN_NAME, cityName)
        values.put(COLUMN_COUNTRY, country)
        values.put(COLUMN_CURRENT_DATE, currentDate)
        values.put(COLUMN_SKY_DESCRIPTION, skyDescription)
        values.put(COLUMN_TEMPERATURE, temperature)
        values.put(COLUMN_FEELS_LIKE, feelsLike)
        values.put(COLUMN_MIN_TEMPERATURE, minTemperature)
        values.put(COLUMN_MAX_TEMPERATURE, maxTemperature)
        values.put(COLUMN_SUNRISE, sunrise)
        values.put(COLUMN_SUNSET, sunset)
        values.put(COLUMN_WIND_SPEED, windSpeed)

        database.update(CACHE_TABLE_NAME, values, "$COLUMN_ID=1", arrayOf())

    }

    *//** Получение закэшированных данных из БД *//*
    fun getCacheDataFromDB(){
        val query: String
        val cursor: Cursor

        val database = this.readableDatabase
        query = "SELECT  * FROM $CACHE_TABLE_NAME"
        cursor = database.rawQuery(query, null)

        while (cursor.moveToNext()) {
            CacheData.cityName = cursor.getString(cursor.getColumnIndex(COLUMN_NAME))
            CacheData.country = cursor.getString(cursor.getColumnIndex(COLUMN_COUNTRY))
            CacheData.currentDate = cursor.getString(cursor.getColumnIndex(COLUMN_CURRENT_DATE))
            CacheData.skyDescription = cursor.getString(cursor.getColumnIndex(COLUMN_SKY_DESCRIPTION))
            CacheData.temperature = cursor.getDouble(cursor.getColumnIndex(COLUMN_TEMPERATURE))
            CacheData.feelsLike = cursor.getDouble(cursor.getColumnIndex(COLUMN_FEELS_LIKE))
            CacheData.minTemperature = cursor.getDouble(cursor.getColumnIndex(COLUMN_MIN_TEMPERATURE))
            CacheData.maxTemperature = cursor.getDouble(cursor.getColumnIndex(COLUMN_MAX_TEMPERATURE))
            CacheData.sunrise = cursor.getString(cursor.getColumnIndex(COLUMN_SUNRISE))
            CacheData.sunset = cursor.getString(cursor.getColumnIndex(COLUMN_SUNSET))
            CacheData.windSpeed = cursor.getDouble(cursor.getColumnIndex(COLUMN_WIND_SPEED))

        }
    }*/
}