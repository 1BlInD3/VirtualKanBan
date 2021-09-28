package com.fusetech.virtualkanban.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.fusetech.mobilleltarkotlin.showMe
import com.fusetech.virtualkanban.R
import com.fusetech.virtualkanban.activities.MainActivity
import com.fusetech.virtualkanban.adapters.MozgasAdapter
import com.fusetech.virtualkanban.databinding.FragmentMozgasResultBinding
import com.fusetech.virtualkanban.viewmodels.RaktarMozgasViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MozgasResult : Fragment(),MozgasAdapter.CurrentSelection {

    val viewModel: RaktarMozgasViewModel by viewModels()
    private lateinit var binding: FragmentMozgasResultBinding
    var kiindulasRakhely = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_mozgas_result, container, false)
        binding.viewModel = viewModel
        binding.raktarCelMozgas.requestFocus()
        return binding.root
    }

    fun initRecycler(){
        binding.mozgasRecycler.adapter = MozgasAdapter(viewModel.getItems().value!!,this)
        binding.mozgasRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.mozgasRecycler.setHasFixedSize(true)
    }

    override fun onCurrentClick(position: Int) {

    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        viewModel.kiinduloRakhely = arguments?.getString("KIINDULAS")!!
        CoroutineScope(IO).launch {
            viewModel.loadItems(viewModel.kiinduloRakhely)
            CoroutineScope(Main).launch {
                if(MainActivity.zarolt){
                    showMe("Van zárolt",requireContext())
                    initRecycler()
                    viewModel.getItems().observe(viewLifecycleOwner,{
                        binding.mozgasRecycler.adapter?.notifyDataSetChanged()
                    })
                }else{
                    showMe("${viewModel.getItems().value!!.size}",requireContext())
                }
            }
        }
    }
    fun setText(code: String){
        binding.raktarCelMozgas.setText(code)
    }
}