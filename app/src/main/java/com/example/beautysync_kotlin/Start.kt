package com.example.beautysync_kotlin

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.beautysync_kotlin.databinding.ActivityStartBinding
import com.example.beautysync_kotlin.user.login.Login

class Start : AppCompatActivity() {

    private lateinit var binding : ActivityStartBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.user.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }

        binding.beautician.setOnClickListener {
            val intent = Intent(this, com.example.beautysync_kotlin.beautician.login.Login::class.java)
            startActivity(intent)
        }



    }
}