package com.example.beautysync_kotlin.both.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.beautysync_kotlin.beautician.MainActivity
import com.example.beautysync_kotlin.both.misc.Messages
import com.example.beautysync_kotlin.databinding.OrderPostBinding
import com.example.beautysync_kotlin.both.models.Orders
import com.example.beautysync_kotlin.user.misc.Reviews
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

private const val TAG = "OrdersAdapter"
class OrdersAdapter(private val context: Context, private var orders: MutableList<Orders>, private var beauticianOrUser: String, private var status: String) : RecyclerView.Adapter<OrdersAdapter.ViewHolder>()  {


    private val httpClient = OkHttpClient()
    private val mHandler: Handler = Handler(Looper.getMainLooper())

    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

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

    @SuppressLint("SimpleDateFormat")
    private var sdfYearMonth = SimpleDateFormat("yyyy, MM")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(OrderPostBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(orders[position])
        val item = orders[position]



        if (beauticianOrUser == "User") {
            holder.user.text = "Beautician: ${item.beauticianUsername}"
            holder.takeHome.visibility = View.GONE

            holder.messagesForScheduling.visibility = View.GONE
            holder.messageButton.text = "Messages For Scheduling"
        } else if (beauticianOrUser == "Beautician") {
            holder.user.text = "User: @${item.userName}"
            holder.takeHome.visibility = View.VISIBLE
            val cost = "%.2f".format(item.itemPrice.toDouble() * 0.95)
            holder.takeHome.text = "Take Home: $$cost"

            if (status == "pending") {
                holder.messagesForScheduling.visibility = View.VISIBLE
                holder.messageButton.text = "Accept"
            }  else if (status == "scheduled") {
                holder.messagesForScheduling.visibility = View.GONE
                holder.messageButton.text = "Messages For Scheduling"
            }
        }

        if (status == "complete") {
            if (beauticianOrUser == "User") {
                holder.cancelButton.text = "Review"
            } else {
                holder.cancelButton.isVisible = false
                holder.messageButton.isVisible = false
                holder.messagesForScheduling.isVisible = false
            }
        }

        if (item.cancelled == "yes") {
            holder.notes.isVisible = false
            holder.messagesForScheduling.visibility = View.GONE
            holder.cancelButton.visibility = View.GONE
            if (beauticianOrUser == "User") {
                holder.cancelledText.text = "This event has been cancelled by the beautician. A refund of the event price has been issued to you."
            } else {
                holder.cancelledText.text = "This event has been cancelled by the user."
            }
            holder.messageButton.text = "Ok"
        } else {
            holder.notes.isVisible = true
            holder.messagesForScheduling.visibility = View.VISIBLE
            holder.cancelButton.visibility = View.VISIBLE
            holder.cancelledText.isVisible = false
            if (beauticianOrUser == "User") {
                holder.user.text = "Beautician: ${item.beauticianUsername}"
                holder.takeHome.visibility = View.GONE

                holder.messagesForScheduling.visibility = View.GONE
                holder.messageButton.text = "Messages For Scheduling"
            } else if (beauticianOrUser == "Beautician") {
                holder.user.text = "User: @${item.userName}"
                holder.takeHome.visibility = View.VISIBLE
                val cost = "%.2f".format(item.itemPrice.toDouble() * 0.95)
                holder.takeHome.text = "Take Home: $$cost"

                if (status == "pending") {
                    holder.messagesForScheduling.visibility = View.VISIBLE
                    holder.messageButton.text = "Accept"
                }  else if (status == "scheduled") {
                    holder.messagesForScheduling.visibility = View.GONE
                    holder.messageButton.text = "Messages For Scheduling"
                }
            }
        }

        holder.messagesForScheduling.setOnClickListener {
            val intent = Intent(context, Messages::class.java)
            intent.putExtra("item", item)
            intent.putExtra("beautician_or_user", beauticianOrUser)
            context.startActivity(intent)
        }

        holder.messageButton.setOnClickListener {

            if (holder.messageButton.text == "Messages" || holder.messageButton.text == "Messages For Scheduling") {
                val intent = Intent(context, Messages::class.java)
                intent.putExtra("item", item)
                intent.putExtra("beautician_or_user", beauticianOrUser)
                context.startActivity(intent)
            } else if (holder.messageButton.text == "Ok") {
                val data1: Map<String, Any> = hashMapOf("cancelled" to "yes", "status" to "cancelled")
                db.collection("Orders").document(item.documentId).update(data1)
                if (beauticianOrUser == "User") {
                    val data : Map<String, Any> = hashMapOf("status" to "cancelled")
                    db.collection("User").document(auth.currentUser!!.uid).collection("Orders").document(item.documentId).update(data)
                } else {
                    val data : Map<String, Any> = hashMapOf("status" to "cancelled")
                    db.collection("Beautician").document(auth.currentUser!!.uid).collection("Orders").document(item.documentId).update(data)
                }

                orders.removeAt(position)
                notifyDataSetChanged()
            } else {
                //Accept
                val week = if (Calendar.getInstance()[Calendar.WEEK_OF_MONTH] > 4) {
                    4 } else { Calendar.getInstance()[Calendar.WEEK_OF_MONTH] }

                val yearMonth = sdfYearMonth.format(Date())
                val weekOfMonth = "Week $week"

                db.collection("Orders").document(item.documentId).get().addOnSuccessListener { document ->
                    if (document != null) {
                        val data = document.data

                        val status = data?.get("status") as String

                        if (status != "cancelled") {
                            val data2 : Map<String, Any> = hashMapOf("status" to "scheduled")
                            db.collection("User").document(item.userImageId).collection("Orders").document(item.documentId).update(data2)
                            db.collection("Beautician").document(item.beauticianImageId).collection("Orders").document(item.documentId).update(data2)
                            db.collection("Orders").document(item.documentId).update(data2)
                     

                            val data1 : Map<String, Any> = hashMapOf("totalPay" to item.itemPrice.toDouble() * 0.95)
                            db.collection("Beautician").document(auth.currentUser!!.uid).collection("Dashboard").document(item.itemType).collection(item.itemId).document("Month").collection(yearMonth).document("Week").collection(weekOfMonth).document().set(data1)
                            db.collection("Beautician").document(auth.currentUser!!.uid).collection("Dashboard").document(item.itemType).collection(item.itemId).document("Month").collection(yearMonth).document("Total").get().addOnSuccessListener { document1 ->
                    if (document1 != null) {
                        val data3 = document1.data

                        if (data3 != null) {
                            val total = data3["totalPay"] as Number
                            val data5 : Map<String, Any> = hashMapOf("totalPay" to (total.toDouble() + (item.itemPrice.toDouble() * 0.95)))
                            db.collection("Beautician").document(auth.currentUser!!.uid).collection("Dashboard").document(item.itemType).collection(item.itemId).document("Month").collection(yearMonth).document("Total").update(data5)
                        } else {
                            val data5 : Map<String, Any> = hashMapOf("totalPay" to (item.itemPrice.toDouble() * 0.95))
                            db.collection("Beautician").document(auth.currentUser!!.uid).collection("Dashboard").document(item.itemType).collection(item.itemId).document("Month").collection(yearMonth).document("Total").set(data5)
                        }
                    }
                }
                            db.collection("Beautician").document(auth.currentUser!!.uid).collection("Dashboard").document(item.itemType).get().addOnSuccessListener { document1 ->
                    if (document1 != null) {
                        val data3 = document1.data

                        if (data3 != null) {
                            val total = data3["totalPay"] as Number
                            val data5 : Map<String, Any> = hashMapOf("totalPay" to (total.toDouble() + (item.itemPrice.toDouble() * 0.95)))
                            db.collection("Beautician").document(auth.currentUser!!.uid).collection("Dashboard").document(item.itemType).update(data5)
                        } else {
                            val data5 : Map<String, Any> = hashMapOf("totalPay" to (item.itemPrice.toDouble() * 0.95))
                            db.collection("Beautician").document(auth.currentUser!!.uid).collection("Dashboard").document(item.itemType).set(data5)
                        }
                    }
                }
                            db.collection("Beautician").document(auth.currentUser!!.uid).collection("Dashboard").document(item.itemType).collection(item.itemId).document("Total").get().addOnSuccessListener { document1 ->
                    if (document1 != null) {
                        val data3 = document1.data

                        if (data3 != null) {
                            val total = data3["totalPay"] as Number
                            val data5 : Map<String, Any> = hashMapOf("totalPay" to (total.toDouble() + (item.itemPrice.toDouble() * 0.95)))
                            db.collection("Beautician").document(auth.currentUser!!.uid).collection("Dashboard").document(item.itemType).collection(item.itemId).document("Total").update(data5)
                        } else {
                            val data5 : Map<String, Any> = hashMapOf("totalPay" to (item.itemPrice.toDouble() * 0.95))
                            db.collection("Beautician").document(auth.currentUser!!.uid).collection("Dashboard").document(item.itemType).collection(item.itemId).document("Total").set(data5)
                        }
                    }
                }

                        } else {
                            Toast.makeText(context, "This item has been cancelled by the user.", Toast.LENGTH_LONG).show()
                        }
                    }
                }

                orders.removeAt(position)
                notifyItemRemoved(position)
            }
        }

        holder.cancelButton.setOnClickListener {
            if (status == "complete") {
                val intent = Intent(context, Reviews::class.java)
                intent.putExtra("item", item)
                context.startActivity(intent)
            } else if (status == "scheduled") {
                if (cancelLogic("${item.eventDay} ${item.eventTime}", item.status)) {
                    //showOptions
                    AlertDialog.Builder(context)
                        .setTitle("Cancel Appointment")
                        .setMessage("Are you sure you want cancel this appointment?")
                        // if the dialog is cancelable
                        .setCancelable(false)
                        .setPositiveButton("Yes") { dialog, _ ->
                            db.collection("Orders").document(item.documentId).get().addOnSuccessListener { document ->
                                if (document != null) {
                                    val data = document.data

                                    if (data != null) {
                                        val beauticianImageId = data["beauticianImageId"] as String
                                        val userImageId = data["userImageId"] as String
                                        val paymentId = data["paymentId"] as String
                                        val itemPrice = data["itemPrice"] as String

                                        refund(paymentId, itemPrice, beauticianImageId, userImageId)

                                        val data1: Map<String, Any> = hashMapOf("cancelled" to "yes")
                                        val data2: Map<String, Any> = hashMapOf("status" to "cancelled", "cancelled" to "yes")

                                        db.collection("Orders").document(item.documentId).update(data2)
                                        db.collection("User").document(userImageId).collection("Orders").document(item.documentId).update(data1)
                                        db.collection("Beautician").document(beauticianImageId).collection("Orders").document(item.documentId).update(data1)
                                        if (beauticianOrUser == "User") {
                                            db.collection("User").document(userImageId).collection("Orders").document(item.documentId).update(data2)
                                            Toast.makeText(context, "Item cancelled. You have been refunded the price of the event.", Toast.LENGTH_LONG).show()
                                        } else {
                                            db.collection("Beautician").document(beauticianImageId).collection("Orders").document(item.documentId).update(data2)
                                            Toast.makeText(context, "This event has been cancelled.", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }
                            }

                            orders.removeAt(position)
                            notifyItemRemoved(position)

                            dialog.dismiss()

                        }
                        .setNegativeButton("No") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .create()
                        .show()
                } else {
                    Toast.makeText(context, "You cannot cancel event with less than two hours till service time.", Toast.LENGTH_LONG).show()
                }
            } else {
                //showOptions
                AlertDialog.Builder(context)
                    .setTitle("Cancel Appointment")
                    .setMessage("Are you sure you want cancel this appointment?")
                    // if the dialog is cancelable
                    .setCancelable(false)
                    .setPositiveButton("Yes") { dialog, _ ->
                        db.collection("Orders").document(item.documentId).get().addOnSuccessListener { document ->
                            if (document != null) {
                                val data = document.data

                                if (data != null) {
                                    val beauticianImageId = data["beauticianImageId"] as String
                                    val userImageId = data["userImageId"] as String
                                    val paymentId = data["paymentId"] as String
                                    val itemPrice = data["itemPrice"] as String

                                    refund(paymentId, itemPrice, beauticianImageId, userImageId)

                                    val data1: Map<String, Any> = hashMapOf("cancelled" to "yes")
                                    val data2: Map<String, Any> = hashMapOf("status" to "cancelled", "cancelled" to "yes")

                                    db.collection("Orders").document(item.documentId).update(data2)
                                    if (beauticianOrUser == "User") {
                                        db.collection("User").document(userImageId).collection("Orders").document(item.documentId).update(data2)
                                        db.collection("Beautician").document(beauticianImageId).collection("Orders").document(item.documentId).update(data1)
                                        Toast.makeText(context, "Item cancelled. You have been refunded the price of the event.", Toast.LENGTH_LONG).show()
                                    } else {
                                        db.collection("Beautician").document(beauticianImageId).collection("Orders").document(item.documentId).update(data2)
                                        db.collection("User").document(userImageId).collection("Orders").document(item.documentId).update(data1)
                                        Toast.makeText(context, "This event has been cancelled.", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        }

                        orders.removeAt(position)
                        notifyItemRemoved(position)

                        dialog.dismiss()

                    }
                    .setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create()
                    .show()
            }
        }



    }



    override fun getItemCount() = orders.size

    @SuppressLint("SimpleDateFormat")
    private fun cancelLogic(date: String, status: String) : Boolean {
        val month = sdfMonth.format(Date())
        val day = sdfDay.format(Date())
        val year = sdfYear.format(Date())
        var hour = sdfHour.format(Date())
        val min = sdfMin.format(Date())
        val amOrPm = sdfAmOrPm.format(Date())

        if (amOrPm == "PM") {
            hour = "${hour.toInt() + 12}"
        }


        val month1 = date.substring(0, 2)
        val day1 = date.substring(3,5)
        val year1 = date.substring(6,10)
        var hour1 = date.substring(11,13)
        val min1 = date.substring(14,16)
        val amOrPm1 = date.substring(17,19)

        if (amOrPm1 == "PM") {
            hour1 = "${hour1.toInt() + 12}"
        }

        if (year == year1) {
            if (month == month1) {
                if (day == day1) {
                    if (hour1.toInt() - hour.toInt() == 2) {
                        if (min.toInt() >= min1.toInt()) {
                            if (status != "pending") {
                                return false
                            } else {
                                //showOptions
                                return true
                            }
                        } else {
                            //showOptions
                            return true
                        }
                    } else if (hour1.toInt() - hour.toInt() < 2){
                        if (status != "pending") {
                            return false
                        } else {
                            //showOptions
                            return true
                        }
                    } else {
                        //showOptions
                        return true
                    }
                } else {
                    //showOptions
                    return true
                }
            } else {
                //showOptions
                return true
            }
        } else {
            //showOptions
            return true
        }


    }

    private fun showOptions(documentId: String) {

    }


    private fun refund(paymentId: String, amount: String, beauticianImageId: String, userImageId: String) {
        val amount1 = "%.0f".format((amount.toDouble() * 100))
        Log.d(TAG, "refund: amount ${amount1.toDouble() * 100}")
        val body = FormBody.Builder()
            .add("paymentId", paymentId)
            .add("amount", amount1)
            .build()

        val request = Request.Builder()
            .url("http://beautysync-stripeserver.onrender.com/refund")
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

                            val refundId = json.getString("refund_id")


                            mHandler.post {
                                val data : Map<String, Any> = hashMapOf("refundId" to refundId, "amount" to amount, "paymentId" to paymentId, "beauticianUserImageId" to beauticianImageId, "userImageId" to userImageId, "date" to sdf.format(Date()))
                                db.collection("Refunds").document().set(data)

                            }
                        }
                    }
                }
            })

    }

    private fun displayAlert(
        message: String
    ) {

        val builder = AlertDialog.Builder(context)
            .setTitle("Failed to load page.")
            .setMessage(message)

        builder.setPositiveButton("Ok", null)
        builder
            .create()
            .show()

    }

    fun submitList(items: MutableList<Orders>) {
        this.orders = items
    }

    class ViewHolder(itemView: OrderPostBinding) : RecyclerView.ViewHolder(itemView.root) {

        val context = itemView.root.context!!
        var itemType = itemView.itemType
        var user = itemView.user
        private var serviceDate = itemView.serviceDate
        var location = itemView.location
        var notes = itemView.notes
        var takeHome = itemView.takeHome
        val messagesForScheduling = itemView.messageForScheduling
        val messageForSchedulingNotification = itemView.messageForSchedulingNotificationImage
        val cancelButton = itemView.cancelButton
        val messageButton = itemView.messagesButton
        val cancelledText = itemView.cancelledText
        val messageNotification = itemView.messagesNotificationImage

        @SuppressLint("SetTextI18n")
        fun bind(item: Orders) {

            when (item.itemType) {
                "hairCareItems" -> {
                    itemType.text = "Hair Care Item | ${item.itemTitle}"
                }

                "skinCareItems" -> {
                    itemType.text = "Skin Care Item | ${item.itemTitle}"
                }

                "nailCareItems" -> {
                    itemType.text = "Nail Care Item | ${item.itemTitle}"
                }
            }

            serviceDate.text = "Service Date: ${item.eventDay} ${item.eventTime}"
            location.text =
                "Location: ${item.streetAddress} ${item.beauticianCity}, ${item.beauticianState} ${item.zipCode}"
            if (item.notesToBeautician == "") {
                notes.text = "No Note"
            } else {
                notes.text = "Notes: ${item.notesToBeautician}"
            }
        }
        }
}