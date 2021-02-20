package com.example.weatherapp.Activities

import android.Manifest
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.weatherapp.*
import com.example.weatherapp.ProjectSettings.CommonSettings
import com.example.weatherapp.ProjectSettings.ProjectConstants.PROGRESS_BAR_DELAY
import com.example.weatherapp.ProjectSettings.ProjectConstants.REQUEST_GPS_CODE
import com.example.weatherapp.ProjectSettings.ProjectConstants.REQUEST_PERMISSION_LOCATION
import com.example.weatherapp.ProjectSettings.WeatherDataForDisplay
import com.example.weatherapp.R
import com.example.weatherapp.Model.OpenWeatherMap
import com.example.weatherapp.workWithDatabase.CacheDataClass
import com.example.weatherapp.workWithDatabase.CitiesClass
import com.example.weatherapp.workWithDatabase.DatabaseHelper
import com.google.android.gms.location.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import java.io.IOException
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity() {

    // OK HTTP
    private val okHttpClient = OkHttpClient()
    private val okHttpHelper = OkHttpHelper()

    // Для работы с координатами местоположения
    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null
    lateinit var locationManager: LocationManager
    lateinit var mLastLocation: Location
    private lateinit var mLocationRequest: LocationRequest
    private var isLocationPermissionGranted = false

    private var latitude: Double? = null
    private var longitude: Double? = null

    // Сервис для получения данных о погоде
    private var openWeatherMap = OpenWeatherMap()

    // Для работы с БД
    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainContainer.visibility = View.GONE

        databaseHelper = DatabaseHelper(this)

        /* CommonSettings.isCityNameChosen=false*/ // Будет нужно, если убрать автоматический переход со второй активности
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        mLocationRequest = LocationRequest()

        /** Запуск корутины для отображения погоды, если город выбран */
        if (CommonSettings.isCityNameChosen)
            WeatherTask().execute()

        /** Вызов окна включения gps, если дано разрешение на определение местоположения,
         * а город не выбран */
        else displayWeatherForCurrentCity()

        /** Запуск активности для выбора города */
        val changeCityLink: TextView = changeCityLinkTextView
        changeCityLink.setOnClickListener {
            startChoiceCityActivity()
        }

        /** Обновление погоды для установленного города при нажатии кнопки */
        val updateWeatherButton: Button = updateWeatherButton
        updateWeatherButton.setOnClickListener {
            WeatherTask().execute()
        }
    }

    /** Отображение погоды для города, определенного по координатам */
    private fun displayWeatherForCurrentCity(){
        // Проверка наличия разрешения на определение местоположения
        checkPermissionForLocation(this)
        if (isLocationPermissionGranted) {
            // Проверка, включен ли gps
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                buildAlertMessageNoGps()
            // Определение координат местоположения и отображение данных
            startLocationUpdates()
        }
    }

    /** Вывод диалогового окна включения gps */
    private fun buildAlertMessageNoGps() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
            .setCancelable(false)
            .setPositiveButton("Yes") { dialog, id ->
                startActivityForResult(
                    Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    , REQUEST_GPS_CODE
                )
            }
            .setNegativeButton("No") { dialog, id ->
                dialog.cancel()
                errorTextTextView.text = getString(R.string.turn_on_gps)
                displayCachedDataIfAny() // Отображение закэшированных данных, если они есть
            }
        val alert: AlertDialog = builder.create()
        alert.show()
    }

    /** Вывод диалогового окна повторного вызова функции отображения данных
     * при возобновлении интернет-соединения */

    private fun buildAlertDialogWhenNoInternet() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Please, check your internet connection and try again")
            .setCancelable(false)
            .setPositiveButton("TRY AGAIN") { dialog, id ->
                WeatherTask().execute()
            }
            .setNegativeButton("cancel") { dialog, id ->
                dialog.cancel()
                displayCachedDataIfAny() // Отображение закэшированных данных, если они есть
            }
        val alert: AlertDialog = builder.create()
        alert.show()
    }

    /** Запуск активности выбора городов */
    private fun startChoiceCityActivity() {
        val intent = Intent(this, ChoiceCityActivity::class.java)

        // Флаг для очистки истории переходов между активностями
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)

        startActivity(intent)
    }

    /** Запуск обновления координат местоположения */
    private fun startLocationUpdates() {
        // Установка высокой точности определения местоположения (мобильная связь, wi-fi и gps)
        mLocationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

       /* while (latitude==null || longitude == null)
            Toast.makeText(applicationContext, "WAIT, PLEASE", Toast.LENGTH_LONG).show()*/

        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(mLocationRequest!!)
        val locationSettingsRequest = builder.build()

        val settingsClient = LocationServices.getSettingsClient(this)
        settingsClient.checkLocationSettings(locationSettingsRequest)

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(
                this, ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        mFusedLocationProviderClient!!.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation
            onLocationChanged(locationResult.lastLocation)
        }
    }

    /** Действия при смене координат местоположения */
    fun onLocationChanged(location: Location) {
        mLastLocation = location
        latitude = mLastLocation.latitude
        longitude = mLastLocation.longitude

        /** вызов методов корутины для отображения погоды */
        WeatherTask().execute()
    }

    /** Остановка обновления координат местоположения */
    private fun stopLocationUpdates() {
        mFusedLocationProviderClient?.removeLocationUpdates(mLocationCallback)
    }

    /** Действия по результатам проверки разрешения на определение местоположения */
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        if (requestCode == REQUEST_PERMISSION_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                    buildAlertMessageNoGps()
                startLocationUpdates()
            } else {
                isLocationPermissionGranted = false
                Toast.makeText(
                    this@MainActivity,
                    "Permission Denied", Toast.LENGTH_SHORT
                ).show()
                errorTextTextView.text = getString(R.string.give_location_permission)
                displayCachedDataIfAny() // Отображение закэшированных данных, если они есть
            }
        }
    }

    /** Проверка разрешения на определение местоположения */
    private fun checkPermissionForLocation(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (context.checkSelfPermission(ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED
            )
                ActivityCompat.requestPermissions(
                    this, arrayOf(ACCESS_FINE_LOCATION),
                    REQUEST_PERMISSION_LOCATION)
                    else  isLocationPermissionGranted = true
        }
    }

    /** Проверка, есть ли закэшированные данные, и отображение, если есть*/
    private fun displayCachedDataIfAny() {
    // Проверка, есть ли данные для отображения сейчас
        if (WeatherDataForDisplay.cityName != null)
            displayWeatherData()
        else {
            // Получение кэшированных данных из БД, если они там есть
            if (!CacheDataClass(this).isCacheTableEmpty()) {
                CacheDataClass(this).getCacheDataFromDB()
                displayWeatherData()
            } else errorTextTextView.visibility = View.VISIBLE
        }
    }

    /** Функции корутины. Отображение данных о погоде */
    inner class WeatherTask : CoroutineScope {
        private var job: Job = Job()
        override val coroutineContext: CoroutineContext
            get() = Dispatchers.Main + job // Запуск кода в основном потоке

        fun cancel() {
            job.cancel()
        }

        fun execute() = launch {
            onPreExecute()
            val result = doInBackground() // Запуск фонового потока без блокирования основного
            onPostExecute(result)
        }

        private suspend fun doInBackground(): String = withContext(Dispatchers.IO) {
            var response: String // Итоговая ссылка запроса
            var usedApi: HttpUrl // Запрос, отправляемый на сервер
            try {
                // Формирование запроса к серверу по названию выбранного города
                if (CommonSettings.isCityNameChosen) {
                    usedApi =
                        CommonSettings.weatherMapAPIRequestByCityName(
                            CommonSettings.chosenCityName
                        )
                    stopLocationUpdates()
                }
                // Формирование запроса к серверу по определенным координатам
                else usedApi = CommonSettings.weatherMapAPIRequestByLocation(
                    latitude.toString(), longitude.toString()
                )

                // Запись полученной ссылки запроса в итоговую переменную
                response = okHttpHelper.GET(okHttpClient, usedApi).toString()

            } catch (exception: IOException) {
                exception.printStackTrace()
                response = null.toString()
            }
            return@withContext response
        }

        private suspend fun onPreExecute() {
            // Запуск прогресс бара при подготовке данных от сервера
            loaderProgressBar.visibility = View.VISIBLE
            mainContainer.visibility = View.GONE
            errorTextTextView.visibility = View.GONE
            delay(PROGRESS_BAR_DELAY) // Задержка, чтобы при обновлении успевал прокрутиться прогресс бар
        }

        private fun onPostExecute(result: String) {
            try {
                val gson = Gson()
                val typeToken = object : TypeToken<OpenWeatherMap>() {}.type

                // Получение данных о погоде с сервера
                openWeatherMap = gson.fromJson<OpenWeatherMap>(result, typeToken)

                /** Запись полученных данных в переменные для отображения*/
                writeOpenWeatherMapDataToWeatherDataForDisplay()

                /** Получение кэшированных данных из БД */
                displayWeatherData() // Отображение погоды на экране

                /** Вывод иконки в соответствии с погодой */
                Picasso.with(this@MainActivity)
                    .load(CommonSettings.getImage(openWeatherMap.weather!![0].icon!!))
                    .into(imageView)

                // Запись нового города в переменную
                if (!CommonSettings.isCityNameChosen) {
                    CommonSettings.newCityName = "${openWeatherMap.name}"
                    CitiesClass(this@MainActivity).addNewCityToDb(CommonSettings.newCityName)
                }

            } catch (exception: Exception) {
                loaderProgressBar.visibility = View.GONE

                /** Исключение возникает, если корутина не может получить данные.
                 * Первая причина - отсутствие интернета.
                 * Вторая - отсутствие разрешения на определение местоположения
                 * Третья - разрешение дано, но выключен gps*/
                if (!InternetConnection(this@MainActivity).isNetworkConnected())
                buildAlertDialogWhenNoInternet()
                else displayWeatherForCurrentCity()

            }
        }
    }

    /** Запись данных о погоде, полученных с сервера, в переменные */
    fun writeOpenWeatherMapDataToWeatherDataForDisplay() {
        WeatherDataForDisplay.cityName = openWeatherMap.name
        WeatherDataForDisplay.country = openWeatherMap.sys!!.country

        WeatherDataForDisplay.currentDate = CommonSettings.currentDate

        WeatherDataForDisplay.skyDescription = openWeatherMap.weather!![0].description

        WeatherDataForDisplay.temperature = openWeatherMap.main!!.temp
        WeatherDataForDisplay.feelsLike = openWeatherMap.main!!.feels_like
        WeatherDataForDisplay.minTemperature = openWeatherMap.main!!.temp_min
        WeatherDataForDisplay.maxTemperature = openWeatherMap.main!!.temp_max

        WeatherDataForDisplay.sunrise =
            CommonSettings.convertUnixTimeStampToDateTime(openWeatherMap.sys!!.sunrise)
        WeatherDataForDisplay.sunset =
            CommonSettings.convertUnixTimeStampToDateTime(openWeatherMap.sys!!.sunset)

        WeatherDataForDisplay.windSpeed = openWeatherMap.wind!!.speed

    }

    /** Отображение данных о погоде на экране */
    fun displayWeatherData() {
        addressTextView.text = "${WeatherDataForDisplay.cityName}, ${WeatherDataForDisplay.country}"

        updatedAtTextView.text = WeatherDataForDisplay.currentDate

        skyDescriptionTextView.text = "${WeatherDataForDisplay.skyDescription}"

        tempTextView.text = "${WeatherDataForDisplay.temperature.toInt()}°C"
        feelsLikeTextView.text = "feels like: ${WeatherDataForDisplay.feelsLike} °C"
        tempMinTextView.text = "min temp: ${WeatherDataForDisplay.minTemperature} °C"
        tempMaxTextView.text = "max temp: ${WeatherDataForDisplay.maxTemperature} °C"

        sunriseTextView.text = WeatherDataForDisplay.sunrise
        sunsetTextView.text = WeatherDataForDisplay.sunset
        windTextView.text = "${WeatherDataForDisplay.windSpeed} м/с"


        loaderProgressBar.visibility = View.GONE
        mainContainer.visibility = View.VISIBLE
    }

    /** Сохранение данных при свертывании активности */
    override fun onPause() {
        super.onPause()

        // Запись кэшированных данных в таблицу БД.
        if (WeatherDataForDisplay.cityName != null)
            CacheDataClass(this).updateCacheTable(
                WeatherDataForDisplay.cityName,
                WeatherDataForDisplay.country,
                WeatherDataForDisplay.currentDate,
                WeatherDataForDisplay.skyDescription,
                WeatherDataForDisplay.temperature,
                WeatherDataForDisplay.feelsLike,
                WeatherDataForDisplay.minTemperature,
                WeatherDataForDisplay.maxTemperature,
                WeatherDataForDisplay.sunrise,
                WeatherDataForDisplay.sunset,
                WeatherDataForDisplay.windSpeed
            )
    }

}