package com.capztone.seafishfy.ui.activities.ViewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capztone.seafishfy.ui.activities.models.MenuItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchViewModel : ViewModel() {
    val menuItemsLiveData: MutableLiveData<List<MenuItem>> = MutableLiveData()

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var database: FirebaseDatabase = FirebaseDatabase.getInstance()

    fun retrieveMenuItems() {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

        currentUserUid?.let { uid ->
            database = FirebaseDatabase.getInstance()
            val userLocationReference = database.reference.child("locations").child(uid)

            userLocationReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val shopNamesString = snapshot.child("shopname").getValue(String::class.java)
                    shopNamesString?.let { shopNames ->
                        val shopNamesList = shopNames.split(",").map { it.trim() }
                        shopNamesList.forEach { shopName ->
                            fetchMenuItemsForShop(shopName)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle onCancelled
                }
            })
        }
    }

    private fun fetchMenuItemsForShop(shopName: String) {
        val menuReferences = listOf(
            database.reference.child(shopName).child("menu"),
            database.reference.child(shopName).child("menu1"),
            database.reference.child(shopName).child("menu2")
        )



        viewModelScope.launch {
            val menuItems = mutableListOf<MenuItem>()
            withContext(Dispatchers.IO) {
                menuReferences.forEach { reference ->
                    reference.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for (foodSnapshot in snapshot.children) {
                                val menuItem = foodSnapshot.getValue(MenuItem::class.java)
                                menuItem?.let {
                                    menuItems.add(it)
                                }
                            }
                            menuItemsLiveData.postValue(menuItems.toList())
                        }

                        override fun onCancelled(error: DatabaseError) {
                            // Handle onCancelled
                        }
                    })
                }
            }
        }
    }
}
