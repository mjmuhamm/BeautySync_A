package com.example.beautysync_kotlin.beautician.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BankAccount(
    val bankName: String,
    val accountHolder: String,
    val accountNumber: String,
    val routingNumber: String,
    var externalAccountId: String,
) : Parcelable
