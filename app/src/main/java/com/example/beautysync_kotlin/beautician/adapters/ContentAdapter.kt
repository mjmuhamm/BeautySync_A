package com.example.beautysync_kotlin.beautician.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.beautysync_kotlin.R
import com.example.beautysync_kotlin.beautician.misc.Content
import com.example.beautysync_kotlin.both.models.VideoModel
import com.google.firebase.auth.FirebaseAuth

private const val TAG = "ContentAdapter"
class ContentAdapter(private var context: Context, private var content: MutableList<VideoModel>, private var beauticianImageId: String) : BaseAdapter() {

    override fun getCount() = content.size

    override fun getItem(position: Int) = content[position]

    override fun getItemId(position: Int) = position.toLong()
    private val auth = FirebaseAuth.getInstance()

    fun submitList(content: MutableList<VideoModel>, beauticianImageId: String) {
        this.content = content
        this.beauticianImageId = beauticianImageId
    }

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, p1: View?, p2: ViewGroup?): View {

        val view: View = View.inflate(context, R.layout.content_post, null)
        val videoView : ImageView = view.findViewById(R.id.video_view)
        val videoViews : TextView = view.findViewById(R.id.video_views)

        Glide.with(context).load(content[position].dataUri).centerCrop().into(videoView)

        videoView.setOnClickListener {
            Log.d(TAG, "getView: happening")
            val cont : ArrayList<VideoModel> = ArrayList()
            cont.add(content[position])
            for (i in 0 until content.size) {
                if (content[position].id != content[i].id) {
                    cont.add(content[i])
                }
            }
            val intent = Intent(context, Content::class.java)
            intent.putExtra("beautician_or_user", "chef")
            intent.putExtra("beautician_image_id", beauticianImageId)
            if (beauticianImageId == auth.currentUser!!.uid) { intent.putExtra("is_user", "yes") } else { intent.putExtra("is_user", "no") }
            intent.putExtra("content", cont)
            context.startActivity(intent)
        }

        videoViews.text = "${content[position].views}"

        return view
    }
}