package com.example.beautysync_kotlin.beautician.misc

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.media3.exoplayer.ExoPlayer
import com.example.beautysync_kotlin.R
import com.example.beautysync_kotlin.beautician.MainActivity
import com.example.beautysync_kotlin.both.adapters.VideoAdapter
import com.example.beautysync_kotlin.both.models.VideoModel
import com.example.beautysync_kotlin.databinding.ActivityContentBinding
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit

@Suppress("DEPRECATION")
class Content : AppCompatActivity() {

    private lateinit var binding: ActivityContentBinding

    private var content: MutableList<VideoModel> = arrayListOf()
    private var beauticianOrUser = ""
    private var beauticianImageId = ""
    private lateinit var videoAdapter: VideoAdapter
    private lateinit var exoPlayer: ExoPlayer

    private val httpClient = OkHttpClient()
    private val mHandler: Handler = Handler(Looper.getMainLooper())

    private var isUser = ""



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityContentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        beauticianImageId = intent.getStringExtra("beautician_image_id").toString()
        beauticianOrUser = intent.getStringExtra("beautician_or_user").toString()
        content = intent.getParcelableArrayListExtra("content")!!
        isUser = intent.getStringExtra("is_user").toString()

        exoPlayer = ExoPlayer.Builder(this).build()
        videoAdapter = VideoAdapter(this, exoPlayer, content, "chef", beauticianImageId, isUser)
        binding.viewPager.adapter = videoAdapter
        videoAdapter.submitList(content, beauticianImageId)

        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        videoAdapter.notifyItemInserted(0)
        for (i in 1 until content.size) {
            videoAdapter.notifyItemInserted(i)
        }

//        if (beauticianOrUser == "Beautician") {
//            binding.deleteButton.isVisible = true
//        } else {
//            binding.deleteButton.isVisible = true
//        }

//        binding.deleteButton.setOnClickListener {
//            AlertDialog.Builder(this)
//                .setTitle("Delete Content Item")
//                .setMessage("Are your sure you want to delete this content item?")
//                // if the dialog is cancelable
//                .setCancelable(false)
//                .setPositiveButton("Yes") { dialog, _ ->
//
//                    dialog.dismiss()
//                }
//                .setNegativeButton("No") { dialog, _ ->
//                    dialog.dismiss()
//                }
//                .create()
//                .show()
//        }

    }



}