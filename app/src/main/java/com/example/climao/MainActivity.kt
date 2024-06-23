package com.example.climao
import android.annotation.SuppressLint
import retrofit2.Retrofit
import android.os.Bundle
import com.example.climao.databinding.ActivityMainBinding
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import client.WeatherClient
import kotlinx.coroutines.Dispatchers
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.util.Log
import android.view.View
import android.widget.Switch
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import models.weather.WeatherResponse

import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import kotlin.properties.Delegates


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var temperature by Delegates.notNull<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        // Check if onboarding is complete
        val sharedPreferences: SharedPreferences = getSharedPreferences("prefs", MODE_PRIVATE)
        val isOnboardingCompleted = sharedPreferences.getBoolean("isOnboardingCompleted", false)

        if (!isOnboardingCompleted) {
            // Start OnboardingActivity
            startActivity(Intent(this, OnboardingActivity::class.java))
            finish()
            return
        }

        setContentView(binding.root)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        //fetchLocation()

        //updateTemperatureColor(39) // TODO: INTEGRAR COM CHECAGEM DE CLIMA

        val climasyncSwitch = binding.climasyncSwitch

        climasyncSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                onSwitchEnabled()
            } else {
                onSwitchDisabled()
            }
        }

        // Set up the button click listener
        binding.dexAlertaBtn.setOnClickListener {
            // Create an Intent to start DexAlertaActivity
            val intent = Intent(this, DexAlertaConfiguracoes::class.java)
            startActivity(intent)
        }
    }
    private fun fetchLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                101
            )
            return
        }
    }

    private fun fetchLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Se permissões de localização não forem concedidas, solicite-as novamente.
            fetchLocationPermission()
            return
        }

        // Obtenha a última localização conhecida do usuário
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
            // Verifique se a localização é válida
            if (location != null) {
                // Obtenha os detalhes do endereço da localização atual
                GlobalScope.launch(Dispatchers.Main) {
                    getWeatherInfo(location.latitude, location.longitude)
                    updateTemperatureColor(temperature)
                }
            }else{
               binding.textView4.text = ""
                binding.textView5.text = "Localização não encontrada"
            }
        }
    }
    private fun getWeatherInfo(latitude:Double, longitude:Double ){

        val service = Retrofit.Builder()
            .baseUrl("https://climasync-4ibw.onrender.com") //TODO: alterar URL
            .build().create(WeatherClient::class.java)

        runBlocking {
            withContext(Dispatchers.Default) {
                    val response = service.getWeatherInfo(latitude!!, longitude!!)
                    if (response.isSuccessful) {
                        val responseFormatted = Json.decodeFromString<WeatherResponse>(response.body()?.string()!!)
                        temperature = responseFormatted.results.temp
                        binding.textView4.text = temperature.toString()

                        Log.d("TEST",responseFormatted.toString())
            }
        }


    }}

    private fun updateTemperatureColor(temperature: Int) {
        val weatherRectangle = binding.weatherRectangle

        // Determine the color based on temperature
        val color = when {
            temperature < 25  -> R.color.cold // Cold
            temperature in 25..28 -> R.color.cool // Cool
            temperature in 29..33 -> R.color.warm // Warm
            else -> R.color.hot // Hot
        }

        // Apply background tint
        val colorStateList = ContextCompat.getColorStateList(this, color)
        weatherRectangle.backgroundTintList = colorStateList
    }

    private fun onSwitchEnabled(){
        val weatherRectangle = binding.weatherRectangle
        val extCircle = binding.extCircle
        val middleCircle = binding.middleCircle
        val innerCircle = binding.innerCircle
        val status = binding.homeStatus
        val blackFilter = binding.blackFilter

        blackFilter.visibility = View.INVISIBLE

        // Apply background tint
        val colorStateList = ContextCompat.getColorStateList(this, R.color.primary)
        weatherRectangle.backgroundTintList = colorStateList

        extCircle.visibility = View.VISIBLE
        middleCircle.visibility = View.VISIBLE
        innerCircle.visibility = View.VISIBLE

        status.text = "Online"
        status.setTextColor(ContextCompat.getColor(this, R.color.green))

        //fetchLocation()
    }

    private fun onSwitchDisabled(){
        val weatherRectangle = binding.weatherRectangle
        val extCircle = binding.extCircle
        val middleCircle = binding.middleCircle
        val innerCircle = binding.innerCircle
        val status = binding.homeStatus
        val blackFilter = binding.blackFilter

        blackFilter.visibility = View.VISIBLE

        extCircle.visibility = View.INVISIBLE
        middleCircle.visibility = View.INVISIBLE
        innerCircle.visibility = View.INVISIBLE

        status.text = "Offline"
        status.setTextColor(ContextCompat.getColor(this, R.color.terciary))

        // Apply background tint
        val colorStateList = ContextCompat.getColorStateList(this, R.color.switch_disabled)
        weatherRectangle.backgroundTintList = colorStateList
    }

}
