package com.example.weatherapp

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
import com.example.weatherapp.Model.OpenWeatherMap
import com.example.weatherapp.ProjectConstants.PROGRESS_BAR_DELAY
import com.example.weatherapp.ProjectConstants.REQUEST_GPS_CODE
import com.example.weatherapp.ProjectConstants.REQUEST_PERMISSION_LOCATION
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
    lateinit var  locationManager: LocationManager
    lateinit var mLastLocation: Location
    private lateinit var mLocationRequest: LocationRequest

    private var latitude:Double? = null
    private var longitude:Double? = null

    // Сервис для получения данных о погоде
    private var openWeatherMap = OpenWeatherMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainContainer.visibility = View.GONE

       /* CommonSettings.isCityNameChosen=false*/ // Будет нужно, если убрать автоматический переход со второй активности
        locationManager =
                getSystemService(Context.LOCATION_SERVICE) as LocationManager

        mLocationRequest = LocationRequest()

        /** Вызов окна включения gps, если дано разрешение на определение местоположения,
         * а город не выбран */
        if (checkPermissionForLocation(this)) {
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) &&
                !CommonSettings.isCityNameChosen)
                buildAlertMessageNoGps()
            startLocationUpdates()
        }

        /** Запуск корутины для отображения погоды, если город выбран */
        if (CommonSettings.isCityNameChosen)
            WeatherTask().execute()

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

    /** Вывод диалогового окна включения gps */
    private fun buildAlertMessageNoGps() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes") { dialog, id ->
                    startActivityForResult(
                            Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                            , REQUEST_GPS_CODE)
                }
                .setNegativeButton("No") { dialog, id ->
                    dialog.cancel()
                    // Добавить сообщение или загрузку закешированных данных.
                    // Сейчас будет выводиться просто фон
                }
        val alert: AlertDialog = builder.create()
        alert.show()
    }

    /** Запуск активности выбора городов */
    private fun startChoiceCityActivity(){
        val intent = Intent(this, ChoiceCityActivity::class.java)
        // Флаг для очистки истории переходов между активностями
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        startActivity(intent)
    }

    /** Запуск обновления координат местоположения */
    private fun startLocationUpdates() {
        // Установка высокой точности определения местоположения (мобильная связь, wi-fi и gps)
        mLocationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        /*mLocationRequest!!.setInterval(INTERVAL)
        mLocationRequest!!.setFastestInterval(FASTEST_INTERVAL)*/  //если понадобится автоматическое обновление координат

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
        mFusedLocationProviderClient!!.requestLocationUpdates(mLocationRequest, mLocationCallback,
                Looper.myLooper())
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
        mFusedLocationProviderClient!!.removeLocationUpdates(mLocationCallback)
    }

    /** Действия по результатам проверки разрешения на определение местоположения */
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_PERMISSION_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                    buildAlertMessageNoGps()
                startLocationUpdates()
            } else {
                Toast.makeText(this@MainActivity,
                    "Permission Denied", Toast.LENGTH_SHORT).show()
                // Добавить загрузку закешированных данных.
                // Сейчас будет выводиться просто фон и тост
            }
        }
    }

    /** Проверка разрешения на определение местоположения */
    private fun checkPermissionForLocation(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (context.checkSelfPermission(ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED)
                true
            else {
                ActivityCompat.requestPermissions(this, arrayOf(ACCESS_FINE_LOCATION),
                        REQUEST_PERMISSION_LOCATION)
                false
            }
        } else true
    }

    /** Функции корутины. Отображение данных о погоде */
    inner class WeatherTask:CoroutineScope{
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
            var response:String // Итоговая ссылка запроса
            var usedApi: HttpUrl // Запрос, отправляемый на сервер
            try {
                // Формирование запроса к серверу по названию выбранного города
                if (CommonSettings.isCityNameChosen){
                    usedApi =
                        CommonSettings.weatherMapAPIRequestByCityName(CommonSettings.chosenCityName)
                    stopLocationUpdates()
                }
                // Формирование запроса к серверу по определенным координатам
                else usedApi = CommonSettings.weatherMapAPIRequestByLocation(
                        latitude.toString(), longitude.toString())

                // Запись полученной ссылки запроса в итоговую переменную
                response = okHttpHelper.GET(okHttpClient,usedApi).toString()
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
                val typeToken = object:TypeToken<OpenWeatherMap>(){}.type

                // Получение данных о погоде с сервера
                openWeatherMap = gson.fromJson<OpenWeatherMap>(result, typeToken)

                // Отображение полученных данных на экране
                addressTextView.text = "${openWeatherMap.name},${openWeatherMap.sys!!.country}"
                updatedAtTextView.text = "${CommonSettings.currentDate}"
                skyDescriptionTextView.text = "${openWeatherMap.weather!![0].description}"
                tempTextView.text = "${openWeatherMap.main!!.temp.toInt()}°C"
                feelsLikeTextView.text = "feels like: ${openWeatherMap.main!!.feels_like} °C"
                tempMinTextView.text = "min temp: ${openWeatherMap.main!!.temp_min} °C"
                tempMaxTextView.text = "max temp: ${openWeatherMap.main!!.temp_max} °C"
                sunriseTextView.text =
                        CommonSettings.convertUnixTimeStampToDateTime(openWeatherMap.sys!!.sunrise)
                sunsetTextView.text =
                        CommonSettings.convertUnixTimeStampToDateTime(openWeatherMap.sys!!.sunset)
                windTextView.text = "${openWeatherMap.wind!!.speed} м/с"

                Picasso.with(this@MainActivity)
                    .load(CommonSettings.getImage(openWeatherMap.weather!![0].icon!!))
                    .into(imageView)

                loaderProgressBar.visibility = View.GONE
                mainContainer.visibility = View.VISIBLE
            } catch (exception: Exception) {
                loaderProgressBar.visibility = View.GONE
                errorTextTextView.visibility = View.VISIBLE
            }

            // Запись нового города в переменную
            if (!CommonSettings.isCityNameChosen)
            CommonSettings.newCityName = "${openWeatherMap.name}"

        }
    }
}