package com.example.beautysync_kotlin.beautician.fragments

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
import androidx.core.view.isVisible
import com.example.beautysync_kotlin.R
import com.example.beautysync_kotlin.both.models.Orders
import com.google.firebase.auth.FirebaseAuth
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
 * Use the [Dashboard.newInstance] factory method to
 * create an instance of this fragment.
 */
private const val TAG = "Dashboard"
class Dashboard : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    private val httpClient = OkHttpClient()
    private val mHandler: Handler = Handler(Looper.getMainLooper())

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
        loadOrders()
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Dashboard.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Dashboard().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
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

        db.collection("Beautician").document(auth.currentUser!!.uid).collection("BankingInfo").get().addOnSuccessListener { documents ->
            if (documents != null) {
                for (doc in documents.documents) {
                    val data = doc.data

                    val stripeAccountId = data?.get("stripeAccountId") as String


                    val body = FormBody.Builder()
                        .add("amount", "${itemPrice.toDouble() * 0.95 * 100}")
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
                                            val data1: Map<String, Any> = hashMapOf("transferId" to transferId, "orderId" to id, "date" to sdf.format(
                                                Date()
                                            ), "userImageId" to userImageId, "beauticianImageId" to beauticianImageId, "eventDate" to eventDate)
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