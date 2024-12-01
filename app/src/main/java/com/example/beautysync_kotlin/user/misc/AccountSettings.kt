package com.example.beautysync_kotlin.user.misc

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.beautysync_kotlin.R
import com.example.beautysync_kotlin.Start
import com.example.beautysync_kotlin.databinding.ActivityAccountSettings2Binding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class AccountSettings : AppCompatActivity() {
    private lateinit var binding : ActivityAccountSettings2Binding
    private val db = Firebase.firestore
    private val storage = Firebase.storage
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAccountSettings2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.logOut.setOnClickListener {
            val intent = Intent(this, Start::class.java)
            Firebase.auth.signOut()
            startActivity(intent)
            finishAffinity()
        }

        binding.deleteAccount.setOnClickListener {
            val intent = Intent(this, Start::class.java)
            AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account?")
                // if the dialog is cancelable
                .setCancelable(false)
                .setPositiveButton("Yes") { dialog, _ ->
                        db.collection("User").document(FirebaseAuth.getInstance().currentUser!!.uid).delete()

                        db.collection("Usernames").document(FirebaseAuth.getInstance().currentUser!!.uid).delete()
                        storage.reference.child("users/${FirebaseAuth.getInstance().currentUser!!.uid}").delete()
                        FirebaseAuth.getInstance().currentUser!!.delete()
                        Toast.makeText(this, "Sorry to see you go. Hope to see you back.", Toast.LENGTH_LONG).show()
                        startActivity(intent)
                        finishAffinity()

                        dialog.dismiss()
                }

                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
        }
    }
}