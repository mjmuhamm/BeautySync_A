package com.example.beautysync_kotlin

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.beautysync_kotlin.databinding.ActivityStartBinding
import com.example.beautysync_kotlin.user.MainActivity
import com.example.beautysync_kotlin.user.login.Login
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class Start : AppCompatActivity() {

    private lateinit var binding : ActivityStartBinding
    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

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

        checkLoginStatus()

    }

    private fun checkLoginStatus() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("where_to", "home")

        val intent1 = Intent(this, com.example.beautysync_kotlin.beautician.MainActivity::class.java)
        intent1.putExtra("where_to", "home")
        if (auth.currentUser != null) {
            if (auth.currentUser!!.displayName != null) {
                if (auth.currentUser!!.displayName!! == "User") {
                    startActivity(intent)
                    finish()
                } else if (auth.currentUser!!.displayName!! == "Beautician") {
                    startActivity(intent1)
                    finish()
                }
            }
        }
    }
}