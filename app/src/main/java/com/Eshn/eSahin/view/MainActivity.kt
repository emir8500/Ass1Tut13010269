package com.Eshn.eSahin.view

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.content.Context
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.fragment_settings2.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.Eshn.eSahin.R
import com.Eshn.eSahin.databinding.ActivityMainBinding
import com.Eshn.eSahin.view.fragments.CalendarFragment
import com.Eshn.eSahin.view.fragments.SettingsFragment
import com.Eshn.eSahin.viewmodel.MainViewModel
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_settings2.*


private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var hum: Sensor? = null
    private var tempp: Sensor? = null



    private val settingsFragment = SettingsFragment()
    private val calendarFragment = CalendarFragment()
    private val homeFragment = Home()



    private lateinit var viewmodel: MainViewModel

    private lateinit var binding : ActivityMainBinding

    private lateinit var GET: SharedPreferences
    private lateinit var SET: SharedPreferences.Editor



    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        //var sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        //var stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY)


        binding = ActivityMainBinding.inflate(layoutInflater)
        GET = getSharedPreferences(packageName, MODE_PRIVATE)
        SET = GET.edit()


        viewmodel = ViewModelProviders.of(this).get(MainViewModel::class.java)

        var cName = GET.getString("cityName", "Galway")?.toLowerCase()
        edt_city_name.setText(cName)
        viewmodel.refreshData(cName!!)

        getLiveData()

        swipe_refresh_layout.setOnRefreshListener {
            ll_data.visibility = View.GONE
            tv_error.visibility = View.GONE
            pb_loading.visibility = View.GONE

            var cityName = GET.getString("cityName", cName)?.toLowerCase()
            edt_city_name.setText(cityName)
            viewmodel.refreshData(cityName!!)
            swipe_refresh_layout.isRefreshing = false
        }

        img_search_city.setOnClickListener {
            val cityName = edt_city_name.text.toString()
            SET.putString("cityName", cityName)
            SET.apply()
            viewmodel.refreshData(cityName)
            getLiveData()
            Log.i(TAG, "onCreate: " + cityName)
        }



        bottom_nav.setOnNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.settings -> replaceFragment(settingsFragment)
                R.id.home -> replaceFragment(homeFragment)
                R.id.calendar -> replaceFragment(calendarFragment)
            }
            true
        }


    }

    override fun onResume() {
        super.onResume()
        setupSensor()



        if (hum == null) {
            // show toast message, if there is no sensor in the device
            Toast.makeText(this, "No humidity sensor detected on this device", Toast.LENGTH_SHORT).show()
        }else if(tempp == null) {
            Toast.makeText(this, "No temperature sensor detected on this device", Toast.LENGTH_SHORT).show()
        } else {
            // register listener with sensorManager
            sensorManager.registerListener(this,hum,SensorManager.SENSOR_DELAY_NORMAL)
             sensorManager?.registerListener( this,tempp ,SensorManager.SENSOR_DELAY_NORMAL)
            // PROBLEM WITH REGISTER LISTENER
        }
    }
//

    @SuppressLint("CommitTransaction")
    private fun replaceFragment(fragment : Fragment) {
        if(fragment!=null) {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, fragment)
            transaction.commit()
        }
    }


    private fun getLiveData() {

        viewmodel.weatherrData.observe(this, Observer { data ->
            data?.let {
                ll_data.visibility = View.VISIBLE

                tv_city_code.text = data.sys.country.toString()
                tv_city_name.text = data.name.toString()



                Glide.with(this).load("https://openweathermap.org/img/wn/" + data.weather.get(0).icon + "@2x.png").into(img_weather_pictures)

                tv_degree.text = data.main.temp.toString() + "°C"

                tv_humidity.text = data.main.humidity.toString() + "%"
                tv_wind_speed.text = data.wind.speed.toString() + "km/s"
                tv_lat.text = data.coord.lat.toString()
                tv_lon.text = data.coord.lon.toString()


            }
        })

        viewmodel.weatherrError.observe(this, Observer { error ->
            error?.let {
                if (error) {
                    tv_error.visibility = View.VISIBLE
                    pb_loading.visibility = View.GONE
                    ll_data.visibility = View.GONE
                } else {
                    tv_error.visibility = View.GONE
                }
            }
        })

        viewmodel.weatherrLoading.observe(this, Observer { loading ->
            loading?.let {
                if (loading) {
                    pb_loading.visibility = View.VISIBLE
                    tv_error.visibility = View.GONE
                    ll_data.visibility = View.GONE
                } else {
                    pb_loading.visibility = View.GONE
                }
            }
        })

    }


    private fun setupSensor() {
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        hum = sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY)
        tempp = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)
    }


    override fun onSensorChanged(p0: SensorEvent?) {
        if(p0?.sensor?.type == Sensor.TYPE_RELATIVE_HUMIDITY) {
            val humidity = p0.values[0]

            humm.text= "$humidity %"
        }
        if(p0?.sensor?.type == Sensor.TYPE_AMBIENT_TEMPERATURE) {
            val temperature = p0.values[0]

            temp.text = "$temperature 'C"
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        return
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

}