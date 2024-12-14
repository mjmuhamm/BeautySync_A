package com.example.beautysync_kotlin.user.fragments

import android.annotation.SuppressLint
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
import com.example.beautysync_kotlin.R
import com.example.beautysync_kotlin.databinding.FragmentOrdersBinding
import com.example.beautysync_kotlin.both.adapters.OrdersAdapter
import com.google.firebase.auth.FirebaseAuth
import com.example.beautysync_kotlin.both.models.Orders
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
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
 * Use the [Orders.newInstance] factory method to
 * create an instance of this fragment.
 */
private const val TAG = "Orders"
class Orders : Fragment() {

    private var _binding : FragmentOrdersBinding? = null
    private val binding get() = _binding!!

    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    private lateinit var ordersAdapter: OrdersAdapter
    private var orders : MutableList<Orders> = arrayListOf()

    private var item = "pending"

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

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

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
        // Inflate the layout for this fragment
        _binding = FragmentOrdersBinding.inflate(inflater, container, false)

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        ordersAdapter = OrdersAdapter(requireContext(), orders, "User", "pending")
        binding.recyclerView.adapter = ordersAdapter


        loadOrders(item)

        binding.pending.setOnClickListener {
            item = "pending"

            binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
            ordersAdapter = OrdersAdapter(requireContext(), orders, "User", "pending")
            binding.recyclerView.adapter = ordersAdapter

            loadOrders(item)

            binding.pending.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.secondary))
            binding.pending.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.scheduled.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.scheduled.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            binding.complete.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.complete.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
        }

        binding.scheduled.setOnClickListener {
            item = "scheduled"

            binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
            ordersAdapter = OrdersAdapter(requireContext(), orders, "User", "scheduled")
            binding.recyclerView.adapter = ordersAdapter

            loadOrders(item)

            binding.pending.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.pending.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            binding.scheduled.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.secondary))
            binding.scheduled.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.complete.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.complete.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
        }

        binding.complete.setOnClickListener {
            item = "complete"

            binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
            ordersAdapter = OrdersAdapter(requireContext(), orders, "User", "complete")
            binding.recyclerView.adapter = ordersAdapter

            loadOrders(item)

            binding.pending.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.pending.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            binding.scheduled.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.scheduled.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            binding.complete.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.secondary))
            binding.complete.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        }

        return binding.root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Orders.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Orders().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Suppress("UNCHECKED_CAST")
    private fun loadOrders(item: String) {
        orders.clear()
        ordersAdapter.submitList(orders)
        ordersAdapter.notifyDataSetChanged()

        db.collection("User").document(auth.currentUser!!.uid).collection("Orders").addSnapshotListener { documents, _ ->
            if (documents != null) {
                for (doc in documents.documents) {
                    val data = doc.data

                    val itemType = data?.get("itemType") as String
                    val itemTitle = data["itemTitle"] as String
                    val itemDescription = data["itemDescription"] as String
                    val itemPrice = data["itemPrice"] as String
                    val imageCount = data["imageCount"] as Number
                    val beauticianUsername = data["beauticianUsername"] as String
                    val beauticianPassion = data["beauticianPassion"] as String
                    val beauticianCity = data["beauticianCity"] as String
                    val beauticianState = data["beauticianState"] as String
                    val beauticianImageId = data["beauticianImageId"] as String
                    val itemOrders = data["itemOrders"] as Number
                    val itemRating = data["itemRating"] as ArrayList<Number>
                    val hashtags = data["hashtags"] as ArrayList<String>
                    val liked = data["liked"] as ArrayList<String>
                    val streetAddress = data["streetAddress"] as String
                    val zipCode = data["zipCode"] as String
                    val eventDay = data["eventDay"] as String
                    val eventTime = data["eventTime"] as String
                    val notesToBeautician = data["notesToBeautician"] as String
                    val userImageId = data["userImageId"] as String
                    val status = data["status"] as String
                    val itemId = data["itemId"] as String
                    val userName = data["userName"] as String
                    val notifications = data["notifications"] as String
                    val cancelled = data["cancelled"] as String

                    binding.progressBar.isVisible = false

                    Log.d(TAG, "loadOrders: eventDay $eventDay $eventTime")
                    if (!compareDates(status,"$eventDay $eventTime", doc.id, beauticianImageId, userImageId)) {
                        if (status == item) {
                            val x = Orders(
                                itemType,
                                itemTitle,
                                itemDescription,
                                itemPrice,
                                imageCount,
                                beauticianUsername,
                                beauticianPassion,
                                beauticianCity,
                                beauticianState,
                                beauticianImageId,
                                liked,
                                itemOrders,
                                itemRating,
                                hashtags,
                                doc.id,
                                eventDay,
                                eventTime,
                                streetAddress,
                                zipCode,
                                notesToBeautician,
                                userImageId,
                                userName,
                                status,
                                notifications,
                                itemId,
                                cancelled
                            )

                            if (orders.size == 0) {
                                orders.add(x)
                                ordersAdapter.submitList(orders)
                                ordersAdapter.notifyItemInserted(0)
                            } else {
                                val index =
                                    orders.indexOfFirst { it.documentId == doc.id }
                                if (index == -1) {
                                    orders.add(x)
                                    ordersAdapter.submitList(orders)
                                    ordersAdapter.notifyItemInserted(orders.size - 1)
                                }
                            }
                        }
                    }
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
                Log.d(TAG, "compareDates: happening1")
                transferStatus(documentId, beauticianImageId, userImageId)
                return true
            } else if (year1.toInt() == year2.toInt()) {
                if (month1.toInt() > month2.toInt()) {
                    //pass
                    Log.d(TAG, "compareDates: happening2")
                    transferStatus(documentId, beauticianImageId, userImageId)
                    return true
                } else {
                    if (month1.toInt() == month2.toInt()) {
                        if (day1.toInt() > day2.toInt()) {
                            //pass
                            Log.d(TAG, "compareDates: happening3 day1 ${day1} | day2 $day2")
                            transferStatus(documentId, beauticianImageId, userImageId)
                            return true
                        } else {
                            if (day1.toInt() == day2.toInt()) {
                                if (amOrPm1 == "PM" && amOrPm2 == "AM") {
                                    when {
                                        hour1.toInt() > 1 -> {
                                            //pass
                                            Log.d(TAG, "compareDates: happening4")
                                            transferStatus(
                                                documentId,
                                                beauticianImageId,
                                                userImageId
                                            )
                                            return true
                                        }

                                        min1.toInt() >= min2.toInt() -> {
                                            //pass
                                            Log.d(TAG, "compareDates: happening5")
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
                                        Log.d(TAG, "compareDates: happening6")
                                        transferStatus(documentId, beauticianImageId, userImageId)
                                        return true
                                    } else if (hour1.toInt() > hour2.toInt() && (min1.toInt() >= min2.toInt())) {
                                        //pass
                                        Log.d(TAG, "compareDates: happening7")
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

        Log.d(TAG, "transferStatus: userimageid $userImageId")
        Log.d(TAG, "transferStatus: documentId $documentId")
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