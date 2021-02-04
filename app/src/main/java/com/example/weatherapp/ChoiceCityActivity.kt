package com.example.weatherapp
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
    private var citiesList:MutableList<String> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choice_city)

        //Запись в файл нового определенного по координатам города
        FileWorkHelper.addLineToFile(CommonSettings.newCityName, "citiesList.txt", applicationContext)

        //Добавление изначального списка городов в список для отображения
        citiesList.addAll(resources.getStringArray(R.array.cities))
        //Добавление городов, определенных по координатам, в список для отображения
        citiesList.addAll(FileWorkHelper.readLinesFromFile("citiesList.txt",applicationContext))

        arrayAdapter= ArrayAdapter(applicationContext,R.layout.custom_listview_item,citiesList)

        citiesListView?.adapter = arrayAdapter
        citiesListView?.choiceMode = ListView.CHOICE_MODE_SINGLE
        citiesListView?.onItemClickListener = this
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        var cityName:String = parent?.getItemAtPosition(position) as String

        CommonSettings.isCityNameChosen = true
        CommonSettings.chosenCityName = cityName

        Toast.makeText(applicationContext, "$cityName is chosen", Toast.LENGTH_LONG).show()
    }
}