package com.example.weatherapp.Activities
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.weatherapp.ProjectSettings.CommonSettings
import com.example.weatherapp.R
import com.example.weatherapp.workWithDatabase.CitiesClass
import com.example.weatherapp.workWithDatabase.DatabaseHelper
import kotlinx.android.synthetic.main.activity_choice_city.*

class ChoiceCityActivity : AppCompatActivity(), AdapterView.OnItemClickListener {

    private lateinit var databaseHelper: DatabaseHelper // Объект класса для работы с БД

    private var arrayAdapter:ArrayAdapter<String> ? = null // Адаптер для работы со списком
    private var citiesList:MutableList<String> = arrayListOf() // Список городов для отображения

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choice_city)

        databaseHelper = DatabaseHelper(this)

        // Добавление всех городов из БД в список для отображения
        citiesList = CitiesClass(this).getCitiesListFromDb()

        arrayAdapter = ArrayAdapter(this, R.layout.custom_listview_item, citiesList)

        citiesListView?.adapter = arrayAdapter
        citiesListView?.choiceMode = ListView.CHOICE_MODE_SINGLE
        citiesListView?.onItemClickListener = this

        /** Поиск местоположения и погоды при нажатии ссылки */
        val findMyCityLinkTextView: TextView = findMyCityLinkTextView
        findMyCityLinkTextView.setOnClickListener {
            CommonSettings.isCityNameChosen = false // Установка флага в значение "город не выбран"
            startMainActivity()
        }
    }

    /** Действия при выборе города из списка */
    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        // Получение названия города, выбранного из отображенного списка
        var cityName:String = parent?.getItemAtPosition(position) as String

        CommonSettings.isCityNameChosen = true // Установка флага в значение "город выбран"
        CommonSettings.chosenCityName = cityName // Запись названия выбранного города в переменную

        Toast.makeText(applicationContext, "$cityName is chosen", Toast.LENGTH_LONG).show()
        startMainActivity()
    }

    /**Запуск основной активности */
    private fun startMainActivity(){
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}