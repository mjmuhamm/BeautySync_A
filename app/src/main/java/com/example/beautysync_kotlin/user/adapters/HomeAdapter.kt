package com.example.beautysync_kotlin.user.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.beautysync_kotlin.databinding.HomePostBinding
import com.example.beautysync_kotlin.user.models.ServiceItems
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import okhttp3.OkHttpClient

class HomeAdapter(private val context: Context, private var items: MutableList<ServiceItems>) : RecyclerView.Adapter<HomeAdapter.ViewHolder>()  {


    private val httpClient = OkHttpClient()
    private val db = Firebase.firestore

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(HomePostBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]






        }



    override fun getItemCount() = items.size

    private fun sendMessage(title: String, notification: String, topic: String) {


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

    fun submitList(items: MutableList<ServiceItems>) {
        this.items = items
    }

    class ViewHolder(itemView: HomePostBinding) : RecyclerView.ViewHolder(itemView.root) {

        val context = itemView.root.context!!
        var itemTitle = itemView.itemTitle
        var userName = itemView.userName
        var userImage = itemView.userImage
        val itemImage = itemView.itemImage
        var itemLikes = itemView.likeText
        var likeButton = itemView.likeButton
        var likeImage = itemView.likeImage
        var itemOrders = itemView.orderText
        var rating = itemView.ratingText
        var itemPrice = itemView.itemPrice
        var orderButton = itemView.orderButton




    }
}