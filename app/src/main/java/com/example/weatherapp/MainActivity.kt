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
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.weatherapp.Model.OpenWeatherMap
import com.google.android.gms.location.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.squareup.picasso.Picasso
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import java.io.IOException
import kotlin.coroutines.CoroutineContext
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    //OK HTTP
    private val okHttpClient = OkHttpClient()
    private val okHttpHelper = OkHttpHelper()

    //Определение местоположения
    private val INTERVAL: Long = 25000 //время, через которое пройдет обновление, в мс
    private val FASTEST_INTERVAL: Long = 1000 //время обновления в мс
    private val REQUEST_PERMISSION_LOCATION = 10


    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null
    lateinit var mLastLocation: Location
    internal lateinit var mLocationRequest: LocationRequest

    private var latitude:Double? = null
    private var longitude:Double? = null

    //Сервис для получения данных о погоде
    internal var openWeatherMap = OpenWeatherMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mLocationRequest = LocationRequest()
        val locationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            buildAlertMessageNoGps()

        if (checkPermissionForLocation(this))
            startLocationUpdates()

        val changeCityLink: TextView = changeCityLinkTextView
        changeCityLink.setOnClickListener {
            startSearchingCityActivity()
        }
        }

    /**Запуск активности выбора городов*/
    private fun startSearchingCityActivity(){
        val intent = Intent(this, SearchingCityActivity::class.java)
        startActivity(intent)
    }

    /**Вывод диалогового окна включения gps*/
    private fun buildAlertMessageNoGps() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes") { dialog, id ->
                    startActivityForResult(
                            Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                            , 11)
                }
                .setNegativeButton("No") { dialog, id ->
                    dialog.cancel()
                    finish()
                }
        val alert: AlertDialog = builder.create()
        alert.show()
    }

    protected fun startLocationUpdates() {
        /**Установка высокой точности определения местоположения*/
        mLocationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest!!.setInterval(INTERVAL)
        mLocationRequest!!.setFastestInterval(FASTEST_INTERVAL)

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

    fun onLocationChanged(location: Location) {
        mLastLocation = location
        latitude = mLastLocation.latitude
        longitude = mLastLocation.longitude

        /**вызов методов корутины*/
        WeatherTask().execute()
    }

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
                    PackageManager.PERMISSION_GRANTED) {
                true
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(ACCESS_FINE_LOCATION),
                        REQUEST_PERMISSION_LOCATION)
                false
            }
        } else {
            true
        }
    }


    /**COROUTINS function*/

    inner class WeatherTask:CoroutineScope{
        private var job: Job = Job()
        override val coroutineContext: CoroutineContext
            get() = Dispatchers.Main + job // to run code in Main(UI) Thread
        fun cancel() {
            job.cancel()
        }

        fun execute() = launch {
            onPreExecute()
            val result = doInBackground() // runs in background thread without blocking the Main Thread
            onPostExecute(result)
        }
        private suspend fun doInBackground(): String = withContext(Dispatchers.IO) {
            var response:String
            try {
                response = okHttpHelper.GET(
                    okHttpClient,
                    CommonSettings.weatherMapAPIRequest(latitude.toString(), longitude.toString())
                ).toString()
            } catch (e: IOException) {
                e.printStackTrace()
                response = null.toString()
            }
            return@withContext response
        }

        private suspend fun onPreExecute() {
            //Showing the ProgressBar, Making the main design GONE
            loaderProgressBar.visibility = View.VISIBLE
            mainContainer.visibility = View.GONE
            errorTextTextView.visibility = View.GONE
            delay(100) //задержка, чтобы при обновлении успевал прокрутиться прогресс бар
        }

        private fun onPostExecute(result: String) {
            try {
                val gson = Gson()
                val typeToken = object:TypeToken<OpenWeatherMap>(){}.type

                openWeatherMap = gson.fromJson<OpenWeatherMap>(result, typeToken)

                addressTextView.text = "${openWeatherMap.name},${openWeatherMap.sys!!.country}"
                updatedAtTextView.text = "Updated at: ${CommonSettings.currentDate}"
                skyDescriptionTextView.text = "${openWeatherMap.weather!![0].description}"
                tempTextView.text = "${openWeatherMap.main!!.temp} °C"
                feelsLikeTextView.text = "feels like: ${openWeatherMap.main!!.feels_like} °C"
                tempMinTextView.text = "min temp: ${openWeatherMap.main!!.temp_min} °C"
                tempMaxTextView.text = "max temp: ${openWeatherMap.main!!.temp_max} °C"
                sunriseTextView.text = CommonSettings.convertUnixTimeStampToDateTime(openWeatherMap.sys!!.sunrise)
                sunsetTextView.text = CommonSettings.convertUnixTimeStampToDateTime(openWeatherMap.sys!!.sunset)
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
        }
    }
}