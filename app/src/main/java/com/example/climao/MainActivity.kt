package com.example.climao

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import client.BackClient
import com.example.climao.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import models.client.User
import models.tuya.StatusResponse
import models.weather.WeatherResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Retrofit
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@kotlinx.serialization.ExperimentalSerializationApi
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var backClient: BackClient
    private var pushToken: String? = null
    private var deviceID: String = "vdevo171874684507405"
    private lateinit var user: User

    private val _nowTemperature = MutableLiveData<Int>(30)
    val nowTemperature: LiveData<Int> get() = _nowTemperature

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        backClient = Retrofit.Builder()
            .baseUrl("https://climasync-4ibw.onrender.com/") //local: http://10.0.2.2:5000/
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


        pushToken = getTokenFromPrefs()
        if (pushToken != null) {
            Log.d("FirebaseCloudMessaging", "Token retrieved from SharedPreferences: $pushToken")
        } else {
            askNotificationPermission()
        }
        if( pushToken == null){
            Log.d("DEBUG", "Push token não inicializado")
            pushToken = "pushToken"
            // TODO: checar se a variável pushToken tem valor
        }

        getUserInfo()

        fetchLocationPermission()
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

        val ventiladorSwitch = binding.ventiladorSwitch

        ventiladorSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                onVentiladorSwitchEnabled()
            } else {
                onVentiladoSwitchDisabled()
            }
        }

        // Set up the button click listener
        binding.dexAlertaBtn.setOnClickListener {
            // Create an Intent to start DexAlertaActivity
            val myIntent = Intent()

            val intent = Intent(this, DexAlertaConfiguracoes::class.java)
            intent.putExtra("user", user)
            startActivity(intent)
        }

        nowTemperature.observe(this, Observer { newValue ->
            // This block gets called when myVariable changes
            onMyVariableChanged(newValue)
        })
    }

    private fun onVentiladoSwitchDisabled() {
        GlobalScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Default) {
                val response = backClient.deviceCommand(deviceID, false)
                if (response.isSuccessful) {
                    val body = response.body()?.string()!!
                    Log.d("REQUEST_RESPONSE", body)
                }
            }
        }

        binding.ventiladorStatusText.text = "Desligado"
        binding.ventiladorStatusText.setTextColor(ContextCompat.getColor(this, R.color.light_gray))

        val colorStateList = ContextCompat.getColorStateList(this, R.color.light_gray)
        binding.ventiladorStatusIcon.imageTintList = colorStateList
    }

    private fun onVentiladorSwitchEnabled() {
        GlobalScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Default) {
                val response = backClient.deviceCommand(deviceID, true)
                if (response.isSuccessful) {
                    val body = response.body()?.string()!!
                    Log.d("REQUEST_RESPONSE", body)
                }
            }
        }

        binding.ventiladorStatusText.text = "Ligado"
        binding.ventiladorStatusText.setTextColor(ContextCompat.getColor(this, R.color.secondary))

        val colorStateList = ContextCompat.getColorStateList(this, R.color.secondary)
        binding.ventiladorStatusIcon.imageTintList = colorStateList
    }

    private fun getUserInfo(){
        val jsonObject = JSONObject()

        jsonObject.put("firebase_token",pushToken)

        val jsonObjectString = jsonObject.toString()
        val requestBody = jsonObjectString.toRequestBody("application/json".toMediaTypeOrNull())
        Log.d("REQUEST_BODY", jsonObjectString)

        GlobalScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Default) {
                val response = backClient.getUser(requestBody)
                if (response.isSuccessful) {
                    val body = response.body()?.string()!!
                    Log.d("REQUEST_RESPONSE", body)
                    val responseFormatted = Json.decodeFromString<User>(body)

                    withContext(Dispatchers.Main) {
                        user = responseFormatted

                        // atualizar a última notificação
                        binding.tituloAlerta.text = user.titulo_alerta
                        binding.corpoAlerta.text = user.corpo_alerta

                        //converter data da última notificação
                        // Parse the input date string
                        val zonedDateTime = ZonedDateTime.parse(user.timestamp_alerta).withZoneSameInstant(
                            ZoneId.of("UTC-3"))

                        // Define the desired output format
                        val outputFormatter = DateTimeFormatter.ofPattern(
                            "HH:mm dd/MM",
                            Locale.getDefault())

                        // Format the parsed date to the desired output format
                        binding.horaAlerta.text = zonedDateTime.format(outputFormatter)


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
        val jsonObject = JSONObject()
        jsonObject.put("token", pushToken)
        jsonObject.put("latitude", latitude)
        jsonObject.put("longitude", longitude)

        val jsonObjectString = jsonObject.toString()
        val requestBody = jsonObjectString.toRequestBody("application/json".toMediaTypeOrNull())
        Log.d("REQUEST_BODY", jsonObjectString)

        GlobalScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Default) {
                    val response = backClient.getWeatherInfo(requestBody)
                    if (response.isSuccessful) {
                        val body = response.body()?.string()!!
                        Log.d("REQUEST_RESPONSE", body)
                        val responseFormatted = Json.decodeFromString<WeatherResponse>(body)

                        withContext(Dispatchers.Main) {
                            updateVariable(responseFormatted.temp)

                            binding.textView4.text = responseFormatted.temp.toString() + "ºC"
                            binding.textView5.text = responseFormatted.condition
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
                val response = backClient.getDeviceStatus(deviceID)
                if (response.isSuccessful) {
                    val body = response.body()?.string()!!
                    Log.d("REQUEST_RESPONSE", body)
                    val responseFormatted = Json.decodeFromString<StatusResponse>(body)

                    withContext(Dispatchers.Main) {
                        binding.ventiladorSwitch.isChecked = responseFormatted.isTurnedOn

                        //TODO: ajustar as cores
                        if(responseFormatted.isOnline){
                            binding.ventiladorOnlineStatusText.text = "Online"
                            //binding.ventiladorOnlineStatusText.setTextColor(ContextCompat.getColor(this, R.color.green))

                        }else{
                            binding.ventiladorOnlineStatusText.text = "Offline"
                            //binding.ventiladorOnlineStatusText.setTextColor(ContextCompat.getColor(this, R.color.light_gray))
                        }
                    }

                }
            }

        }

    }
    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                getTokenAndStore()
            } else if (shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.
            } else {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            getTokenAndStore()
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            getTokenAndStore()
        } else {
            // TODO: Inform user that your app will not show notifications.
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

    private fun saveTokenToPrefs(token: String) {
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("fcm_token", token)
        editor.apply()
    }

    private fun getTokenFromPrefs(): String? {
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("fcm_token", null)
    }

    private fun getTokenAndStore() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FirebaseCloudMessaging", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result
            Log.w("FirebaseCloudMessaging", "Fetching FCM registration token: $token")

            // Save token to SharedPreferences
            saveTokenToPrefs(token)
        })
    }

}
