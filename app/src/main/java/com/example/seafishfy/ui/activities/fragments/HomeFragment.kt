package com.example.seafishfy.ui.activities.fragments

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.seafishfy.R
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.interfaces.ItemClickListener
import com.denzcoskun.imageslider.models.SlideModel

import com.example.seafishfy.databinding.FragmentHomeBinding
import com.example.seafishfy.ui.activities.ContactusActivity
import com.example.seafishfy.ui.activities.Discount
import com.example.seafishfy.ui.activities.LocationActivity
import com.example.seafishfy.ui.activities.adapters.DiscountAdapter
import com.example.seafishfy.ui.activities.adapters.MenuAdapter
import com.example.seafishfy.ui.activities.models.DiscountItem
import com.example.seafishfy.ui.activities.models.MenuItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var menuItems: MutableList<MenuItem>
    private lateinit var databaseReference: DatabaseReference



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        databaseReference = FirebaseDatabase.getInstance().getReference("locations")

        // Retrieve and display address and locality
        retrieveAddressAndLocality()
        binding.menuItemTextView.setOnClickListener {
            val bottomSheetDialog = MenuBottomSheetFragment()
            bottomSheetDialog.show(parentFragmentManager, "Test")
        }
        binding.dropdown.setOnClickListener {
            showPopupMenu(it)
        }
        binding.dotsMenu.setOnClickListener {
            showPopupMenus(it)
        }
        database = FirebaseDatabase.getInstance()

        retrieveAndDisplayPopularItems()
        retrieveAndDisplayMenu1Items()
        retrieveAndDisplayMenu3Items()
        retrieveAndDisplayDiscountItems()

        return binding.root
    }


    private fun retrieveAddressAndLocality() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userId?.let { uid ->
            val userLocationRef = databaseReference.child(userId)
            userLocationRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val address = snapshot.child("address").getValue(String::class.java)
                        val locality = snapshot.child("locality").getValue(String::class.java)

                        // Update TextViews with retrieved data
                        binding.tvAddress.text = address
                        binding.tvLocality.text = locality
                    } else {
                        // Handle case where data doesn't exist
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
        }
    }

    private fun showPopupMenus(view: View) {
        val popupMenus = PopupMenu(requireContext(), view)
        popupMenus.menuInflater.inflate(R.menu.option_menu, popupMenus.menu)

        // Set text color for the PopupMenu items
        val menus = popupMenus.menu


        popupMenus.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.about -> {
                    // Handle "Use Current Location" click

                    val intent = Intent(requireContext(), ContactusActivity::class.java)
                    startActivity(intent)
                    // Hide the dropdown icon after picking an address
                    binding.dropdown.visibility = View.GONE
                    // Hide the dropdown icon after picking an address

                    true
                }

                // Add more saved address clicks as needed
                else -> false
            }
        }

        popupMenus.setOnDismissListener {
            // Dismiss the PopupMenu when it's dismissed
            popupMenus.dismiss()
        }

        popupMenus.show()
    }

    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menuInflater.inflate(R.menu.address, popupMenu.menu)

        // Set text color for the PopupMenu items
        val menu = popupMenu.menu
        for (i in 0 until menu.size()) {
            val menuItem = menu.getItem(i)
            val spannableString = SpannableString(menuItem.title.toString())
            spannableString.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.navy)),
                0,
                spannableString.length,
                0
            )
            menuItem.title = spannableString
        }

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.use_current_location -> {
                    // Handle "Use Current Location" click
                    val intent = Intent(requireContext(), LocationActivity::class.java)
                    startActivity(intent)
                    // Hide the dropdown icon after picking an address
                    binding.dropdown.visibility = View.GONE
                    true
                }

                // Add more saved address clicks as needed
                else -> false
            }
        }

        popupMenu.setOnDismissListener {
            // Dismiss the PopupMenu when it's dismissed
            popupMenu.dismiss()
        }

        popupMenu.show()
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

    private fun retrieveAndDisplayMenu3Items() {
        val menu3Ref: DatabaseReference = database.reference.child("menu2")
        val menu3Items = mutableListOf<MenuItem>()

        menu3Ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (menu3Snapshot in snapshot.children) {
                    val menuItem = menu3Snapshot.getValue(MenuItem::class.java)
                    menuItem?.let {
                        menu3Items.add(it)
                    }
                }
                Log.d("MENU3_ITEMS", "onDataChange : Data Received")
                setMenu3ItemAdapter(menu3Items)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Data Not Fetching", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun retrieveAndDisplayDiscountItems() {
        val discountRef: DatabaseReference = database.reference.child("discount")
        val discountItems = mutableListOf<DiscountItem>()

        discountRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (discountSnapshot in snapshot.children) {
                    val menuItem = discountSnapshot.getValue(DiscountItem::class.java)
                    menuItem?.let {
                        discountItems.add(it)
                    }
                }
                Log.d("DISCOUNT_ITEMS", "onDataChange : Data Received")
                setDiscountItemAdapter(discountItems)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Discount Data Not Fetching", Toast.LENGTH_SHORT).show()
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
        binding.popularRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.popularRecyclerView.adapter = adapter
    }

    private fun setMenu1ItemAdapter(subsetMenuItems: List<MenuItem>) {
        val adapter = MenuAdapter(subsetMenuItems, requireContext())
        binding.popularRecyclerView1.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.popularRecyclerView1.adapter = adapter
    }

    private fun setMenu3ItemAdapter(subsetMenuItems: List<MenuItem>) {
        val adapter = MenuAdapter(subsetMenuItems, requireContext())
        binding.popularRecyclerView3.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.popularRecyclerView3.adapter = adapter
    }
    private fun setDiscountItemAdapter(discountItems: MutableList<DiscountItem>) {
        val adapter = DiscountAdapter(discountItems, requireContext())
        binding.discountRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.discountRecyclerView.adapter = adapter
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupImageSlider()
    }
    private fun setupImageSlider() {
        val imageList = ArrayList<SlideModel>()
        // Add your image resources here
        imageList.add(SlideModel(R.drawable.ban, scaleType = ScaleTypes.FIT))
        imageList.add(SlideModel(R.drawable.banner1, scaleType = ScaleTypes.FIT))
        imageList.add(SlideModel(R.drawable.banner2, scaleType = ScaleTypes.FIT))
        imageList.add(SlideModel(R.drawable.discountpic, scaleType = ScaleTypes.FIT))

        val imageSlide = binding.imageSlider
        imageSlide.setImageList(imageList)

        imageSlide.setItemClickListener(object : ItemClickListener {
            override fun doubleClick(position: Int) {
                // Double click listener implementation
            }

            override fun onItemSelected(position: Int) {
                // Check if the selected image is "ban"
                if (position == 3) { // Assuming "ban" image is at position 0
                    // Launch the DiscountActivity
                    val intent = Intent(requireContext(), Discount::class.java)
                    startActivity(intent)
                } else {
                    val itemMessage = "Selected Image $position"
                    Toast.makeText(requireContext(), itemMessage, Toast.LENGTH_SHORT).show()
                }
            }
        })
    }




}