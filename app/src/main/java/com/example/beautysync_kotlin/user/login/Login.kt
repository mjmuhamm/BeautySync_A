package com.example.beautysync_kotlin.user.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.example.beautysync_kotlin.R
import com.example.beautysync_kotlin.databinding.ActivityLoginBinding
import com.example.beautysync_kotlin.user.MainActivity
import com.example.beautysync_kotlin.user.info.PersonalInfo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class Login : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private var db = Firebase.firestore
    private var auth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.signup.setOnClickListener {
            val intent = Intent(this, PersonalInfo::class.java)
            startActivity(intent)
        }

        binding.login.setOnClickListener {
            login()
        }

        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun login() {
        binding.progressBar.isVisible = true
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("where_to", "home")
        auth.signInWithEmailAndPassword(binding.email.text.toString(), binding.password.text.toString()).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser!!

                db.collection("User").document(user.uid).get().addOnSuccessListener { document ->
                    if (document != null) {
                        val data = document.data

                        if (data != null) {
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this, "Please create a user account.", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } else {
                binding.progressBar.isVisible = false
                Toast.makeText(this, "Error, Please check your information.", Toast.LENGTH_LONG).show()
            }
        }
    }
}

