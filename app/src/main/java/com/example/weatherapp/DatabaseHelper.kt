package com.example.weatherapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper


class DatabaseHelper(private var context: Context) : SQLiteOpenHelper(
        context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "WEATHER DATABASE"

        private const val TABLE_NAME = "cities"
        private const val CACHE_TABLE_NAME = "cacheTable"

        private const val COLUMN_ID = "cityId"
        private const val COLUMN_NAME = "cityName"
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
        private const val COLUMN_WIND_SPEED = "windSpeed"

        private const val CREATE_TABLE_QUERY =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" + COLUMN_ID +
                    " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_NAME + " TEXT NOT NULL);"

        private const val CREATE_CACHE_TABLE_QUERY =
            "CREATE TABLE IF NOT EXISTS " + CACHE_TABLE_NAME + " (" + COLUMN_ID +
                    " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_NAME + " TEXT," +
                    COLUMN_COUNTRY + " TEXT," + COLUMN_LATITUDE + " DOUBLE," +
                    COLUMN_LONGITUDE + " DOUBLE," + COLUMN_CURRENT_DATE + " TEXT," +
                    COLUMN_SKY_DESCRIPTION + " TEXT," + COLUMN_TEMPERATURE + " DOUBLE," +
                    COLUMN_FEELS_LIKE + " DOUBLE," + COLUMN_MIN_TEMPERATURE + " DOUBLE," +
                    COLUMN_MAX_TEMPERATURE + " DOUBLE," + COLUMN_SUNRISE + " DOUBLE," +
                    COLUMN_SUNSET + " DOUBLE," + COLUMN_WIND_SPEED + " DOUBLE);"

        private const val INSERT_EMPTY_ROW_QUERY = "INSERT INTO $CACHE_TABLE_NAME DEFAULT VALUES"

        private const val DROP_TABLE_QUERY = "DROP TABLE IF EXISTS $TABLE_NAME"

        private const val SELECT_QUERY = "SELECT * FROM $TABLE_NAME ORDER BY $COLUMN_NAME"

    }

    /** Создание базы данных. Заполнение таблицы списка городов исходными значениями */
    override fun onCreate(database: SQLiteDatabase?) {
        database!!.execSQL(CREATE_TABLE_QUERY) // Создание таблицы хранимого списка городов
        database.execSQL(CREATE_CACHE_TABLE_QUERY) // Создание таблицы для закэшированных данных
        database.execSQL(INSERT_EMPTY_ROW_QUERY) // Вставка пустой строки в таблицу закэшированных данных
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
}