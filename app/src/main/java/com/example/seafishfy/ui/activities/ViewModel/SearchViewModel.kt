package com.example.seafishfy.ui.activities.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.seafishfy.ui.activities.models.MenuItem
import com.google.firebase.database.*

class SearchViewModel : ViewModel() {

    private val _menuItems = MutableLiveData<List<MenuItem>>()
    val menuItems: LiveData<List<MenuItem>> = _menuItems

    private lateinit var database: FirebaseDatabase
    private val originalMenuItems = mutableListOf<MenuItem>()

    fun retrieveMenuItems() {
        database = FirebaseDatabase.getInstance()
        val foodReferencer1: DatabaseReference = database.reference.child("menu")
        val foodReferencer2: DatabaseReference = database.reference.child("menu1")
        val foodReferencer3: DatabaseReference = database.reference.child("menu2")

        val menuReferences = listOf(foodReferencer1, foodReferencer2, foodReferencer3)

        menuReferences.forEach { reference ->
            reference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (foodSnapshot in snapshot.children) {
                        val menuItem = foodSnapshot.getValue(MenuItem::class.java)
                        menuItem?.let {
                            originalMenuItems.add(it)
                        }
                    }
                    _menuItems.value = originalMenuItems.toList()
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle onCancelled
                }
            })
        }
    }

    fun filterMenuItems(query: String) {
        val filteredMenuItem = originalMenuItems.filter {
            it.foodName?.contains(query, ignoreCase = true) == true
        }
        _menuItems.value = filteredMenuItem
    }
}
