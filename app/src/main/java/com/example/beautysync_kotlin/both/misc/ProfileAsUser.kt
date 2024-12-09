package com.example.beautysync_kotlin.both.misc

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.beautysync_kotlin.R
import com.example.beautysync_kotlin.beautician.adapters.ContentAdapter
import com.example.beautysync_kotlin.beautician.adapters.MeAdapter
import com.example.beautysync_kotlin.both.models.VideoModel
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
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

private const val TAG = "ProfileAsUser"
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

    private val httpClient = OkHttpClient()
    private val mHandler: Handler = Handler(Looper.getMainLooper())

    private var content: MutableList<VideoModel> = arrayListOf()
    private lateinit var contentAdapter: ContentAdapter


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityProfileAsUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        beauticianOrUser = intent.getStringExtra("beautician_or_user").toString()
        userId = intent.getStringExtra("user_id").toString()

        binding.likesRecyclerView.layoutManager = LinearLayoutManager(this)
        ordersAndLikesAdapter = OrdersAndLikesAdapter(this, items, "guestLikes", "profileAsUser")
        binding.likesRecyclerView.adapter = ordersAndLikesAdapter

        binding.beauticiansRecyclerView.layoutManager = LinearLayoutManager(this)
        beauticiansAdapter = BeauticiansAdapter(this, beauticians, "profileAsUser")
        binding.beauticiansRecyclerView.adapter = beauticiansAdapter

        binding.reviewsRecyclerView.layoutManager = LinearLayoutManager(this)
        reviewsAdapter = ReviewsAdapter(this, reviews)
        binding.reviewsRecyclerView.adapter = reviewsAdapter

        binding.itemsRecyclerView.layoutManager = LinearLayoutManager(this)
        meAdapter = MeAdapter(this, items, itemType, "yes")
        binding.itemsRecyclerView.adapter = meAdapter

        contentAdapter = ContentAdapter(this, content, userId)
        binding.contentView.adapter = contentAdapter

        if (beauticianOrUser == "Beautician") {
            itemType = "hairCareItems"
            loadHeadingInfo()
            loadItemInfo(itemType)
            binding.likesRecyclerView.isVisible = false
            binding.beauticiansRecyclerView.isVisible = false
            binding.reviewsRecyclerView.isVisible = false
            binding.itemsRecyclerView.isVisible = true
            binding.contentView.isVisible = false
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
            loadHeading()
            loadBeauticians()
            binding.likesRecyclerView.isVisible = false
            binding.beauticiansRecyclerView.isVisible = true
            binding.reviewsRecyclerView.isVisible = false
            binding.itemsRecyclerView.isVisible = false
            binding.contentView.isVisible = false

            binding.button1.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            binding.button1.setTextColor(ContextCompat.getColor(this, R.color.main))
            binding.button2.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
            binding.button2.setTextColor(ContextCompat.getColor(this, R.color.white))
            binding.button3.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            binding.button3.setTextColor(ContextCompat.getColor(this, R.color.main))
            binding.button4.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            binding.button4.setTextColor(ContextCompat.getColor(this, R.color.main))
        }

        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.button1.setOnClickListener {
            binding.progressBar.isVisible = true
            if (beauticianOrUser == "Beautician") {
                itemType = "hairCareItems"
                loadItemInfo(itemType)
                binding.likesRecyclerView.isVisible = false
                binding.beauticiansRecyclerView.isVisible = false
                binding.reviewsRecyclerView.isVisible = false
                binding.itemsRecyclerView.isVisible = true
                binding.contentView.isVisible = false
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
            binding.progressBar.isVisible = true
            if (beauticianOrUser == "Beautician") {
                itemType = "skinCareItems"
                loadItemInfo(itemType)
                binding.likesRecyclerView.isVisible = false
                binding.beauticiansRecyclerView.isVisible = false
                binding.reviewsRecyclerView.isVisible = false
                binding.itemsRecyclerView.isVisible = true
                binding.contentView.isVisible = false
            } else {
                binding.likesRecyclerView.isVisible = false
                binding.beauticiansRecyclerView.isVisible = true
                binding.reviewsRecyclerView.isVisible = false
                binding.itemsRecyclerView.isVisible = false
                binding.contentView.isVisible = false
                loadBeauticians()
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
            binding.progressBar.isVisible = true
            if (beauticianOrUser == "Beautician") {
                itemType = "nailCareItems"
                loadItemInfo(itemType)
                binding.likesRecyclerView.isVisible = false
                binding.beauticiansRecyclerView.isVisible = false
                binding.reviewsRecyclerView.isVisible = false
                binding.itemsRecyclerView.isVisible = true
                binding.contentView.isVisible = false
            } else {
                binding.likesRecyclerView.isVisible = true
                binding.beauticiansRecyclerView.isVisible = false
                binding.reviewsRecyclerView.isVisible = false
                binding.itemsRecyclerView.isVisible = false
                binding.contentView.isVisible = false
                loadLikes()
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
            binding.progressBar.isVisible = true
            if (beauticianOrUser == "Beautician") {
                itemType = "Content"
                binding.likesRecyclerView.isVisible = false
                binding.beauticiansRecyclerView.isVisible = false
                binding.reviewsRecyclerView.isVisible = false
                binding.itemsRecyclerView.isVisible = false
                binding.contentView.isVisible = true
                loadContent()
            } else {
                binding.likesRecyclerView.isVisible = false
                binding.beauticiansRecyclerView.isVisible = false
                binding.reviewsRecyclerView.isVisible = true
                binding.itemsRecyclerView.isVisible = false
                binding.contentView.isVisible = false
                loadReviews()
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
                binding.progressBar.isVisible = false
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


                    db.collection(item).document(doc.id).get().addOnSuccessListener { document ->
                        if (document != null) {
                            val data1 = document.data

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

    private var createdAt = 0
    @Suppress("UNCHECKED_CAST")
    private fun loadContent() {

        val client = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS) // Connect timeout
            .readTimeout(20, TimeUnit.SECONDS) // Read timeout
            .writeTimeout(20, TimeUnit.SECONDS) // Write timeout
            .build()

//        binding.progressBar.isVisible = true
        Log.d(TAG, "loadVideos: $createdAt")
        val body = FormBody.Builder()
            .add("name", "${binding.userName.text}b")
            .build()

        val request = Request.Builder()
            .url("https://beautysync-videoserver.onrender.com/get-user-videos")
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .post(body)
            .build()

        client.newCall(request)
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {

                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        val responseData = response.body!!.string()
                        val responseJson =
                            JSONObject(responseData)

                        val videos = responseJson.getJSONArray("videos")

                        Log.d(TAG, "onResponse: ${videos.length()}")

                        mHandler.post {
                            binding.progressBar.isVisible = false

                            for (i in 0 until videos.length()) {
                                val id = videos.getJSONObject(i)["id"].toString()
                                val createdAtI = videos.getJSONObject(i)["createdAt"].toString()
                                if (i == videos.length() - 1) {
                                    createdAt = createdAtI.toInt()
                                }
                                var views = 5
                                var liked = arrayListOf<String>()
                                var comments = 0
                                var shared = 0



                                db.collection("Content").document(id).get().addOnSuccessListener { document ->
                                    if (document.exists()) {
                                        val data = document.data

                                        if (data?.get("views") != null) {
                                            val viewsI = data["views"] as Number
                                            views = viewsI.toInt()
                                        }
                                        if (data?.get("comments") != null) {
                                            val commentsI = data["comments"] as Number
                                            comments = commentsI.toInt()
                                        }

                                        if (data?.get("liked") != null) {
                                            val likedI = data["liked"] as java.util.ArrayList<String>
                                            liked = likedI
                                        }

                                        if (data?.get("shared") != null) {
                                            val sharedI = data["shared"] as Number
                                            shared = sharedI.toInt()
                                        }
                                        val newVideo = VideoModel(videos.getJSONObject(i)["dataUrl"].toString(), id, createdAtI, videos.getJSONObject(i)["name"].toString(), videos.getJSONObject(i)["description"].toString(), views, liked, comments, shared, videos.getJSONObject(i)["thumbnailUrl"].toString())

                                        if (content.isEmpty()) {
                                            content.add(newVideo)
                                            contentAdapter.submitList(content, auth.currentUser!!.uid)
                                            contentAdapter.notifyDataSetChanged()
                                        } else {
                                            val index = content.indexOfFirst { it.id == id }
                                            if (index == -1) {
                                                content.add(newVideo)
                                                contentAdapter.submitList(content, auth.currentUser!!.uid)
                                                contentAdapter.notifyDataSetChanged()
                                            }
                                        }
                                    } else {
                                        val newVideo = VideoModel(videos.getJSONObject(i)["dataUrl"].toString(), id, createdAtI, videos.getJSONObject(i)["name"].toString(), videos.getJSONObject(i)["description"].toString(), views, liked, comments, shared, videos.getJSONObject(i)["thumbnailUrl"].toString())

                                        if (content.isEmpty()) {
                                            content.add(newVideo)
                                            contentAdapter.submitList(content, auth.currentUser!!.uid)
                                            contentAdapter.notifyDataSetChanged()
                                        } else {
                                            val index = content.indexOfFirst { it.id == id }
                                            if (index == -1) {
                                                content.add(newVideo)
                                                contentAdapter.submitList(content, auth.currentUser!!.uid)
                                                contentAdapter.notifyDataSetChanged()
                                            }
                                        }
                                    }
                                }

                            }

                        }
                    }
                }
            })
    }

    //User
    @SuppressLint("SetTextI18n")
    private fun loadHeading() {
        db.collection("User").document(userId).collection("PersonalInfo").get().addOnSuccessListener { documents ->
            if (documents != null) {
                for (doc in documents.documents) {
                    val data = doc.data

                    val username = data?.get("userName") as String
                    val whatDoYouHopeToFind = data["whatDoYouHopeToFind"] as String
                    val city = data["city"] as String
                    val state = data["state"] as String
                    val local = data["local"] as Number
                    val region = data["region"] as Number
                    val nation = data["nation"] as Number

                    binding.userName.text = "@$username"
                    binding.passion.text = whatDoYouHopeToFind
                    if (local.toInt() == 1) {
                        binding.userLocation.text = "Location: $city, $state"
                    } else if (region.toInt() == 1) {
                        binding.userLocation.text = "Location: $state"
                    } else if (nation.toInt() == 1) {
                        binding.userLocation.text = "Location: Nationwide"
                    }

                }
            }
        }
        storage.reference.child("users/${userId}/profileImage/${userId}.png").downloadUrl.addOnSuccessListener { itemUrl ->
            Glide.with(this).load(itemUrl).placeholder(R.drawable.default_profile).into(binding.userImage)
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadBeauticians() {
        beauticians.clear()
        beauticiansAdapter.submitList(beauticians)
        beauticiansAdapter.notifyDataSetChanged()

        db.collection("User").document(userId).collection("Beauticians").get().addOnSuccessListener { documents ->
            if (documents != null) {
                binding.progressBar.isVisible = false
                for (doc in documents.documents) {
                    val data = doc.data

                    val beauticianCity = data?.get("beauticianCity") as String
                    val beauticianImageId = data["beauticianImageId"] as String
                    val beauticianPassion = data["beauticianPassion"] as String
                    val beauticianState = data["beauticianState"] as String
                    val beauticianUsername = data["beauticianUsername"] as String
                    val itemCount = data["itemCount"] as Number



                    val x = Beauticians(beauticianCity, beauticianImageId, beauticianPassion, beauticianState, beauticianUsername, itemCount.toInt(), doc.id)

                    if (beauticians.size == 0) {
                        beauticians.add(x)
                        beauticiansAdapter.submitList(beauticians)
                        beauticiansAdapter.notifyItemInserted(0)
                    } else {
                        val index =
                            items.indexOfFirst { it.documentId == doc.id }
                        if (index == -1) {
                            beauticians.add(x)
                            beauticiansAdapter.submitList(beauticians)
                            beauticiansAdapter.notifyItemInserted(beauticians.size - 1)
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Suppress("UNCHECKED_CAST")
    private fun loadLikes() {
        items.clear()
        ordersAndLikesAdapter.submitList(items)
        ordersAndLikesAdapter.notifyDataSetChanged()

        db.collection("User").document(userId).collection("Likes").get().addOnSuccessListener { documents ->
            if (documents != null) {
                binding.progressBar.isVisible = false
                for (doc in documents.documents) {
                    val data = doc.data

                    val beauticianCity = data?.get("beauticianCity") as String
                    val beauticianImageId = data["beauticianImageId"] as String
                    val beauticianPassion = data["beauticianPassion"] as String
                    val beauticianState = data["beauticianState"] as String
                    val beauticianUsername = data["beauticianUsername"] as String
                    val hashtags = data["hashtags"] as ArrayList<String>
                    val imageCount = data["imageCount"] as Number
                    val itemDescription = data["itemDescription"] as String
                    val itemPrice = data["itemPrice"] as String
                    val itemTitle = data["itemTitle"] as String
                    val itemType = data["itemType"] as String



                    db.collection(itemType).document(doc.id).get().addOnSuccessListener { document ->
                        if (document != null) {
                            val data1 = document.data

                            val liked = data1?.get("liked") as ArrayList<String>
                            val itemOrders = data1["itemOrders"] as Number
                            val itemRating = data1["itemRating"] as ArrayList<Number>

                            val x = ServiceItems(itemType, itemTitle, Uri.EMPTY, itemDescription, itemPrice, imageCount.toInt(), beauticianUsername, Uri.EMPTY, beauticianPassion, beauticianCity, beauticianState, beauticianImageId, liked, itemOrders.toInt(), itemRating, hashtags, doc.id)

                            if (items.size == 0) {
                                items.add(x)
                                ordersAndLikesAdapter.submitList(items)
                                ordersAndLikesAdapter.notifyItemInserted(0)
                            } else {
                                val index =
                                    items.indexOfFirst { it.documentId == doc.id }
                                if (index == -1) {
                                    items.add(x)
                                    ordersAndLikesAdapter.submitList(items)
                                    ordersAndLikesAdapter.notifyItemInserted(items.size - 1)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Suppress("UNCHECKED_CAST")
    private fun loadReviews() {
        reviews.clear()
        reviewsAdapter.submitList(reviews)
        reviewsAdapter.notifyDataSetChanged()

        db.collection("User").document(userId).collection("Reviews").get().addOnSuccessListener { documents ->
            if (documents != null) {
                binding.progressBar.isVisible = false
                for (doc in documents.documents) {
                    val data = doc.data


                    val beauticianImageId = data?.get("beauticianImageId") as String
                    val beauticianUsername = data["beauticianUsername"] as String
                    val date = data["date"] as String
                    val expectations = data["expectations"] as Number
                    val itemDescription = data["itemDescription"] as String
                    val itemId = data["itemId"] as String
                    val itemTitle = data["itemTitle"] as String
                    val itemType = data["itemType"] as String
                    val liked = data["liked"] as ArrayList<String>
                    val orderDate = data["orderDate"] as String
                    val quality = data["quality"] as Number
                    val rating = data["rating"] as Number
                    val recommend = data["recommend"] as Number
                    val thoughts = data["thoughts"] as String
                    val userImageId = data["userImageId"] as String
                    val userName = data["userName"] as String



                    val x = UserReview(date, expectations, quality, rating, recommend, thoughts, itemDescription, itemId, itemTitle, itemType, userImageId, userName, liked, beauticianUsername, beauticianImageId, orderDate, doc.id)

                    if (reviews.size == 0) {
                        reviews.add(x)
                        reviewsAdapter.submitList(reviews)
                        reviewsAdapter.notifyItemInserted(0)
                    } else {
                        val index =
                            reviews.indexOfFirst { it.documentId == doc.id }
                        if (index == -1) {
                            reviews.add(x)
                            reviewsAdapter.submitList(reviews)
                            reviewsAdapter.notifyItemInserted(reviews.size - 1)
                        }
                    }

                }
            }
        }
    }

}