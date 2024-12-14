package com.example.beautysync_kotlin.user.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.beautysync_kotlin.R
import com.example.beautysync_kotlin.both.models.Orders
import com.example.beautysync_kotlin.databinding.ActivityAccountSettings2Binding
import com.example.beautysync_kotlin.databinding.FragmentMe2Binding
import com.example.beautysync_kotlin.databinding.FragmentMeBinding
import com.example.beautysync_kotlin.user.adapters.me.BeauticiansAdapter
import com.example.beautysync_kotlin.user.adapters.me.OrdersAndLikesAdapter
import com.example.beautysync_kotlin.user.adapters.me.ReviewsAdapter
import com.example.beautysync_kotlin.user.misc.AccountSettings
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Me.newInstance] factory method to
 * create an instance of this fragment.
 */
private const val TAG = "Me"
class Me : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding : FragmentMeBinding? = null
    private val binding get() = _binding!!

    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()
    private val storage = Firebase.storage

    //User
    private lateinit var ordersAndLikesAdapter : OrdersAndLikesAdapter
    private lateinit var beauticiansAdapter : BeauticiansAdapter
    private lateinit var reviewsAdapter: ReviewsAdapter

    private var items : MutableList<ServiceItems> = arrayListOf()
    private var beauticians: MutableList<Beauticians> = arrayListOf()
    private var reviews: MutableList<UserReview> = arrayListOf()

    private var itemType = "orders"

    //Compare Dates
    @SuppressLint("SimpleDateFormat")
    private var sdfMonth = SimpleDateFormat("MM")

    @SuppressLint("SimpleDateFormat")
    private var sdfDay = SimpleDateFormat("dd")

    @SuppressLint("SimpleDateFormat")
    private var sdfYear = SimpleDateFormat("yyyy")

    @SuppressLint("SimpleDateFormat")
    private var sdfHour = SimpleDateFormat("hh")

    @SuppressLint("SimpleDateFormat")
    private var sdfMin = SimpleDateFormat("mm")

    @SuppressLint("SimpleDateFormat")
    private var sdfAmOrPm = SimpleDateFormat("a")

    @SuppressLint("SimpleDateFormat")
    private val sdf = SimpleDateFormat("MM-dd-yyyy")

    private val httpClient = OkHttpClient()
    private val mHandler: Handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMeBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment

        binding.ordersAndLikesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        ordersAndLikesAdapter = OrdersAndLikesAdapter(requireContext(), items, itemType, "me")
        binding.ordersAndLikesRecyclerView.adapter = ordersAndLikesAdapter

        binding.beauticiansRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        beauticiansAdapter = BeauticiansAdapter(requireContext(), beauticians, "me")
        binding.beauticiansRecyclerView.adapter = beauticiansAdapter

        binding.reviewsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        reviewsAdapter = ReviewsAdapter(requireContext(), reviews)
        binding.reviewsRecyclerView.adapter = reviewsAdapter

        loadHeading()
        loadOrders1()

        binding.ordersAndLikesRecyclerView.isVisible = true
        binding.beauticiansRecyclerView.isVisible = false
        binding.reviewsRecyclerView.isVisible = false

        binding.settingsButton.setOnClickListener {
            val intent = Intent(requireContext(), AccountSettings::class.java)
            startActivity(intent)
        }

        binding.orders.setOnClickListener {
            itemType = "orders"

            binding.ordersAndLikesRecyclerView.isVisible = true
            binding.beauticiansRecyclerView.isVisible = false
            binding.reviewsRecyclerView.isVisible = false

            binding.ordersAndLikesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            ordersAndLikesAdapter = OrdersAndLikesAdapter(requireContext(), items, itemType, "me")
            binding.ordersAndLikesRecyclerView.adapter = ordersAndLikesAdapter

            loadOrders()
            binding.orders.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.secondary))
            binding.orders.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.beauticians.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.beauticians.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            binding.likes.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.likes.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            binding.reviews.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.reviews.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
        }

        binding.beauticians.setOnClickListener {
            loadBeauticians()

            binding.ordersAndLikesRecyclerView.isVisible = false
            binding.beauticiansRecyclerView.isVisible = true
            binding.reviewsRecyclerView.isVisible = false

            binding.orders.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.orders.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            binding.beauticians.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.secondary))
            binding.beauticians.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.likes.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.likes.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            binding.reviews.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.reviews.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
        }

        binding.likes.setOnClickListener {
            itemType = "likes"

            binding.ordersAndLikesRecyclerView.isVisible = true
            binding.beauticiansRecyclerView.isVisible = false
            binding.reviewsRecyclerView.isVisible = false

            binding.ordersAndLikesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            ordersAndLikesAdapter = OrdersAndLikesAdapter(requireContext(), items, itemType, "me")
            binding.ordersAndLikesRecyclerView.adapter = ordersAndLikesAdapter

            loadLikes()

            binding.orders.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.orders.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            binding.beauticians.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.beauticians.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            binding.likes.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.secondary))
            binding.likes.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.reviews.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.reviews.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
        }

        binding.reviews.setOnClickListener {

            binding.ordersAndLikesRecyclerView.isVisible = false
            binding.beauticiansRecyclerView.isVisible = false
            binding.reviewsRecyclerView.isVisible = true


            loadReviews()

            binding.orders.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.orders.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            binding.beauticians.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.beauticians.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            binding.likes.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.likes.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            binding.reviews.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.secondary))
            binding.reviews.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        }


        loadOrders()

        return binding.root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Me.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Me().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    @SuppressLint("SetTextI18n")
    private fun loadHeading() {
        db.collection("User").document(auth.currentUser!!.uid).collection("PersonalInfo").get().addOnSuccessListener { documents ->
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
                        binding.location.text = "Location: $city, $state"
                    } else if (region.toInt() == 1) {
                        binding.location.text = "Location: $state"
                    } else if (nation.toInt() == 1) {
                        binding.location.text = "Location: Nationwide"
                    }

                }
            }
        }
        storage.reference.child("users/${auth.currentUser!!.uid}/profileImage/${auth.currentUser!!.uid}.png").downloadUrl.addOnSuccessListener { itemUrl ->
            Glide.with(this).load(itemUrl).placeholder(R.drawable.default_profile).into(binding.userImage)
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    @Suppress("UNCHECKED_CAST")
    private fun loadOrders() {
        items.clear()
        ordersAndLikesAdapter.submitList(items)
        ordersAndLikesAdapter.notifyDataSetChanged()

        db.collection("User").document(auth.currentUser!!.uid).collection("Orders").get().addOnSuccessListener { documents ->
            if (documents != null) {
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
                    val itemId = data["itemId"] as String
                    val itemPrice = data["itemPrice"] as String
                    val itemTitle = data["itemTitle"] as String
                    val itemType = data["itemType"] as String
                    Log.d(TAG, "loadOrders: itemType $itemType")
                    Log.d(TAG, "loadOrders: itemType $itemId")

                    db.collection(itemType).document(itemId).get().addOnSuccessListener {
                        document ->
                        if (document != null) {
                            val data1 = document.data

                            val liked = data1?.get("liked") as ArrayList<String>
                            val itemOrders = data1["itemOrders"] as Number
                            val itemRating = data1["itemRating"] as ArrayList<Number>

                            val x = ServiceItems(itemType, itemTitle, Uri.EMPTY, itemDescription, itemPrice, imageCount.toInt(), beauticianUsername, Uri.EMPTY, beauticianPassion, beauticianCity, beauticianState, beauticianImageId, liked, itemOrders.toInt(), itemRating, hashtags, itemId)

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
    private fun loadBeauticians() {
        beauticians.clear()
        beauticiansAdapter.submitList(beauticians)
        beauticiansAdapter.notifyDataSetChanged()

        db.collection("User").document(auth.currentUser!!.uid).collection("Beauticians").get().addOnSuccessListener { documents ->
            if (documents != null) {
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

        db.collection("User").document(auth.currentUser!!.uid).collection("Likes").get().addOnSuccessListener { documents ->
            if (documents != null) {
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

        db.collection("User").document(auth.currentUser!!.uid).collection("Reviews").get().addOnSuccessListener { documents ->
            if (documents != null) {
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

    private fun loadOrders1() {

        db.collection("User").document(auth.currentUser!!.uid).collection("Orders").addSnapshotListener { documents, _ ->
            if (documents != null) {
                for (doc in documents.documents) {
                    val data = doc.data

                    val beauticianImageId = data?.get("beauticianImageId") as String
                    val eventDay = data["eventDay"] as String
                    val eventTime = data["eventTime"] as String
                    val userImageId = data["userImageId"] as String
                    val status = data["status"] as String


                    compareDates(status,"$eventDay $eventTime", doc.id, beauticianImageId, userImageId)


                }
            }
        }
    }

    private fun compareDates(status: String, date: String, documentId: String, beauticianImageId: String, userImageId: String) : Boolean {
        val month1 = sdfMonth.format(Date())
        val day1 = sdfDay.format(Date())
        val year1 = sdfYear.format(Date())
        val date1 = sdf.format(Date())
        val hour1 = sdfHour.format(Date())
        val min1 = sdfMin.format(Date())
        val amOrPm1 = sdfAmOrPm.format(Date())


        val month2 = date.substring(0, 2)
        val day2 = date.substring(3,5)
        val year2 = date.substring(6,10)
        val date2 = date
        val hour2 = date.substring(11,13)
        val min2 = date.substring(14,16)
        val amOrPm2 = date.substring(17,19)

        if (status == "scheduled") {
            if (year1.toInt() > year2.toInt()) {
                //pass
                transferStatus(documentId, beauticianImageId, userImageId)
                return true
            } else if (year1.toInt() == year2.toInt()) {
                if (month1.toInt() > month2.toInt()) {
                    //pass
                    transferStatus(documentId, beauticianImageId, userImageId)
                    return true
                } else {
                    if (month1.toInt() == month2.toInt()) {
                        if (day1.toInt() > day2.toInt()) {
                            //pass
                            transferStatus(documentId, beauticianImageId, userImageId)
                            return true
                        } else {
                            if (day1.toInt() == day2.toInt()) {
                                if (amOrPm1 == "PM" && amOrPm2 == "AM") {
                                    when {
                                        hour1.toInt() > 1 -> {
                                            //pass
                                            transferStatus(
                                                documentId,
                                                beauticianImageId,
                                                userImageId
                                            )
                                            return true
                                        }

                                        min1.toInt() >= min2.toInt() -> {
                                            //pass
                                            transferStatus(
                                                documentId,
                                                beauticianImageId,
                                                userImageId
                                            )
                                            return true
                                        }
                                    }

                                } else if ((amOrPm1 == "AM" && amOrPm2 == "AM") || amOrPm1 == "PM" && amOrPm2 == "PM") {
                                    if (hour1.toInt() - hour2.toInt() > 1) {
                                        //pass
                                        transferStatus(documentId, beauticianImageId, userImageId)
                                        return true
                                    } else if (hour1.toInt() > hour2.toInt() && (min1.toInt() >= min2.toInt())) {
                                        //pass
                                        transferStatus(documentId, beauticianImageId, userImageId)
                                        return true
                                    }

                                }
                            }
                        }
                    }
                }
            }
        }
        return false
    }

    private fun transferStatus(documentId: String, beauticianImageId: String, userImageId: String) {

        db.collection("Orders").document(documentId).get().addOnSuccessListener { document ->
            if (document != null) {
                val data1 = document.data

                val status = data1?.get("status") as String
                val itemPrice = data1["itemPrice"] as String
                val eventDay = data1["eventDay"] as String
                val eventTime = data1["eventTime"] as String

                if (status != "cancelled" && status != "complete") {
                    transferFunds(itemPrice, documentId, "$eventDay $eventTime", beauticianImageId, userImageId)
                    val data : Map<String, Any> = hashMapOf("status" to "complete")
                    db.collection("User").document(userImageId).collection("Orders").document(documentId).update(data)
                    db.collection("Beautician").document(beauticianImageId).collection("Orders").document(documentId).update(data)
                    db.collection("Orders").document(documentId).update(data)
                }
            }
        }
    }

    private fun transferFunds(itemPrice: String, orderId: String, eventDate: String, beauticianImageId: String, userImageId: String) {

        val date = Calendar.getInstance().timeInMillis / 1000
        val x = itemPrice.toDouble() * 0.95 * 100
        val y = "%.0f".format(x)
        db.collection("Beautician").document(beauticianImageId).collection("BankingInfo").get().addOnSuccessListener { documents ->
            if (documents != null) {
                for (doc in documents.documents) {
                    val data = doc.data

                    val stripeAccountId = data?.get("stripeAccountId") as String


                    val body = FormBody.Builder()
                        .add("amount", y)
                        .add("stripeAccountId", stripeAccountId)
                        .build()

                    val request = Request.Builder()
                        .url("https://beautysync-stripeserver.onrender.com/transfer")
                        .addHeader("Content-Type", "application/json; charset=utf-8")
                        .post(body)
                        .build()

                    httpClient.newCall(request)
                        .enqueue(object : Callback {
                            override fun onFailure(call: Call, e: IOException) {
                            }

                            @SuppressLint("SetTextI18n")
                            override fun onResponse(call: Call, response: Response) {
                                when {
                                    !response.isSuccessful -> {

                                    }
                                    else -> {
                                        val responseData = response.body!!.string()
                                        val json =
                                            JSONObject(responseData)

                                        val transferId = json.getString("transferId")


                                        mHandler.post {
                                            val data1: Map<String, Any> = hashMapOf("transferId" to transferId, "orderId" to id, "date" to sdf.format(Date()), "amount" to "${itemPrice.toDouble() * 0.95 * 100}", "userImageId" to userImageId, "beauticianImageId" to beauticianImageId, "eventDate" to eventDate)
                                            db.collection("Transfers").document(orderId).set(data1)
                                        }
                                    }
                                }
                            }
                        })
                }
            }
        }
    }


}