package com.example.beautysync_kotlin.beautician.info

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.example.beautysync_kotlin.R
import com.example.beautysync_kotlin.beautician.MainActivity
import com.example.beautysync_kotlin.beautician.models.BankAccount
import com.example.beautysync_kotlin.beautician.models.Utils
import com.example.beautysync_kotlin.databinding.ActivityBankingInfoBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.stripe.android.core.utils.ContextUtils.packageInfo
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.Calendar
import java.util.UUID

class BankingInfo : AppCompatActivity() {
    private lateinit var binding : ActivityBankingInfoBinding

    private val db = Firebase.firestore

    private val httpClient = OkHttpClient()
    private val mHandler: Handler = Handler(Looper.getMainLooper())

    //Bank
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var bankName: EditText
    private lateinit var accountHolder: EditText
    private lateinit var accountNumber: EditText
    private lateinit var routingNumber: EditText
    private lateinit var exitButton: TextView
    private lateinit var saveButton: MaterialButton

    private var bankAccount: BankAccount = BankAccount("", "", "","","")

    private var stripeId = ""
    private var ip = ""
    private var accountType = "Individual"
    private var documentId = UUID.randomUUID().toString()

    private var newOrEdit = "new"

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ip = Utils.getIPAddress(true)

        binding = ActivityBankingInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Bank
        bottomSheetDialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        val bottomSheetView = LayoutInflater.from(this)
            .inflate(R.layout.banking_bottom_sheet, R.id.banking_bottom_sheet as? RelativeLayout)

        bankName = bottomSheetView.findViewById(R.id.bank_name)
        accountHolder = bottomSheetView.findViewById(R.id.account_holder)
        accountNumber = bottomSheetView.findViewById(R.id.account_number)
        routingNumber = bottomSheetView.findViewById(R.id.routing_number)
        exitButton = bottomSheetView.findViewById(R.id.exit_button)
        saveButton = bottomSheetView.findViewById(R.id.add_button)

        bottomSheetDialog.setContentView(bottomSheetView)

        if (newOrEdit == "new") {
            binding.backButton.isVisible = false
        }

        binding.backButton.setOnClickListener {
            if (newOrEdit == "edit") {
                onBackPressedDispatcher.onBackPressed()
            } else {
                Toast.makeText(this, "Back Button is not allowed yet.", Toast.LENGTH_LONG).show()
            }
        }

        binding.clickHereToView.setOnClickListener {
            val intent = Intent(this, StripesTermsOfService::class.java)
            startActivity(intent)
        }

        exitButton.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        binding.externalAccountEditButton.setOnClickListener {
            bottomSheetDialog.show()
        }



        binding.businessButton.setOnClickListener {
            Toast.makeText(this, "Business option not yet available.", Toast.LENGTH_LONG).show()
        }

        saveButton.setOnClickListener {
            if (bankName.text.isEmpty()) {
                Toast.makeText(this, "Please enter a bank name in the allotted field.", Toast.LENGTH_LONG).show()
            } else if (accountHolder.text.isEmpty()) {
                Toast.makeText(this, "Please enter an account holder in the allotted field.", Toast.LENGTH_LONG).show()
            } else if (accountNumber.text.isEmpty()) {
                Toast.makeText(this, "Please enter a valid account number in the allotted field.", Toast.LENGTH_LONG).show()
            } else if (routingNumber.text.isEmpty() || routingNumber.text.length != 9) {
                Toast.makeText(this, "Please enter a valid routing number in the allotted field.", Toast.LENGTH_LONG).show()
            } else {
                if (newOrEdit == "new") {
                    bankAccount = BankAccount(bankName.text.toString(), accountHolder.text.toString(), accountNumber.text.toString(), routingNumber.text.toString(), UUID.randomUUID().toString())

                    binding.externalAccountLayout.visibility = View.VISIBLE
                    binding.externalAccountText.text = "Account #: ****${bankAccount.accountNumber.takeLast(4)}"

                    bottomSheetDialog.dismiss()
                } else {

                    AlertDialog.Builder(this)
                        .setMessage("This will delete your old external account and create a new one with this information. Continue?")
                        // if the dialog is cancelable
                        .setCancelable(false)
                        .setPositiveButton("Yes") { dialog, _ ->
                            deleteExternalAccount(stripeId, bankAccount.externalAccountId)
                            createExternalAccount(stripeId, accountType)
                            dialog.dismiss()
                        }
                        .setNegativeButton("No") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .create()
                        .show()

                }

            }
        }
        binding.saveButton.setOnClickListener {
            if (binding.mccCode.text.isEmpty()) {
                Toast.makeText(this, "Please enter your mcc code in the allotted field.", Toast.LENGTH_LONG).show()
            } else if (binding.businessUrl.text.isEmpty()) {
                Toast.makeText(this, "Please enter your business url in the allotted field.", Toast.LENGTH_LONG).show()
            } else if (binding.firstName.text.isEmpty()) {
                Toast.makeText(this, "Please enter your first name in the allotted field.", Toast.LENGTH_LONG).show()
            } else if (binding.lastName.text.isEmpty()) {
                Toast.makeText(this, "Please enter your last name in the allotted field.", Toast.LENGTH_LONG).show()
            } else if (binding.email.text.isEmpty() || !isValidEmail(binding.email.text.toString())) {
                Toast.makeText(this, "Please enter your email address in the allotted field.", Toast.LENGTH_LONG).show()
            } else if (binding.phone.text.isEmpty() || binding.phone.text.length != 10) {
                Toast.makeText(this, "Please enter your phone in the following format: 5555555555.", Toast.LENGTH_LONG).show()
            } else if ((binding.dobDay.text.isEmpty() || binding.dobDay.text.length != 2) || (binding.dobMonth.text.isEmpty() || binding.dobMonth.text.length != 2) || (binding.dobYear.text.isEmpty() || binding.dobYear.text.length != 4)) {
                Toast.makeText(this, "Please enter your date of birth in the following format: 01-01-2001.", Toast.LENGTH_LONG).show()
            } else if (binding.streetAddress.text.isEmpty()) {
                Toast.makeText(this, "Please enter your street address in the allotted field.", Toast.LENGTH_LONG).show()
            } else if (binding.city.text.isEmpty()) {
                Toast.makeText(this, "Please enter your city in the allotted field.", Toast.LENGTH_LONG).show()
            } else if (binding.state.text.isEmpty()) {
                Toast.makeText(this, "Please enter your state in the allotted field.", Toast.LENGTH_LONG).show()
            } else if (binding.zipCode.text.isEmpty()) {
                Toast.makeText(this, "Please enter your zip code in the allotted field.", Toast.LENGTH_LONG).show()
            } else if (binding.socialSecurityNumber.text.isEmpty() || binding.socialSecurityNumber.text.length != 9) {
                Toast.makeText(this, "Please enter your ssn in the following format: 1111111111.", Toast.LENGTH_LONG).show()
            } else if (bankAccount.bankName == "") {
                Toast.makeText(this, "Please enter your bank account info.", Toast.LENGTH_LONG)
                    .show()
            } else {
                if (newOrEdit == "edit") {
                    updateIndividualAccount()
                } else {
                    createIndividualAccount()
                }
            }
        }
    }

    private fun createIndividualAccount() {
        val intent = Intent(this@BankingInfo, MainActivity::class.java)
        intent.putExtra("where_to", "home")

        val date = Calendar.getInstance().timeInMillis / 1000
        binding.progressBar.isVisible = true
        val body = FormBody.Builder()
            .add("mcc", binding.mccCode.text.toString())
            .add("url", binding.businessUrl.text.toString())
            .add("date", "$date")
            .add("ip", ip)
            .add("first_name", binding.firstName.text.toString())
            .add("last_name", binding.lastName.text.toString())
            .add("dob_day", binding.dobDay.text.toString())
            .add("dob_month", binding.dobMonth.text.toString())
            .add("dob_year", binding.dobYear.text.toString())
            .add("line_1", binding.streetAddress.text.toString())
            .add("line_2", binding.streetAddress2.text.toString())
            .add("postal_code", binding.zipCode.text.toString())
            .add("city", binding.city.text.toString())
            .add("state", binding.state.text.toString())
            .add("email", binding.email.text.toString())
            .add("phone", binding.phone.text.toString())
            .add("ssn", binding.socialSecurityNumber.text.toString())
            .add("account_holder", bankAccount.accountHolder)
            .add("account_number", bankAccount.accountNumber.toString())
            .add("routing_number", bankAccount.routingNumber.toString())
            .build()

        val request = Request.Builder()
            .url("https://beautysync-stripeserver.onrender.com/create-individual-account")
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .post(body)
            .build()

        httpClient.newCall(request)
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    displayAlert("Error: ${e.localizedMessage}")
                }

                @SuppressLint("SetTextI18n")
                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        displayAlert(
                            "Error: $response"
                        )
                        binding.progressBar.isVisible = false
                    } else {
                        val responseData = response.body!!.string()
                        val json =
                            JSONObject(responseData)

                        val id = json.getString("id")
                        val externalAccountId = json.getString("external_account")


                        mHandler.post {
                            val data: Map<String, Any> = hashMapOf("accountType" to "Individual", "stripeAccountId" to id, "externalAccountId" to externalAccountId)
                            db.collection("Beautician").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("BankingInfo").document().set(data)

                            Toast.makeText(this@BankingInfo, "Saved successfully.", Toast.LENGTH_LONG).show()
                            startActivity(intent)
                            finish()


                            binding.progressBar.isVisible = false
                        }
                    }
                }
            })

    }

    private fun updateIndividualAccount() {

        val intent = Intent(this@BankingInfo, MainActivity::class.java)
        intent.putExtra("where_to", "home")
        binding.progressBar.isVisible = true
        val body = FormBody.Builder()
            .add("mcc", binding.mccCode.text.toString())
            .add("url", binding.businessUrl.text.toString())
            .add("first_name", binding.firstName.text.toString())
            .add("last_name", binding.lastName.text.toString())
            .add("dob_day", binding.dobDay.text.toString())
            .add("dob_month", binding.dobMonth.text.toString())
            .add("dob_year", binding.dobYear.text.toString())
            .add("line1", binding.streetAddress.text.toString())
            .add("line2", binding.streetAddress2.text.toString())
            .add("postal_code", binding.zipCode.text.toString())
            .add("city", binding.city.text.toString())
            .add("state", binding.state.text.toString())
            .add("email", binding.email.text.toString())
            .add("phone", binding.phone.text.toString())
            .build()

        val request = Request.Builder()
            .url("https://beautysync-stripeserver.onrender.com/update-individual-account")
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .post(body)
            .build()

        httpClient.newCall(request)
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    displayAlert("Error: $e")
                }

                @SuppressLint("SetTextI18n")
                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        displayAlert(
                            "Error: $response"
                        )
                    } else {

                        mHandler.post {

                            Toast.makeText(this@BankingInfo, "Update successful.", Toast.LENGTH_LONG).show()
                            startActivity(intent)
                            finish()


                            binding.progressBar.isVisible = false
                        }
                    }
                }
            })

    }

    private fun createExternalAccount(stripeId: String, accountType: String) {
        val intent = Intent(this@BankingInfo, MainActivity::class.java)
        intent.putExtra("where_to", "home")
        binding.progressBar.isVisible = true
        val body = FormBody.Builder()
            .add("stripeAccountId", stripeId)
            .add("account_holder", bankAccount.accountHolder)
            .add("account_type", accountType)
            .add("routing_number", bankAccount.routingNumber.toString())
            .add("account_number", bankAccount.accountNumber.toString())
            .build()

        val request = Request.Builder()
            .url("https://beautysync-stripeserver.onrender.com/create-bank-account")
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .post(body)
            .build()

        httpClient.newCall(request)
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    displayAlert("Error: $e")
                }

                @SuppressLint("SetTextI18n")
                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        displayAlert(
                            "Error: $response"
                        )
                    } else {
                        val responseData = response.body!!.string()
                        val json =
                            JSONObject(responseData)

                        val externalAccount = json.getString("externalAccount")


                        mHandler.post {
                            if (newOrEdit == "new") {
                                if (accountType == "Individual") {
                                    Toast.makeText(this@BankingInfo, "Saved successfully! Please check your banking status for more information on your banking status.", Toast.LENGTH_LONG).show()
                                    startActivity(intent)
                                    finish()
                                }
                            } else {
                                val data : Map<String, Any> = hashMapOf("externalAccountId" to externalAccount)
                                db.collection("Beautician").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("BankingInfo").document(documentId).update(data)
                                Toast.makeText(this@BankingInfo, "Saved successfully!", Toast.LENGTH_LONG).show()
                                bottomSheetDialog.dismiss()
                            }
                        }
                    }
                }
            })

    }


    private fun deleteExternalAccount(stripeId: String, externalAccountId: String) {
        binding.progressBar.isVisible = true
        val body = FormBody.Builder()
            .add("stripeAccountId", stripeId)
            .add("externalAccountId", externalAccountId)
            .build()

        val request = Request.Builder()
            .url("https://beautysync-stripeserver.onrender.com/delete-bank-account")
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .post(body)
            .build()

        httpClient.newCall(request)
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    displayAlert( "Error: $e")
                }

                @SuppressLint("SetTextI18n")
                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        displayAlert(
                            "Error: $response"
                        )
                    } else {
                        mHandler.post {
                            bottomSheetDialog.dismiss()
                            val data : Map<String, Any> = hashMapOf("externalAccountId" to "")
                            db.collection("Beautician").document(FirebaseAuth.getInstance().currentUser!!.uid).collection("BankingInfo").document(documentId).update(data)
                            Toast.makeText(this@BankingInfo, "External account deleted.", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            })

    }

    private fun displayAlert(
        message: String
    ) {
        runOnUiThread {
            val builder = AlertDialog.Builder(this)
                .setTitle("Failed to load page.")
                .setMessage(message)

            builder.setPositiveButton("Ok", null)
            builder
                .create()
                .show()
        }
    }

    fun isValidEmail(target: CharSequence?): Boolean {
        return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target!!).matches()
    }
}