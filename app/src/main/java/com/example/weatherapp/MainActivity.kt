package com.example.weatherapp

import android.Manifest
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.number.NumberFormatter.with
import android.icu.number.NumberRangeFormatter.with
import android.location.Location
import android.location.LocationManager
import android.os.AsyncTask
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

class MainActivity : AppCompatActivity() {
    var result: String? = null
    val API = "b5695569eeb89ee67d2b47e883e416dd"
    val uiScope = CoroutineScope(Dispatchers.Default)
    internal var openWeatherMap = OpenWeatherMap()

    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null
    private val INTERVAL: Long = 5000
    private val FASTEST_INTERVAL: Long = 1000
    lateinit var mLastLocation: Location
    internal lateinit var mLocationRequest: LocationRequest
    private val REQUEST_PERMISSION_LOCATION = 10
    private var latitude:Double? = null
    private var longitude:Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mLocationRequest = LocationRequest()
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps()
        }
        if (checkPermissionForLocation(this)) {
            startLocationUpdates()
        }

            weatherTask().execute()

            /**COROUTINS using*/
       /* uiScope.launch {
               callToOpenWeatherMap()
               extractJSON()
            }*/
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
        mLocationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        /*mLocationRequest!!.setInterval(INTERVAL)*/
       /* mLocationRequest!!.setFastestInterval(FASTEST_INTERVAL)*/

        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(mLocationRequest!!)
        val locationSettingsRequest = builder.build()

        val settingsClient = LocationServices.getSettingsClient(this)
        settingsClient.checkLocationSettings(locationSettingsRequest)

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
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
        weatherTask().execute(CommonSettings.weatherMapAPIRequest(latitude.toString(),longitude.toString()))
    }

    private fun stoplocationUpdates() {
        mFusedLocationProviderClient!!.removeLocationUpdates(mLocationCallback)
    }

    /**проверка разрешения на определение местоположения*/
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_PERMISSION_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates()
            } else {
                Toast.makeText(this@MainActivity, "Permission Denied", Toast.LENGTH_SHORT).show()
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
    private suspend fun callToOpenWeatherMap() {
       withContext(Dispatchers.Default) {
            try {
                result = URL(
                    CommonSettings.weatherMapAPIRequest(
                        latitude.toString(),
                        longitude.toString()
                    )
                ).readText(
                    Charsets.UTF_8
                )
            } catch (e: Exception) {
                result = null
            }
        }
    }
    private suspend fun extractJSON(){
        withContext(Dispatchers.Default) {try {
            val gson = Gson()
            val typeToken = object:TypeToken<OpenWeatherMap>(){}.type

            openWeatherMap = gson.fromJson<OpenWeatherMap>(result, typeToken)

            findViewById<TextView>(R.id.address).text = "${openWeatherMap.name},${openWeatherMap.sys!!.country}"
            findViewById<TextView>(R.id.updated_at).text =  "Updated at: ${CommonSettings.currentDate}"
            findViewById<TextView>(R.id.sky_description).text = "${openWeatherMap.weather!![0].description}"
            findViewById<TextView>(R.id.temp).text = "${openWeatherMap.main!!.temp} °C"
            findViewById<TextView>(R.id.temp_min).text = "${openWeatherMap .main!!.temp_min} °C"
            findViewById<TextView>(R.id.temp_max).text = "${openWeatherMap.main!!.temp_max} °C"
            findViewById<TextView>(R.id.sunrise).text = "${CommonSettings.convertUnixTimeStampToDateTime(openWeatherMap.sys!!.sunrise)}"
            findViewById<TextView>(R.id.sunset).text = "${CommonSettings.convertUnixTimeStampToDateTime(openWeatherMap.sys!!.sunset)}"
            findViewById<TextView>(R.id.wind).text = "${openWeatherMap.wind!!.speed} м/с"

            findViewById<ProgressBar>(R.id.loader).visibility = View.GONE
            findViewById<RelativeLayout>(R.id.mainContainer).visibility = View.VISIBLE
        } catch (e: Exception) {
            findViewById<ProgressBar>(R.id.loader).visibility = View.GONE
            findViewById<TextView>(R.id.errorText).visibility = View.VISIBLE
        }
            }

        }

    inner class weatherTask() : AsyncTask<String, Void, String>() {
        override fun onPreExecute() {
            super.onPreExecute()
             //Showing the ProgressBar, Making the main design GONE
            findViewById<ProgressBar>(R.id.loader).visibility = View.VISIBLE
            findViewById<RelativeLayout>(R.id.mainContainer).visibility = View.GONE
            findViewById<TextView>(R.id.errorText).visibility = View.GONE
        }
        override fun doInBackground(vararg params: String?): String? {
          /*  var stream:String?=null
            var urlString = params[0]

            val http = httpHelper()
            stream = http.getHTTPData(urlString)
            return stream*/
            var response:String?
            try{
                response = URL("https://api.openweathermap.org/data/2.5/weather?lat=$latitude&lon=$longitude&units=metric&appid=$API").readText(
                    Charsets.UTF_8
                )
            }catch (e: Exception){
                response = null
            }
            return response
        }
        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            try {
                val gson = Gson()
                val typeToken = object:TypeToken<OpenWeatherMap>(){}.type

                openWeatherMap = gson.fromJson<OpenWeatherMap>(result, typeToken)

                findViewById<TextView>(R.id.address).text = "${openWeatherMap.name},${openWeatherMap.sys!!.country}"
                findViewById<TextView>(R.id.updated_at).text =  "Updated at: ${CommonSettings.currentDate}"
                findViewById<TextView>(R.id.sky_description).text = "${openWeatherMap.weather!![0].description}"
                findViewById<TextView>(R.id.temp).text = "${openWeatherMap.main!!.temp} °C"
                findViewById<TextView>(R.id.feels_like).text = "feels like: ${openWeatherMap.main!!.feels_like} °C"
                findViewById<TextView>(R.id.temp_min).text = "min temp: ${openWeatherMap .main!!.temp_min} °C"
                findViewById<TextView>(R.id.temp_max).text = "max temp: ${openWeatherMap.main!!.temp_max} °C"
                findViewById<TextView>(R.id.sunrise).text = CommonSettings.convertUnixTimeStampToDateTime(openWeatherMap.sys!!.sunrise)
                findViewById<TextView>(R.id.sunset).text = CommonSettings.convertUnixTimeStampToDateTime(openWeatherMap.sys!!.sunset)
                findViewById<TextView>(R.id.wind).text = "${openWeatherMap.wind!!.speed} м/с"

                Picasso.with(this@MainActivity)
                        .load(CommonSettings.getImage(openWeatherMap.weather!![0].icon!!))
                        .into(findViewById<ImageView>(R.id.imageView))

                findViewById<ProgressBar>(R.id.loader).visibility = View.GONE
                findViewById<RelativeLayout>(R.id.mainContainer).visibility = View.VISIBLE
            } catch (e: Exception) {
                findViewById<ProgressBar>(R.id.loader).visibility = View.GONE
                findViewById<TextView>(R.id.errorText).visibility = View.VISIBLE
            }

        }
    }

}