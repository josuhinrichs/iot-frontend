package com.example.climao

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.climao.databinding.ActivityOnboardingBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Locale

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var pushToken: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        pushToken = getTokenFromPrefs()
        if (pushToken != null) {
            Log.d("FirebaseCloudMessaging", "Token retrieved from SharedPreferences: $pushToken")
        } else {
            askNotificationPermission()
        }

        val layouts = listOf(
            R.layout.onboarding,
            R.layout.onboarding2,
            R.layout.onboarding3
        )

        val adapter = OnboardingAdapter(layouts)
        binding.viewPager.adapter = adapter

    }

    fun fetchUserLocation(view: View) {
        fetchLocationPermission()
        askNotificationPermission()
        val btnFinalizar: Button = findViewById(R.id.btnFinalizar)
        btnFinalizar.isEnabled = true
        btnFinalizar.backgroundTintList = ContextCompat.getColorStateList(this, R.color.secondary)
        btnFinalizar.setTextColor(ContextCompat.getColor(this, R.color.white))
    }

    private fun getTokenFromPrefs(): String? {
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("fcm_token", null)
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


    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            getTokenAndStore()
        } else {
            // TODO: Inform user that your app will not show notifications.
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

    private fun saveTokenToPrefs(token: String) {
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("fcm_token", token)
        editor.apply()
    }

}
