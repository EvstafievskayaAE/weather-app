package com.example.weatherapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_searching_city.*

class SearchingCityActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_searching_city)

        val arrayAdapter: ArrayAdapter<*>

        val cities = arrayOf(
            "Tokio", "Moscow", "San-Francisco", "Vladivostok", "Durban"
        )

        arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, cities)
        citiesListView.adapter = arrayAdapter


    }
}