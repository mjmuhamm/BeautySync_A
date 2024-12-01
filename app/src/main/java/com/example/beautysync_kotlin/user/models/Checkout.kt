package com.example.beautysync_kotlin.user.models

data class Checkout(
    val itemType: String,
    val itemTitle: String,
    val itemDescription: String,
    val itemPrice: String,
    val imageCount: Int,
    val beauticianUsername: String,
    val beauticianPassion: String,
    val beauticianCity: String,
    val beauticianState: String,
    val beauticianImageId: String,
    var liked: ArrayList<String>,
    val itemOrders: Int,
    val itemRating: ArrayList<Number>,
    val hashtags: ArrayList<String>,
    val documentId: String,
    val eventDay: String,
    val eventTime: String,
    val streetAddress: String,
    val zipCode: String,
    val noteToBeautician: String,
    val itemId: String
)
