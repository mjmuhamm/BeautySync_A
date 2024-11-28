package com.example.beautysync_kotlin.beautician.info

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.beautysync_kotlin.R
import com.example.beautysync_kotlin.databinding.ActivityPersonalInfo2Binding

class PersonalInfo : AppCompatActivity() {
    private lateinit var binding: ActivityPersonalInfo2Binding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPersonalInfo2Binding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}