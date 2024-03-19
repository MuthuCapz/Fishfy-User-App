package com.example.seafishfy.ui.activities.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.seafishfy.databinding.FragmentMenuBottomSheetBinding
import com.example.seafishfy.ui.activities.adapters.SearchAdapter
import com.example.seafishfy.ui.activities.models.MenuItem
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.database.*

class MenuBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentMenuBottomSheetBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var menuItems: MutableList<MenuItem>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMenuBottomSheetBinding.inflate(inflater, container, false)

        binding.backButton.setOnClickListener {
            dismiss()
        }

        retrieveMenuItems()


        return binding.root
    }

    private fun retrieveMenuItems() {
        database = FirebaseDatabase.getInstance()
        val menuReferences = listOf("menu","menu1", "menu2")

        menuItems = mutableListOf()

        menuReferences.forEach { menuRef ->
            val foodRef: DatabaseReference = database.reference.child(menuRef)
            foodRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (foodSnapshot in snapshot.children) {
                        val menuItem = foodSnapshot.getValue(MenuItem::class.java)
                        menuItem?.let {
                            menuItems.add(it)
                        }
                    }
                    Log.d("ITEMS", "onDataChange: Data Received")
                    // Once data received, set the adapter
                    setAdapter()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Data Not Fetching", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun setAdapter() {
        if (menuItems.isNotEmpty()) {
            val adapter = SearchAdapter(menuItems, requireContext())
            binding.menuBottomSheetRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            binding.menuBottomSheetRecyclerView.adapter = adapter
            Log.d("ITEMS", "setAdapter: Data set")
        } else {
            Log.d("ITEMS", "setAdapter: Data Not set")
        }
    }


}
