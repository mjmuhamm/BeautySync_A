package com.example.beautysync_kotlin.beautician.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.beautysync_kotlin.R
import com.example.beautysync_kotlin.beautician.adapters.MeAdapter
import com.example.beautysync_kotlin.beautician.misc.AccountSettings
import com.example.beautysync_kotlin.beautician.misc.ServiceItemAdd
import com.example.beautysync_kotlin.databinding.FragmentMe2Binding
import com.example.beautysync_kotlin.user.adapters.HomeAdapter
import com.example.beautysync_kotlin.user.models.ServiceItems
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Me.newInstance] factory method to
 * create an instance of this fragment.
 */
class Me : Fragment() {

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding : FragmentMe2Binding? = null
    private val binding get() = _binding!!

    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()
    private val storage = Firebase.storage

    private var city = ""
    private var state = ""

    private var itemType = "hairCareItems"
    private val items : MutableList<ServiceItems> = arrayListOf()

    private lateinit var meAdapter : MeAdapter


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
        _binding = FragmentMe2Binding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        binding.serviceRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        meAdapter = MeAdapter(requireContext(), items, itemType, "no")
        binding.serviceRecyclerView.adapter = meAdapter

        loadItemInfo(itemType)

        binding.settingsButton.setOnClickListener {
            val intent = Intent(requireContext(), AccountSettings::class.java)
            startActivity(intent)
        }

        binding.addItemButton.setOnClickListener {
            val intent = Intent(requireContext(), ServiceItemAdd::class.java)
            intent.putExtra("item_type", itemType)
            intent.putExtra("new_or_edit", "new")
            intent.putExtra("beautician_username", binding.userName.text.toString())
            intent.putExtra("beautician_passion", binding.passion.text.toString())
            intent.putExtra("beautician_city", city)
            intent.putExtra("beautician_state", state)
            startActivity(intent)

        }
        binding.hairCare.setOnClickListener {
            itemType = "hairCareItems"
            loadItemInfo(itemType)
            binding.hairCare.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.secondary))
            binding.hairCare.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.skinCare.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.skinCare.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            binding.nailCare.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.nailCare.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
        }

        binding.skinCare.setOnClickListener {
            itemType = "skinCareItems"
            loadItemInfo(itemType)
            binding.hairCare.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.hairCare.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            binding.skinCare.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.secondary))
            binding.skinCare.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.nailCare.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.nailCare.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
        }

        binding.nailCare.setOnClickListener {
            itemType = "nailCareItems"
            loadItemInfo(itemType)
            binding.hairCare.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.hairCare.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            binding.skinCare.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.skinCare.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            binding.nailCare.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.secondary))
            binding.nailCare.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        }

        loadHeadingInfo()
        return binding.root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Me.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Me().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    @SuppressLint("SetTextI18n")
    private fun loadHeadingInfo() {
        db.collection("Beautician").document(auth.currentUser!!.uid).collection("PersonalInfo").get().addOnSuccessListener { documents ->
            if (documents != null) {
                for (doc in documents.documents) {
                    val data = doc.data

                    val userName = data?.get("userName") as String
                    binding.userName.text = "@$userName"
                }
            }
        }

        db.collection("Beautician").document(auth.currentUser!!.uid).collection("BusinessInfo").get().addOnSuccessListener { documents ->
            if (documents != null) {
                for (doc in documents.documents) {
                    val data = doc.data

                    val passion = data?.get("passion") as String
                    val city = data["city"] as String
                    val state = data["state"] as String

                    this.city = city
                    this.state = state
                    binding.passion.text = passion
                    binding.location.text ="Location: $city, $state"
                }
            }
        }

        storage.reference.child("beauticians/${auth.currentUser!!.uid}/profileImage/${auth.currentUser!!.uid}.png").downloadUrl.addOnSuccessListener { itemUri ->
            Glide.with(requireContext()).load(itemUri).placeholder(R.drawable.default_profile).into(binding.userImage)
        }
    }
    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    @Suppress("UNCHECKED_CAST")
    private fun loadItemInfo(item: String) {
        items.clear()
        meAdapter.submitList(items)
        meAdapter.notifyDataSetChanged()

        db.collection("Beautician").document(auth.currentUser!!.uid).collection(item).get().addOnSuccessListener { documents ->
            if (documents != null) {
                for (doc in documents.documents) {
                    val data = doc.data

                    val itemType = data?.get("itemType") as String
                    val itemTitle = data["itemTitle"] as String
                    val itemDescription = data["itemDescription"] as String
                    val itemPrice = data["itemPrice"] as String
                    val imageCount = data["imageCount"] as Number
                    val hashtags = data["hashtags"] as ArrayList<String>

                    db.collection(item).document(doc.id).get().addOnSuccessListener { document ->
                        if (document != null) {
                            val data1 = doc.data

                            val liked = data1?.get("liked") as ArrayList<String>
                            val itemOrders = data1["itemOrders"] as Number
                            val itemRating = data1["itemRating"] as ArrayList<Number>

                            val x = ServiceItems(itemType, itemTitle, Uri.EMPTY, itemDescription, itemPrice, imageCount.toInt(), binding.userName.text.toString(), Uri.EMPTY, binding.passion.text.toString(), city, state, auth.currentUser!!.uid, liked, itemOrders.toInt(), itemRating, hashtags, doc.id)

                            if (items.size == 0) {
                                items.add(x)
                                meAdapter.submitList(items)
                                meAdapter.notifyItemInserted(0)
                            } else {
                                val index =
                                    items.indexOfFirst { it.documentId == doc.id }
                                if (index == -1) {
                                    items.add(x)
                                    meAdapter.submitList(items)
                                    meAdapter.notifyItemInserted(items.size - 1)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}