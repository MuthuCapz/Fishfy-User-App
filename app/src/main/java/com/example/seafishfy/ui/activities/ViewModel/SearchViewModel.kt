package com.example.seafishfy.ui.activities.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.seafishfy.ui.activities.models.MenuItem
import com.google.firebase.database.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

        CoroutineScope(Dispatchers.IO).launch {
            menuReferences.forEach { reference ->
                reference.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val items = mutableListOf<MenuItem>()
                        for (foodSnapshot in snapshot.children) {
                            val menuItem = foodSnapshot.getValue(MenuItem::class.java)
                            menuItem?.let {
                                items.add(it)
                            }
                        }
                        originalMenuItems.addAll(items)
                        _menuItems.postValue(originalMenuItems.toList())
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle onCancelled
                    }
                })
            }
        }
    }

    fun filterMenuItems(query: String) {
        CoroutineScope(Dispatchers.Default).launch {
            val filteredMenuItem = originalMenuItems.filter {
                it.foodName?.contains(query, ignoreCase = true) == true
            }
            _menuItems.postValue(filteredMenuItem)
        }
    }
}
