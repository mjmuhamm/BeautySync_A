package com.example.beautysync_kotlin.both.models

import android.net.Uri

data class Search(
    val beauticianOrUser: String,
    val userImage: Uri,
    val userImageId: String,
    val userName: String,
    val userEmail: String,
    val userFullName: String,
    val documentId: String
)
