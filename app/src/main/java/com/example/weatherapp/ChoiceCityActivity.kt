package com.example.weatherapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_choice_city.*


class ChoiceCityActivity : AppCompatActivity(), AdapterView.OnItemClickListener {


    private var arrayAdapter:ArrayAdapter<String> ? = null
//    private val cities = arrayOf(
//            "Tokyo", "Moscow", "San-Francisco", "Vladivostok", "Durban"
//    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choice_city)


        arrayAdapter= ArrayAdapter(applicationContext,R.layout.custom_listview_item,
                resources.getStringArray(R.array.cities))
        citiesListView?.adapter = arrayAdapter
        citiesListView?.choiceMode = ListView.CHOICE_MODE_SINGLE
        citiesListView?.onItemClickListener = this

    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        var cityName:String = parent?.getItemAtPosition(position) as String
        val intent = Intent(this@ChoiceCityActivity, MainActivity::class.java)
        //Создаем данные для передачи:

        CommonSettings.isCityName = true
        CommonSettings.chosenCityName = cityName
        /*intent.putExtra("cityName", cityName)*/
        Toast.makeText(applicationContext, "$cityName is chosen", Toast.LENGTH_LONG).show()
        //Запускаем переход:
        /*startActivity(intent)*/
    }
}