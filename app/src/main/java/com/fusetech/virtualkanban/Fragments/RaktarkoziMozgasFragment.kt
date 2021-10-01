package com.fusetech.virtualkanban.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import com.fusetech.mobilleltarkotlin.showMe
import com.fusetech.virtualkanban.R
import com.fusetech.virtualkanban.adapters.MozgasAdapter
import com.fusetech.virtualkanban.databinding.FragmentRaktarkoziMozgasBinding
import com.fusetech.virtualkanban.interfaces.RaktarMozgas
import com.fusetech.virtualkanban.viewmodels.RaktarkoziViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.lang.Exception

@AndroidEntryPoint
class RaktarkoziMozgasFragment : Fragment(), MozgasAdapter.CurrentSelection, RaktarMozgas {

    val viewModel: RaktarkoziViewModel by viewModels()
    private lateinit var binding: FragmentRaktarkoziMozgasBinding
    private lateinit var loadResult: LoadResult
    interface LoadResult{
        fun loadPolcItems(code: String)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_raktarkozi_mozgas, container, false)
        binding.viewModel = viewModel
        binding.progressBar4.visibility = View.GONE
        viewModel.raktarMozgas = this
        binding.raktarPolcMozgas.requestFocus()
        return binding.root
    }

    override fun onCurrentClick(position: Int) {

    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
    }

    override fun sendCode(message: String) {
        showMe(message, requireContext())
    }

    override fun setProgressOn() {
        binding.progressBar4.visibility = View.VISIBLE
    }

    override fun setProgressOff() {
        binding.progressBar4.visibility = View.GONE
    }

    override fun setText(text: String) {
        binding.raktarPolcMozgas.setText(text)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun init(code: String) {
        loadResult.loadPolcItems(code)
    }

    fun getCode(code: String) {
        viewModel.getData(code)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        loadResult = if(context is LoadResult){
            context
        }else{
            throw Exception("Must implement")
        }
    }
    fun removeBin(){
        binding.raktarPolcMozgas.setText("")
    }
}