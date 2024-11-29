package com.example.beautysync_kotlin.user.info

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.example.beautysync_kotlin.R
import com.example.beautysync_kotlin.databinding.ActivityPersonalInfoBinding
import com.example.beautysync_kotlin.user.MainActivity
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.util.regex.Matcher
import java.util.regex.Pattern


class PersonalInfo : AppCompatActivity() {
    private lateinit var binding: ActivityPersonalInfoBinding
    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()
    private val storage = Firebase.storage

    private var usernames: MutableList<String> = arrayListOf()
    private var emails: MutableList<String> = arrayListOf()

    private var userImage = Uri.EMPTY

    private var local = 1
    private var region = 0
    private var nation = 0

    private var newOrEdit = ""
    var profilePic = "no"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPersonalInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadUsernames()
        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.editImage.setOnClickListener {
            ImagePicker.with(this)
                .crop()                    //Crop image(Optional), Check Customization for more option
                .compress(1024)            //Final image size will be less than 1 MB(Optional)
                .maxResultSize(
                    1080,
                    1080
                )    //Final image resolution will be less than 1080 x 1080(Optional)
                .start()
        }

        binding.local.setOnClickListener {
            local = 1
            region = 0
            nation = 0
            binding.city.isVisible = true
            binding.state.isVisible = true
            binding.local.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
            binding.local.setTextColor(ContextCompat.getColor(this, R.color.white))
            binding.region.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            binding.region.setTextColor(ContextCompat.getColor(this, R.color.main))
            binding.nation.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            binding.nation.setTextColor(ContextCompat.getColor(this, R.color.main))
        }

        binding.region.setOnClickListener {
            local = 0
            region = 1
            nation = 0
            binding.city.setText("")
            binding.city.isVisible = false
            binding.state.isVisible = true
            binding.local.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            binding.local.setTextColor(ContextCompat.getColor(this, R.color.main))
            binding.region.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
            binding.region.setTextColor(ContextCompat.getColor(this, R.color.white))
            binding.nation.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            binding.nation.setTextColor(ContextCompat.getColor(this, R.color.main))

        }

        binding.nation.setOnClickListener {
            local = 0
            region = 0
            nation = 1
            binding.city.isVisible = false
            binding.state.isVisible = false
            binding.city.setText("")
            binding.state.setText("")
            binding.local.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            binding.local.setTextColor(ContextCompat.getColor(this, R.color.main))
            binding.region.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            binding.region.setTextColor(ContextCompat.getColor(this, R.color.main))
            binding.nation.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary))
            binding.nation.setTextColor(ContextCompat.getColor(this, R.color.white))
        }

        binding.saveButton.setOnClickListener {
            saveInfo()
        }
    }


    private fun loadUsernames() {
        db.collection("Usernames").get().addOnSuccessListener { documents ->
            if (documents != null) {
                for (doc in documents.documents) {

                    val data = doc.data

                    val username = data?.get("userName") as String
                    val email = data["email"] as String
                    if (usernames.isEmpty()) {
                        usernames.add(username)
                        emails.add(email)
                    } else {
                        val index = usernames.indexOfFirst { it == username }
                        if (index == -1) {
                            usernames.add(doc.id)
                            emails.add(email)
                        }
                    }

                }
            }
        }
    }

    private fun saveInfo() {
        val intent = Intent(this, MainActivity::class.java)
        
         if (binding.fullName.text.isEmpty()) {
            Toast.makeText(
                this,
                "Please enter your full name in the allotted field.",
                Toast.LENGTH_LONG
            ).show()
        } else if (binding.userName.text.isEmpty() || (binding.userName.text.length < 4)) {
            Toast.makeText(
                this,
                "Please enter a username with at least 4 characters.",
                Toast.LENGTH_LONG
            ).show()
        } else if (usernames.contains(binding.userName.text.toString())) {
            Toast.makeText(
                this,
                "This username already exists. Please try another username.",
                Toast.LENGTH_LONG
            ).show()
        } else if (searchForSpecialChara(binding.userName.text.toString())) {
            Toast.makeText(
                this,
                "Please remove all special characters in your username",
                Toast.LENGTH_LONG
            ).show()
        } else if (binding.email.text.isEmpty() || !isValidEmail(binding.email.text.toString())) {
            Toast.makeText(this, "Please enter a valid email.", Toast.LENGTH_LONG).show()
        } else if (emails.contains(binding.email.text.toString())) {
            Toast.makeText(this, "This is email is already taken.", Toast.LENGTH_LONG).show()
        } else if (binding.userName.text.isEmpty() || binding.userName.text.contains(" ")) {
            Toast.makeText(this, "Please enter your username with no spaces.", Toast.LENGTH_LONG)
                .show()
        } else if (!isValidPassword(binding.password.text.toString()) && (binding.password.text.isEmpty() || binding.confirmPassword.text.isEmpty())) {
            Toast.makeText(
                this,
                "Please make sure password has 1 uppercase letter, 1 special character, 1 number, 1 lowercase letter, and matches with the second insert.",
                Toast.LENGTH_LONG
            ).show()
        } else if(binding.password.text.toString() != binding.confirmPassword.text.toString()) {
            Toast.makeText(this, "Please make sure your password matches with the second insert.", Toast.LENGTH_LONG).show()
        } else if (binding.lookingForText.text.isEmpty()) {
            Toast.makeText(this, "Please enter what you hope to find in a beautician, in the allotted field.", Toast.LENGTH_LONG).show()
        } else if (local == 1 && (binding.city.text.isEmpty() || binding.state.text.isEmpty())) {
            Toast.makeText(this, "Please enter your city and your state's abbreviation in the allotted field.", Toast.LENGTH_LONG).show()
        } else if (region == 1 && (binding.state.text.isEmpty())) {
            Toast.makeText(this, "Please enter your state in the allotted field.", Toast.LENGTH_LONG).show()
        } else if ((local == 1 || region == 1) && stateFilter(binding.state.text.toString()) == "not good") {
            Toast.makeText(this, "Please enter the abbreviation for your state in the allotted field.", Toast.LENGTH_LONG).show()
        } else {
            if (newOrEdit != "edit") {
                auth.createUserWithEmailAndPassword(
                    binding.email.text.toString(),
                    binding.password.text.toString()
                ).addOnCompleteListener { task ->
                    if (task.isSuccessful) {


                        val user = auth.currentUser
                        val profileUpdates = userProfileChangeRequest {
                            displayName = "User"
                        }
                        val data: Map<String, Any> = hashMapOf(
                            "fullName" to binding.fullName.text.toString(),
                            "userName" to binding.userName.text.toString(),
                            "email" to binding.email.text.toString(),
                            "local" to local,
                            "region" to region,
                            "nation" to nation,
                            "city" to binding.city.text.toString(),
                            "state" to binding.state.text.toString(),
                            "beauticianOrUser" to "User",
                            "whatDoYouHopeToFind" to binding.lookingForText.text.toString()
                        )
                        val data1: Map<String, Any> = hashMapOf(
                            "userName" to binding.userName.text.toString(),
                            "email" to binding.email.text.toString(),
                            "beauticianOrUser" to "User",
                            "fullName" to binding.fullName.text.toString()
                        )




                        user!!.updateProfile(profileUpdates).addOnCompleteListener { _ ->
                            if (userImage != Uri.EMPTY) {
                                profilePic = "yes"
                                storage.reference.child("users/${FirebaseAuth.getInstance().currentUser!!.uid}/profileImage/${FirebaseAuth.getInstance().currentUser!!.uid}.png")
                                    .putFile(userImage!!)
                            }
                        }
                        val data2: Map<String, Any> = hashMapOf(
                            "beauticianOrUser" to "User",
                            "notificationToken" to "",
                            "notifications" to "",
                            "profilePic" to profilePic
                        )

                        db.collection("User").document(auth.currentUser!!.uid)
                            .collection("PersonalInfo").document().set(data)
                        db.collection("Usernames").document(auth.currentUser!!.uid).set(data1)
                        db.collection("User").document(auth.currentUser!!.uid).set(data2)

                        Toast.makeText(this, "Information saved.", Toast.LENGTH_LONG).show()
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(
                            this,
                            "Something went wrong. Please try again later.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } else {
                //edit
            }
        }


    }


    fun stateFilter(state: String): String {
        val stateAbbr: MutableList<String> = arrayListOf(
            "AL",
            "AK",
            "AZ",
            "AR",
            "AS",
            "CA",
            "CO",
            "CT",
            "DE",
            "DC",
            "FL",
            "GA",
            "HI",
            "ID",
            "IL",
            "IN",
            "IA",
            "KS",
            "KY",
            "LA",
            "ME",
            "MD",
            "MA",
            "MI",
            "MN",
            "MS",
            "MO",
            "NE",
            "NV",
            "NH",
            "NJ",
            "NM",
            "NY",
            "NC",
            "ND",
            "OH",
            "OK",
            "OR",
            "PA",
            "PR",
            "RI",
            "SC",
            "SD",
            "TN",
            "TX",
            "TT",
            "UT",
            "VT",
            "VA",
            "VI",
            "WA",
            "WY",
            "WV",
            "WI",
            "WY"
        )

        for (i in 0 until stateAbbr.size) {
            val a = stateAbbr[i].lowercase()
            if (a == state.lowercase()) {
                return "good"
            }
        }
        return "not good"
    }

    private fun searchForSpecialChara(search: String): Boolean {
        val p: Pattern = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE)
        val m: Matcher = p.matcher(search)
        val b: Boolean = m.find()

        if (b) {
            Log.d(TAG, "There is a special character in my string")
            return true
        } else {
            return false
        }

    }

    private fun isValidEmail(target: CharSequence?): Boolean {
        return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target.toString())
            .matches()
    }

    internal fun isValidPassword(password: String): Boolean {
        if (password.length < 8) return false
        if (password.firstOrNull { it.isDigit() } == null) return false
        if (password.filter { it.isLetter() }.firstOrNull { it.isUpperCase() } == null) return false
        if (password.filter { it.isLetter() }.firstOrNull { it.isLowerCase() } == null) return false
        if (password.firstOrNull { !it.isLetterOrDigit() } == null) return false

        return true
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            Activity.RESULT_OK -> {
                //Image Uri will not be null for RESULT_OK
                val uri: Uri = data?.data!!

                userImage = uri
                profilePic = "yes"
//                imageChanged = "Yes"
                if (newOrEdit == "edit") {
                    val data5 : Map<String, Any> = hashMapOf("profilePic" to "yes")
                    db.collection("User").document(FirebaseAuth.getInstance().currentUser!!.uid).update(data5)
                    storage.reference.child("users/${FirebaseAuth.getInstance().currentUser!!.uid}/profileImage/${FirebaseAuth.getInstance().currentUser!!.uid}.png").putFile(uri)
                    Toast.makeText(this, "Image Uploaded.", Toast.LENGTH_LONG).show()
                }
                Glide.with(this).load(uri).placeholder(R.drawable.default_profile).into(binding.userImage)
            }
            ImagePicker.RESULT_ERROR -> {
                Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
            }
            else -> {
                Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

