package com.example.seafishfy.ui.activities.fragments
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.seafishfy.databinding.FragmentHistoryBinding
import com.example.seafishfy.ui.activities.adapters.RecentBuyAdapter
import com.example.seafishfy.ui.activities.viewmodel.HistoryViewModel

class HistoryFragment : Fragment() {

 private lateinit var binding: FragmentHistoryBinding
 private lateinit var viewModel: HistoryViewModel
 private lateinit var adapter: RecentBuyAdapter

 override fun onCreateView(
  inflater: LayoutInflater, container: ViewGroup?,
  savedInstanceState: Bundle?
 ): View {
  binding = FragmentHistoryBinding.inflate(inflater, container, false)
  return binding.root
 }

 override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
  super.onViewCreated(view, savedInstanceState)

  viewModel = ViewModelProvider(this).get(HistoryViewModel::class.java)
  adapter = RecentBuyAdapter(requireContext(), viewModel)

  binding.recentRecyclerView.layoutManager = LinearLayoutManager(requireContext())
  binding.recentRecyclerView.adapter = adapter

  observeOrders()
  viewModel.fetchOrders()
 }

 private fun observeOrders() {
  viewModel.orders.observe(viewLifecycleOwner) { orders ->
   adapter.submitList(orders)
  }
 }

 fun onCancelOrder(orderId: String) {
  viewModel.cancelOrder(orderId) { isSuccess ->
   if (isSuccess) {
    Toast.makeText(requireContext(), "Your order has been cancelled", Toast.LENGTH_SHORT).show()
   } else {
    Toast.makeText(requireContext(), "Failed to cancel order", Toast.LENGTH_SHORT).show()
   }
  }
 }
}
