package com.example.beautysync_kotlin.beautician.misc

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.example.beautysync_kotlin.R
import com.example.beautysync_kotlin.beautician.MainActivity
import com.example.beautysync_kotlin.beautician.models.ServiceItemImage
import com.example.beautysync_kotlin.databinding.ActivityServiceItemAddBinding
import com.example.beautysync_kotlin.user.models.ServiceItems
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.util.UUID

private const val TAG = "ServiceItemAdd"
class ServiceItemAdd : AppCompatActivity() {
    private lateinit var binding : ActivityServiceItemAddBinding

    private val db = Firebase.firestore
    private val storage = Firebase.storage
    private val auth = FirebaseAuth.getInstance()

    private var hashtags : MutableList<String> = arrayListOf()

    private var imageArr = ArrayList<ServiceItemImage>()
    private val imageList = ArrayList<SlideModel>()

    private var item : ServiceItems? = null
    private var itemType = ""
    private var itemLabel = ""



    private var newOrEdit = "new"
    private var beauticianUsername = ""
    private var beauticianPassion = ""
    private var beauticianCity = ""
    private var beauticianState = ""
    private var documentId = UUID.randomUUID().toString()



    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityServiceItemAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        itemType = intent.getStringExtra("item_type").toString()
        when (itemType) {
            "hairCareItems" -> {
                itemLabel = "Hair Care Item"
            }
            "skinCareItems" -> {
                itemLabel = "Skin Care Item"
            }
            "nailCareItems" -> {
                itemLabel = "Nail Care Item"
            }
        }
        binding.addItemLabel.text = itemLabel
        newOrEdit = intent.getStringExtra("new_or_edit").toString()
        @Suppress("DEPRECATION")
        item = intent.getParcelableExtra("item")
        intent.getStringExtra("beautician_username")
        intent.getStringExtra("beautician_passion")
        intent.getStringExtra("beautician_city")
        intent.getStringExtra("beautician_state")
        if (item != null) {
            documentId = item!!.documentId
            beauticianUsername = item!!.beauticianUsername
            beauticianPassion = item!!.beauticianPassion
            beauticianCity = item!!.beauticianCity
            beauticianState = item!!.beauticianState

            binding.itemTitle.setText(item!!.itemTitle)
            binding.itemDescription.setText(item!!.itemDescription)
            binding.itemPrice.setText(item!!.itemPrice)
            if (item!!.hashtags.size > 0) {
                for (i in 0 until item!!.hashtags.size) {
                    if (i == 0) {
                        binding.hashtags.text = item!!.hashtags[i]
                    } else {
                        binding.hashtags.text = "${binding.hashtags.text}, ${item!!.hashtags[i]}"
                    }
                    binding.hashtagDelete.isVisible = true
                }
            }
            binding.cancelImage.isVisible = true

            for (i in 0 until item!!.imageCount.toInt()) {
                var path = "$itemType/${item!!.beauticianImageId}/${item!!.documentId}/${item!!.documentId}$i.png"
                storage.reference.child(path).downloadUrl.addOnSuccessListener { itemUri ->
                    imageList.add(SlideModel(itemUri.toString(), "", ScaleTypes.CENTER_CROP))
                    imageArr.add(ServiceItemImage(itemUri!!, path))
                    binding.imageSlider.setImageList(imageList)
                }
            }

        }

        binding.addImage.setOnClickListener {
            ImagePicker.with(this)
                .crop()                    //Crop image(Optional), Check Customization for more option
                .compress(1024)            //Final image size will be less than 1 MB(Optional)
                .maxResultSize(
                    1080,
                    1080
                )    //Final image resolution will be less than 1080 x 1080(Optional)
                .start()
        }

        binding.cancelImage.setOnClickListener {
            if (newOrEdit == "new") {
                imageArr.clear()
                imageList.clear()
                binding.imageSlider.setImageList(imageList)
                binding.cancelImage.isVisible = false

            } else {
                AlertDialog.Builder(this)
                    .setTitle("Delete Image")
                    .setMessage("This will delete all of your item's images. Are you sure you want to continue?")
                    // if the dialog is cancelable
                    .setCancelable(false)
                    .setPositiveButton("Yes") { dialog, _ ->
                        val storageRef = storage.reference
                        val renewRef = storage.reference
                        for (i in 0 until imageArr.size) {
                            storageRef.child(imageArr[i].imgPath).delete()
                        }
                        imageArr.clear()
                        imageList.clear()
                        binding.imageSlider.setImageList(imageList)
                        if (imageList.size == 0) {
                            binding.cancelImage.isVisible = false
                        }

                        val data: Map<String, Any> =
                            hashMapOf("imageCount" to imageArr.size)
                        db.collection("Beautician").document(FirebaseAuth.getInstance().currentUser!!.uid).collection(itemType).document(documentId).update(data)
                        db.collection(itemType).document(documentId).update(data)
                        Toast.makeText(this, "Images Deleted.", Toast.LENGTH_LONG).show()


                        dialog.dismiss()
                    }
                    .setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create()
                    .show()
            }
        }

        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.hashtagAdd.setOnClickListener {
            if (binding.hashtagText.text.contains("[!\"#$%&'()*+,-./:;\\\\<=>?@\\[\\]^_`{|}~]".toRegex())) {
                Toast.makeText(this, "Please take out all special characters. For Example: natural", Toast.LENGTH_LONG).show()
            } else {
                if (binding.hashtags.text.isEmpty()) {
                    binding.hashtags.text = "#${binding.hashtagText.text}"
                } else {
                    binding.hashtags.text = "${binding.hashtags.text}, #${binding.hashtagText.text}"
                }
                hashtags.add("#${binding.hashtagText.text}")
                binding.hashtagText.setText("")
                binding.hashtagDelete.isVisible = true
            }
        }

        binding.hashtagDelete.setOnClickListener {
            binding.hashtagDelete.isVisible = false
            hashtags.clear()
            binding.hashtagText.setText("")
            binding.hashtags.text = ""
        }

        binding.saveButton.setOnClickListener {
            saveInfo()
        }
    }

    private fun saveInfo() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("where_to", "home")
        if (binding.itemTitle.text.isEmpty()) {
            Toast.makeText(this, "Please enter an item title", Toast.LENGTH_LONG).show()
        } else if (imageArr.size == 0) {
            Toast.makeText(this, "Please select at least one image.", Toast.LENGTH_LONG).show()
        } else if (binding.itemDescription.text.isEmpty()) {
            Toast.makeText(this, "Please enter an item description.", Toast.LENGTH_LONG).show()
        } else if (binding.itemPrice.text.isEmpty()) {
            Toast.makeText(this, "Please enter an item price like so: 54.44", Toast.LENGTH_LONG).show()
        } else {
            if (newOrEdit == "new") {
                val data: Map<String, Any> = hashMapOf(
                    "itemType" to itemType,
                    "itemTitle" to binding.itemTitle.text.toString(),
                    "itemDescription" to binding.itemDescription.text.toString(),
                    "itemPrice" to binding.itemPrice.text.toString(),
                    "imageCount" to imageList.size,
                    "beauticianUsername" to beauticianUsername,
                    "beauticianPassion" to beauticianPassion,
                    "beauticianCity" to beauticianCity,
                    "beauticianState" to beauticianState,
                    "beauticianImageId" to auth.currentUser!!.uid,
                    "liked" to arrayListOf<String>(),
                    "itemOrders" to 0,
                    "itemRating" to arrayListOf<Int>(),
                    "hashtags" to hashtags
                )
                db.collection("Beautician").document(auth.currentUser!!.uid).collection(itemType).document(documentId).set(data)
                db.collection(itemType).document(documentId).set(data)
                for (i in 0 until imageArr.size) {
                    storage.reference.child("$itemType/${auth.currentUser!!.uid}/$documentId/$documentId$i.png").putFile(imageArr[i].img).addOnCompleteListener {

                        if (i == imageArr.size - 1) {
                            Toast.makeText(this, "Item Saved.", Toast.LENGTH_LONG).show()
                            startActivity(intent)
                            finish()
                        }
                    }
                }


            } else {
                val data: Map<String, Any> = hashMapOf(
                    "itemType" to itemType,
                    "itemTitle" to binding.itemTitle.text.toString(),
                    "itemDescription" to binding.itemDescription.text.toString(),
                    "itemPrice" to binding.itemPrice.text.toString(),
                    "imageCount" to imageList.size,
                    "beauticianUsername" to beauticianUsername,
                    "beauticianPassion" to beauticianPassion,
                    "beauticianCity" to beauticianCity,
                    "beauticianState" to beauticianState,
                    "beauticianImageId" to auth.currentUser!!.uid,
                    "hashtags" to hashtags
                )

                db.collection("Beautician").document(auth.currentUser!!.uid).collection(itemType).document(documentId).update(data)
                db.collection(itemType).document(documentId).update(data)

                Toast.makeText(this, "Item Updated.", Toast.LENGTH_LONG).show()
                startActivity(intent)
                finish()
            }
        }


    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            Activity.RESULT_OK -> {
                //Image Uri will not be null for RESULT_OK
                val uri: Uri = data?.data!!
                imageList.add(SlideModel(uri.toString(), "", ScaleTypes.CENTER_CROP))
                var path = "${itemType}/${FirebaseAuth.getInstance().currentUser!!.uid}/${documentId}/$documentId${imageList.count() - 1}.png"
                imageArr.add(ServiceItemImage(uri, path))
                if (newOrEdit == "edit") {
                    storage.reference.child(path).putFile(uri)
                    val data1 : Map<String, Any> = hashMapOf("imageCount" to imageArr.size)
                    db.collection("Beautician").document(auth.currentUser!!.uid).collection(itemType).document(documentId).update(data1)
                    db.collection(itemType).document(documentId).update(data1)
                    Toast.makeText(this, "Image Added.", Toast.LENGTH_LONG).show()
                }
                binding.imageSlider.setImageList(imageList)
                binding.cancelImage.isVisible = true
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