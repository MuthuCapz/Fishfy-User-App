package com.capztone.seafishfy.ui.activities.fragments

import com.capztone.seafishfy.ui.activities.ViewModel.ShopOneViewModel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.capztone.seafishfy.R
import com.capztone.seafishfy.databinding.FragmentShopOneBinding
import com.capztone.seafishfy.ui.activities.adapters.DiscountAdapter
import com.capztone.seafishfy.ui.activities.adapters.MenuAdapter
import com.capztone.seafishfy.ui.activities.models.DiscountItem
import com.capztone.seafishfy.ui.activities.models.MenuItem
import com.capztone.seafishfy.ui.activities.Utils.ToastHelper
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.*

class ShopOneFragment : Fragment() {

    private lateinit var binding: FragmentShopOneBinding
    private val viewModel: ShopOneViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentShopOneBinding.inflate(inflater, container, false)

        observeViewModel()
        val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNav.visibility = View.GONE
        binding.menuItemTextView.setOnClickListener {
            val bottomSheetDialog = MenuBottomSheetFragment()
            bottomSheetDialog.show(parentFragmentManager, "Test")
        }

        return binding.root
    }

    private fun observeViewModel() {


        viewModel.popularItems.observe(viewLifecycleOwner) { items ->
            setPopularItemAdapter(items)
        }

        viewModel.menu1Items.observe(viewLifecycleOwner) { items ->
            setMenu1ItemAdapter(items)
        }

        viewModel.menu3Items.observe(viewLifecycleOwner) { items ->
            setMenu3ItemAdapter(items)
        }

        viewModel.discountItems.observe(viewLifecycleOwner) { items ->
            setDiscountItemAdapter(items)
        }
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

    private fun setDiscountItemAdapter(discountItems: List<DiscountItem>) {
        val adapter = DiscountAdapter(discountItems, requireContext())
        binding.discountRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.discountRecyclerView.adapter = adapter

        // Disable the Discount RecyclerView between 7:00 AM and 5:00 PM in Indian time
        val currentTime = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata"))
        val hour = currentTime.get(Calendar.HOUR_OF_DAY)
        if (hour in 7 until 17) {
            // Set alpha to 0.5 to visually indicate that it's disabled
            binding.discountRecyclerView.alpha = 0.5f
            // Disable clicks during the disabled time
            binding.discountRecyclerView.isClickable = false
            // Set an OnClickListener to show a toast message
            binding.discountRecyclerView.setOnClickListener {
                ToastHelper.showCustomToast(requireContext(), "Discount items opens only after 5 PM")
            }
        } else {
            // If not in disabled time, enable clicks and remove OnClickListener
            binding.discountRecyclerView.isClickable = true
            binding.discountRecyclerView.setOnClickListener(null)
        }
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
        imageList.add(SlideModel(R.drawable.banner3, scaleType = ScaleTypes.FIT))


    }
}
