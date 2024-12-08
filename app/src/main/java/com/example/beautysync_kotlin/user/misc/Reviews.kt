package com.example.beautysync_kotlin.user.misc

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.beautysync_kotlin.R
import com.example.beautysync_kotlin.both.models.Orders
import com.example.beautysync_kotlin.databinding.ActivityReviewsBinding
import com.example.beautysync_kotlin.user.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID

@Suppress("DEPRECATION")
class Reviews : AppCompatActivity() {

    private lateinit var binding : ActivityReviewsBinding

    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    private var item : Orders? = null

    private var expectation = 0
    private var quality = 0
    private var beauticianRating = 0
    private var recommend = 1

    @SuppressLint("SimpleDateFormat")
    private val sdf = SimpleDateFormat("MM-dd-yyyy")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityReviewsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        item = intent.getParcelableExtra("item")

        if (item != null) {
            binding.itemTitle.text = item!!.itemTitle
            binding.itemDescription.text = item!!.itemDescription
        }

        binding.expectations1.setOnClickListener {
            expectation = 1
            binding.expectations1.setImageResource(R.drawable.star_filled)
            binding.expectations2.setImageResource(R.drawable.star_unfilled)
            binding.expectations3.setImageResource(R.drawable.star_unfilled)
            binding.expectations4.setImageResource(R.drawable.star_unfilled)
            binding.expectations5.setImageResource(R.drawable.star_unfilled)
        }

        binding.expectations2.setOnClickListener {
            expectation = 2
            binding.expectations1.setImageResource(R.drawable.star_filled)
            binding.expectations2.setImageResource(R.drawable.star_filled)
            binding.expectations3.setImageResource(R.drawable.star_unfilled)
            binding.expectations4.setImageResource(R.drawable.star_unfilled)
            binding.expectations5.setImageResource(R.drawable.star_unfilled)
        }

        binding.expectations3.setOnClickListener {
            expectation = 3
            binding.expectations1.setImageResource(R.drawable.star_filled)
            binding.expectations2.setImageResource(R.drawable.star_filled)
            binding.expectations3.setImageResource(R.drawable.star_filled)
            binding.expectations4.setImageResource(R.drawable.star_unfilled)
            binding.expectations5.setImageResource(R.drawable.star_unfilled)
        }

        binding.expectations4.setOnClickListener {
            expectation = 4
            binding.expectations1.setImageResource(R.drawable.star_filled)
            binding.expectations2.setImageResource(R.drawable.star_filled)
            binding.expectations3.setImageResource(R.drawable.star_filled)
            binding.expectations4.setImageResource(R.drawable.star_filled)
            binding.expectations5.setImageResource(R.drawable.star_unfilled)
        }

        binding.expectations5.setOnClickListener {
            expectation = 5
            binding.expectations1.setImageResource(R.drawable.star_filled)
            binding.expectations2.setImageResource(R.drawable.star_filled)
            binding.expectations3.setImageResource(R.drawable.star_filled)
            binding.expectations4.setImageResource(R.drawable.star_filled)
            binding.expectations5.setImageResource(R.drawable.star_filled)
        }

        binding.quality1.setOnClickListener {
            quality = 1
            binding.quality1.setImageResource(R.drawable.star_filled)
            binding.quality2.setImageResource(R.drawable.star_unfilled)
            binding.quality3.setImageResource(R.drawable.star_unfilled)
            binding.quality4.setImageResource(R.drawable.star_unfilled)
            binding.quality5.setImageResource(R.drawable.star_unfilled)
        }

        binding.quality2.setOnClickListener {
            quality = 2
            binding.quality1.setImageResource(R.drawable.star_filled)
            binding.quality2.setImageResource(R.drawable.star_filled)
            binding.quality3.setImageResource(R.drawable.star_unfilled)
            binding.quality4.setImageResource(R.drawable.star_unfilled)
            binding.quality5.setImageResource(R.drawable.star_unfilled)
        }

        binding.quality3.setOnClickListener {
            quality = 3
            binding.quality1.setImageResource(R.drawable.star_filled)
            binding.quality2.setImageResource(R.drawable.star_filled)
            binding.quality3.setImageResource(R.drawable.star_filled)
            binding.quality4.setImageResource(R.drawable.star_unfilled)
            binding.quality5.setImageResource(R.drawable.star_unfilled)
        }

        binding.quality4.setOnClickListener {
            quality = 4
            binding.quality1.setImageResource(R.drawable.star_filled)
            binding.quality2.setImageResource(R.drawable.star_filled)
            binding.quality3.setImageResource(R.drawable.star_filled)
            binding.quality4.setImageResource(R.drawable.star_filled)
            binding.quality5.setImageResource(R.drawable.star_unfilled)
        }

        binding.quality5.setOnClickListener {
            quality = 5
            binding.quality1.setImageResource(R.drawable.star_filled)
            binding.quality2.setImageResource(R.drawable.star_filled)
            binding.quality3.setImageResource(R.drawable.star_filled)
            binding.quality4.setImageResource(R.drawable.star_filled)
            binding.quality5.setImageResource(R.drawable.star_filled)
        }

        binding.beauticianRating1.setOnClickListener {
            beauticianRating = 1
            binding.beauticianRating1.setImageResource(R.drawable.star_filled)
            binding.beauticianRating2.setImageResource(R.drawable.star_unfilled)
            binding.beauticianRating3.setImageResource(R.drawable.star_unfilled)
            binding.beauticianRating4.setImageResource(R.drawable.star_unfilled)
            binding.beauticianRating5.setImageResource(R.drawable.star_unfilled)
        }

        binding.beauticianRating2.setOnClickListener {
            beauticianRating = 2
            binding.beauticianRating1.setImageResource(R.drawable.star_filled)
            binding.beauticianRating2.setImageResource(R.drawable.star_filled)
            binding.beauticianRating3.setImageResource(R.drawable.star_unfilled)
            binding.beauticianRating4.setImageResource(R.drawable.star_unfilled)
            binding.beauticianRating5.setImageResource(R.drawable.star_unfilled)
        }

        binding.beauticianRating3.setOnClickListener {
            beauticianRating = 3
            binding.beauticianRating1.setImageResource(R.drawable.star_filled)
            binding.beauticianRating2.setImageResource(R.drawable.star_filled)
            binding.beauticianRating3.setImageResource(R.drawable.star_filled)
            binding.beauticianRating4.setImageResource(R.drawable.star_unfilled)
            binding.beauticianRating5.setImageResource(R.drawable.star_unfilled)
        }

        binding.beauticianRating4.setOnClickListener {
            beauticianRating = 4
            binding.beauticianRating1.setImageResource(R.drawable.star_filled)
            binding.beauticianRating2.setImageResource(R.drawable.star_filled)
            binding.beauticianRating3.setImageResource(R.drawable.star_filled)
            binding.beauticianRating4.setImageResource(R.drawable.star_filled)
            binding.beauticianRating5.setImageResource(R.drawable.star_unfilled)
        }

        binding.beauticianRating5.setOnClickListener {
            beauticianRating = 5
            binding.beauticianRating1.setImageResource(R.drawable.star_filled)
            binding.beauticianRating2.setImageResource(R.drawable.star_filled)
            binding.beauticianRating3.setImageResource(R.drawable.star_filled)
            binding.beauticianRating4.setImageResource(R.drawable.star_filled)
            binding.beauticianRating5.setImageResource(R.drawable.star_filled)
        }

        binding.experienceYes.setOnClickListener {
            recommend = 1
            binding.experienceYes.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
            binding.experienceYes.setTextColor(ContextCompat.getColor(this, R.color.white))
            binding.experienceNo.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            binding.experienceNo.setTextColor(ContextCompat.getColor(this, R.color.main))
        }

        binding.experienceNo.setOnClickListener {
            recommend = 0
            binding.experienceYes.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            binding.experienceYes.setTextColor(ContextCompat.getColor(this, R.color.main))
            binding.experienceNo.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
            binding.experienceNo.setTextColor(ContextCompat.getColor(this, R.color.white))
        }

        binding.saveButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("where_to", "orders")
            if (expectation == 0) {
                Toast.makeText(this, "Please rate your experience in the allotted area.", Toast.LENGTH_LONG).show()
            } else if (quality == 0) {
                Toast.makeText(this, "Please rate the quality of service in the allotted area.", Toast.LENGTH_LONG).show()
            } else if (beauticianRating == 0) {
                Toast.makeText(this, "Please rate the beautician in the allotted area.", Toast.LENGTH_LONG).show()
            } else {
                val thoughts = if (binding.thoughts.text.toString() == "") { "No Thoughts" } else { binding.thoughts.text.toString() }
                val data : Map<String, Any> = hashMapOf("expectations" to expectation, "quality" to quality, "rating" to beauticianRating, "recommend" to recommend, "thoughts" to thoughts, "itemType" to item!!.itemType, "itemId" to item!!.itemId, "itemDescription" to item!!.itemDescription, "itemTitle" to item!!.itemTitle, "date" to sdf.format(Date()), "userImageId" to item!!.userImageId, "liked" to item!!.liked, "userName" to item!!.userName, "beauticianUsername" to item!!.beauticianUsername, "beauticianImageId" to item!!.beauticianImageId, "orderDate" to item!!.eventDay)
                val data1: Map<String, Any> = hashMapOf("itemRating" to FieldValue.arrayUnion(beauticianRating))
                val data2: Map<String, Any> = hashMapOf("status" to "reviewed")
                val documentId = UUID.randomUUID().toString()
                db.collection(item!!.itemType).document(item!!.itemId).update(data1)
                db.collection(item!!.itemType).document(item!!.itemId).collection("Reviews").document(documentId).set(data)
                db.collection("User").document(auth.currentUser!!.uid).collection("Reviews").document(documentId).set(data)
                db.collection("User").document(auth.currentUser!!.uid).collection("Orders").document(item!!.documentId).update(data2)
                Toast.makeText(this, "Review Added. Thank You!", Toast.LENGTH_LONG).show()
                startActivity(intent)
                finish()
            }
        }


    }
}