package com.example.beautysync_kotlin.both.misc

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.beautysync_kotlin.R
import com.example.beautysync_kotlin.beautician.adapters.MeAdapter
import com.example.beautysync_kotlin.databinding.ActivityProfileAsUserBinding
import com.example.beautysync_kotlin.user.adapters.me.BeauticiansAdapter
import com.example.beautysync_kotlin.user.adapters.me.OrdersAndLikesAdapter
import com.example.beautysync_kotlin.user.adapters.me.ReviewsAdapter
import com.example.beautysync_kotlin.user.models.Beauticians
import com.example.beautysync_kotlin.user.models.ServiceItems
import com.example.beautysync_kotlin.user.models.UserReview
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class ProfileAsUser : AppCompatActivity() {
    private lateinit var binding : ActivityProfileAsUserBinding

    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()
    private val storage = Firebase.storage

    //Beautician
    private lateinit var meAdapter : MeAdapter

    //User
    private lateinit var ordersAndLikesAdapter : OrdersAndLikesAdapter
    private lateinit var beauticiansAdapter : BeauticiansAdapter
    private lateinit var reviewsAdapter: ReviewsAdapter

    private var items : MutableList<ServiceItems> = arrayListOf()
    private var beauticians: MutableList<Beauticians> = arrayListOf()
    private var reviews: MutableList<UserReview> = arrayListOf()

    private var itemType = "orders"

    private var beauticianOrUser = ""
    private var userId = ""

    private var item : ServiceItems? = null


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityProfileAsUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        beauticianOrUser = intent.getStringExtra("beautician_or_user").toString()
        userId = intent.getStringExtra("user_id").toString()

        binding.likesRecyclerView.layoutManager = LinearLayoutManager(this)
        ordersAndLikesAdapter = OrdersAndLikesAdapter(this, items, "guestLikes")
        binding.likesRecyclerView.adapter = ordersAndLikesAdapter

        binding.beauticiansRecyclerView.layoutManager = LinearLayoutManager(this)
        beauticiansAdapter = BeauticiansAdapter(this, beauticians)
        binding.beauticiansRecyclerView.adapter = beauticiansAdapter

        binding.reviewsRecyclerView.layoutManager = LinearLayoutManager(this)
        reviewsAdapter = ReviewsAdapter(this, reviews)
        binding.reviewsRecyclerView.adapter = reviewsAdapter

        binding.itemsRecyclerView.layoutManager = LinearLayoutManager(this)
        meAdapter = MeAdapter(this, items, itemType, "yes")
        binding.itemsRecyclerView.adapter = meAdapter

        if (beauticianOrUser == "Beautician") {
            itemType = "hairCareItems"
            loadHeadingInfo()
            loadItemInfo(itemType)
            binding.likesRecyclerView.isVisible = false
            binding.beauticiansRecyclerView.isVisible = false
            binding.reviewsRecyclerView.isVisible = false
            binding.itemsRecyclerView.isVisible = true
            binding.button1.isVisible = true
            binding.button1.text = "Hair Care"
            binding.button2.text = "Skin Care"
            binding.button3.text = "Nail Care"
            binding.button4.text = "Content"
        } else {
            binding.button1.isVisible = false
            binding.button2.text = "Beauticians"
            binding.button3.text = "Likes"
            binding.button4.text = "Reviews"
        }

        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.button1.setOnClickListener {
            if (beauticianOrUser == "Beautician") {
                itemType = "hairCareItems"
                loadItemInfo(itemType)
                binding.likesRecyclerView.isVisible = false
                binding.beauticiansRecyclerView.isVisible = false
                binding.reviewsRecyclerView.isVisible = false
                binding.itemsRecyclerView.isVisible = true
            }
            binding.button1.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
            binding.button1.setTextColor(ContextCompat.getColor(this, R.color.white))
            binding.button2.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            binding.button2.setTextColor(ContextCompat.getColor(this, R.color.main))
            binding.button3.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            binding.button3.setTextColor(ContextCompat.getColor(this, R.color.main))
            binding.button4.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            binding.button4.setTextColor(ContextCompat.getColor(this, R.color.main))
        }

        binding.button2.setOnClickListener {
            if (beauticianOrUser == "Beautician") {
                itemType = "skinCareItems"
                loadItemInfo(itemType)
                binding.likesRecyclerView.isVisible = false
                binding.beauticiansRecyclerView.isVisible = false
                binding.reviewsRecyclerView.isVisible = false
                binding.itemsRecyclerView.isVisible = true
            }
            binding.button1.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            binding.button1.setTextColor(ContextCompat.getColor(this, R.color.main))
            binding.button2.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
            binding.button2.setTextColor(ContextCompat.getColor(this, R.color.white))
            binding.button3.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            binding.button3.setTextColor(ContextCompat.getColor(this, R.color.main))
            binding.button4.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            binding.button4.setTextColor(ContextCompat.getColor(this, R.color.main))
        }

        binding.button3.setOnClickListener {
            if (beauticianOrUser == "Beautician") {
                itemType = "nailCareItems"
                loadItemInfo(itemType)
                binding.likesRecyclerView.isVisible = false
                binding.beauticiansRecyclerView.isVisible = false
                binding.reviewsRecyclerView.isVisible = false
                binding.itemsRecyclerView.isVisible = true
            }
            binding.button1.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            binding.button1.setTextColor(ContextCompat.getColor(this, R.color.main))
            binding.button2.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            binding.button2.setTextColor(ContextCompat.getColor(this, R.color.main))
            binding.button3.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
            binding.button3.setTextColor(ContextCompat.getColor(this, R.color.white))
            binding.button4.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            binding.button4.setTextColor(ContextCompat.getColor(this, R.color.main))
        }

        binding.button4.setOnClickListener {
            if (beauticianOrUser == "Beautician") {
                itemType = "Content"
            }
            binding.button1.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            binding.button1.setTextColor(ContextCompat.getColor(this, R.color.main))
            binding.button2.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            binding.button2.setTextColor(ContextCompat.getColor(this, R.color.main))
            binding.button3.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            binding.button3.setTextColor(ContextCompat.getColor(this, R.color.main))
            binding.button4.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
            binding.button4.setTextColor(ContextCompat.getColor(this, R.color.white))
        }




    }

    //Beautician
    @SuppressLint("SetTextI18n")
    private fun loadHeadingInfo() {
        db.collection("Beautician").document(userId).collection("PersonalInfo").get().addOnSuccessListener { documents ->
            if (documents != null) {
                for (doc in documents.documents) {
                    val data = doc.data

                    val userName = data?.get("userName") as String
                    binding.userName.text = "@$userName"
                }
            }
        }

        db.collection("Beautician").document(userId).collection("BusinessInfo").get().addOnSuccessListener { documents ->
            if (documents != null) {
                for (doc in documents.documents) {
                    val data = doc.data

                    val passion = data?.get("passion") as String
                    val city = data["city"] as String
                    val state = data["state"] as String

                    binding.passion.text = passion
                    binding.userLocation.text ="Location: $city, $state"
                }
            }
        }

        storage.reference.child("beauticians/$userId/profileImage/$userId.png").downloadUrl.addOnSuccessListener { itemUri ->
            Glide.with(this).load(itemUri).placeholder(R.drawable.default_profile).into(binding.userImage)
        }
    }
    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    @Suppress("UNCHECKED_CAST")
    private fun loadItemInfo(item: String) {
        items.clear()
        meAdapter.submitList(items)
        meAdapter.notifyDataSetChanged()

        db.collection("Beautician").document(userId).collection(item).get().addOnSuccessListener { documents ->
            if (documents != null) {
                for (doc in documents.documents) {
                    val data = doc.data

                    val itemType = data?.get("itemType") as String
                    val itemTitle = data["itemTitle"] as String
                    val itemDescription = data["itemDescription"] as String
                    val itemPrice = data["itemPrice"] as String
                    val imageCount = data["imageCount"] as Number
                    val hashtags = data["hashtags"] as ArrayList<String>
                    val beauticianCity = data["beauticianCity"] as String
                    val beauticianState = data["beauticianState"] as String
                    binding.progressBar.isVisible = false

                    db.collection(item).document(doc.id).get().addOnSuccessListener { document ->
                        if (document != null) {
                            val data1 = doc.data

                            val liked = data1?.get("liked") as ArrayList<String>
                            val itemOrders = data1["itemOrders"] as Number
                            val itemRating = data1["itemRating"] as ArrayList<Number>

                            val x = ServiceItems(itemType, itemTitle, Uri.EMPTY, itemDescription, itemPrice, imageCount.toInt(), binding.userName.text.toString(), Uri.EMPTY, binding.passion.text.toString(), beauticianCity, beauticianState, userId, liked, itemOrders.toInt(), itemRating, hashtags, doc.id)

                            if (items.size == 0) {
                                items.add(x)
                                meAdapter.submitList(items)
                                meAdapter.notifyItemInserted(0)
                            } else {
                                val index =
                                    items.indexOfFirst { it.documentId == doc.id }
                                if (index == -1) {
                                    items.add(x)
                                    meAdapter.submitList(items)
                                    meAdapter.notifyItemInserted(items.size - 1)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}