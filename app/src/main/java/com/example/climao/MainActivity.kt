package com.example.climao
import android.annotation.SuppressLint
import retrofit2.Retrofit
import android.os.Bundle
import com.example.climao.databinding.ActivityMainBinding
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.health.connect.datatypes.units.Temperature
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import client.BackClient
import kotlinx.coroutines.Dispatchers
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import android.view.View
import android.widget.Switch
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import kotlinx.coroutines.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import models.weather.WeatherResponse

import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import models.client.User
import models.tuya.StatusResponse
import org.json.JSONObject
import kotlin.properties.Delegates
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

@kotlinx.serialization.ExperimentalSerializationApi
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var backClient: BackClient
    private lateinit var pushToken: String
    private var temperature: Int = 30
    private lateinit var user: User

    private val _nowTemperature = MutableLiveData<Int>(30)
    val nowTemperature: LiveData<Int> get() = _nowTemperature

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        backClient = Retrofit.Builder()
            .baseUrl("https://climasync-4ibw.onrender.com/")
            .build().create(BackClient::class.java)

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
        askNotificationPermission()
        getUserInfo()
        fetchLocation()
        fetchDeviceStatus()
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

        nowTemperature.observe(this, Observer { newValue ->
            // This block gets called when myVariable changes
            onMyVariableChanged(newValue)
        })
    }
    private fun getUserInfo(){
        val jsonObject = JSONObject()
        jsonObject.put("firebase_token", pushToken)

        val jsonObjectString = jsonObject.toString()
        val requestBody = jsonObjectString.toRequestBody("application/json".toMediaTypeOrNull())

        GlobalScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Default) {
                val response = backClient.getUser(requestBody)
                if (response.isSuccessful) {
                    val body = response.body()?.string()!!
                    Log.d("REQUEST_RESPONSE", body)
                    val responseFormatted = Json.decodeFromString<User>(body)

                    withContext(Dispatchers.Main) {
                        user = responseFormatted
                    }

                }
            }
        }
    }
    private fun onMyVariableChanged(newValue: Int?) {
        // Determine the color based on temperature
        val weatherRectangle = binding.weatherRectangle

        // Determine the color based on temperature
        val color = when {
            newValue!! < 25  -> R.color.cold // Cold
            newValue in 25..28 -> R.color.cool // Cool
            newValue in 29..33 -> R.color.warm // Warm
            else -> R.color.hot // Hot
        }

        // Apply background tint
        val colorStateList = ContextCompat.getColorStateList(this, color)
        weatherRectangle.backgroundTintList = colorStateList
    }

    private fun updateVariable(newValue: Int) {
        _nowTemperature.value = newValue
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
                Log.d("DEBUG", "CHEGOU NA REQUISIÇÃO DO CLIMA")
                    if (response.isSuccessful) {
                        val body = response.body()?.string()!!
                        Log.d("REQUEST_RESPONSE", body)
                        val responseFormatted = Json.decodeFromString<WeatherResponse>(body)

                        withContext(Dispatchers.Main) {
                            updateVariable(responseFormatted.results.temp)

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
                    Log.d("REQUEST_RESPONSE", body)
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

    private fun onSwitchEnabled(){
        val weatherRectangle = binding.weatherRectangle
        val extCircle = binding.extCircle
        val middleCircle = binding.middleCircle
        val innerCircle = binding.innerCircle
        val status = binding.homeStatus
        val blackFilter = binding.blackFilter

        blackFilter.visibility = View.INVISIBLE

        extCircle.visibility = View.VISIBLE
        middleCircle.visibility = View.VISIBLE
        innerCircle.visibility = View.VISIBLE

        status.text = "Online"
        status.setTextColor(ContextCompat.getColor(this, R.color.green))

        fetchLocation()
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
