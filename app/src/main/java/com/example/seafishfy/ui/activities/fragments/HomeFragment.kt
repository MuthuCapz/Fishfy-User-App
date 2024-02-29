package com.example.seafishfy.ui.activities.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.interfaces.ItemClickListener
import com.denzcoskun.imageslider.models.SlideModel
import com.example.seafishfy.databinding.FragmentHomeBinding
import com.example.seafishfy.ui.activities.adapters.MenuAdapter
import com.example.seafishfy.ui.activities.models.MenuItem
import com.google.firebase.database.*

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var menuItems: MutableList<MenuItem>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        database = FirebaseDatabase.getInstance()

        retrieveAndDisplayPopularItems()
        retrieveAndDisplayMenu1Items()

        return binding.root
    }

    private fun retrieveAndDisplayPopularItems() {
        val foodRef: DatabaseReference = database.reference.child("menu")
        menuItems = mutableListOf()

        foodRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (foodSnapshot in snapshot.children) {
                    val menuItem = foodSnapshot.getValue(MenuItem::class.java)
                    menuItem?.let {
                        menuItems.add(it)
                    }
                }
                Log.d("ITEMS", "onDataChange : Data Received")
                randomPopularItems()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Data Not Fetching", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun retrieveAndDisplayMenu1Items() {
        val menu1Ref: DatabaseReference = database.reference.child("menu1")
        val menu1Items = mutableListOf<MenuItem>()

        menu1Ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (menu1Snapshot in snapshot.children) {
                    val menuItem = menu1Snapshot.getValue(MenuItem::class.java)
                    menuItem?.let {
                        menu1Items.add(it)
                    }
                }
                Log.d("MENU1_ITEMS", "onDataChange : Data Received")
                setMenu1ItemAdapter(menu1Items)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Data Not Fetching", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun randomPopularItems() {
        val index = menuItems.indices.toList().shuffled()
        val numItemToShow = 6
        val subsetMenuItems = index.take(numItemToShow).map { menuItems[it] }
        setPopularItemAdapter(subsetMenuItems)
    }

    private fun setPopularItemAdapter(subsetMenuItems: List<MenuItem>) {
        val adapter = MenuAdapter(subsetMenuItems, requireContext())
        binding.popularRecyclerView.layoutManager = GridLayoutManager(requireContext(), 5)
        binding.popularRecyclerView.adapter = adapter
    }

    private fun setMenu1ItemAdapter(menu1Items: List<MenuItem>) {
        val adapter = MenuAdapter(menu1Items, requireContext())
        binding.popularRecyclerView1.layoutManager = GridLayoutManager(requireContext(), 5)
        binding.popularRecyclerView1.adapter = adapter
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupImageSlider()
    }

    private fun setupImageSlider() {
        val imageList = ArrayList<SlideModel>()
        // Add your image resources here

    }
}
