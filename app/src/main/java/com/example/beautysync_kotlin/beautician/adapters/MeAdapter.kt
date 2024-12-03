package com.example.beautysync_kotlin.beautician.adapters

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.beautysync_kotlin.R
import com.example.beautysync_kotlin.beautician.misc.ServiceItemAdd
import com.example.beautysync_kotlin.both.misc.ItemDetail
import com.example.beautysync_kotlin.databinding.HomePostBinding
import com.example.beautysync_kotlin.databinding.ItemPostBinding
import com.example.beautysync_kotlin.user.adapters.HomeAdapter
import com.example.beautysync_kotlin.user.misc.OrderDetails
import com.example.beautysync_kotlin.user.models.ServiceItems
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import okhttp3.OkHttpClient
import kotlin.math.log

class MeAdapter(private val context: Context, private var items: MutableList<ServiceItems>, private var itemType: String, private var guest: String) : RecyclerView.Adapter<MeAdapter.ViewHolder>()  {


    private val httpClient = OkHttpClient()
    private val db = Firebase.firestore

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemPostBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
        val item = items[position]

        holder.editItem.isVisible = true
        holder.itemTitle.text = item.itemTitle
        holder.itemDescription.text = item.itemDescription
        holder.itemLikes.text = "${item.liked.size}"
        holder.itemOrders.text = "${item.itemOrders}"
        var rating = 0.0
        for (i in 0 until item.itemRating.size) {
            rating += item.itemRating[i].toString().toInt()

            if (i == item.itemRating.size - 1) {
                rating /= item.itemRating.size
            }
        }
        holder.rating.text = "$rating"
        holder.itemPrice.text = "$${item.itemPrice}"

        if (guest == "yes") {
            holder.editItem.visibility = View.GONE
        } else {
            holder.editItem.visibility = View.VISIBLE
        }

        holder.editItem.setOnClickListener {
            val intent = Intent(context, ServiceItemAdd::class.java)
            intent.putExtra("item_type", itemType)
            intent.putExtra("new_or_edit", "edit")
            intent.putExtra("item", item)
            context.startActivity(intent)
        }

        holder.itemImage.setOnClickListener {
            val intent = Intent(context, ItemDetail::class.java)
            intent.putExtra("item", item)
            context.startActivity(intent)
        }

        holder.likeButton.setOnClickListener {

        }

        holder.orderButton.setOnClickListener {
            if (guest == "yes") {
                val intent = Intent(context, OrderDetails::class.java)
                intent.putExtra("item", item)
                context.startActivity(intent)
            }
        }
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

    class ViewHolder(itemView: ItemPostBinding) : RecyclerView.ViewHolder(itemView.root) {

        val context = itemView.root.context!!
        val editItem = itemView.editItemButton
        var itemTitle = itemView.itemTitle
        val itemDescription = itemView.itemDescription
        val itemImage = itemView.itemImage
        var itemLikes = itemView.likeText
        var likeButton = itemView.likeButton
        var likeImage = itemView.likeImage
        var itemOrders = itemView.orderText
        var rating = itemView.ratingText
        var itemPrice = itemView.itemPrice
        var orderButton = itemView.orderButton

        @SuppressLint("SetTextI18n")
        fun bind(item: ServiceItems) {
            val imageRef = Firebase.storage.reference
            imageRef.child("${item.itemType}/${item.beauticianImageId}/${item.documentId}/${item.documentId}0.png").downloadUrl.addOnSuccessListener { itemUri ->
                Glide.with(context).load(itemUri).placeholder(R.drawable.default_profile).into(itemImage)
            }
        }
    }
}