package com.fusetech.virtualkanban.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
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
import com.fusetech.virtualkanban.interfaces.MozgasListener
import com.fusetech.virtualkanban.viewmodels.RaktarMozgasViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import java.io.File

@AndroidEntryPoint
class MozgasResult : Fragment(),MozgasAdapter.CurrentSelection,MozgasListener {

    val viewModel: RaktarMozgasViewModel by viewModels()
    private lateinit var binding: FragmentMozgasResultBinding
    private lateinit var save: FileSave
    interface FileSave{
        fun saveFile(cikk: String, mennyiseg: Double, kiinduloPolc: String, celPolc: String, rbol: String, rba: String)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_mozgas_result, container, false)
        binding.viewModel = viewModel
        binding.raktarCelMozgas.visibility = View.GONE
        viewModel.mozgasListener = this
        return binding.root
    }

    private fun initRecycler(){
        binding.mozgasRecycler.adapter = MozgasAdapter(viewModel.getItems().value!!,this)
        binding.mozgasRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.mozgasRecycler.setHasFixedSize(true)
    }

    override fun onCurrentClick(position: Int) {
        binding.raktarCelMozgas.visibility = View.VISIBLE
        binding.raktarCelMozgas.requestFocus()
        binding.raktarCelMozgas.selectAll()
        viewModel.position = position
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
                    val builder = AlertDialog.Builder(requireContext())
                    builder.setTitle("Mozgatás")
                    builder.setMessage("Az egész polcot át szeretnéd mozgatni?")
                    builder.setPositiveButton("Igen"){_,_->
                        binding.raktarCelMozgas.visibility = View.VISIBLE
                        binding.raktarCelMozgas.requestFocus()
                        viewModel.yesClicked = true
                    }
                    builder.setNegativeButton("Nem"){_,_ ->
                        initRecycler()
                        viewModel.getItems().observe(viewLifecycleOwner,{
                            binding.mozgasRecycler.adapter?.notifyDataSetChanged()
                        })
                    }
                    builder.setOnCancelListener {
                        initRecycler()
                        viewModel.getItems().observe(viewLifecycleOwner,{
                            binding.mozgasRecycler.adapter?.notifyDataSetChanged()
                        })
                    }
                    builder.create()
                    builder.show().getButton(DialogInterface.BUTTON_POSITIVE).requestFocus()
                }
            }
        }
    }
    fun setText(code: String){
        viewModel.checkPolc(code)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        save = if(context is FileSave){
            context
        }else{
            throw Exception("Must impolement")
        }
    }
    fun getFileFromActivity(file: File,cikk: String, mennyiseg: Double, kiinduloPolc: String, celPolc: String, rbol: String, rba: String){
        CoroutineScope(IO).launch {
            viewModel.sendToScala(file,cikk,mennyiseg,kiinduloPolc,celPolc,rbol,rba)
        }
    }

    override fun message(message: String) {
        showMe(message,requireContext())
    }

    override fun setSend() {
        for (i in 0 until viewModel.getItems().value!!.size){
            save.saveFile(viewModel.getItems().value!![i].mCikk,viewModel.getItems().value!![i].mMennyiseg,viewModel.kiinduloRakhely,viewModel.celRaktar,"02","02")
        }
        CoroutineScope(Main).launch {
            showMe("Mind kész",requireContext())
        }
    }

    override fun sendOneByOne(position: Int) {
           save.saveFile(viewModel.getItems().value!![position].mCikk,viewModel.getItems().value!![position].mMennyiseg,viewModel.kiinduloRakhely,viewModel.celRaktar,"02","02")
    }

    override fun setPolcText(code: String) {
        CoroutineScope(Main).launch {
            binding.raktarCelMozgas.setText(code)
        }
    }
}