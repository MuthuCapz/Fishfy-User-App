package com.example.seafishfy.ui.activities.fragments
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.seafishfy.databinding.FragmentHistoryBinding
import com.example.seafishfy.ui.activities.adapters.RecentBuyAdapter
import com.example.seafishfy.ui.activities.models.Order
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase

class HistoryFragment : Fragment() {

 private lateinit var binding: FragmentHistoryBinding
 private lateinit var adapter: RecentBuyAdapter
 private lateinit var database: DatabaseReference
 private lateinit var auth: FirebaseAuth

 override fun onCreateView(
  inflater: LayoutInflater, container: ViewGroup?,
  savedInstanceState: Bundle?
 ): View {
  // Inflate the layout for this fragment
  binding = FragmentHistoryBinding.inflate(inflater, container, false)
  return binding.root
 }

 override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
  super.onViewCreated(view, savedInstanceState)

  auth = Firebase.auth
  database = FirebaseDatabase.getInstance().reference.child("OrderDetails")

  fetchOrderDetails()
 }

 private fun fetchOrderDetails() {
  val userId = auth.currentUser?.uid
  if (userId != null) {
   database.orderByChild("userUid").equalTo(userId)
    .addListenerForSingleValueEvent(object : ValueEventListener {
     override fun onDataChange(snapshot: DataSnapshot) {
      val orderList = mutableListOf<Order>()
      for (orderSnapshot in snapshot.children) {
       val order = orderSnapshot.getValue(Order::class.java)
       order?.let { orderList.add(it) }
      }
      displayOrderDetails(orderList)
     }

     override fun onCancelled(error: DatabaseError) {
      // Handle error
      Toast.makeText(requireContext(), "Error fetching orders", Toast.LENGTH_SHORT).show()
     }
    })
  }
 }

 private fun displayOrderDetails(orderList: List<Order>) {
  adapter = RecentBuyAdapter(orderList, requireContext(), this)
  binding.recentRecyclerView.layoutManager = LinearLayoutManager(requireContext())
  binding.recentRecyclerView.adapter = adapter
 }

 fun onCancelOrder(orderId: String) {
  // Get reference to the specific order in the database
  val orderRef = database.child(orderId)

  // Update the order status and store the cancellation message
  orderRef.updateChildren(
   mapOf(
    "cancellationMessage" to "Order Cancelled"
   )
  ).addOnSuccessListener {
   // Handle success
   // For example, you can show a toast message indicating successful cancellation
   Toast.makeText(requireContext(), "Your Order is cancelled", Toast.LENGTH_SHORT).show()
  }.addOnFailureListener {
   // Handle failure
   // For example, you can show a toast message indicating cancellation failure
   Toast.makeText(requireContext(), "Failed to cancel order", Toast.LENGTH_SHORT).show()
  }
 }
}
