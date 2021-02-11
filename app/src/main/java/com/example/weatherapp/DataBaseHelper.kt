package com.example.weatherapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper


class DataBaseHelper(context: Context) : SQLiteOpenHelper(
        context, DATABASE_NAME, null, DATABASE_VERSION) {

    var context = context

    companion object {
        const val DATABASE_VERSION = 1
        const val TABLE_NAME = "cities"
        const val COLUMN_ID = "cityId"
        const val COLUMN_NAME = "cityName"
        const val DATABASE_NAME = "WEATHER DATABASE"
    }

    override fun onCreate(database: SQLiteDatabase?) {
        val createTable = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" + COLUMN_ID +
                " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_NAME + " TEXT NOT NULL);"
        database!!.execSQL(createTable)
        putCitiesListToDB(database)

    }

    override fun onUpgrade(database: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        database!!.execSQL("DROP TABLE IF EXISTS " + Companion.DATABASE_NAME)
        onCreate(database)
    }

    private fun putCitiesListToDB(database: SQLiteDatabase?) {
        val contentValues = ContentValues()
        val sourceCitiesList: MutableList<String> =
                context.resources.getStringArray(R.array.cities).toMutableList()
        for (i in 0 until sourceCitiesList.size) {
            contentValues.put(COLUMN_NAME, sourceCitiesList[i])
            database!!.insert(TABLE_NAME, null, contentValues)
        }
    }

    fun addNewCityToDb(cityName: String) {
        val database = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COLUMN_NAME, cityName)
        database.insert(TABLE_NAME, null, contentValues)
    }

    fun getCitiesListFromDb(): MutableList<String> {
        val citiesList: MutableList<String> = ArrayList()
        val database = this.readableDatabase
        val query = "Select * from " + Companion.TABLE_NAME + " order by " + COLUMN_NAME
        val cursor = database.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            do {
                val cityName = cursor.getString(cursor.getColumnIndex(COLUMN_NAME))
                citiesList.add(cityName)
            } while (cursor.moveToNext())
        }
        return citiesList
    }
}