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
import com.example.beautysync_kotlin.databinding.CheckoutPostBinding
import com.example.beautysync_kotlin.databinding.HomePostBinding
import com.example.beautysync_kotlin.user.misc.ClickListener
import com.example.beautysync_kotlin.user.misc.OrderDetails
import com.example.beautysync_kotlin.user.models.Checkout
import com.example.beautysync_kotlin.user.models.ServiceItems
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import okhttp3.OkHttpClient

class CheckoutAdapter(private val context: Context, private var items: MutableList<Checkout>, private var listener: ClickListener) : RecyclerView.Adapter<CheckoutAdapter.ViewHolder>()  {


    private val httpClient = OkHttpClient()
    private val db = Firebase.firestore

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(CheckoutPostBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
        val item = items[position]

        holder.cancelButton.setOnClickListener {
            listener.updateCost(item.itemPrice.toDouble(), item.documentId)
            db.collection("User").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("Cart").document(item.documentId).delete()
            items.removeAt(position)
            notifyItemRemoved(position)
        }

    }



    override fun getItemCount() = items.size

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

    fun submitList(items: MutableList<Checkout>) {
        this.items = items
    }

    class ViewHolder(itemView: CheckoutPostBinding) : RecyclerView.ViewHolder(itemView.root) {

        val context = itemView.root.context!!
        var itemTitle = itemView.itemTitle
        var userImage = itemView.userImage
        var location = itemView.location
        var date = itemView.date
        val notes = itemView.notes
        val eventCost = itemView.eventCost
        val cancelButton = itemView.cancelButton

        @SuppressLint("SetTextI18n")
        fun bind(item: Checkout) {
            val imageRef = Firebase.storage.reference
            imageRef.child("beauticians/${item.beauticianImageId}/profileImage/${item.beauticianImageId}.png").downloadUrl.addOnSuccessListener { itemUri ->
                Glide.with(context).load(itemUri).placeholder(R.drawable.default_profile).into(userImage)
            }

            itemTitle.text = item.itemTitle
            location.text = "Location: ${item.streetAddress} ${item.beauticianCity}, ${item.beauticianState} ${item.zipCode}"
            date.text = "Date: ${item.eventDay} ${item.eventTime}"
            eventCost.text = "$${item.itemPrice}"

            if (item.noteToBeautician == "") { notes.text = "No Note." } else { notes.text = "Note: ${item.noteToBeautician}" }



        }


    }
}