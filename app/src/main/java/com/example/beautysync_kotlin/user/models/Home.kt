package com.example.beautysync_kotlin.user.models

import android.net.Uri

data class ServiceItems(
    val itemType: String,
    val itemTitle: String,
    var itemImage: Uri,
    val itemDescription: String,
    val itemPrice: String,
    val imageCount: Int,
    val beauticianUsername: String,
    var beauticianUserImage: Uri,
    val beauticianPassion: String,
    val beauticianCity: String,
    val beauticianState: String,
    val beauticianImageId: String,
    var liked: ArrayList<String>,
    val itemOrders: Int,
    val itemRating: ArrayList<Number>,
    val hashtags: ArrayList<String>,
    val documentId: String
)
