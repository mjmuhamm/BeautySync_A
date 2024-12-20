package com.example.beautysync_kotlin.beautician.fragments

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
import com.example.beautysync_kotlin.beautician.adapters.ContentAdapter
import com.example.beautysync_kotlin.beautician.adapters.MeAdapter
import com.example.beautysync_kotlin.beautician.misc.AccountSettings
import com.example.beautysync_kotlin.beautician.misc.ServiceItemAdd
import com.example.beautysync_kotlin.both.models.VideoModel
import com.example.beautysync_kotlin.databinding.FragmentMe2Binding
import com.example.beautysync_kotlin.user.adapters.HomeAdapter
import com.example.beautysync_kotlin.user.models.ServiceItems
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
import java.util.concurrent.TimeUnit

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

    private var _binding : FragmentMe2Binding? = null
    private val binding get() = _binding!!

    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()
    private val storage = Firebase.storage

    private var city = ""
    private var state = ""

    private var itemType = "hairCareItems"
    private val items : MutableList<ServiceItems> = arrayListOf()
    private lateinit var meAdapter : MeAdapter

    private var content: MutableList<VideoModel> = arrayListOf()
    private lateinit var contentAdapter: ContentAdapter


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
        _binding = FragmentMe2Binding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        binding.serviceRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        meAdapter = MeAdapter(requireContext(), items, itemType, "no")
        binding.serviceRecyclerView.adapter = meAdapter

        contentAdapter = ContentAdapter(requireContext(), content, auth.currentUser!!.uid)
        binding.contentView.adapter = contentAdapter


        loadItemInfo(itemType)
        loadOrders()
        binding.settingsButton.setOnClickListener {
            val intent = Intent(requireContext(), AccountSettings::class.java)
            startActivity(intent)
        }

        binding.addItemButton.setOnClickListener {
            val intent = Intent(requireContext(), ServiceItemAdd::class.java)
            intent.putExtra("item_type", itemType)
            intent.putExtra("new_or_edit", "new")
            intent.putExtra("beautician_username", binding.userName.text.toString())
            intent.putExtra("beautician_passion", binding.passion.text.toString())
            intent.putExtra("beautician_city", city)
            intent.putExtra("beautician_state", state)
            startActivity(intent)

        }
        binding.hairCare.setOnClickListener {
            itemType = "hairCareItems"
            loadItemInfo(itemType)

            binding.serviceRecyclerView.isVisible = true
            binding.contentView.isVisible = false

            binding.hairCare.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.secondary))
            binding.hairCare.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.skinCare.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.skinCare.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            binding.nailCare.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.nailCare.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            binding.content.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.content.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
        }

        binding.skinCare.setOnClickListener {
            itemType = "skinCareItems"

            binding.serviceRecyclerView.isVisible = true
            binding.contentView.isVisible = false

            loadItemInfo(itemType)
            binding.hairCare.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.hairCare.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            binding.skinCare.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.secondary))
            binding.skinCare.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.nailCare.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.nailCare.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            binding.content.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.content.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
        }

        binding.nailCare.setOnClickListener {
            itemType = "nailCareItems"

            binding.serviceRecyclerView.isVisible = true
            binding.contentView.isVisible = false

            loadItemInfo(itemType)

            Log.d(TAG, "onCreateView: 3")

            binding.hairCare.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.hairCare.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            binding.skinCare.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.skinCare.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            binding.nailCare.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.secondary))
            binding.nailCare.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.content.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.content.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
        }

        binding.content.setOnClickListener {
            itemType = "content"

            binding.serviceRecyclerView.isVisible = false
            binding.contentView.isVisible = true

            loadContent()

            Log.d(TAG, "onCreateView: 4")

            binding.hairCare.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.hairCare.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            binding.skinCare.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.skinCare.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            binding.nailCare.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.nailCare.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            binding.content.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.secondary))
            binding.content.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        }

        loadHeadingInfo()
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
    private fun loadHeadingInfo() {
        db.collection("Beautician").document(auth.currentUser!!.uid).collection("PersonalInfo").get().addOnSuccessListener { documents ->
            if (documents != null) {
                for (doc in documents.documents) {
                    val data = doc.data

                    val userName = data?.get("userName") as String
                    binding.userName.text = "@$userName"
                }
            }
        }

        db.collection("Beautician").document(auth.currentUser!!.uid).collection("BusinessInfo").get().addOnSuccessListener { documents ->
            if (documents != null) {
                for (doc in documents.documents) {
                    val data = doc.data

                    val passion = data?.get("passion") as String
                    val city = data["city"] as String
                    val state = data["state"] as String

                    this.city = city
                    this.state = state
                    binding.passion.text = passion
                    binding.location.text ="Location: $city, $state"
                }
            }
        }

        storage.reference.child("beauticians/${auth.currentUser!!.uid}/profileImage/${auth.currentUser!!.uid}.png").downloadUrl.addOnSuccessListener { itemUri ->
            Glide.with(requireContext()).load(itemUri).placeholder(R.drawable.default_profile).into(binding.userImage)
        }
    }
    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    @Suppress("UNCHECKED_CAST")
    private fun loadItemInfo(item: String) {
        items.clear()
        meAdapter.submitList(items)
        meAdapter.notifyDataSetChanged()

        db.collection("Beautician").document(auth.currentUser!!.uid).collection(item).get().addOnSuccessListener { documents ->
            if (documents != null) {
                for (doc in documents.documents) {
                    val data = doc.data

                    val itemType = data?.get("itemType") as String
                    val itemTitle = data["itemTitle"] as String
                    val itemDescription = data["itemDescription"] as String
                    val itemPrice = data["itemPrice"] as String
                    val imageCount = data["imageCount"] as Number
                    val hashtags = data["hashtags"] as ArrayList<String>

                    db.collection(item).document(doc.id).get().addOnSuccessListener { document ->
                        if (document != null) {
                            val data1 = doc.data

                            val liked = data1?.get("liked") as ArrayList<String>
                            val itemOrders = data1["itemOrders"] as Number
                            val itemRating = data1["itemRating"] as ArrayList<Number>

                            val x = ServiceItems(itemType, itemTitle, Uri.EMPTY, itemDescription, itemPrice, imageCount.toInt(), binding.userName.text.toString(), Uri.EMPTY, binding.passion.text.toString(), city, state, auth.currentUser!!.uid, liked, itemOrders.toInt(), itemRating, hashtags, doc.id)

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
        binding.addItemButton.isVisible = true

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
            .url("http://beautysync-videoserver.onrender.com/get-user-videos")
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
                                binding.progressBar.isVisible = false


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

    private fun loadOrders() {

        db.collection("Beautician").document(auth.currentUser!!.uid).collection("Orders").addSnapshotListener { documents, _ ->
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
                Log.d(TAG, "compareDates: pass1")
                transferStatus(documentId, beauticianImageId, userImageId)
                return true
            } else if (year1.toInt() == year2.toInt()) {
                if (month1.toInt() > month2.toInt()) {
                    //pass
                    Log.d(TAG, "compareDates: pass2")
                    transferStatus(documentId, beauticianImageId, userImageId)
                    return true
                } else {
                    if (month1.toInt() == month2.toInt()) {
                        if (day1.toInt() > day2.toInt()) {
                            //pass
                            Log.d(TAG, "compareDates: pass3")
                            transferStatus(documentId, beauticianImageId, userImageId)
                            return true
                        } else {
                            if (day1.toInt() == day2.toInt()) {
                                if (amOrPm1 == "PM" && amOrPm2 == "AM") {
                                    when {
                                        hour1.toInt() > 1 -> {
                                            //pass
                                            Log.d(TAG, "compareDates: pass4")
                                            transferStatus(
                                                documentId,
                                                beauticianImageId,
                                                userImageId
                                            )
                                            return true
                                        }

                                        min1.toInt() >= min2.toInt() -> {
                                            //pass
                                            Log.d(TAG, "compareDates: pass5")
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
                                        Log.d(TAG, "compareDates: pass6")
                                        transferStatus(documentId, beauticianImageId, userImageId)
                                        return true
                                    } else if (hour1.toInt() > hour2.toInt() && (min1.toInt() >= min2.toInt())) {
                                        //pass
                                        Log.d(TAG, "compareDates: pass7")
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