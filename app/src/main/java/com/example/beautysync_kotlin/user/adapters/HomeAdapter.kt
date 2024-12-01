package com.example.beautysync_kotlin.user.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.beautysync_kotlin.R
import com.example.beautysync_kotlin.both.misc.ItemDetail
import com.example.beautysync_kotlin.databinding.ActivityItemDetailBinding
import com.example.beautysync_kotlin.databinding.HomePostBinding
import com.example.beautysync_kotlin.user.misc.OrderDetails
import com.example.beautysync_kotlin.user.models.ServiceItems
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import okhttp3.OkHttpClient

class HomeAdapter(private val context: Context, private var items: MutableList<ServiceItems>) : RecyclerView.Adapter<HomeAdapter.ViewHolder>()  {


    private val httpClient = OkHttpClient()
    private val db = Firebase.firestore

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(HomePostBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
        val item = items[position]

        holder.itemImage.setOnClickListener {
            val intent = Intent(context, ItemDetail::class.java)
            intent.putExtra("item", item)
            context.startActivity(intent)
        }

        holder.orderButton.setOnClickListener {
            val intent = Intent(context, OrderDetails::class.java)
            intent.putExtra("item", item)
            context.startActivity(intent)
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

    class ViewHolder(itemView: HomePostBinding) : RecyclerView.ViewHolder(itemView.root) {

        val context = itemView.root.context!!
        var itemTitle = itemView.itemTitle
        var itemDescription = itemView.itemDescription
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

        @SuppressLint("SetTextI18n")
        fun bind(item: ServiceItems) {
            val imageRef = Firebase.storage.reference
            imageRef.child("${item.itemType}/${item.beauticianImageId}/${item.documentId}/${item.documentId}0.png").downloadUrl.addOnSuccessListener { itemUri ->
                Glide.with(context).load(itemUri).placeholder(R.drawable.default_profile).into(itemImage)
            }

            val storageRef = Firebase.storage.reference
            storageRef.child("beauticians/${item.beauticianImageId}/profileImage/${item.beauticianImageId}.png").downloadUrl.addOnSuccessListener { itemUri ->
                Glide.with(context).load(itemUri).placeholder(R.drawable.default_profile).into(userImage)
            }

            itemTitle.text = item.itemTitle
            userName.text = item.beauticianUsername
            itemDescription.text = item.itemDescription
            itemLikes.text = "${item.liked.size}"
            itemPrice.text = "$${item.itemPrice}"
            itemOrders.text = "${item.itemOrders}"
            var rating1 = 0.0
            for (i in 0 until item.itemRating.size) {
                rating1 += item.itemRating[i].toString().toInt()

                if (i == item.itemRating.size - 1) {
                    rating1 /= item.itemRating.size
                }
            }
            rating.text = "$rating1"

            if (item.liked.contains(FirebaseAuth.getInstance().currentUser!!.uid)) {
                likeImage.setImageResource(R.drawable.heart_filled)
            } else {
                likeImage.setImageResource(R.drawable.heart_unfilled)
            }

        }


    }
}