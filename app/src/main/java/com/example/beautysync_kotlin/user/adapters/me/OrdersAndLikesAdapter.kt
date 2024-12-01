package com.example.beautysync_kotlin.user.adapters.me

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.beautysync_kotlin.R
import com.example.beautysync_kotlin.both.misc.ItemDetail
import com.example.beautysync_kotlin.both.misc.ProfileAsUser
import com.example.beautysync_kotlin.databinding.UserItemPostBinding
import com.example.beautysync_kotlin.user.models.ServiceItems
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import okhttp3.OkHttpClient

class OrdersAndLikesAdapter(private val context: Context, private var items: MutableList<ServiceItems>, private var itemType : String) : RecyclerView.Adapter<OrdersAndLikesAdapter.ViewHolder>()  {

    private val httpClient = OkHttpClient()
    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(UserItemPostBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
        val item = items[position]

        if (itemType == "likes") {
            holder.likeImage.setImageResource(R.drawable.heart_filled)
            holder.likeImage.isSelected = true
        } else if (item.liked.contains(FirebaseAuth.getInstance().currentUser!!.uid)) {
            holder.likeImage.setImageResource(R.drawable.heart_filled)
            holder.likeImage.isSelected = true
        }

        holder.location.text = "${item.beauticianCity}, ${item.beauticianState}"
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

        holder.likeImage.setOnClickListener {
            if (itemType == "likes") {
                val data: Map<String, Any> = hashMapOf("liked" to FieldValue.arrayRemove(FirebaseAuth.getInstance().currentUser!!.uid))
                db.collection(item.itemType).document(item.documentId).update(data)
                db.collection("User").document(auth.currentUser!!.uid).collection("Likes").document(item.documentId).delete()
                items.removeAt(position)
                notifyItemRemoved(position)
            } else {
                val data : Map<String, Any> = hashMapOf(
                    "itemType" to item.itemType,
                    "itemTitle" to item.itemTitle,
                    "itemDescription" to item.itemDescription,
                    "itemPrice" to item.itemPrice,
                    "imageCount" to item.imageCount,
                    "beauticianUsername" to item.beauticianUsername,
                    "beauticianPassion" to item.beauticianPassion,
                    "beauticianCity" to item.beauticianCity,
                    "beauticianState" to item.beauticianState,
                    "beauticianImageId" to item.beauticianImageId,
                    "liked" to item.liked,
                    "itemOrders" to item.itemOrders,
                    "itemRating" to item.itemRating,
                    "hashtags" to item.hashtags
                )

                if (!holder.likeImage.isSelected) {
                    holder.likeImage.setImageResource(R.drawable.heart_filled)
                    holder.likeImage.isSelected = true
                    holder.itemLikes.text = "${holder.itemLikes.text.toString().toInt() + 1}"
                    val data1: Map<String, Any> = hashMapOf("liked" to FieldValue.arrayUnion(FirebaseAuth.getInstance().currentUser!!.uid))
                    db.collection(item.itemType).document(item.documentId).update(data1)
                    db.collection("User").document(auth.currentUser!!.uid).collection("Likes").document(item.documentId).update(data)
                    db.collection("User").document(auth.currentUser!!.uid).collection("Beauticians").document(item.beauticianImageId).get().addOnSuccessListener { document ->
                        if (document != null) {
                            val data2 = document.data
                            if (data2 != null) {
                                val itemCount = data2["itemCount"] as Number
                                val data3 : Map<String, Any> = hashMapOf("itemCount" to itemCount.toInt() + 1)
                                db.collection("User").document(auth.currentUser!!.uid).collection("Beauticians").document(item.beauticianImageId).update(data3)
                            } else {
                                val data3 : Map<String, Any> = hashMapOf(
                                    "itemCount" to 1,
                                    "beauticianImageId" to item.beauticianImageId,
                                    "beauticianUsername" to item.beauticianUsername,
                                    "beauticianPassion" to item.beauticianPassion,
                                    "beauticianCity" to item.beauticianCity,
                                    "beauticianState" to item.beauticianState
                                )
                                db.collection("User").document(auth.currentUser!!.uid).collection("Beauticians").document(item.beauticianImageId).update(data3)
                            }
                        }
                    }
                } else {
                    holder.likeImage.setImageResource(R.drawable.heart_unfilled)
                    holder.likeImage.isSelected = false
                    holder.itemLikes.text = "${holder.itemLikes.text.toString().toInt() - 1}"
                    val data1: Map<String, Any> = hashMapOf("liked" to FieldValue.arrayRemove(FirebaseAuth.getInstance().currentUser!!.uid))
                    db.collection(item.itemType).document(item.documentId).update(data1)
                    db.collection("User").document(auth.currentUser!!.uid).collection("Likes").document(item.documentId).delete()

                    db.collection("User").document(auth.currentUser!!.uid).collection("Beauticians").document(item.beauticianImageId).get().addOnSuccessListener { document ->
                        if (document != null) {
                            val data2 = document.data
                            if (data2 != null) {
                                val itemCount = data2["itemCount"] as Number
                                if (itemCount.toInt() == 1) {
                                    db.collection("User").document(auth.currentUser!!.uid).collection("Beauticians").document(item.beauticianImageId).delete()
                                } else {
                                    val data3 : Map<String, Any> = hashMapOf("itemCount" to itemCount.toInt() - 1)
                                    db.collection("User").document(auth.currentUser!!.uid).collection("Beauticians").document(item.beauticianImageId).update(data3)
                                }
                            }
                        }
                    }
                }
            }
        }

        holder.userImage.setOnClickListener {
            val intent = Intent(context, ProfileAsUser::class.java)
            intent.putExtra("beautician_or_user", "Beautician")
            intent.putExtra("user_id", item.beauticianImageId)
            context.startActivity(intent)
        }

        holder.itemImage.setOnClickListener {
            val intent = Intent(context, ItemDetail::class.java)
            intent.putExtra("item", item)
            context.startActivity(intent)
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

    fun submitList(items: MutableList<ServiceItems>) {
        this.items = items
    }

    class ViewHolder(itemView: UserItemPostBinding) : RecyclerView.ViewHolder(itemView.root) {

        val context = itemView.root.context!!
        var userImage = itemView.userImage
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
        val location = itemView.location

        @SuppressLint("SetTextI18n")
        fun bind(item: ServiceItems) {
            val imageRef = Firebase.storage.reference
            imageRef.child("${item.itemType}/${item.beauticianImageId}/${item.documentId}/${item.documentId}0.png").downloadUrl.addOnSuccessListener { itemUri ->
                Glide.with(context).load(itemUri).placeholder(R.drawable.default_profile).into(itemImage)
            }

            val imageRef1 = Firebase.storage.reference
            imageRef1.child("beauticians/${item.beauticianImageId}/profileImage/${item.beauticianImageId}.png").downloadUrl.addOnSuccessListener { itemUri ->
                Glide.with(context).load(itemUri).placeholder(R.drawable.default_profile).into(userImage)
            }

        }
    }

}