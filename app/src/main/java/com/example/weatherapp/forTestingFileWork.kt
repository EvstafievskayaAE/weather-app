package com.example.weatherapp

import android.content.Context
import java.io.File
import java.io.FileWriter


fun main(){
    var list =readFileAsLinesUsingUseLines("test.txt")
    addListToFile(list,"TEST2.txt")
    var a:String = list.get(0)
    }

    fun addLineToFile(addedLine: String, fileName: String) {
        val writer = FileWriter(fileName,true)
        writer.use { out ->
            out.write("\n${addedLine}")
        }
    }



    fun addListToFile(addedList: List<String>, fileName: String) {
        val writer = FileWriter(fileName,true)
        writer.use { out ->
            out.write("\n${addedList}")
        }
    }

    fun readFileAsLinesUsingBufferedReader(fileName: String): List<String>
        = File(fileName).bufferedReader().readLines()

    fun readFileAsLinesUsingUseLines(fileName: String): List<String>
        = File(fileName).useLines { it.toList() }