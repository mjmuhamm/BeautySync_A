package com.example.beautysync_kotlin.user.adapters.me

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.beautysync_kotlin.R
import com.example.beautysync_kotlin.both.misc.ProfileAsUser
import com.example.beautysync_kotlin.databinding.UserBeauticianPostBinding
import com.example.beautysync_kotlin.databinding.UserItemPostBinding
import com.example.beautysync_kotlin.user.models.Beauticians
import com.example.beautysync_kotlin.user.models.ServiceItems
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import okhttp3.OkHttpClient

class BeauticiansAdapter(private val context: Context, private var items: MutableList<Beauticians>, private var meOrProfileAsUser: String) : RecyclerView.Adapter<BeauticiansAdapter.ViewHolder>()  {

    private val httpClient = OkHttpClient()
    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(UserBeauticianPostBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
        val item = items[position]

        if (meOrProfileAsUser != "me") {
            db.collection("User").document(auth.currentUser!!.uid).collection("Beauticians").document(item.beauticianImageId).get().addOnSuccessListener { document ->
                if (document.exists()) {
                    holder.likeImage.isSelected = true
                    holder.likeImage.setImageResource(R.drawable.heart_filled)
                } else {
                    holder.likeImage.isSelected = false
                    holder.likeImage.setImageResource(R.drawable.heart_unfilled)
                }
            }

        }

        holder.userImage.setOnClickListener {
            val intent = Intent(context, ProfileAsUser::class.java)
            intent.putExtra("beautician_or_user", "Beautician")
            intent.putExtra("user_id", item.beauticianImageId)
            context.startActivity(intent)
        }

        holder.likeButton.setOnClickListener {
            if (meOrProfileAsUser == "me") {
                db.collection("User").document(auth.currentUser!!.uid).collection("Beauticians")
                    .document(item.beauticianImageId).delete()
                items.removeAt(position)
                notifyItemRemoved(position)
            }  else {
                if (holder.likeImage.isSelected) {
                    db.collection("User").document(auth.currentUser!!.uid).collection("Beauticians").document(item.beauticianImageId).delete()
                    holder.likeImage.setImageResource(R.drawable.heart_unfilled)
                    holder.likeImage.isSelected = false
                } else {
                    val data : Map<String, Any> = hashMapOf("beauticianCity" to item.beauticianCity, "beauticianState" to item.beauticianState, "beauticianImageId" to item.beauticianImageId, "beauticianPassion" to item.beauticianPassion, "itemCount" to 1)
                    db.collection("User").document(auth.currentUser!!.uid).collection("Beauticians").document(item.beauticianImageId).set(data)
                    holder.likeImage.setImageResource(R.drawable.heart_filled)
                    holder.likeImage.isSelected = true

                }
            }
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

    fun submitList(items: MutableList<Beauticians>) {
        this.items = items
    }

    class ViewHolder(itemView: UserBeauticianPostBinding) : RecyclerView.ViewHolder(itemView.root) {

        val context = itemView.root.context!!
        val userImage = itemView.userImage
        private val passion = itemView.passion
        private val location = itemView.location
        private val username = itemView.username
        val likeButton = itemView.heartButton
        val likeImage = itemView.likeImage

        @SuppressLint("SetTextI18n")
        fun bind(item: Beauticians) {

            val imageRef1 = Firebase.storage.reference
            imageRef1.child("beauticians/${item.beauticianImageId}/profileImage/${item.beauticianImageId}.png").downloadUrl.addOnSuccessListener { itemUri ->
                Glide.with(context).load(itemUri).placeholder(R.drawable.default_profile).into(userImage)
            }

            passion.text = item.beauticianPassion
            username.text = item.beauticianUsername
            location.text = "Location: ${item.beauticianCity}, ${item.beauticianState}"

        }
    }

}