package com.sunnyweather.android.ui.weather

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import com.sunnyweather.android.R
import com.sunnyweather.android.databinding.ActivityWeatherBinding
import com.sunnyweather.android.logic.model.Weather
import com.sunnyweather.android.logic.model.getSky
import java.text.SimpleDateFormat
import java.util.Locale

class WeatherActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWeatherBinding

    val viewModel by lazy { ViewModelProvider(this).get(WeatherViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding= ActivityWeatherBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.nowLayout.titleLayout){view,insets->
            val statusBar=insets.getInsets(WindowInsetsCompat.Type.statusBars())
            view.setPadding(
                view.paddingLeft,
                statusBar.top,
                view.paddingRight,
                view.paddingBottom
            )
            insets
        }
        if (viewModel.locationLng.isEmpty()){
            viewModel.locationLng=intent.getStringExtra("location_lng")?:""
        }
        if (viewModel.locationLat.isEmpty()){
            viewModel.locationLat=intent.getStringExtra("location_lat")?:""
        }
        if (viewModel.placeName.isEmpty()) {
            viewModel.placeName =
                intent.getStringExtra("place_name") ?: ""
        }
        viewModel.weatherLiveData.observe(this){result ->
            val weather=result.getOrNull()
            if (weather!=null){
                showWeather(weather)
            }else{
                Toast.makeText(this,"无法获取天气信息", Toast.LENGTH_SHORT).show()
                result.exceptionOrNull()?.printStackTrace()
            }
            binding.swipeRefresh.isRefreshing=false
        }
        binding.swipeRefresh.setColorSchemeResources(R.color.purple_500)
        refreshWeather()
        binding.swipeRefresh.setOnRefreshListener {
            refreshWeather()
        }

        binding.nowLayout.navBtn.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
        binding.drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener{
            override fun onDrawerStateChanged(newState: Int) {}

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}

            override fun onDrawerOpened(drawerView: View) {}

            override fun onDrawerClosed(drawerView: View) {
                val manager=getSystemService(Context.INPUT_METHOD_SERVICE)as InputMethodManager
                manager.hideSoftInputFromWindow(drawerView.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            }
        })
    }

    private fun showWeather(weather: Weather){
        val placeName: TextView=findViewById<TextView>(R.id.placeName)
        placeName.text=viewModel.placeName
        val realtime=weather.realtime
        val daily=weather.daily
        binding.nowLayout.currentTemp.text="${realtime.temperature.toInt()}℃"
        binding.nowLayout.currentSky.text= getSky(realtime.skycon).info
        binding.nowLayout.currentAQI.text="空气指数${realtime.airQuality.aqi.chn.toInt()}"
        binding.nowLayout.root.setBackgroundResource(getSky(realtime.skycon).bg)
        binding.forecastLayout.forecastLayout.removeAllViews()
        val days=daily.skycon.size
        for(i in 0 until days){
            val skycon=daily.skycon[i]
            val temperature = daily.temperature[i]
            val view = LayoutInflater.from(this).inflate(R.layout.forecast_item,
                binding.forecastLayout.root, false)
            val dateInfo = view.findViewById(R.id.dateInfo) as TextView
            val skyIcon = view.findViewById(R.id.skyIcon) as ImageView
            val skyInfo = view.findViewById(R.id.skyInfo) as TextView
            val temperatureInfo = view.findViewById(R.id.temperatureInfo) as TextView
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dateInfo.text = simpleDateFormat.format(skycon.date)
            val sky = getSky(skycon.value)
            skyIcon.setImageResource(sky.icon)
            skyInfo.text = sky.info
            val tempText = "${temperature.min.toInt()} ~ ${temperature.max.toInt()} ℃"
            temperatureInfo.text = tempText
            binding.forecastLayout.forecastLayout.addView(view)
        }
        val lifeIndex=daily.lifeIndex
        binding.lifeIndexLayout.coldRiskText.text=lifeIndex.coldRisk[0].desc
        binding.lifeIndexLayout.dressingText.text=lifeIndex.dressing[0].desc
        binding.lifeIndexLayout.ultravioletText.text=lifeIndex.ultraviolet[0].desc
        binding.lifeIndexLayout.carWashingText.text=lifeIndex.carWashing[0].desc
        binding.weatherLayout.visibility= View.VISIBLE
    }

    fun refreshWeather(){
        viewModel.refreshWeather(viewModel.locationLng,viewModel.locationLat)
        binding.swipeRefresh.isRefreshing=true
    }
}