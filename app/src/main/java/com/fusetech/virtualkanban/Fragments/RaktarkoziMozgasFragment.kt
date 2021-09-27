package com.fusetech.virtualkanban.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import com.fusetech.virtualkanban.R
import com.fusetech.virtualkanban.databinding.FragmentRaktarkoziMozgasBinding
import com.fusetech.virtualkanban.viewmodels.RaktarkoziViewModel

class RaktarkoziMozgasFragment : Fragment() {

    val viewModel : RaktarkoziViewModel by viewModels()
    private lateinit var binding: FragmentRaktarkoziMozgasBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_raktarkozi_mozgas, container, false)
        binding.viewModel = viewModel
        binding.progressBar4.visibility = View.GONE
        return binding.root
    }
    fun initRecycler(){
        
    }
}