package com.example.weatherapp.Activities
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.weatherapp.workWithDatabase.CitiesClass
import com.example.weatherapp.workWithDatabase.DatabaseHelper
import com.example.weatherapp.ProjectSettings.CommonSettings
import com.example.weatherapp.R
import kotlinx.android.synthetic.main.activity_choice_city.*

class ChoiceCityActivity : AppCompatActivity(), AdapterView.OnItemClickListener {

    private lateinit var databaseHelper: DatabaseHelper // Объект класса для работы с БД

    private var arrayAdapter:ArrayAdapter<String> ? = null // Адаптер для работы со списком
    private var citiesListFromDb:MutableList<String> = arrayListOf() // Список городов из БД
    private var citiesList:MutableList<String> = arrayListOf() // Список городов для отображения

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choice_city)

        databaseHelper = DatabaseHelper(this)

        fillCitiesList() // Заполнение списка городов

        arrayAdapter = ArrayAdapter(this, R.layout.custom_listview_item, citiesList)

        citiesListView?.adapter = arrayAdapter
        citiesListView?.choiceMode = ListView.CHOICE_MODE_SINGLE
        citiesListView?.onItemClickListener = this
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        // Получение названия города, выбранного з отображенного списка
        var cityName:String = parent?.getItemAtPosition(position) as String

        CommonSettings.isCityNameChosen = true // Установка флага в значение "город выбран"
        CommonSettings.chosenCityName = cityName // Запись названия выбранного города в переменную

        Toast.makeText(applicationContext, "$cityName is chosen", Toast.LENGTH_LONG).show()
        startMainActivity()
    }

    /**Заполнение списка городов*/
    private fun fillCitiesList(){
        // Получение начального списка городов из БД
        citiesListFromDb = CitiesClass(this).getCitiesListFromDb()

        // Запись в БД нового определенного по координатам города,
        // если город определен и его еще нет в списке

        if (CommonSettings.newCityName != ""
            && !citiesListFromDb.contains(CommonSettings.newCityName))
            CitiesClass(this).addNewCityToDb(CommonSettings.newCityName)

        // Добавление всех городов из БД в список для отображения
        citiesList = CitiesClass(this).getCitiesListFromDb()
    }

    /**Запуск основной активности */
    private fun startMainActivity(){
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}