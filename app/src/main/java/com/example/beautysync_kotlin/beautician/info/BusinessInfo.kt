package com.example.beautysync_kotlin.beautician.info

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.example.beautysync_kotlin.R
import com.example.beautysync_kotlin.beautician.MainActivity
import com.example.beautysync_kotlin.databinding.ActivityBusinessInfoBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class BusinessInfo : AppCompatActivity() {
    private lateinit var binding : ActivityBusinessInfoBinding

    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    private var newOrEdit = "new"

    private var openToTravel = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityBusinessInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (newOrEdit == "new") {
            binding.backButton.isVisible = true
        }

        binding.backButton.setOnClickListener {
            if (newOrEdit == "new") {
                Toast.makeText(this, "Back Button is not allowed yet.", Toast.LENGTH_LONG).show()
            } else {
                onBackPressedDispatcher.onBackPressed()
            }
        }

        binding.yesButton.setOnClickListener {
            openToTravel = 1
            binding.yesButton.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
            binding.yesButton.setTextColor(ContextCompat.getColor(this, R.color.white))
            binding.noButton.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            binding.noButton.setTextColor(ContextCompat.getColor(this, R.color.main))
        }

        binding.noButton.setOnClickListener {
            openToTravel = 0
            binding.yesButton.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            binding.yesButton.setTextColor(ContextCompat.getColor(this, R.color.main))
            binding.noButton.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
            binding.noButton.setTextColor(ContextCompat.getColor(this, R.color.white))
        }
        binding.saveButton.setOnClickListener {
            saveInfo()
        }
    }

    private fun saveInfo() {
        val intent = Intent(this, BankingInfo::class.java)
        val intent1 = Intent(this, MainActivity::class.java)
        if (binding.education.text.isEmpty()) {
            Toast.makeText(this, "Please enter your education in the allotted field. Self Taught is Fine!", Toast.LENGTH_LONG).show()
        } else if (binding.streetAddress.text.isEmpty() || binding.city.text.isEmpty() || binding.state.text.isEmpty() || binding.zipCode.text.isEmpty()) {
            Toast.makeText(this, "Please enter your street address in the allotted fields.", Toast.LENGTH_LONG).show()
        } else if (binding.passion.text.isEmpty()) {
            Toast.makeText(this, "Please enter a small statement regarding your passion in the allotted field.", Toast.LENGTH_LONG).show()
        } else if (stateFilter(binding.state.text.toString()) == "not good") {
            Toast.makeText(this, "Please enter the abbreviation for your state in the allotted field.", Toast.LENGTH_LONG).show()
        } else {
            val data: Map<String, Any> = hashMapOf(
                "education" to binding.education.text.toString(),
                "passion" to binding.passion.text.toString(),
                "streetAddress" to binding.streetAddress.text.toString(),
                "city" to binding.city.text.toString(),
                "state" to binding.state.text.toString(),
                "zipCode" to binding.zipCode.text.toString(),
                "openToTravel" to openToTravel
            )
            if (newOrEdit == "new") {
                db.collection("Beautician").document(auth.currentUser!!.uid).collection("BusinessInfo").document().set(data)
                Toast.makeText(this, "Business Info Updated", Toast.LENGTH_LONG).show()
                startActivity(intent)
                finish()
            } else {
                db.collection("Beautician").document(auth.currentUser!!.uid).collection("BusinessInfo").document().update(data)
                Toast.makeText(this, "Business Info Updated", Toast.LENGTH_LONG).show()
                startActivity(intent1)
                finish()
            }
        }
    }

    fun stateFilter(state: String): String {
        val stateAbbr: MutableList<String> = arrayListOf(
            "AL",
            "AK",
            "AZ",
            "AR",
            "AS",
            "CA",
            "CO",
            "CT",
            "DE",
            "DC",
            "FL",
            "GA",
            "HI",
            "ID",
            "IL",
            "IN",
            "IA",
            "KS",
            "KY",
            "LA",
            "ME",
            "MD",
            "MA",
            "MI",
            "MN",
            "MS",
            "MO",
            "NE",
            "NV",
            "NH",
            "NJ",
            "NM",
            "NY",
            "NC",
            "ND",
            "OH",
            "OK",
            "OR",
            "PA",
            "PR",
            "RI",
            "SC",
            "SD",
            "TN",
            "TX",
            "TT",
            "UT",
            "VT",
            "VA",
            "VI",
            "WA",
            "WY",
            "WV",
            "WI",
            "WY"
        )

        for (i in 0 until stateAbbr.size) {
            val a = stateAbbr[i].lowercase()
            if (a == state.lowercase()) {
                return "good"
            }
        }
        return "not good"
    }
}