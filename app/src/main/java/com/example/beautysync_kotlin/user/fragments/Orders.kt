package com.example.beautysync_kotlin.user.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.beautysync_kotlin.R
import com.example.beautysync_kotlin.databinding.FragmentOrdersBinding
import com.example.beautysync_kotlin.both.adapters.OrdersAdapter
import com.google.firebase.auth.FirebaseAuth
import com.example.beautysync_kotlin.both.models.Orders
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Orders.newInstance] factory method to
 * create an instance of this fragment.
 */
class Orders : Fragment() {

    private var _binding : FragmentOrdersBinding? = null
    private val binding get() = _binding!!

    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    private lateinit var ordersAdapter: OrdersAdapter
    private var orders : MutableList<Orders> = arrayListOf()

    private var item = "pending"

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

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
        _binding = FragmentOrdersBinding.inflate(inflater, container, false)

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        ordersAdapter = OrdersAdapter(requireContext(), orders, "User", "pending")
        binding.recyclerView.adapter = ordersAdapter

        loadOrders(item)

        binding.pending.setOnClickListener {
            item = "pending"

            binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
            ordersAdapter = OrdersAdapter(requireContext(), orders, "User", "pending")
            binding.recyclerView.adapter = ordersAdapter

            loadOrders(item)

            binding.pending.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.secondary))
            binding.pending.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.scheduled.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.scheduled.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            binding.complete.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.complete.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
        }

        binding.scheduled.setOnClickListener {
            item = "scheduled"

            binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
            ordersAdapter = OrdersAdapter(requireContext(), orders, "User", "scheduled")
            binding.recyclerView.adapter = ordersAdapter

            loadOrders(item)

            binding.pending.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.pending.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            binding.scheduled.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.secondary))
            binding.scheduled.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.complete.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.complete.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
        }

        binding.complete.setOnClickListener {
            item = "complete"

            binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
            ordersAdapter = OrdersAdapter(requireContext(), orders, "User", "complete")
            binding.recyclerView.adapter = ordersAdapter

            loadOrders(item)

            binding.pending.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.pending.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            binding.scheduled.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.scheduled.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
            binding.complete.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.secondary))
            binding.complete.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
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
         * @return A new instance of fragment Orders.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Orders().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Suppress("UNCHECKED_CAST")
    private fun loadOrders(item: String) {
        orders.clear()
        ordersAdapter.submitList(orders)
        ordersAdapter.notifyDataSetChanged()

        db.collection("User").document(auth.currentUser!!.uid).collection("Orders").addSnapshotListener { documents, _ ->
            if (documents != null) {
                for (doc in documents.documents) {
                    val data = doc.data

                    val itemType = data?.get("itemType") as String
                    val itemTitle = data["itemTitle"] as String
                    val itemDescription = data["itemDescription"] as String
                    val itemPrice = data["itemPrice"] as String
                    val imageCount = data["imageCount"] as Number
                    val beauticianUsername = data["beauticianUsername"] as String
                    val beauticianPassion = data["beauticianPassion"] as String
                    val beauticianCity = data["beauticianCity"] as String
                    val beauticianState = data["beauticianState"] as String
                    val beauticianImageId = data["beauticianImageId"] as String
                    val itemOrders = data["itemOrders"] as Number
                    val itemRating = data["itemRating"] as ArrayList<Number>
                    val hashtags = data["hashtags"] as ArrayList<String>
                    val liked = data["liked"] as ArrayList<String>
                    val streetAddress = data["streetAddress"] as String
                    val zipCode = data["zipCode"] as String
                    val eventDay = data["eventDay"] as String
                    val eventTime = data["eventTime"] as String
                    val notesToBeautician = data["notesToBeautician"] as String
                    val userImageId = data["userImageId"] as String
                    val status = data["status"] as String
                    val itemId = data["itemId"] as String
                    val userName = data["userName"] as String
                    val notifications = data["notifications"] as String

                    binding.progressBar.isVisible = false
                    if (status == item) {
                        val x = Orders(
                            itemType,
                            itemTitle,
                            itemDescription,
                            itemPrice,
                            imageCount,
                            beauticianUsername,
                            beauticianPassion,
                            beauticianCity,
                            beauticianState,
                            beauticianImageId,
                            liked,
                            itemOrders,
                            itemRating,
                            hashtags,
                            doc.id,
                            eventDay,
                            eventTime,
                            streetAddress,
                            zipCode,
                            notesToBeautician,
                            userImageId,
                            userName,
                            status,
                            notifications,
                            itemId
                        )

                        if (orders.size == 0) {
                            orders.add(x)
                            ordersAdapter.submitList(orders)
                            ordersAdapter.notifyItemInserted(0)
                        } else {
                            val index =
                                orders.indexOfFirst { it.documentId == doc.id }
                            if (index == -1) {
                                orders.add(x)
                                ordersAdapter.submitList(orders)
                                ordersAdapter.notifyItemInserted(orders.size - 1)
                            }
                        }
                    }
                }
            }
        }
    }
}