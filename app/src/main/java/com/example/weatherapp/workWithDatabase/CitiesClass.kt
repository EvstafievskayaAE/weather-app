package com.example.weatherapp.workWithDatabase

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.example.weatherapp.ProjectSettings.CommonSettings
import com.example.weatherapp.R

/** Класс для работы со списком городов и БД*/
class CitiesClass(var context: Context) : DatabaseHelper(context) {

    /** Относящиеся к таблице городов константы */
    companion object {
        /* Список городов из БД */
        private var citiesListFromDb:MutableList<String> = arrayListOf()

        /* Названия полей таблицы для хранения списка городов */

        const val TABLE_NAME = "cities"
        const val COLUMN_ID = "Id"
        const val COLUMN_NAME = "cityName"

        /* SQL-запросы для работы с таблицей списка городов */

        const val CREATE_TABLE_QUERY =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" + COLUMN_ID +
                    " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_NAME + " TEXT NOT NULL);"

        const val DROP_TABLE_QUERY = "DROP TABLE IF EXISTS $TABLE_NAME"

        const val SELECT_ALL_QUERY = "SELECT * FROM $TABLE_NAME ORDER BY $COLUMN_NAME"
    }

    /** Загрузка исходного списка городов из ресурсов в базу данных */
    fun putCitiesListToDB(database: SQLiteDatabase?) {
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

        // Получение начального списка городов из БД
        citiesListFromDb = getCitiesListFromDb()

        // Запись в БД нового определенного по координатам города,
        // если город определен и его еще нет в списке

        if (cityName != "" && !citiesListFromDb.contains(cityName)) {
            contentValues.put(COLUMN_NAME, cityName)
            database.insert(TABLE_NAME, null, contentValues)
        }
    }

    /** Получение списка всех городов из базы данных */
    fun getCitiesListFromDb(): MutableList<String> {
        val citiesList: MutableList<String> = ArrayList()
        val database = this.readableDatabase
        val cursor = database.rawQuery(SELECT_ALL_QUERY, null)
        if (cursor.moveToFirst()) {
            do {
                val cityName = cursor.getString(cursor.getColumnIndex(COLUMN_NAME))
                citiesList.add(cityName)
            } while (cursor.moveToNext())
        }
        return citiesList
    }
}