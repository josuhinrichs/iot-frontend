package com.example.climao

import android.os.Bundle
import com.example.climao.databinding.DexAlertaBinding
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity

class DexAlertaActivity : AppCompatActivity() {

    private lateinit var binding: DexAlertaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DexAlertaBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}