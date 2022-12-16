package com.dabee.tpquickplacebykakaosearchapi.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.dabee.tpquickplacebykakaosearchapi.databinding.FragmentSearchListBinding
import com.dabee.tpquickplacebykakaosearchapi.databinding.FragmentSearchMapBinding

class SearchMapFragment:Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return binding.root
    }

    val binding by lazy { FragmentSearchMapBinding.inflate(layoutInflater) }


}