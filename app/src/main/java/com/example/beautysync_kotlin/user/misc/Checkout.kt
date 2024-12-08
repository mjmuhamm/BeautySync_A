package com.example.beautysync_kotlin.user.misc

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.beautysync_kotlin.R
import com.example.beautysync_kotlin.databinding.ActivityCheckoutBinding
import com.example.beautysync_kotlin.user.MainActivity
import com.example.beautysync_kotlin.user.adapters.CheckoutAdapter
import com.example.beautysync_kotlin.user.models.Checkout
import com.example.beautysync_kotlin.user.models.ServiceItems
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import com.stripe.android.paymentsheet.PaymentSheetResult
import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID

private const val TAG = "Checkout"
class Checkout : AppCompatActivity(), ClickListener {
    private lateinit var binding : ActivityCheckoutBinding

    private lateinit var checkoutAdapter: CheckoutAdapter

    private val backEndUrl = "http://beautysync-stripeserver.onrender.com/create-payment-intent"
    private val httpClient = OkHttpClient()
    private val mHandler: Handler = Handler(Looper.getMainLooper())

    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()
    private val storage = Firebase.storage

    private var items : MutableList<Checkout> = arrayListOf()

    private var foodTotal = 0.0

    private lateinit var paymentSheet: PaymentSheet
    private lateinit var customerConfig: PaymentSheet.CustomerConfiguration
    private lateinit var paymentIntentClientSecret: String
    private lateinit var publishableKey: String

    private var paymentId = ""

    private var listener = ""
    private var userName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        paymentSheet = PaymentSheet(this, ::onPaymentSheetResult)

        binding.checkoutRecyclerView.layoutManager = LinearLayoutManager(this)
        checkoutAdapter = CheckoutAdapter(this, items, this)
        binding.checkoutRecyclerView.adapter = checkoutAdapter

        loadCart()
        loadUsername()

        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.payButton.setOnClickListener {
            presentPaymentSheet()
        }
    }
    @SuppressLint("SetTextI18n")
    @Suppress("UNCHECKED_CAST")
    private fun loadCart() {
        db.collection("User").document(auth.currentUser!!.uid).collection("Cart").get().addOnSuccessListener { documents ->
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
                    val liked = data["liked"] as ArrayList<String>
                    val itemOrders = data["itemOrders"] as Number
                    val itemRating = data["itemRating"] as ArrayList<Number>
                    val hashtags = data["hashtags"] as ArrayList<String>
                    val eventDay = data["eventDay"] as String
                    val eventTime = data["eventTime"] as String
                    val streetAddress = data["streetAddress"] as String
                    val zipCode = data["zipCode"] as String
                    val noteToBeautician = data["notesToBeautician"] as String
                    val itemId = data["itemId"] as String

                    val x = Checkout(itemType, itemTitle, itemDescription, itemPrice, imageCount.toInt(), beauticianUsername, beauticianPassion, beauticianCity, beauticianState, beauticianImageId, liked, itemOrders.toInt(), itemRating, hashtags, doc.id, eventDay, eventTime, streetAddress, zipCode, noteToBeautician, itemId)

                    if (items.size == 0) {
                        foodTotal += itemPrice.toDouble()
                        val cost = "%.2f".format(foodTotal)
                        val taxes = foodTotal * 0.125
                        val tax = "%.2f".format(taxes)
                        val total = "%.2f".format(foodTotal + taxes)
                        binding.foodTotalText.text = "$$cost"
                        binding.taxesAndFeesText.text = "$$tax"
                        binding.totalPrice.text = "$$total"
                        fetchPaymentIntent(total.toDouble())
                        items.add(x)
                        checkoutAdapter.submitList(items)
                        checkoutAdapter.notifyItemInserted(0)
                    } else {
                        val index = items.indexOfFirst { it.documentId == doc.id }
                        if (index == -1) {
                            foodTotal += itemPrice.toDouble()
                            val cost = "%.2f".format(foodTotal)
                            val taxes = "%.2f".format(foodTotal * 0.125)
                            val total = "%.2f".format(foodTotal + taxes.toDouble())
                            val tax = "%.2f".format(foodTotal * 0.125)
                            binding.foodTotalText.text = "$$cost"
                            binding.taxesAndFeesText.text = "$${tax}"
                            binding.totalPrice.text = "$$total"
                            fetchPaymentIntent(total.toDouble())
                            items.add(x)
                            checkoutAdapter.submitList(items)
                            checkoutAdapter.notifyItemInserted(items.size - 1)
                        }
                    }

                }
            }
        }
    }

    private fun fetchPaymentIntent(costOfEvent: Double) {

        val cost = "%.0f".format(costOfEvent * 100)

//        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = FormBody.Builder()
            .add("amount", cost)
            .build()

        val request = Request.Builder()
            .url(backEndUrl)
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .post(body)
            .build()

        httpClient.newCall(request)
            .enqueue(object: Callback {
                override fun onFailure(call: Call, e: IOException) {
                    displayAlert("Error: $e")
                }

                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        displayAlert(
                            "Error: $response"
                        )
                    } else {
                        val responseData = response.body!!.string()
                        val responseJson =
                            JSONObject(responseData)

                        mHandler.post {

                            binding.payButton.isEnabled = true
                            binding.payButton.setTextColor(ContextCompat.getColor(this@Checkout, R.color.main))
                            publishableKey = responseJson.getString("publishableKey")
                            paymentIntentClientSecret = responseJson.getString("paymentIntent")
                            paymentId = responseJson.getString("paymentId")
                            customerConfig = PaymentSheet.CustomerConfiguration(
                                responseJson.getString("customer"),
                                responseJson.getString("ephemeralKey")
                            )

                            binding.progressBar.isVisible = false

                            // Set up PaymentConfiguration with your Stripe publishable key
                            PaymentConfiguration.init(applicationContext, publishableKey)
                        }
                    }
                }
            })
    }

    private fun onPaymentSheetResult(paymentSheetResult: PaymentSheetResult) {
        when(paymentSheetResult) {
            is PaymentSheetResult.Canceled -> {
                print("Canceled")
            }
            is PaymentSheetResult.Failed -> {
                Toast.makeText(this, "There was an error processing your payment. Please check your information and try again.", Toast.LENGTH_LONG).show()
            }
            is PaymentSheetResult.Completed -> {
                saveInfo()
            }
        }
    }

    private fun presentPaymentSheet() {
        Log.d(TAG, "presentPaymentSheet: this is happening")
        paymentSheet.presentWithPaymentIntent(
            paymentIntentClientSecret,
            PaymentSheet.Configuration(
                merchantDisplayName = "BeautySync, Inc",
                customer = customerConfig,
                allowsDelayedPaymentMethods = true
            )
        )
    }

    private fun displayAlert(
        message: String
    ) {
        runOnUiThread {
            val builder = AlertDialog.Builder(this)
                .setTitle("Failed to load page.")
                .setMessage(message)

            builder.setPositiveButton("Ok", null)
            builder
                .create()
                .show()
        }
    }

    @SuppressLint("SetTextI18n")
    override fun updateCost(costOfEvent: Double, documentId: String) {
        this.listener = "yes"
        foodTotal -= costOfEvent
        val num = "%.2f".format(foodTotal)
        val taxes = "%.2f".format(foodTotal * 0.125)
        val total = "%.2f".format(foodTotal + (foodTotal * 0.125))
        binding.foodTotalText.text = "$$num"
        binding.taxesAndFeesText.text = "$$taxes"
        binding.totalPrice.text = "$$total"

        db.collection("User").document(auth.currentUser!!.uid).collection("Cart").document(documentId).delete()


    }

    private fun loadUsername() {
        db.collection("User").document(auth.currentUser!!.uid).collection("PersonalInfo").get().addOnSuccessListener { documents ->
            if (documents != null) {
                for (doc in documents.documents) {
                    val data = doc.data

                    val userName = data?.get("userName") as String

                    this.userName = userName
                }
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun saveInfo() {
        val sdf = SimpleDateFormat("MM`-dd-yyyy")
        val currentDate = sdf.format(Date())

        val intent = Intent(this, MainActivity::class.java)
        for (i in 0 until items.size) {
            val documentId = UUID.randomUUID().toString()
            val data: Map<String, Any> = hashMapOf(
                "itemType" to items[i].itemType,
                "itemTitle" to items[i].itemTitle,
                "itemDescription" to items[i].itemDescription,
                "itemPrice" to items[i].itemPrice,
                "imageCount" to items[i].imageCount,
                "beauticianUsername" to items[i].beauticianUsername,
                "beauticianPassion" to items[i].beauticianPassion,
                "beauticianCity" to items[i].beauticianCity,
                "beauticianState" to items[i].beauticianState,
                "beauticianImageId" to items[i].beauticianImageId,
                "liked" to items[i].liked,
                "itemOrders" to items[i].itemOrders,
                "itemRating" to items[i].itemRating,
                "hashtags" to items[i].hashtags,
                "eventDay" to items[i].eventDay,
                "eventTime" to items[i].eventTime,
                "streetAddress" to items[i].streetAddress,
                "zipCode" to items[i].zipCode,
                "notesToBeautician" to items[i].noteToBeautician,
                "userImageId" to auth.currentUser!!.uid,
                "status" to "pending",
                "itemId" to items[i].itemId,
                "userName" to this.userName,
                "date" to currentDate,
                "notifications" to "",
                "cancelled" to "",
            )

            val data1: Map<String, Any> = hashMapOf(
                "itemType" to items[i].itemType,
                "itemTitle" to items[i].itemTitle,
                "itemDescription" to items[i].itemDescription,
                "itemPrice" to items[i].itemPrice,
                "imageCount" to items[i].imageCount,
                "beauticianUsername" to items[i].beauticianUsername,
                "beauticianPassion" to items[i].beauticianPassion,
                "beauticianCity" to items[i].beauticianCity,
                "beauticianState" to items[i].beauticianState,
                "beauticianImageId" to items[i].beauticianImageId,
                "liked" to items[i].liked,
                "itemOrders" to items[i].itemOrders,
                "itemRating" to items[i].itemRating,
                "hashtags" to items[i].hashtags,
                "eventDay" to items[i].eventDay,
                "eventTime" to items[i].eventTime,
                "streetAddress" to items[i].streetAddress,
                "zipCode" to items[i].zipCode,
                "notesToBeautician" to items[i].noteToBeautician,
                "userImageId" to auth.currentUser!!.uid,
                "status" to "pending",
                "itemId" to items[i].itemId,
                "userName" to this.userName,
                "date" to currentDate,
                "notifications" to "",
                "paymentId" to this.paymentId,
                "cancelled" to ""
            )

            db.collection("User").document(auth.currentUser!!.uid).collection("Orders").document(documentId).set(data)
            db.collection("Beautician").document(items[i].beauticianImageId).collection("Orders").document(documentId).set(data)
            db.collection("Orders").document(documentId).set(data1)

            db.collection(items[i].itemType).document(items[i].itemId).get().addOnSuccessListener { document ->
                if (document != null) {
                    val data2 = document.data

                    val itemOrders = data2?.get("itemOrders") as Number
                    val data3 : Map<String, Any> = hashMapOf("itemOrders" to itemOrders.toInt() + 1)
                    db.collection(items[i].itemType).document(items[i].itemId).update(data3)
                }
            }

            db.collection("User").document(auth.currentUser!!.uid).collection("Beauticians").document(items[i].beauticianImageId).get().addOnSuccessListener { document ->
                if (document != null) {
                    val data2 = document.data

                    if (data2 != null) {
                        val itemCount = data2["itemCount"] as Number
                        val data3: Map<String, Any> = hashMapOf("itemCount" to itemCount.toInt() + 1)
                        db.collection("User").document(auth.currentUser!!.uid).collection("Beauticians").document(items[i].beauticianImageId).update(data3)
                    } else {
                        //no
                        val data3 : Map<String, Any> = hashMapOf(
                            "itemCount" to 1,
                            "beauticianImageId" to items[i].beauticianImageId,
                            "beauticianUsername" to items[i].beauticianUsername,
                            "beauticianPassion" to items[i].beauticianPassion,
                            "beauticianCity" to items[i].beauticianCity,
                            "beauticianState" to items[i].beauticianState
                        )
                        db.collection("User").document(auth.currentUser!!.uid).collection("Beauticians").document(items[i].beauticianImageId).set(data3)
                    }
                }
            }
            db.collection("User").document(auth.currentUser!!.uid).collection("Cart").document(items[i].documentId).delete()
        }
        Toast.makeText(this, "Order Processed! Please view your Orders' tab for updates.", Toast.LENGTH_LONG).show()
        startActivity(intent)
        finish()
    }

}

interface ClickListener {
    fun updateCost(costOfEvent: Double, documentId: String)
}