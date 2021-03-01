package com.example.weatherapp.workWithDatabase

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/** Класс для работы с БД.
 * Создание, инициализация начальных данных, обновление при изменении версии */

open class DatabaseHelper(private var context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME, null,
    DATABASE_VERSION
) {

    /** Основные параметры БЛ */
    companion object {
        private const val DATABASE_VERSION = 2
        private const val DATABASE_NAME = "WEATHER DATABASE"
    }

    /** Создание базы данных,
     * содержащей таблицу списка городов и таблицу для хранения кэшированных данных */
    override fun onCreate(database: SQLiteDatabase?) {
        database!!.execSQL(CitiesClass.CREATE_TABLE_QUERY) // Создание таблицы хранимого списка городов
        database.execSQL(CacheDataClass.CREATE_CACHE_TABLE_QUERY) // Создание таблицы для закэшированных данных
        database.execSQL(CacheDataClass.INSERT_EMPTY_ROW_QUERY) // Вставка пустой строки в таблицу закэшированных данных
        CitiesClass(context).putCitiesListToDB(database)  // Заполнение таблицы хранимого списка городов
    }

    /** Обновление базы данных */
    override fun onUpgrade(database: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        database!!.execSQL(CitiesClass.DROP_TABLE_QUERY)
        database.execSQL(CacheDataClass.DROP_TABLE_QUERY)
        onCreate(database)
    }
}