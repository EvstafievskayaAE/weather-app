package com.example.weatherapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper


class DatabaseHelper(private var context: Context) : SQLiteOpenHelper(
        context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1

        private const val TABLE_NAME = "cities"
        private const val COLUMN_ID = "cityId"
        private const val COLUMN_NAME = "cityName"
        private const val DATABASE_NAME = "WEATHER DATABASE"

        private const val CREATE_TABLE_QUERY =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" + COLUMN_ID +
                    " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_NAME + " TEXT NOT NULL);"

        private const val DROP_TABLE_QUERY = "DROP TABLE IF EXISTS $TABLE_NAME"

        private const val SELECT_QUERY = "SELECT * FROM $TABLE_NAME ORDER BY $COLUMN_NAME"

    }

    /** Создание базы данных. Заполнение таблицы списка городов исходными значениями */
    override fun onCreate(database: SQLiteDatabase?) {
        database!!.execSQL(CREATE_TABLE_QUERY)
        putCitiesListToDB(database)
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