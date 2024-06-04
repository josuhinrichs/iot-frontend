package com.example.climao

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.climao.databinding.ActivityDexAlertaConfiguracoesBinding
import com.example.climao.databinding.ActivityMainBinding

class DexAlertaConfiguracoes : AppCompatActivity() {

    private lateinit var binding: ActivityDexAlertaConfiguracoesBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDexAlertaConfiguracoesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnEditar.setOnClickListener {
            // Create an Intent to start DexAlertaActivity
            val intent = Intent(this, DexAlertaActivity::class.java)
            startActivity(intent)
        }

    }


}