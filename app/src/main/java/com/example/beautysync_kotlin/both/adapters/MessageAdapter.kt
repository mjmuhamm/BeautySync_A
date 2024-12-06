package com.example.beautysync_kotlin.both.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.beautysync_kotlin.R
import com.example.beautysync_kotlin.both.models.Messages
import com.example.beautysync_kotlin.both.models.Orders
import com.example.beautysync_kotlin.databinding.MessagePostBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import okhttp3.OkHttpClient
import java.net.URI

private const val TAG = "MessageAdapter"
class MessageAdapter(private val context: Context, private var messages: MutableList<Messages>, private var beauticianOrUser: String, private var userImage : Uri, private var beauticianImage : Uri) : RecyclerView.Adapter<MessageAdapter.ViewHolder>()  {


    private val httpClient = OkHttpClient()
    private val db = Firebase.firestore

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(MessagePostBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(messages[position])
        val item = messages[position]

        val month = item.date.substring(0,2)
        val day = item.date.substring(3,5)
        val year = item.date.substring(6,10)
        var hour = item.date.substring(11,13)
        val min = item.date.substring(14,16)
        var amOrPm = ""

        var date = ""


        if (hour.toInt() > 12) {
            hour = "0${hour.toInt() - 12}"
            amOrPm = "PM"
        } else {
            amOrPm = "AM"
        }

        date = "$month-$day-$year $hour:$min $amOrPm"


        if (beauticianOrUser == item.beauticianOrUser) {
            holder.homeLayout.isVisible = true
            holder.awayLayout.isVisible = false
            holder.systemMessage.isVisible = false

            if (beauticianOrUser == "User") {
                Glide.with(context).load(userImage).placeholder(R.drawable.default_profile)
                    .into(holder.homeUserImage)
            } else {
                Glide.with(context).load(beauticianImage).placeholder(R.drawable.default_profile)
                    .into(holder.homeUserImage)
            }

            holder.homeMessage.text = item.message
            holder.homeDate.text = date

        } else if (item.beauticianOrUser != "") {
            holder.homeLayout.isVisible = false
            holder.awayLayout.isVisible = true
            holder.systemMessage.isVisible = false

            if (item.beauticianOrUser == "User") {
                Glide.with(context).load(userImage).placeholder(R.drawable.default_profile)
                    .into(holder.awayUserImage)
            } else {
                Glide.with(context).load(beauticianImage).placeholder(R.drawable.default_profile)
                    .into(holder.awayUserImage)
            }

            holder.awayMessage.text = item.message
            holder.awayDate.text = date

        } else {
            holder.homeLayout.isVisible = false
            holder.awayLayout.isVisible = false
            holder.systemMessage.isVisible = true

            holder.systemMessage.text = item.message

        }






    }



    override fun getItemCount() = messages.size

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

    fun submitList(items: MutableList<Messages>) {
        this.messages = items
    }

    class ViewHolder(itemView: MessagePostBinding) : RecyclerView.ViewHolder(itemView.root) {

        val context = itemView.root.context!!
        val homeLayout = itemView.homeLayout
        val homeUserImage = itemView.homeUserImage
        val homeMessage = itemView.homeMessage
        val homeDate = itemView.homeDate
        val awayLayout = itemView.awayLayout
        val awayUserImage = itemView.awayUserImage
        val awayMessage = itemView.awayMessage
        val awayDate = itemView.awayDate
        val systemMessage = itemView.systemMessage

        @SuppressLint("SetTextI18n")
        fun bind(item: Messages) {





        }


    }
}