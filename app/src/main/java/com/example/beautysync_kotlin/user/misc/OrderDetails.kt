package com.example.beautysync_kotlin.user.misc

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.aminography.primecalendar.civil.CivilCalendar
import com.aminography.primecalendar.common.operators.dayOfMonth
import com.aminography.primecalendar.common.operators.month
import com.aminography.primecalendar.common.operators.year
import com.aminography.primedatepicker.picker.PrimeDatePicker
import com.aminography.primedatepicker.picker.callback.MultipleDaysPickCallback
import com.aminography.primedatepicker.picker.callback.SingleDayPickCallback
import com.example.beautysync_kotlin.R
import com.example.beautysync_kotlin.databinding.ActivityOrderDetailsBinding
import com.example.beautysync_kotlin.user.MainActivity
import com.example.beautysync_kotlin.user.models.ServiceItems
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

private const val TAG = "OrderDetails"
@Suppress("DEPRECATION")
class OrderDetails : AppCompatActivity() {
    private lateinit var binding : ActivityOrderDetailsBinding

    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    private var item : ServiceItems? = null

    private var eventDay = ""
    private var eventTime = ""
    private lateinit var datePicker : PrimeDatePicker

    private var streetAddress = ""
    private var city = ""
    private var state = ""
    private var zipCode = ""

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityOrderDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        item = intent.getParcelableExtra("item")
        if (item != null) {
            loadBeauticianLocation()
            binding.itemTitle.text = item!!.itemTitle
            binding.itemDescription.text = item!!.itemDescription
            binding.priceOfEvent.text = "$${item!!.itemPrice}"

        }



        //Date Picker
        val callback = SingleDayPickCallback { day ->

                val month = day.month
                val day1 = day.dayOfMonth
                val year = day.year

            val userDate = "${day.monthDayString}, $year"

                val newMonth = if (month < 10) {
                    "0${month + 1}"
                } else {
                    "${month + 1}"
                }
                val newDay = if (day1 < 10) {
                    "0$day1"
                } else {
                    "$day1"
                }

                eventDay = "$newMonth-$newDay-$year"

            binding.timePickerLayout.visibility = View.VISIBLE
            binding.dateForTimePicker.text = userDate
            binding.dateOfEventLabel.isVisible = false
            binding.datePickedLayout.isVisible = false
            binding.locationLabel.isVisible = false
            binding.locationPickedLayout.isVisible = false
            binding.notesLabel.isVisible = false
            binding.notesToBeautician.isVisible = false
            binding.addItemLayout.isVisible = false
            binding.clearButton.isVisible = false
        }

        val today = CivilCalendar()
        datePicker = PrimeDatePicker.bottomSheetWith(today).pickSingleDay(callback).minPossibleDate(today).build()


        binding.datePickerButton.setOnClickListener {
            datePicker.show(supportFragmentManager, "Event Day")
        }

        binding.timePickerOkButton.setOnClickListener {
            var hour = binding.timePicker.hour
            val minute = binding.timePicker.minute

            val newHour : String

            val amOrPm : String

            if (hour > 12) {
                hour -= 12
                newHour = if (hour < 10) { "0$hour" } else { "$hour" }
                amOrPm = "PM"
            } else {
                newHour = if (hour < 10) { "0$hour" } else { "$hour" }
                amOrPm = "AM"
            }

            val newMinute = if (minute < 10) {
                "0$minute"
            } else {
                "$minute"
            }

                eventTime = "$newHour:$newMinute $amOrPm"

                binding.datePickerText.setTextColor(ContextCompat.getColor(this, R.color.main))
                binding.clearButton.isVisible = true
                binding.timePickerLayout.visibility = View.GONE
                binding.dateOfEventLabel.isVisible = true
                binding.datePickedLayout.isVisible = true
                binding.locationLabel.isVisible = true
                binding.locationPickedLayout.isVisible = true
                binding.notesLabel.isVisible = true
                binding.notesToBeautician.isVisible = true
                binding.addItemLayout.isVisible = true
                binding.datePickerButton.isEnabled = false

                binding.datePickerText.text = "${binding.dateForTimePicker.text} $eventTime"

        }

        binding.clearButton.setOnClickListener {
            eventDay = ""
            eventTime = ""
            binding.datePickerText.text = ""
            binding.datePickerButton.isEnabled = true
            datePicker = PrimeDatePicker.bottomSheetWith(today).pickSingleDay(callback).minPossibleDate(today).build()
        }

        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.addButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("where_to", "home")
            if (!binding.clearButton.isVisible) {
                Toast.makeText(this, "Please select a date for this event.", Toast.LENGTH_LONG).show()
            } else {
                val data : Map<String,Any> = hashMapOf(
                    "itemType" to item!!.itemType,
                    "itemTitle" to item!!.itemTitle,
                    "itemDescription" to item!!.itemDescription,
                    "itemPrice" to item!!.itemPrice,
                    "imageCount" to item!!.imageCount,
                    "beauticianUsername" to item!!.beauticianUsername,
                    "beauticianPassion" to item!!.beauticianPassion,
                    "beauticianCity" to item!!.beauticianCity,
                    "beauticianState" to item!!.beauticianState,
                    "beauticianImageId" to item!!.beauticianImageId,
                    "liked" to item!!.liked,
                    "itemOrders" to item!!.itemOrders,
                    "itemRating" to item!!.itemRating,
                    "hashtags" to item!!.hashtags,
                    "eventDay" to eventDay,
                    "eventTime" to eventTime,
                    "streetAddress" to streetAddress,
                    "zipCode" to zipCode,
                    "notesToBeautician" to binding.notesToBeautician.text.toString(),
                    "itemId" to item!!.documentId
                )
                db.collection("User").document(auth.currentUser!!.uid).collection("Cart").document().set(data).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Event Added.", Toast.LENGTH_LONG).show()
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Something went wrong. Please check your connection.", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun loadBeauticianLocation() {
        db.collection("Beautician").document(item!!.beauticianImageId).collection("BusinessInfo").get().addOnSuccessListener { documents ->
            if (documents != null) {
                for (doc in documents.documents) {
                    val data = doc.data

                    val streetAddress = data?.get("streetAddress") as String
                    val city = data["city"] as String
                    val state = data["state"] as String
                    val zipCode = data["zipCode"] as String

                    this.streetAddress = streetAddress
                    this.city = city
                    this.state = state
                    this.zipCode = zipCode
                    binding.locationText.text = "$streetAddress $city, $state $zipCode"
                }
            }
        }
    }
}