package com.example.weatherapp

import android.content.Context
import java.io.FileDescriptor
import java.io.FileInputStream
import java.io.FileOutputStream

object FileWorkHelper{

    /**Добавление строки в файл*/
    fun addLineToFile(addedLine: String, fileName: String, context: Context) {
        val fileOutputStream: FileOutputStream=
            context.openFileOutput(fileName, Context.MODE_PRIVATE)
        fileOutputStream.write("${addedLine}\n".toByteArray())
        fileOutputStream.close()
    }

    /**Чтение строки из файла*/
    fun readLinesFromFile(fileName: String, context: Context):List<String>{
        val fileInputStream: FileInputStream = context.openFileInput(fileName)
        var readLines= fileInputStream.bufferedReader().readLines()
        fileInputStream.close()
        return readLines
    }

}