package com.Eshn.eSahin.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.Eshn.eSahin.R
import com.Eshn.eSahin.databinding.ActivityMainBinding
import com.Eshn.eSahin.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    private lateinit var viewmodel: MainViewModel

    private lateinit var binding : ActivityMainBinding

    private lateinit var GET: SharedPreferences
    private lateinit var SET: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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

//        binding.bottomNav.setOnNavigationItemSelectedListener {
//            when(it.itemId) {
//
//                R.id.home -> replaceFragment(Home())
//                R.id.settings -> replaceFragment(Settings())
//                R.id.calendar -> replaceFragment(Calendar())
//
//
//                else -> {
//
//                }
//            }
//        }


    }

    @SuppressLint("CommitTransaction")
    private fun replaceFragment(fragment : Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.layout,fragment)
        fragmentTransaction.commit()


    }


    private fun getLiveData() {

        viewmodel.weatherrData.observe(this, Observer { data ->
            data?.let {
                ll_data.visibility = View.VISIBLE

                tv_city_code.text = data.sys.country.toString()
                tv_city_name.text = data.name.toString()

                Glide.with(this)
                    .load("https://openweathermap.org/img/wn/" + data.weather.get(0).icon + "@2x.png")
                    .into(img_weather_pictures)

                tv_degree.text = data.main.temp.toString() + "°C"


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
}