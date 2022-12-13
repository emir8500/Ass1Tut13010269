package com.Eshn.eSahin.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import com.Eshn.eSahin.model.WeatherModel
import com.Eshn.eSahin.service.WeatherAPIService


private const val TAG = "MainViewModel"

class MainViewModel : ViewModel() {

    private val weatherApiService = WeatherAPIService()
    private val disposable = CompositeDisposable()

    val weatherrData = MutableLiveData<WeatherModel>()
    val weatherrError = MutableLiveData<Boolean>()
    val weatherrLoading = MutableLiveData<Boolean>()

    fun refreshData(cityName: String) {
        getDataFromAPI(cityName)
    }

    private fun getDataFromAPI(cityName: String) {

        weatherrLoading.value = true
        disposable.add(
            weatherApiService.getDataService(cityName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<WeatherModel>() {

                    override fun onSuccess(t: WeatherModel) {
                        weatherrData.value = t
                        weatherrError.value = false
                        weatherrLoading.value = false
                        Log.d(TAG, "onSuccess: Process Successful")
                    }

                    override fun onError(e: Throwable) {
                        weatherrError.value = true
                        weatherrLoading.value = false
                        Log.e(TAG, "onError: " + e)
                    }

                })
        )

    }

}