package com.example.seafishfy.ui.activities.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.example.seafishfy.R
import com.example.seafishfy.databinding.FragmentShopFourBinding
import com.example.seafishfy.databinding.FragmentShopSixBinding
import com.example.seafishfy.ui.activities.adapters.DiscountAdapter
import com.example.seafishfy.ui.activities.adapters.MenuAdapter
import com.example.seafishfy.ui.activities.models.DiscountItem
import com.example.seafishfy.ui.activities.models.MenuItem
import com.example.seafishfy.ui.activities.Utils.ToastHelper
import com.example.seafishfy.ui.activities.ViewModel.ShopSixViewModel
import java.util.*

class ShopSixFragment : Fragment() {

    private lateinit var binding: FragmentShopSixBinding
    private val viewModel: ShopSixViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentShopSixBinding.inflate(inflater, container, false)

        observeViewModel()

        binding.menuItemTextView.setOnClickListener {
            val bottomSheetDialog = MenuBottomSheetFragment5()
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
