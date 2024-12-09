package com.example.beautysync_kotlin.both.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.beautysync_kotlin.R
import com.example.beautysync_kotlin.beautician.MainActivity
import com.example.beautysync_kotlin.both.models.VideoModel
import com.example.beautysync_kotlin.databinding.ItemVideoBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID
import java.util.concurrent.TimeUnit

private const val TAG = "VideoAdapter"
class VideoAdapter(private var context: Context, private var exoPlayer: ExoPlayer, private var videos: MutableList<VideoModel>, private var beauticianOrUser: String, private var beauticianImageId: String, private var beauticianView: String) : RecyclerView.Adapter<VideoAdapter.ViewHolder>() {

    private val db = Firebase.firestore
    private val user = FirebaseAuth.getInstance().currentUser!!.uid

    private val httpClient = OkHttpClient()
    private val mHandler: Handler = Handler(Looper.getMainLooper())

    @SuppressLint("SimpleDateFormat")
    private var sdf = SimpleDateFormat("MM-dd-yyyy hh:mm:ss.SSS a")



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemVideoBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    @SuppressLint("InflateParams", "SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(videos[position])

        val video = videos[position]




        if (beauticianView == "yes") { holder.deleteButton.visibility = View.VISIBLE } else { holder.deleteButton.visibility = View.GONE }
        holder.backButton.visibility = View.GONE

        Log.d(TAG, "onBindViewHolder: video ${video}")

        val docRef = db.collection("Videos").document(video.id)



        holder.commentButton.setOnClickListener {

//            bottomSheetDialog.show()
        }



        holder.deleteButton.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Delete Image")
                .setMessage("Are you sure you want to delete this video?")
                // if the dialog is cancelable
                .setCancelable(false)
                .setPositiveButton("Yes") { dialog, _ ->

                    deleteContent(video.id)
                    dialog.dismiss()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
        }


        holder.playPauseButton.setOnClickListener {
            if (exoPlayer.isPlaying) {
                exoPlayer.pause()
                holder.playImage.visibility = View.VISIBLE
            } else { exoPlayer.play()
                holder.playImage.visibility = View.GONE }
        }


        holder.likeLayout.setOnClickListener {
            holder.likeLayout.isSelected = !holder.likeLayout.isSelected
            if (holder.likeLayout.isSelected) {
                holder.likeButton.setImageResource(R.drawable.video_heart_filled)
                holder.likeText.text = "${holder.likeText.text.toString().toInt() + 1}"
                val data: Map<String, Any> = hashMapOf("liked" to FieldValue.arrayUnion(user))
                db.collection("Videos").document(video.id).update(data)
            } else {
                holder.likeButton.setImageResource(R.drawable.video_heart_unfilled)
                holder.likeText.text = "${holder.likeText.text.toString().toInt() - 1}"
                val data: Map<String, Any> = hashMapOf("liked" to FieldValue.arrayRemove(user))
                db.collection("Videos").document(video.id).update(data)
            }
        }



    }


    private fun displayAlert(
        message: String
    ) {
        val builder = AlertDialog.Builder(context)
            .setTitle("Failed to load page.")
            .setMessage(message)

        builder.setPositiveButton("Ok", null)
        builder
            .create()
            .show()
    }

    override fun onViewAttachedToWindow(holder: ViewHolder) {
        super.onViewAttachedToWindow(holder)
        exoPlayer = ExoPlayer.Builder(context).build()

        holder.playImage.isVisible = false
        holder.exoPlayer.player = exoPlayer

        exoPlayer.setMediaItem(MediaItem.fromUri(Uri.parse(videos[holder.absoluteAdapterPosition].dataUri)))
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
        exoPlayer.play()
        exoPlayer.repeatMode = Player.REPEAT_MODE_ONE





    }

    override fun onViewDetachedFromWindow(holder: ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.exoPlayer.player!!.pause()

    }

    override fun getItemCount() = videos.size

    fun submitList(content: MutableList<VideoModel>, beauticianImageId: String) {
        this.videos = content
        this.beauticianImageId = beauticianImageId

    }

    private fun deleteContent(id: String) {

        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra("where_to", "me")

        val client = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS) // Connect timeout
            .readTimeout(20, TimeUnit.SECONDS) // Read timeout
            .writeTimeout(20, TimeUnit.SECONDS) // Write timeout
            .build()

        val body = FormBody.Builder()
            .add("entryId", id)
            .build()

        val request = Request.Builder()
            .url("http://beautysync-videoserver.onrender.com/delete-video")
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .post(body)
            .build()

        client.newCall(request)
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {

                }

                @SuppressLint("SetTextI18n")
                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {

                        mHandler.post {

                            Toast.makeText(context, "Content Deleted.", Toast.LENGTH_LONG).show()
                            context.startActivity(intent)


                        }
                    }
                }
            })
    }

    class ViewHolder(itemView: ItemVideoBinding) : RecyclerView.ViewHolder(itemView.root) {

        var exoPlayer = itemView.exoPlayer
        var playImage = itemView.playImage
        var playPauseButton = itemView.playPauseButton
        val likeButton = itemView.likeButton
        val likeText = itemView.likeText
        var likeLayout = itemView.likeLayout
        val user = itemView.user
        val description = itemView.description
        val commentButton = itemView.commentButton
        val commentText = itemView.commentText
        val shareButton = itemView.shareButton
        val shareText = itemView.shareText
        val backButton = itemView.backButton
        val deleteButton = itemView.deleteButton


        @SuppressLint("SetTextI18n")
        fun bind(video: VideoModel) {

            val db = Firebase.firestore

            user.text = video.user
            description.text = video.description

            likeText.text = "${video.liked.size}"
            commentText.text = "${video.comments}"
            shareText.text = "${video.shared}"

            val data : Map<String, Any> = hashMapOf("views" to video.views.toInt() + 1)
            db.collection("Videos").document(video.id).update(data)



        }
    }
}