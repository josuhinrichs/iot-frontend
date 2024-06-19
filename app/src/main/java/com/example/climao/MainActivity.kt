package com.example.climao
import retrofit2.Retrofit
import android.os.Bundle
import com.example.climao.databinding.ActivityMainBinding
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import client.BackClient
import kotlinx.coroutines.Dispatchers
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.util.Log
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import models.weather.WeatherResponse

import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import models.tuya.StatusResponse

@kotlinx.serialization.ExperimentalSerializationApi
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var backClient: BackClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        backClient = Retrofit.Builder()
            .baseUrl("https://climasync-4ibw.onrender.com/")
            .build().create(BackClient::class.java)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        fetchLocation()
        fetchDeviceStatus()

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

    @kotlinx.serialization.ExperimentalSerializationApi
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
                getWeatherInfo(location.latitude, location.longitude)

            }else{
                binding.textView4.text = ""
                binding.textView5.text = "Localização não encontrada"
            }
        }
    }
    @kotlinx.serialization.ExperimentalSerializationApi
    private fun getWeatherInfo(latitude:Double, longitude:Double ){
        GlobalScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Default) {
                    val response = backClient.getWeatherInfo(latitude, longitude)
                    if (response.isSuccessful) {
                        val body = response.body()?.string()!!
                        Log.d("RESPONSE", body)
                        val responseFormatted = Json.decodeFromString<WeatherResponse>(body)

                        withContext(Dispatchers.Main) {
                            binding.textView4.text =
                                responseFormatted.results.temp.toString() + "ºC"
                            binding.textView5.text = responseFormatted.results.description
                        }

                    }
            }

        }

    }

    @kotlinx.serialization.ExperimentalSerializationApi
    private fun fetchDeviceStatus(){
        GlobalScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Default) {
                //TODO: device id como env
                val response = backClient.getDeviceStatus("vdevo171874684507405")
                if (response.isSuccessful) {
                    val body = response.body()?.string()!!
                    Log.d("RESPONSE", body)
                    val responseFormatted = Json.decodeFromString<StatusResponse>(body)

                    withContext(Dispatchers.Main) {
                        // TODO: alterar textos da tomada
                        // TODO: alterar posição do switch

                    }

                }
            }

        }

    }


}
