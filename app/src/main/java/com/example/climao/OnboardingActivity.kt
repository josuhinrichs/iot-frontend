package com.example.climao

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.climao.databinding.ActivityOnboardingBinding
import com.google.android.gms.location.FusedLocationProviderClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Locale

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val layouts = listOf(
            R.layout.onboarding,
            R.layout.onboarding2,
            R.layout.onboarding3
        )

        val adapter = OnboardingAdapter(layouts)
        binding.viewPager.adapter = adapter

    }

    fun completeOnboarding(view: View) {
        val sunView: View = findViewById(R.id.sun)

        val expandAnimation: Animation = AnimationUtils.loadAnimation(this, R.anim.sun_expand)
        val shrinkAndTranslateAnimation: Animation = AnimationUtils.loadAnimation(this, R.anim.sun_shrink_and_translate)

        // Set an animation listener to perform actions after animation
        shrinkAndTranslateAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                // Save onboarding state and start MainActivity
                val sharedPreferences: SharedPreferences = getSharedPreferences("prefs", MODE_PRIVATE)
                with(sharedPreferences.edit()) {
                    putBoolean("isOnboardingCompleted", true)
                    apply()
                }
                startActivity(Intent(this@OnboardingActivity, MainActivity::class.java))
                finish()
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })

        sunView.startAnimation(expandAnimation)
        expandAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                sunView.startAnimation(shrinkAndTranslateAnimation)
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })
    }

    private fun fetchLocationAndCep() {
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
                    val address = getAddressFromLocation(location.latitude, location.longitude)

                    val textoRua: TextView = findViewById(R.id.enderecoRua)
                    textoRua.text = address.thoroughfare

                    val enderecoBairro: TextView = findViewById(R.id.enderecoBairro)
                    enderecoBairro.text = address.thoroughfare

                }
            }
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

    private suspend fun getAddressFromLocation(latitude: Double, longitude: Double): Address {
        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses = geocoder.getFromLocation(latitude, longitude, 1)
        return addresses!![0]
    }

    fun nextPage(view: View) {
        val currentItem = binding.viewPager.currentItem
        if (currentItem < (binding.viewPager.adapter?.itemCount?.minus(1) ?: 0)) {
            binding.viewPager.currentItem = currentItem + 1
        }
    }

    fun backPage(view: View) {
        val currentItem = binding.viewPager.currentItem
        if (currentItem > 0) {
            binding.viewPager.currentItem = currentItem - 1
        }
    }

    fun jumpToLastPage(view: View) {
        val lastItem = (binding.viewPager.adapter?.itemCount ?: 0) - 1
        binding.viewPager.currentItem = lastItem
    }

    fun fetchUserLocation(view: View) {
        fetchLocationPermission()
        fetchLocationAndCep()
    }
}
