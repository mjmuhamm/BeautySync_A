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
import com.example.beautysync_kotlin.databinding.UserReviewsPostBinding
import com.example.beautysync_kotlin.user.models.Beauticians
import com.example.beautysync_kotlin.user.models.UserReview
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import okhttp3.OkHttpClient

class ReviewsAdapter(private val context: Context, private var items: MutableList<UserReview>) : RecyclerView.Adapter<ReviewsAdapter.ViewHolder>()  {

    private val httpClient = OkHttpClient()
    private val db = Firebase.firestore

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(UserReviewsPostBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
        val item = items[position]

        holder.userImage.setOnClickListener {
            val intent = Intent(context, ProfileAsUser::class.java)
            intent.putExtra("beautician_or_user", "Beautician")
            intent.putExtra("user_id", item.beauticianImageId)
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

    fun submitList(items: MutableList<UserReview>) {
        this.items = items
    }

    class ViewHolder(itemView: UserReviewsPostBinding) : RecyclerView.ViewHolder(itemView.root) {

        val context = itemView.root.context!!
        val userImage = itemView.userImage
        val itemTypeAndTitle = itemView.itemTypeAndTitle
        val beautician = itemView.beautician
        val orderDate = itemView.orderDate
        val review = itemView.review
        val doesTheUserRecommend = itemView.recommend
        val expectations = itemView.expectationsMet
        val quality = itemView.qualityRating
        val beauticianRating = itemView.beauticianRating
        val likeText = itemView.likeText



        @SuppressLint("SetTextI18n")
        fun bind(item: UserReview) {

            val imageRef1 = Firebase.storage.reference
            imageRef1.child("beauticians/${item.beauticianImageId}/profileImage/${item.beauticianImageId}.png").downloadUrl.addOnSuccessListener { itemUri ->
                Glide.with(context).load(itemUri).placeholder(R.drawable.default_profile).into(userImage)
            }

            var item1 = ""
            if (item.itemType == "hairCareItems") {
                item1 = "Hair Care"
            } else if (item.itemType == "skinCareItems") {
                item1 = "Skin Care"
            } else if (item.itemType == "Nail Care") {
                item1 = "Nail Care"
            }
            itemTypeAndTitle.text = "$item1 | ${item.itemTitle}"
            orderDate.text = "Ordered: ${item.orderDate}"
            beautician.text = "Beautician: ${item.beauticianUsername}"
            if (item.thoughts == "") { review.text = "No Thoughts" } else { review.text = item.thoughts }
            if (item.recommend == 0) { doesTheUserRecommend.text = "No" } else { doesTheUserRecommend.text = "Yes" }
            expectations.text = "${item.expectations}"
            quality.text = "${item.quality}"
            beauticianRating.text = "${item.rating}"
            likeText.text = "${item.liked.size}"

        }
    }

}