package com.example.beautysync_kotlin.both.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.beautysync_kotlin.databinding.OrderPostBinding
import com.example.beautysync_kotlin.both.models.Orders
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import okhttp3.OkHttpClient

class OrdersAdapter(private val context: Context, private var orders: MutableList<Orders>, private var beauticianOrUser: String, private var status: String) : RecyclerView.Adapter<OrdersAdapter.ViewHolder>()  {


    private val httpClient = OkHttpClient()
    private val db = Firebase.firestore

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(OrderPostBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    @SuppressLint("SetTextI18n")
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



    }



    override fun getItemCount() = orders.size

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
        private var notes = itemView.notes
        var takeHome = itemView.takeHome
        val messagesForScheduling = itemView.messageForScheduling
        val messageForSchedulingNotification = itemView.messageForSchedulingNotificationImage
        val cancelButton = itemView.cancelButton
        val messageButton = itemView.messagesButton
        val messageNotification = itemView.messagesNotificationImage

        @SuppressLint("SetTextI18n")
        fun bind(item: Orders) {

            when (item.itemType) {
                "hairCareItems" -> {
                    itemType.text = "Hair Care Item"
                }
                "skinCareItems" -> {
                    itemType.text = "Skin Care Item"
                }
                "nailCareItems" -> {
                    itemType.text = "Nail Care Item"
                }
            }

            serviceDate.text = "Service Date: ${item.eventDay} ${item.eventTime}"
            location.text = "Location: ${item.streetAddress} ${item.beauticianCity}, ${item.beauticianState} ${item.zipCode}"
            if (item.notesToBeautician == "") {
                notes.text = "No Note"
            } else {
                notes.text = "Notes: ${item.notesToBeautician}"
            }


        }


    }
}