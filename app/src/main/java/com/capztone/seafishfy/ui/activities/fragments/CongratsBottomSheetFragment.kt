package com.example.seafishfy.ui.activities.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.example.seafishfy.databinding.FragmentCongratsBottomSheetBinding

import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.database.DatabaseReference

class CongratsBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentCongratsBottomSheetBinding
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCongratsBottomSheetBinding.inflate(inflater,container,false)
        binding.imageView4.setAnimation("Animation.json")
        binding.imageView4.playAnimation()
        binding.goBackHomeButton.setOnClickListener {
            //dismiss()

        }
         return binding.root
    }
}