package com.example.beautysync_kotlin.both.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.media3.exoplayer.ExoPlayer
import com.example.beautysync_kotlin.R
import com.example.beautysync_kotlin.both.adapters.VideoAdapter
import com.example.beautysync_kotlin.both.models.VideoModel
import com.example.beautysync_kotlin.databinding.FragmentFeedBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.ArrayList
import java.util.concurrent.TimeUnit

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Feed.newInstance] factory method to
 * create an instance of this fragment.
 */
private const val TAG = "Feed"
class Feed : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding!!

    private var backEndUrl = "https://beautysync-videoserver.onrender.com/get-videos"
    private val httpClient = OkHttpClient()

    private val mHandler: Handler = Handler(Looper.getMainLooper())

    private var content: MutableList<VideoModel> = arrayListOf()
    private var chefOrFeed = ""
    private lateinit var videoAdapter: VideoAdapter
    private lateinit var exoPlayer: ExoPlayer

    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFeedBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        exoPlayer = ExoPlayer.Builder(requireContext()).build()
        videoAdapter = VideoAdapter(requireContext(), exoPlayer, content, FirebaseAuth.getInstance().currentUser!!.displayName!!, "", "")
        binding.viewPager.adapter = videoAdapter

        loadVideos()
        return binding.root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Feed.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Feed().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
    override fun onPause() {
        super.onPause()
        binding.viewPager.adapter = null
    }

    override fun onStop() {
        super.onStop()
        binding.viewPager.adapter = null
    }

    override fun onDetach() {
        super.onDetach()
        binding.viewPager.adapter = null
    }

    override fun onResume() {
        super.onResume()
        videoAdapter = VideoAdapter(requireContext(), exoPlayer, content, FirebaseAuth.getInstance().currentUser!!.displayName!!, "", "")
        binding.viewPager.adapter = videoAdapter
    }

    private var createdAt = 0
    private fun loadVideos() {

        val client = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS) // Connect timeout
            .readTimeout(20, TimeUnit.SECONDS) // Read timeout
            .writeTimeout(20, TimeUnit.SECONDS) // Write timeout
            .build()

        Log.d(TAG, "loadVideos: $createdAt")
        val body = FormBody.Builder()
            .add("created_at", createdAt.toString())
            .build()

        val request = Request.Builder()
            .url(backEndUrl)
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .post(body)
            .build()

        client.newCall(request)
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {

                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        val responseData = response.body!!.string()
                        Log.d(TAG, "onResponse: response data $responseData")
                        val responseJson =
                            JSONObject(responseData)

                        val videos = responseJson.getJSONArray("videos")


                        mHandler.post {
                            for (i in 0 until videos.length()) {

                                if (videos.getJSONObject(i)["name"].toString().substring(videos.getJSONObject(i)["name"].toString().length - 1, videos.getJSONObject(i)["name"].toString().length) == "b") {
                                    val id =
                                        videos.getJSONObject(i)["id"].toString()
                                    val createdAtI = videos.getJSONObject(i)["createdAt"].toString()
                                    if (i == videos.length() - 1) {
                                        createdAt = createdAtI.toInt()
                                    }
                                    var views = 0
                                    var liked = arrayListOf<String>()
                                    var comments = 0
                                    var shared = 0

                                    val name = videos.getJSONObject(i)["name"].toString().substring(0, videos.getJSONObject(i)["name"].toString().length - 1)


                                    if (videos.getJSONObject(i)["name"].toString() != "sample" && videos.getJSONObject(
                                            i
                                        )["name"].toString() != "sample1"
                                    ) {

                                        db.collection("Content").document(id).get()
                                            .addOnSuccessListener { document ->
                                                if (document.exists()) {
                                                    val data = document.data

                                                    if (data?.get("views") != null) {
                                                        val viewsI = data["views"] as Number
                                                        views = viewsI.toInt()
                                                    }
                                                    if (data?.get("comments") != null) {
                                                        val commentsI = data["comments"] as Number
                                                        comments = commentsI.toInt()
                                                    }

                                                    if (data?.get("liked") != null) {
                                                        val likedI =
                                                            data["liked"] as ArrayList<String>
                                                        liked = likedI
                                                    }

                                                    if (data?.get("shared") != null) {
                                                        val sharedI = data["shared"] as Number
                                                        shared = sharedI.toInt()
                                                    }
                                                }
                                            }


                                        val newVideo = VideoModel(
                                            videos.getJSONObject(i)["dataUrl"].toString(),
                                            id,
                                            createdAtI,
                                            name,
                                            videos.getJSONObject(i)["description"].toString(),
                                            views,
                                            liked,
                                            comments,
                                            shared,
                                            videos.getJSONObject(i)["thumbnailUrl"].toString()
                                        )

                                        if (content.isEmpty()) {
                                            content.add(newVideo)
                                            content.shuffle()
                                            videoAdapter.submitList(content, "")
                                            videoAdapter.notifyItemInserted(0)
                                        } else {
                                            val index = content.indexOfFirst { it.id == id }
                                            if (index == -1) {
                                                content.add(newVideo)
                                                content.shuffle()
                                                videoAdapter.submitList(content, "")
                                                videoAdapter.notifyItemInserted(content.size - 1)
                                            }
                                        }
                                    }

                                }
                                // Set up PaymentConfiguration with your Stripe publishable key
                            }
                        }
                    }
                }
            })
    }
}