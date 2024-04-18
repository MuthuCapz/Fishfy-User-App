// HomeFragment.kt

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
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.interfaces.ItemClickListener
import com.denzcoskun.imageslider.models.SlideModel
import com.example.seafishfy.R
import com.example.seafishfy.databinding.FragmentHomeBinding
import com.example.seafishfy.ui.activities.ContactusActivity
import com.example.seafishfy.ui.activities.Discount
import com.example.seafishfy.ui.activities.LocationActivity
import com.example.seafishfy.ui.activities.Utils.ToastHelper
import com.example.seafishfy.ui.activities.ViewModel.HomeViewModel
import com.example.seafishfy.ui.activities.adapters.DiscountAdapter
import com.example.seafishfy.ui.activities.adapters.MenuAdapter
import com.example.seafishfy.ui.activities.models.DiscountItem
import com.example.seafishfy.ui.activities.models.MenuItem
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        observeViewModel()

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

        return binding.root
    }

    private fun observeViewModel() {
        viewModel.address.observe(viewLifecycleOwner) { address ->
            binding.tvAddress.text = address
        }

        viewModel.locality.observe(viewLifecycleOwner) { locality ->
            binding.tvLocality.text = locality
        }

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
                context?.let { it1 -> ToastHelper.showCustomToast(it1, "Discount items open only after 5 Pm") }
            }
        } else {
            // If not in disabled time, enable clicks and remove OnClickListener
            binding.discountRecyclerView.isClickable = true
            binding.discountRecyclerView.setOnClickListener(null)
        }
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
