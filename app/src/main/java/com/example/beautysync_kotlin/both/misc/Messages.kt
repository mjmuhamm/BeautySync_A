package com.example.beautysync_kotlin.both.misc

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.aminography.primecalendar.civil.CivilCalendar
import com.aminography.primedatepicker.picker.PrimeDatePicker
import com.aminography.primedatepicker.picker.callback.SingleDayPickCallback
import com.example.beautysync_kotlin.R
import com.example.beautysync_kotlin.beautician.MainActivity
import com.example.beautysync_kotlin.both.adapters.MessageAdapter
import com.example.beautysync_kotlin.both.models.Messages
import com.example.beautysync_kotlin.both.models.Orders
import com.example.beautysync_kotlin.databinding.ActivityMessagesBinding
import com.example.beautysync_kotlin.user.adapters.me.OrdersAndLikesAdapter
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.net.URI
import java.text.SimpleDateFormat
import java.util.Date

@Suppress("DEPRECATION")
class Messages : AppCompatActivity() {

    private lateinit var binding : ActivityMessagesBinding

    private val db = Firebase.firestore
    private val storage = Firebase.storage

    private var item : Orders? = null
    private var beauticianOrUser = ""

    private var beauticianImage = Uri.EMPTY
    private var userImage = Uri.EMPTY

    private var messages : MutableList<Messages> = arrayListOf()
    private lateinit var messageAdapter : MessageAdapter

    //Scheduling
    private var eventDay = ""
    private var eventTime = ""

    private lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var backButton : ImageView
    private lateinit var datePickerButton : ImageView
    private lateinit var datePickerText : TextView
    private lateinit var timePickerLayout : LinearLayout
    private lateinit var dateForTimePicker: TextView
    private lateinit var timePicker: TimePicker
    private lateinit var timePickerOkButton: MaterialButton
    private lateinit var submitDateButton: MaterialButton

    private lateinit var datePicker : PrimeDatePicker

    @SuppressLint("SimpleDateFormat")
    private val sdf = SimpleDateFormat("MM-dd-yyyy HH:mm a")


    @SuppressLint("SetTextI18n", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMessagesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        item = intent.getParcelableExtra("item")
        beauticianOrUser = intent.getStringExtra("beautician_or_user").toString()

        binding.messageRecyclerView.layoutManager = LinearLayoutManager(this)
        messageAdapter = MessageAdapter(this,messages, beauticianOrUser, userImage, beauticianImage)
        binding.messageRecyclerView.adapter = messageAdapter

        if (item != null) {
            loadInfo()
            if (beauticianOrUser == "User") {
                binding.user.text = "Beautician: ${item!!.beauticianUsername}"
                binding.changeDateButton.isVisible = false
            } else {
                binding.user.text = "User: @${item!!.userName}"
                binding.changeDateButton.isVisible = true
            }
            binding.serviceDate.text = "Service Date: ${item!!.eventDay} ${item!!.eventTime}"
        }

        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        //Scheduling
        bottomSheetDialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        val bottomSheetView = LayoutInflater.from(this)
            .inflate(R.layout.scheduling_sheet, R.id.scheduling_sheet as? LinearLayout)

        backButton = bottomSheetView.findViewById(R.id.back_button)
        datePickerButton = bottomSheetView.findViewById(R.id.date_picker_button)
        datePickerText = bottomSheetView.findViewById(R.id.date_picker_text)
        timePickerLayout = bottomSheetView.findViewById(R.id.time_picker_layout)
        dateForTimePicker = bottomSheetView.findViewById(R.id.date_for_time_picker)
        timePicker = bottomSheetView.findViewById(R.id.time_picker)
        timePickerOkButton = bottomSheetView.findViewById(R.id.time_picker_ok_button)
        submitDateButton = bottomSheetView.findViewById(R.id.submit_date_button)

        bottomSheetDialog.setContentView(bottomSheetView)

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

            timePickerLayout.visibility = View.VISIBLE
            dateForTimePicker.text = userDate
        }

        val today = CivilCalendar()
        datePicker = PrimeDatePicker.bottomSheetWith(today).pickSingleDay(callback).minPossibleDate(today).build()

        binding.changeDateButton.setOnClickListener {
            bottomSheetDialog.show()
        }

        datePickerButton.setOnClickListener {
            datePicker.show(supportFragmentManager, "Event Day")
        }

        backButton.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        timePickerOkButton.setOnClickListener {
            var hour = timePicker.hour
            val minute = timePicker.minute

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

            datePickerText.setTextColor(ContextCompat.getColor(this, R.color.main))
            timePickerLayout.visibility = View.GONE
            datePickerButton.isEnabled = false

            datePickerText.text = "${dateForTimePicker.text} $eventTime"
        }

        submitDateButton.setOnClickListener {
            if (eventDay == "") {
                Toast.makeText(this, "Please select a date.", Toast.LENGTH_LONG).show()
            } else {
                submitInfo()
                bottomSheetDialog.dismiss()
            }
        }

        hideSystemUI()



    }

    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, findViewById(android.R.id.content)).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun loadInfo() {
        storage.reference.child("beauticians/${item!!.beauticianImageId}/profileImage/${item!!.beauticianImageId}.png").downloadUrl.addOnSuccessListener { itemUri ->
            beauticianImage = itemUri

            binding.messageRecyclerView.layoutManager = LinearLayoutManager(this)
            messageAdapter = MessageAdapter(this,messages, beauticianOrUser, userImage, beauticianImage)
            binding.messageRecyclerView.adapter = messageAdapter
        }

        storage.reference.child("users/${item!!.userImageId}/profileImage/${item!!.userImageId}.png").downloadUrl.addOnSuccessListener { itemUri ->
            userImage = itemUri

            binding.messageRecyclerView.layoutManager = LinearLayoutManager(this)
            messageAdapter = MessageAdapter(this,messages, beauticianOrUser, userImage, beauticianImage)
            binding.messageRecyclerView.adapter = messageAdapter
        }

        binding.messageRecyclerView.layoutManager = LinearLayoutManager(this)
        messageAdapter = MessageAdapter(this, messages, beauticianOrUser, userImage, beauticianImage)
        binding.messageRecyclerView.adapter = messageAdapter

        loadMessages()
    }

    private fun loadMessages() {
        db.collection("Orders").document(item!!.documentId).collection("Messages").addSnapshotListener { documents, _ ->
            if (documents != null) {
                for (doc in documents.documents) {
                    val data = doc.data

                    val message = data?.get("message") as String
                    val date = data["date"] as String
                    val beauticianOrUser = data["beauticianOrUser"] as String

                    val x = Messages(message, date, beauticianOrUser, doc.id)

                    if (messages.size == 0) {
                        messages.add(x)
                        messageAdapter.submitList(messages)
                        messageAdapter.notifyItemInserted(0)
                    } else {
                        val index = messages.indexOfFirst { it.documentId == doc.id }
                        if (index == -1) {
                            messages.add(x)
                            messages.sortBy { it.date }
                            messageAdapter.submitList(messages)
                            messageAdapter.notifyItemInserted(messages.size - 1)
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun submitInfo() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("where_to", "orders")
        val data : Map<String, Any> = hashMapOf(
            "date" to sdf.format(Date()),
            "message" to "The beautician has changed the service date to $eventDay $eventTime.",
            "beauticianOrUser" to ""
        )
        val data1: Map<String, Any> = hashMapOf("eventDay" to eventDay, "eventTime" to eventTime, "notifications" to "yes")
        val data2: Map<String, Any> = hashMapOf("eventDay" to eventDay, "eventTime" to eventTime)
        db.collection("Orders").document(item!!.documentId).collection("Messages").document().set(data)
        db.collection("User").document(item!!.userImageId).collection("Orders").document(item!!.documentId).update(data1)
        db.collection("Beautician").document(item!!.beauticianImageId).collection("Orders").document(item!!.documentId).update(data2)
        binding.serviceDate.text = "Service Date: $eventDay $eventTime"
        startActivity(intent)
        finish()
    }


}