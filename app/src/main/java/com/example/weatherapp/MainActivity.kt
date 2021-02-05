package com.example.weatherapp

import android.Manifest
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.weatherapp.Model.OpenWeatherMap
import com.example.weatherapp.ProjectConstants.PROGRESS_BAR_DELAY
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

    lateinit var cityName:String

    //OK HTTP
    private val okHttpClient = OkHttpClient()
    private val okHttpHelper = OkHttpHelper()

    //Для работы с координатами местоположения
    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null
    lateinit var mLastLocation: Location
    private lateinit var mLocationRequest: LocationRequest

    private var latitude:Double? = null
    private var longitude:Double? = null

    //Сервис для получения данных о погоде
    private var openWeatherMap = OpenWeatherMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainContainer.visibility = View.GONE

       /* CommonSettings.isCityNameChosen=false*/ //будет нужно, если убрать автоматический переход со второй активности

        mLocationRequest = LocationRequest()

        if (checkPermissionForLocation(this))
            startLocationUpdates()

        val changeCityLink: TextView = changeCityLinkTextView
        changeCityLink.setOnClickListener {
            startChoiceCityActivity()
        }

        val updateWeatherButton: Button = updateWeatherButton
        updateWeatherButton.setOnClickListener {
            WeatherTask().execute()
        }
    }

    /**Запуск активности выбора городов*/
    private fun startChoiceCityActivity(){
        val intent = Intent(this, ChoiceCityActivity::class.java)
        startActivity(intent)
    }

    /**Запуск обновления координат местоположения*/
    protected fun startLocationUpdates() {
        //Установка высокой точности определения местоположения
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
                        this,
                        ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
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

    /**Действия при смене координат местоположения*/
    fun onLocationChanged(location: Location) {
        mLastLocation = location
        latitude = mLastLocation.latitude
        longitude = mLastLocation.longitude

        /**вызов методов корутины*/
        WeatherTask().execute()
    }

    /**Остановка обновления координат местоположения*/
    private fun stoplocationUpdates() {
        mFusedLocationProviderClient!!.removeLocationUpdates(mLocationCallback)
    }

    /**проверка разрешения на определение местоположения*/
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_PERMISSION_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates()
            } else {
                Toast.makeText(this@MainActivity,
                    "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**проверка разрешения на определение местоположения*/
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

    /**COROUTINS function, displays weather data*/
    inner class WeatherTask:CoroutineScope{
        private var job: Job = Job()
        override val coroutineContext: CoroutineContext
            get() = Dispatchers.Main + job //запуск кода в основном потоке
        fun cancel() {
            job.cancel()
        }

        fun execute() = launch {
                onPreExecute()
                val result = doInBackground() //запуск фонового потока без блоеирования основного
                onPostExecute(result)
        }
        private suspend fun doInBackground(): String = withContext(Dispatchers.IO) {
            var response:String
            var useApi: HttpUrl
            try {
                if (CommonSettings.isCityNameChosen){
                    cityName = CommonSettings.chosenCityName
                    useApi = CommonSettings.weatherMapAPIRequestByCityName(cityName)
                    stoplocationUpdates()
                }
                else useApi = CommonSettings.weatherMapAPIRequestByLocation(
                        latitude.toString(), longitude.toString())

                response = okHttpHelper.GET(okHttpClient,useApi).toString()
            } catch (e: IOException) {
                e.printStackTrace()
                response = null.toString()
            }
            return@withContext response
        }

        private suspend fun onPreExecute() {
            //Запуск прогресс бара при подготовке данных от сервера
            loaderProgressBar.visibility = View.VISIBLE
            mainContainer.visibility = View.GONE
            errorTextTextView.visibility = View.GONE
            delay(PROGRESS_BAR_DELAY) //задержка, чтобы при обновлении успевал прокрутиться прогресс бар
        }

        private fun onPostExecute(result: String) {
            try {
                val gson = Gson()
                val typeToken = object:TypeToken<OpenWeatherMap>(){}.type

                openWeatherMap = gson.fromJson<OpenWeatherMap>(result, typeToken)

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
            } catch (e: Exception) {
                loaderProgressBar.visibility = View.GONE
                errorTextTextView.visibility = View.VISIBLE
            }

            //Запись нового города в переменную
            if (!CommonSettings.isCityNameChosen)
            CommonSettings.newCityName = "${openWeatherMap.name}"

        }
    }
}