package com.sunnyweather.android.logic.network

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


object SunnyWeatherNetwork {

    private val placeService= ServiceCreator.create<PlaceService>()
    private val weatherService= ServiceCreator.create<WeatherService>()

    suspend fun searchPlace(query: String)=placeService.searchPlace(query).await()
    suspend fun getDailyWeather(lng: String,lat: String)=weatherService.getDailyWeather(lng,lat).await()
    suspend fun getRealtimeWeather(lng: String,lat: String)=weatherService.getRealtimeWeather(lng,lat).await()

    private suspend fun <T> Call<T>.await():T{
        return suspendCoroutine { continuation ->
            enqueue(object : Callback<T>{
                override fun onResponse(call: Call<T?>, response: Response<T?>) {
                    if (response.isSuccessful) {

                        val body = response.body()

                        if (body != null) {
                            continuation.resume(body)
                        } else {
                            continuation.resumeWithException(
                                RuntimeException(
                                    "HTTP ${response.code()}: response body is null"
                                )
                            )
                        }

                    } else {

                        val errorBody =
                            response.errorBody()?.string()

                        continuation.resumeWithException(
                            RuntimeException(
                                "HTTP ${response.code()} ${response.message()}\n" +
                                        "errorBody=$errorBody"
                            )
                        )
                    }
                }

                override fun onFailure(call: Call<T?>, t: Throwable) {
                    continuation.resumeWithException(t)
                }
            })
        }
    }
}