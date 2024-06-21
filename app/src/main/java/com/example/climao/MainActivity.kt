package com.example.climao
import retrofit2.Retrofit
import android.os.Bundle
import com.example.climao.databinding.ActivityMainBinding
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import client.BackClient
import kotlinx.coroutines.Dispatchers
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
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
    private lateinit var pushToken: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        backClient = Retrofit.Builder()
            .baseUrl("https://climasync-4ibw.onrender.com/")
            .build().create(BackClient::class.java)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        askNotificationPermission()
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
    fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("FirebaseCloudMessaging", "Fetching FCM registration token failed", task.exception)
                    return@OnCompleteListener
                }

                // Get new FCM registration token
                pushToken = task.result
                Log.w("FirebaseCloudMessaging", "Fetching FCM registration token: $pushToken")

            })
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("FirebaseCloudMessaging", "Fetching FCM registration token failed", task.exception)
                    return@OnCompleteListener
                }

                // Get new FCM registration token
                val token = task.result
                Log.w("FirebaseCloudMessaging", "Fetching FCM registration token: $token")

            })
        } else {
            // TODO: Inform user that that your app will not show notifications.
        }
    }


}
