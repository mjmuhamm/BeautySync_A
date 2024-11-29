package com.example.beautysync_kotlin.beautician.misc

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.beautysync_kotlin.R
import com.example.beautysync_kotlin.Start
import com.example.beautysync_kotlin.beautician.info.BankingInfo
import com.example.beautysync_kotlin.beautician.info.BusinessInfo
import com.example.beautysync_kotlin.beautician.info.PersonalInfo
import com.example.beautysync_kotlin.databinding.ActivityAccountSettingsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class AccountSettings : AppCompatActivity() {
    private lateinit var binding : ActivityAccountSettingsBinding
    private val db = Firebase.firestore
    private val storage = Firebase.storage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAccountSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.personalInfo.setOnClickListener {
            val intent = Intent(this, PersonalInfo::class.java)
            intent.putExtra("new_or_edit", "edit")
            startActivity(intent)
        }

        binding.businessInfo.setOnClickListener {
            val intent = Intent(this, BusinessInfo::class.java)
            intent.putExtra("new_or_edit", "edit")
            startActivity(intent)
        }

        binding.bankingInfo.setOnClickListener {
            val intent = Intent(this, BankingInfo::class.java)
            intent.putExtra("new_or_edit", "edit")
            startActivity(intent)
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
                    val array : MutableList<String> = arrayListOf("hairCareItems", "skinCareItems", "nailCareItems")
                    for (i in 0 until array.size) {
                    if (FirebaseAuth.getInstance().currentUser != null) {
                        db.collection("Beautician").document(FirebaseAuth.getInstance().currentUser!!.uid).collection(array[i]).get().addOnSuccessListener { documents ->
                            if (documents != null) {
                                for (doc in documents.documents) {
                                    db.collection(array[i]).document(doc.id).delete()
                                }
                            }
                        }
                    }
                        db.collection("Usernames").document(FirebaseAuth.getInstance().currentUser!!.uid).delete()
                        storage.reference.child("beauticians/${FirebaseAuth.getInstance().currentUser!!.uid}").delete()
                        FirebaseAuth.getInstance().currentUser!!.delete()
                        Toast.makeText(this, "Sorry to see you go. Hope to see you back.", Toast.LENGTH_LONG).show()
                        startActivity(intent)
                        finishAffinity()

                        dialog.dismiss()
                    }
                }

                    .setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create()
                    .show()
        }

    }
}