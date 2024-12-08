package com.example.beautysync_kotlin.both.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Orders(
    val itemType: String,
    val itemTitle: String,
    val itemDescription: String,
    val itemPrice: String,
    val imageCount: Number,
    val beauticianUsername: String,
    val beauticianPassion: String,
    val beauticianCity: String,
    val beauticianState: String,
    val beauticianImageId: String,
    var liked: ArrayList<String>,
    val itemOrders: Number,
    val itemRating: ArrayList<Number>,
    val hashtags: ArrayList<String>,
    val documentId: String,
    val eventDay: String,
    val eventTime: String,
    val streetAddress: String,
    val zipCode: String,
    val notesToBeautician: String,
    val userImageId: String,
    val userName: String,
    val status: String,
    val notifications: String,
    val itemId: String,
    val cancelled: String
) : Parcelable
