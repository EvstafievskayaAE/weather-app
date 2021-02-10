package com.example.weatherapp

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast
import com.example.weatherapp.Model.City

const val DATABASE_VERSION = 1
const val DATABASE_NAME = "WEATHER DATABASE"
const val TABLE_NAME = "cities"
const val COLUMN_ID = "cityId"
const val COLUMN_NAME = "cityName"

class DataBaseHelper(var context: Context) : SQLiteOpenHelper(
        context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(dataBase: SQLiteDatabase?) {
        val createTable = "CREATE TABLE IF NOT EXISTS TABLE" + TABLE_NAME + " (" + COLUMN_ID +
                " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_NAME + " TEXT NOT NULL)"
        dataBase?.execSQL(createTable)
       /* putCitiesListToDB(dataBase)*/
    }

    override fun onUpgrade(dataBase: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        onCreate(dataBase)
    }

    private fun putCitiesListToDB(dataBase: SQLiteDatabase?){
        val contentValues = ContentValues()
        val sourceCitiesList:MutableList<String> =
                context.resources.getStringArray(R.array.cities).toMutableList()
        for (i in 0 until sourceCitiesList.size) {
            contentValues.put(COLUMN_NAME, sourceCitiesList[i])
            dataBase?.insert(TABLE_NAME, null, contentValues)
        }
    }
     fun putCitiesListToDB1(values:List<String>){
        val dataBase = this.writableDatabase
        val contentValues = ContentValues()
        for (element in values) {
            contentValues.put(COLUMN_NAME, element)
            dataBase?.insert(TABLE_NAME, null, contentValues)
        }
    }

    private fun createContentValues():ContentValues {
        val contentValues = ContentValues()
        val sourceCitiesList:MutableList<String> =
                context.resources.getStringArray(R.array.cities).toMutableList()
        for (i in 0 until sourceCitiesList.size) {
            contentValues.put(COLUMN_NAME, sourceCitiesList[i])
        }
        return contentValues
    }

    fun addNewCityToDb(cityName: String) {
        val dataBase = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COLUMN_NAME, cityName)
        val result = dataBase.insert(TABLE_NAME, null, contentValues)
        if (result == (0).toLong()) {
            Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show()
        }
        else {
            Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show()
        }
    }

    fun getCitiesListFromDb(): MutableList<String> {
        val citiesList: MutableList<String> = ArrayList()
        val dataBase = this.readableDatabase
        val query = "Select * from $TABLE_NAME"
        val result = dataBase.rawQuery(query, null)
        if (result.moveToFirst()) {
            do {
                val cityName =result.getString(result.getColumnIndex(COLUMN_NAME))
                citiesList.add(cityName)
            }
            while (result.moveToNext())
        }
        return citiesList
    }
}