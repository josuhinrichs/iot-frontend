package com.example.climao

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.climao.databinding.ActivityOnboardingBinding

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding

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
}
