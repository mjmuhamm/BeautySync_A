package com.example.beautysync_kotlin.user.models

data class Beauticians(
    val beauticianCity: String,
    val beauticianImageId: String,
    val beauticianPassion: String,
    val beauticianState: String,
    val beauticianUsername: String,
    val itemCount: Int,
    val documentId: String
)

data class Reviews(
    val itemType: String,
    val itemTitle: String,
    val orderDate: String,
    val beautician: String,
    val userThoughts: String,
    val doesTheUserRecommend: String,
    val expectations: Number,
    val quality: Number,
    val beauticianRating: Number,
    val userLikes: ArrayList<String>
)

data class UserReview(
    val date: String,
    val expectations: Number,
    val quality: Number,
    val rating: Number,
    val recommend: Number,
    val thoughts: String,
    val itemDescription: String,
    val itemId: String,
    val itemTitle: String,
    val itemType: String,
    val userImageId: String,
    val userName: String,
    val liked: ArrayList<String>,
    val beauticianUsername: String,
    val beauticianImageId: String,
    val orderDate: String,
    val documentId: String
)