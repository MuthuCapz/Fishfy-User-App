package com.capztone.seafishfy.ui.activities.fragments
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.capztone.seafishfy.databinding.FragmentSearchBinding
import com.capztone.seafishfy.ui.activities.adapters.SearchAdapter
import com.capztone.seafishfy.ui.activities.models.MenuItem
import com.capztone.seafishfy.ui.activities.ViewModel.SearchViewModel

class SearchFragment : Fragment() {
    private lateinit var binding: FragmentSearchBinding
    private lateinit var adapter: SearchAdapter // Declare adapter property
    private val viewModel: SearchViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        setupSearchView()
        setupObservers()
        viewModel.retrieveMenuItems()
        return binding.root
    }

    private fun setupObservers() {
        viewModel.menuItemsLiveData.observe(viewLifecycleOwner) { menuItems ->
            menuItems?.let {
                showAllMenu(it)
            }
        }
    }

    private fun showAllMenu(menuItems: List<MenuItem>) {
        adapter = SearchAdapter(menuItems, requireContext())
        binding.menuRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.menuRecyclerView.adapter = adapter // Set the adapter here
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                filterMenuItems(query)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                filterMenuItems(newText)
                return true
            }
        })
    }

    private fun filterMenuItems(query: String) {
        if (::adapter.isInitialized) { // Check if adapter is initialized
            adapter.filter(query) // Call filter method on initialized adapter
        }
    }
}

