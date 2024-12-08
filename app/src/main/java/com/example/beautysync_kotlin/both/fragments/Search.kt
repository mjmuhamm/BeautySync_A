package com.example.beautysync_kotlin.both.fragments

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.beautysync_kotlin.both.adapters.SearchAdapter
import com.example.beautysync_kotlin.databinding.FragmentSearchBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.util.Locale

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Search.newInstance] factory method to
 * create an instance of this fragment.
 */
private const val TAG = "Search"
class Search : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val db = Firebase.firestore
    private val storage = Firebase.storage
    private lateinit var searchAdapter: SearchAdapter
    private var searchItems: MutableList<com.example.beautysync_kotlin.both.models.Search> = ArrayList()

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
        _binding = FragmentSearchBinding.inflate(inflater, container, false)

        searchAdapter = SearchAdapter(requireContext(), searchItems)
        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = searchAdapter


        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText)
                return true
            }

        })

        loadUsers()

        // Inflate the layout for this fragment
        return binding.root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Search.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Search().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun filterList(query: String?) {
        if (query != null) {
            val filteredList = ArrayList<com.example.beautysync_kotlin.both.models.Search>()
            Log.d(TAG, "filterList: search items count ${searchItems.size}")
            for (i in searchItems) {
                if (i.userName.lowercase(Locale.ROOT).contains(query) || i.userFullName.lowercase(Locale.ROOT).contains(query)) {
                    filteredList.add(i)
                }

                if (filteredList.isEmpty()) {
                    Toast.makeText(requireContext(), "No users with those initials.", Toast.LENGTH_SHORT).show()
                } else {
                    searchAdapter.submitList(filteredList)
                    searchAdapter.notifyDataSetChanged()
                }
            }
            loadUsers()
        }
    }

    private fun loadUsers() {
        val storageRef = storage.reference

        db.collection("Usernames").get().addOnSuccessListener { documents ->
            if (documents != null) {
                for (doc in documents.documents) {
                    if (searchItems.size != documents.documents.size) {
                        val data = doc.data

                        val beauticianOrUser = data?.get("beauticianOrUser") as String
                        val email = data["email"] as String
                        val fullName = data["fullName"] as String
                        val username = data["userName"] as String


                        val x = if (beauticianOrUser == "Beautician") { "beauticians" } else { "users" }
                        Log.d(TAG, "loadUsers: x $x")

                        if (searchItems.size == 0) {
                            searchItems.add(com.example.beautysync_kotlin.both.models.Search(beauticianOrUser, Uri.EMPTY, doc.id, username, email, fullName, doc.id))
                            searchAdapter.submitList(searchItems)
                            searchAdapter.notifyItemInserted(0)
                        } else {
                            val index =
                                searchItems.indexOfFirst { it.documentId == doc.id }
                            if (index == -1) {
                                searchItems.add(com.example.beautysync_kotlin.both.models.Search(beauticianOrUser, Uri.EMPTY, doc.id, username, email, fullName, doc.id))
                                searchAdapter.submitList(searchItems)
                                searchAdapter.notifyItemInserted(searchItems.size - 1)
                            }
                        }

                        Log.d(TAG, "loadUsers: users $searchItems")
                    }


                }
            }
        }

    }

}