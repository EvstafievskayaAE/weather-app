package com.example.weatherapp
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.weatherapp.ProjectConstants.citiesListFileName
import kotlinx.android.synthetic.main.activity_choice_city.*

class ChoiceCityActivity : AppCompatActivity(), AdapterView.OnItemClickListener {

    private var arrayAdapter:ArrayAdapter<String> ? = null
    private var citiesList:MutableList<String> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choice_city)

        fillCitiesList()
        arrayAdapter = ArrayAdapter(applicationContext, R.layout.custom_listview_item, citiesList)

        citiesListView?.adapter = arrayAdapter
        citiesListView?.choiceMode = ListView.CHOICE_MODE_SINGLE
        citiesListView?.onItemClickListener = this
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        var cityName:String = parent?.getItemAtPosition(position) as String

        CommonSettings.isCityNameChosen = true
        CommonSettings.chosenCityName = cityName

        Toast.makeText(applicationContext, "$cityName is chosen", Toast.LENGTH_LONG).show()
        startMainActivity()
    }

    /**Заполнение списка городов*/
    private fun fillCitiesList(){

        //Получение начального списка городов
        citiesList.addAll(resources.getStringArray(R.array.cities))

        //Запись начального списка городов в файл
        for (i in 0 until citiesList.size)
            FileWorkHelper.addLineToFile(citiesList[i], citiesListFileName, applicationContext)

        //Запись в файл нового определенного по координатам города, если его еще нет там
        FileWorkHelper.addLineToFile(
                CommonSettings.newCityName, citiesListFileName, applicationContext)

        //Очистка списка, чтобы исключить повторы
        citiesList.clear()

        //Добавление всех городов из файла в список для отображения
        citiesList.addAll(FileWorkHelper.readLinesFromFile(
                citiesListFileName, applicationContext))
    }

    /**Запуск основной активности */
    private fun startMainActivity(){
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}