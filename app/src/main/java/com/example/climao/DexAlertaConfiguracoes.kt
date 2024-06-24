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
import okhttp3.RequestBody
import org.json.JSONObject
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

        val switchAlertaHidratacao = binding.switchAlertaHidratacao
        switchAlertaHidratacao.isChecked = user.alerta_hidratacao

        val switchAlertaCalor = binding.switchAlertaCalor
        switchAlertaCalor.isChecked = user.alerta_calor

        val switchAlertaChuva= binding.switchAlertaChuva
        switchAlertaChuva.isChecked = user.alerta_chuva

        val switchAlertaSol = binding.switchAlertaSol
        switchAlertaSol.isChecked = user.alerta_sol

        val switchAlertaFrio = binding.switchAlertaFrio
        switchAlertaFrio.isChecked = user.alerta_frio

        switchAlertaHidratacao.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                var body: RequestBody.Companion = RequestBody

                body.put("token", user.firebase_token)
                body.put("type", "alerta-hidratacao")
                body.put("value", true)

                GlobalScope.launch(Dispatchers.IO) {
                    withContext(Dispatchers.Default) {

                        val response = backClient.setAlertValue(jsonBody)
                        if (response.isSuccessful) {
                            val body = response.body()?.string()!!
                            Log.d("REQUEST_RESPONSE", body)
                        }
                    }
                }
            } else {
                onSwitchDisabled()
            }
        }

        binding.btnEditar.setOnClickListener {
            // Create an Intent to start DexAlertaActivity
            val intent = Intent(this, DexAlertaActivity::class.java)
            startActivity(intent)
        }

    }


}