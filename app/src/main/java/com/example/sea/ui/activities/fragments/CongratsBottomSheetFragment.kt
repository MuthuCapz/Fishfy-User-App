package com.example.sea.ui.activities.fragments


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.sea.databinding.FragmentCongratsBottomSheetBinding
import com.example.sea.ui.activities.MainActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class CongratsBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentCongratsBottomSheetBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCongratsBottomSheetBinding.inflate(inflater,container,false)

        binding.goBackHomeButton.setOnClickListener {
            //dismiss()
            val intent = Intent(requireContext(), MainActivity::class.java)
            requireContext().startActivity(intent)

        }
        return binding.root
    }
}