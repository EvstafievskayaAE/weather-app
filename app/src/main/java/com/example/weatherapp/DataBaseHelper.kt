package com.example.weatherapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast
import com.example.weatherapp.Model.City

val DATABASE_NAME = "MY DATABASE"
val TABLE_NAME = "Cities"
val COLUMN_ID = "cityId"
val COLUMN_NAME = "cityName"

class DataBaseHelper(var context: Context) : SQLiteOpenHelper(
        context, DATABASE_NAME, null, 1) {
    override fun onCreate(dataBase: SQLiteDatabase?) {
        val createTable = "CREATE TABLE " + TABLE_NAME + " (" + COLUMN_ID +
                " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_NAME + " VARCHAR(256))"
        dataBase?.execSQL(createTable)
    }

    override fun onUpgrade(dataBase: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("Not yet implemented")
    }

    fun insertData(city: City) {
        val database = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COLUMN_NAME, city.cityName)
        val result = database.insert(TABLE_NAME, null, contentValues)
        if (result == (0).toLong()) {
            Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show()
        }
        else {
            Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show()
        }
    }

    fun readData(): MutableList<City> {
        val list: MutableList<City> = ArrayList()
        val dataBase = this.readableDatabase
        val query = "Select * from $TABLE_NAME"
        val result = dataBase.rawQuery(query, null)
        if (result.moveToFirst()) {
            do {
                val city = City(result.getString(result.getColumnIndex(COLUMN_ID)).toInt(),
                        result.getString(result.getColumnIndex(COLUMN_NAME)))
                list.add(city)
            }
            while (result.moveToNext())
        }
        return list
    }
}