package com.example.beautysync_kotlin.beautician.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.aminography.primedatepicker.utils.gone
import com.example.beautysync_kotlin.R
import com.example.beautysync_kotlin.beautician.MainActivity
import com.example.beautysync_kotlin.beautician.info.PersonalInfo
import com.example.beautysync_kotlin.databinding.ActivityLogin2Binding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class Login : AppCompatActivity() {
    private lateinit var binding: ActivityLogin2Binding
    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLogin2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.signup.setOnClickListener {
            val intent = Intent(this, PersonalInfo::class.java)
            startActivity(intent)
        }

        binding.login.setOnClickListener {
            login()
        }
    }

    private fun login() {
        binding.progressBar.isVisible = true
        val intent = Intent(this, MainActivity::class.java)
        auth.signInWithEmailAndPassword(binding.email.text.toString(), binding.password.text.toString()).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val auth = auth.currentUser!!
                db.collection("Beautician").document(auth.uid).get().addOnSuccessListener { document ->
                    if (document != null) {
                        val data = document.data

                        if (data != null) {
                            startActivity(intent)
                            finish()
                        } else {
                            binding.progressBar.gone()
                            Toast.makeText(this, "Please create a beautician account.", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Error, please check your information and try again.", Toast.LENGTH_LONG).show()
            }
        }
    }
}