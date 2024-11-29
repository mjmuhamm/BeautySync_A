package com.example.beautysync_kotlin.user.fragments

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.beautysync_kotlin.R
import com.example.beautysync_kotlin.databinding.FragmentHomeBinding
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
 * Use the [Home.newInstance] factory method to
 * create an instance of this fragment.
 */
class Home : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding : FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    private lateinit var homeAdapter : HomeAdapter

    private var itemType = "hairCareItems"
    private var items: MutableList<ServiceItems> = arrayListOf()

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
        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        //warning free

        binding.serviceRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        homeAdapter = HomeAdapter(requireContext(), items)
        binding.serviceRecyclerView.adapter = homeAdapter
        loadItems(itemType)

        binding.hairCare.setOnClickListener {
            itemType = "hairCareItems"
            loadItems(itemType)
            binding.hairCare.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.secondary))
            binding.hairCare.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.skinCare.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.skinCare.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            binding.nailCare.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.nailCare.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
        }

        binding.skinCare.setOnClickListener {
            itemType = "skinCareItems"
            loadItems(itemType)
            binding.hairCare.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.hairCare.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            binding.skinCare.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.secondary))
            binding.skinCare.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.nailCare.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.nailCare.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
        }

        binding.nailCare.setOnClickListener {
            itemType = "nailCareItems"
            loadItems(itemType)
            binding.hairCare.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.hairCare.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            binding.skinCare.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.skinCare.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            binding.nailCare.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.secondary))
            binding.nailCare.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        }


        return binding.root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Home.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Home().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    @Suppress("UNCHECKED_CAST")
    private fun loadItems(itemType: String) {
        items.clear()
        homeAdapter.submitList(items)
        homeAdapter.notifyDataSetChanged()

        db.collection(itemType).get().addOnSuccessListener { documents ->
            if (documents != null) {
                for (doc in documents.documents) {
                    val data = doc.data

                    val itemTitle = data?.get("itemTitle") as String
                    val itemDescription = data["itemDescription"] as String
                    val itemPrice = data["itemPrice"] as String
                    val imageCount = data["imageCount"] as Number
                    val beauticianUsername = data["beauticianUsername"] as String
                    val beauticianPassion = data["beauticianPassion"] as String
                    val beauticianCity = data["beauticianCity"]  as String
                    val beauticianState = data["beauticianState"] as String
                    val beauticianImageId = data["beauticianImageId"] as String
                    val itemOrders = data["itemOrders"] as Number
                    val itemRating = data["itemRating"] as ArrayList<Number>
                    val hashtags = data["hashtags"] as ArrayList<String>
                    val liked = data["liked"] as ArrayList<String>

                    val x = ServiceItems(itemType, itemTitle, Uri.EMPTY, itemDescription, itemPrice, imageCount.toInt(), beauticianUsername, Uri.EMPTY, beauticianPassion, beauticianCity, beauticianState, beauticianImageId, liked, itemOrders.toInt(), itemRating, hashtags, doc.id)

                    if (items.size == 0) {
                        items.add(x)
                        homeAdapter.submitList(items)
                        homeAdapter.notifyItemInserted(0)
                    } else {
                        val index =
                            items.indexOfFirst { it.documentId == doc.id }
                        if (index == -1) {
                            items.add(x)
                            homeAdapter.submitList(items)
                            homeAdapter.notifyItemInserted(items.size - 1)
                        }
                    }
                }
            }
        }
    }
}