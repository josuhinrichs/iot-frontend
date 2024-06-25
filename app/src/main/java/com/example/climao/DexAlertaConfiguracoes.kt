package com.example.climao

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import client.BackClient
import com.example.climao.databinding.ActivityDexAlertaConfiguracoesBinding
import com.example.climao.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import models.client.User
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.http.Body

class DexAlertaConfiguracoes : AppCompatActivity() {

    private lateinit var binding: ActivityDexAlertaConfiguracoesBinding
    private lateinit var user: User
    private lateinit var backClient: BackClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDexAlertaConfiguracoesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        user = intent.getParcelableExtra<User>("user")!!

        Log.d("TESTE", user.toString())

        backClient = Retrofit.Builder()
            .baseUrl("https://climasync-4ibw.onrender.com/") //local: http://10.0.2.2:5000/
            .build().create(BackClient::class.java)

        val switchAlertaHidratacao = binding.switchAlertaHidratacao
        switchAlertaHidratacao.isChecked = user.alerta_hidratacao

        val switchAlertaChuva= binding.switchAlertaChuva
        switchAlertaChuva.isChecked = user.alerta_chuva

        val switchAlertaSol = binding.switchAlertaSol
        switchAlertaSol.isChecked = user.alerta_sol

        val switchAlertaFrio = binding.switchAlertaFrio
        switchAlertaFrio.isChecked = user.alerta_frio

        switchAlertaHidratacao.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                var body: RequestBody.Companion = RequestBody

                val jsonString = """
                    {
                      "token": "${user.firebase_token}",
                      "type": "alerta_hidratacao",
                      "value": true
                    }
                    """

                val JSON = "application/json; charset=utf-8".toMediaTypeOrNull()
                val requestBody = jsonString.toRequestBody(JSON)

                GlobalScope.launch(Dispatchers.IO) {
                    withContext(Dispatchers.Default) {

                        val response = backClient.setAlertValue(requestBody)
                        if (response.isSuccessful) {
                            val body = response.body()?.string()!!
                            Log.d("REQUEST_RESPONSE", body)
                        }
                    }
                }
            } else {
                var body: RequestBody.Companion = RequestBody

                val jsonString = """
                    {
                      "token": "${user.firebase_token}",
                      "type": "alerta_hidratacao",
                      "value": false
                    }
                    """

                val JSON = "application/json; charset=utf-8".toMediaTypeOrNull()
                val requestBody = jsonString.toRequestBody(JSON)

                GlobalScope.launch(Dispatchers.IO) {
                    withContext(Dispatchers.Default) {

                        val response = backClient.setAlertValue(requestBody)
                        if (response.isSuccessful) {
                            val body = response.body()?.string()!!
                            Log.d("REQUEST_RESPONSE", body)
                        }
                    }
                }
            }
        }

        switchAlertaChuva.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                var body: RequestBody.Companion = RequestBody

                val jsonString = """
                    {
                      "token": "${user.firebase_token}",
                      "type": "alerta_chuva",
                      "value": true
                    }
                    """

                val JSON = "application/json; charset=utf-8".toMediaTypeOrNull()
                val requestBody = jsonString.toRequestBody(JSON)

                GlobalScope.launch(Dispatchers.IO) {
                    withContext(Dispatchers.Default) {

                        val response = backClient.setAlertValue(requestBody)
                        if (response.isSuccessful) {
                            val body = response.body()?.string()!!
                            Log.d("REQUEST_RESPONSE", body)
                        }
                    }
                }
            } else {
                var body: RequestBody.Companion = RequestBody

                val jsonString = """
                    {
                      "token": "${user.firebase_token}",
                      "type": "alerta_chuva",
                      "value": false
                    }
                    """

                val JSON = "application/json; charset=utf-8".toMediaTypeOrNull()
                val requestBody = jsonString.toRequestBody(JSON)

                GlobalScope.launch(Dispatchers.IO) {
                    withContext(Dispatchers.Default) {

                        val response = backClient.setAlertValue(requestBody)
                        if (response.isSuccessful) {
                            val body = response.body()?.string()!!
                            Log.d("REQUEST_RESPONSE", body)
                        }
                    }
                }
            }
        }

        switchAlertaSol.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                var body: RequestBody.Companion = RequestBody

                val jsonString = """
                    {
                      "token": "${user.firebase_token}",
                      "type": "alerta_sol",
                      "value": true
                    }
                    """

                val JSON = "application/json; charset=utf-8".toMediaTypeOrNull()
                val requestBody = jsonString.toRequestBody(JSON)

                GlobalScope.launch(Dispatchers.IO) {
                    withContext(Dispatchers.Default) {

                        val response = backClient.setAlertValue(requestBody)
                        if (response.isSuccessful) {
                            val body = response.body()?.string()!!
                            Log.d("REQUEST_RESPONSE", body)
                        }
                    }
                }
            } else {
                var body: RequestBody.Companion = RequestBody

                val jsonString = """
                    {
                      "token": "${user.firebase_token}",
                      "type": "alerta_sol",
                      "value": false
                    }
                    """

                val JSON = "application/json; charset=utf-8".toMediaTypeOrNull()
                val requestBody = jsonString.toRequestBody(JSON)

                GlobalScope.launch(Dispatchers.IO) {
                    withContext(Dispatchers.Default) {

                        val response = backClient.setAlertValue(requestBody)
                        if (response.isSuccessful) {
                            val body = response.body()?.string()!!
                            Log.d("REQUEST_RESPONSE", body)
                        }
                    }
                }
            }
        }

        switchAlertaFrio.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                var body: RequestBody.Companion = RequestBody

                val jsonString = """
                    {
                      "token": "${user.firebase_token}",
                      "type": "alerta_frio",
                      "value": true
                    }
                    """

                val JSON = "application/json; charset=utf-8".toMediaTypeOrNull()
                val requestBody = jsonString.toRequestBody(JSON)

                GlobalScope.launch(Dispatchers.IO) {
                    withContext(Dispatchers.Default) {

                        val response = backClient.setAlertValue(requestBody)
                        if (response.isSuccessful) {
                            val body = response.body()?.string()!!
                            Log.d("REQUEST_RESPONSE", body)
                        }
                    }
                }
            } else {
                var body: RequestBody.Companion = RequestBody

                val jsonString = """
                    {
                      "token": "${user.firebase_token}",
                      "type": "alerta_frio",
                      "value": false
                    }
                    """

                val JSON = "application/json; charset=utf-8".toMediaTypeOrNull()
                val requestBody = jsonString.toRequestBody(JSON)

                GlobalScope.launch(Dispatchers.IO) {
                    withContext(Dispatchers.Default) {

                        val response = backClient.setAlertValue(requestBody)
                        if (response.isSuccessful) {
                            val body = response.body()?.string()!!
                            Log.d("REQUEST_RESPONSE", body)
                        }
                    }
                }
            }
        }

    }


}