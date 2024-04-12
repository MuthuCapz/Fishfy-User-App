package com.example.seafishfy.ui.activities.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.seafishfy.databinding.FragmentSearchBinding
import com.example.seafishfy.ui.activities.adapters.SearchAdapter
import com.example.seafishfy.ui.activities.models.MenuItem
import com.example.seafishfy.ui.activities.ViewModel.SearchViewModel

class SearchFragment : Fragment() {

    private lateinit var binding: FragmentSearchBinding
    private lateinit var adapter: SearchAdapter
    private lateinit var viewModel: SearchViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(SearchViewModel::class.java)
        setupUI()
        observeViewModel()
        viewModel.retrieveMenuItems()
        return binding.root
    }

    private fun setupUI() {
        adapter = SearchAdapter(emptyList(), requireContext())
        binding.menuRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.menuRecyclerView.adapter = adapter

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                viewModel.filterMenuItems(query)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                viewModel.filterMenuItems(newText)
                return true
            }
        })
    }

    private fun observeViewModel() {
        viewModel.menuItems.observe(viewLifecycleOwner, Observer<List<MenuItem>> { menuItems ->
            adapter.updateItems(menuItems)
        })
    }

}

