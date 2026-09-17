package com.sunnyweather.android.logic

import androidx.lifecycle.liveData
import com.sunnyweather.android.logic.dao.PlaceDao
import com.sunnyweather.android.logic.model.Place
import com.sunnyweather.android.logic.model.Weather
import com.sunnyweather.android.logic.network.SunnyWeatherNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlin.coroutines.CoroutineContext

object Repository {
    fun searchPlace(query: String)= liveData (Dispatchers.IO){
        val result=try {
            val placeResponse= SunnyWeatherNetwork.searchPlace(query)
            if (placeResponse.status=="ok"){
                val place=placeResponse.places
                Result.success(place)
            }else{
                Result.failure(RuntimeException("response status is ${placeResponse.status}"))
            }
        }catch (e: Exception){
            Result.failure<List<Place>>(e)
        }
        emit(result)
    }

    fun refreshWeather(lng: String,lat: String)=liveData(Dispatchers.IO) {
        val result=try {
            coroutineScope {
//                val deferredRealtime=async {
//                    SunnyWeatherNetwork.getRealtimeWeather(lng, lat)
//                }
//                val deferredDaily=async {
//                    SunnyWeatherNetwork.getDailyWeather(lng,lat)
//                }
//                val realtimeResponse=deferredRealtime.await()
//                val dailyResponse=deferredDaily.await()
                val realtimeResponse =
                    SunnyWeatherNetwork.getRealtimeWeather(lng, lat)
                delay(1200)
                val dailyResponse =
                    SunnyWeatherNetwork.getDailyWeather(lng, lat)
                if (realtimeResponse.status=="ok"&&dailyResponse.status=="ok"){
                    val weather= Weather(realtimeResponse.result.realtime,dailyResponse.result.daily)
                    Result.success(weather)
                }else{
                    Result.failure(RuntimeException(
                        "realtime response status is ${realtimeResponse.status}"+
                        "daily response status is ${dailyResponse.status}"
                    ))
                }
            }
        }catch (e: Exception){
            Result.failure<Weather>(e)
        }
        emit(result)
    }

    private fun<T> fire(context: CoroutineContext,block:suspend ()-> T)=liveData (context){
       val result = try {
           Result.success(block())
        }catch (e: Exception){
            Result.failure<T>(e)
        }
        emit(result)
    }

    fun savePlace(place: Place) = PlaceDao.savePlace(place)

    fun getSavedPlace() = PlaceDao.getSavedPlace()

    fun isPlaceSaved() = PlaceDao.isPlaceSaved()
}