package com.example.beautysync_kotlin.both.misc

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.example.beautysync_kotlin.R
import com.example.beautysync_kotlin.databinding.ActivityItemDetailBinding
import com.example.beautysync_kotlin.user.models.ServiceItems
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

@Suppress("DEPRECATION")
class ItemDetail : AppCompatActivity() {
    private lateinit var binding : ActivityItemDetailBinding

    private val storage = Firebase.storage

    private var item : ServiceItems? = null

    private val imageList = ArrayList<SlideModel>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityItemDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        item = intent.getParcelableExtra("item")

        loadInfo()

        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.reviewsButton.setOnClickListener {

        }

        binding.orderButton.setOnClickListener {

        }
    }

    @SuppressLint("SetTextI18n")
    private fun loadInfo() {
        loadImages()
        binding.itemTitle.text = item!!.itemTitle
        binding.itemDescription.text = item!!.itemDescription
        binding.itemPrice.text = "$${item!!.itemPrice}"
        binding.userName.text = item!!.beauticianUsername
    }

    private fun loadImages() {
        for (i in 0 until item!!.imageCount) {
            storage.reference.child("${item!!.itemType}/${item!!.beauticianImageId}/${item!!.documentId}/${item!!.documentId}$i.png").downloadUrl.addOnSuccessListener { itemUrl ->
                imageList.add(SlideModel(itemUrl.toString(), "", ScaleTypes.CENTER_CROP))
                binding.imageSlider.setImageList(imageList)
            }
        }
    }
}