package com.fusetech.virtualkanban.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
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
class MozgasResult : Fragment(), MozgasAdapter.CurrentSelection, MozgasListener {

    val viewModel: RaktarMozgasViewModel by viewModels()
    private lateinit var binding: FragmentMozgasResultBinding
    private lateinit var save: FileSave

    interface FileSave {
        fun saveFile(
            cikk: String,
            mennyiseg: Double,
            kiinduloPolc: String,
            celPolc: String,
            rbol: String,
            rba: String
        )
        fun ujPolcFelvetele()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_mozgas_result, container, false)
        binding.viewModel = viewModel
        binding.raktarCelMozgas.visibility = View.GONE
        viewModel.mozgasListener = this
        binding.button.visibility = View.GONE
        binding.cikkTomb.visibility = View.GONE
        binding.mennyisegTomb.visibility = View.GONE
        binding.textView46.visibility = View.GONE
        binding.textView47.visibility = View.GONE
        binding.constraintLayout6.visibility = View.GONE
        return binding.root
    }

    private fun initRecycler() {
        binding.mozgasRecycler.adapter = MozgasAdapter(viewModel.getItems().value!!, this)
        binding.mozgasRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.mozgasRecycler.setHasFixedSize(true)
    }

    override fun onCurrentClick(position: Int) {
        binding.button.visibility = View.VISIBLE
        viewModel.arrayAddOrDelete(position)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        viewModel.kiinduloRakhely = arguments?.getString("KIINDULAS")!!
        CoroutineScope(IO).launch {
            viewModel.loadItems(viewModel.kiinduloRakhely)
            CoroutineScope(Main).launch {
                binding.mozgasLoadProgress.visibility = View.GONE
                if (MainActivity.zarolt) {
                    //showMe("Van zárolt", requireContext())
                    initRecycler()
                    binding.mozgasRecycler.requestFocus()
                    viewModel.getItems().observe(viewLifecycleOwner, {
                        binding.mozgasRecycler.adapter?.notifyDataSetChanged()
                    })
                } else {
                    val builder = AlertDialog.Builder(requireContext())
                    builder.setTitle("Mozgatás")
                    builder.setMessage("Az egész polcot át szeretnéd mozgatni?")
                    builder.setPositiveButton("Igen") { _, _ ->
                        binding.raktarCelMozgas.visibility = View.VISIBLE
                        binding.raktarCelMozgas.requestFocus()
                        viewModel.yesClicked = true
                    }
                    builder.setNegativeButton("Nem") { _, _ ->
                        initRecycler()
                        viewModel.getItems().observe(viewLifecycleOwner, {
                            binding.mozgasRecycler.adapter?.notifyDataSetChanged()
                        })
                        binding.mozgasRecycler.requestFocus()
                        viewModel.valasztasLista.clear()
                    }
                    builder.setOnCancelListener {
                        initRecycler()
                        viewModel.getItems().observe(viewLifecycleOwner, {
                            binding.mozgasRecycler.adapter?.notifyDataSetChanged()
                        })
                    }
                    builder.create()
                    builder.show().getButton(DialogInterface.BUTTON_POSITIVE).requestFocus()
                }
            }
        }
    }

    fun setText(code: String) {
        viewModel.checkPolc(code)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        save = if (context is FileSave) {
            context
        } else {
            throw Exception("Must impolement")
        }
    }

    fun getFileFromActivity(
        file: File,
        cikk: String,
        mennyiseg: Double,
        kiinduloPolc: String,
        celPolc: String,
        rbol: String,
        rba: String
    ) {
        CoroutineScope(IO).launch {
            viewModel.sendToScala(file, cikk, mennyiseg, kiinduloPolc, celPolc, rbol, rba)
        }
    }

    override fun message(message: String) {
        showMe(message, requireContext())
    }

    override fun setSend() {
        CoroutineScope(Main).launch {
            binding.mozgasLoadProgress.visibility = View.VISIBLE
        }
        for (i in 0 until viewModel.getItems().value!!.size) {
            save.saveFile(
                viewModel.getItems().value!![i].mCikk.trim(),
                viewModel.getItems().value!![i].mMennyiseg,
                viewModel.kiinduloRakhely,
                viewModel.celRaktar,
                "02",
                "02"
            )
        }
        CoroutineScope(Main).launch {
            showMe("Mind kész", requireContext())
            binding.cikkTomb.text = ""
            binding.mennyisegTomb.text = ""
            binding.textView47.text = ""
            binding.raktarCelMozgas.setText("")
            binding.textView46.visibility = View.GONE
            binding.constraintLayout6.visibility = View.GONE
            binding.mozgasLoadProgress.visibility = View.GONE
            save.ujPolcFelvetele()
        }
    }

    override fun sendOneByOne() {
        CoroutineScope(Main).launch {
            binding.mozgasLoadProgress.visibility = View.VISIBLE
            if (viewModel.valasztasLista.size > 0) {
                save.saveFile(
                    viewModel.valasztasLista[0].mCikk.trim(),
                    viewModel.valasztasLista[0].mMennyiseg,
                    viewModel.kiinduloRakhely,
                    viewModel.celRaktar,
                    "02",
                    "02"
                )
                viewModel.valasztasLista.removeAt(0)
                if(viewModel.valasztasLista.size > 0){
                    binding.cikkTomb.text = viewModel.valasztasLista[0].mCikk
                    binding.mennyisegTomb.text = viewModel.valasztasLista[0].mMennyiseg.toString()
                    binding.textView47.text = viewModel.valasztasLista[0].mEgyseg
                }else if (viewModel.valasztasLista.size == 0){
                    CoroutineScope(Main).launch {
                        showMe("Készen van az összes", requireContext())
                        binding.cikkTomb.text = ""
                        binding.mennyisegTomb.text = ""
                        binding.textView47.text = ""
                        binding.raktarCelMozgas.setText("")
                        binding.textView46.visibility = View.GONE
                        binding.constraintLayout6.visibility = View.GONE
                        save.ujPolcFelvetele()
                    }
                }
                binding.mozgasLoadProgress.visibility = View.GONE
            } else {
                CoroutineScope(Main).launch {
                    showMe("Készen van az összes", requireContext())
                    binding.mozgasLoadProgress.visibility = View.GONE
                }
            }
        }
    }

    override fun setPolcText(code: String) {
        CoroutineScope(Main).launch {
            binding.raktarCelMozgas.setText(code)
        }
    }

    override fun whenButtonIsClicked() {
        binding.button.visibility = View.GONE
        binding.textView46.visibility = View.VISIBLE
        binding.cikkTomb.visibility = View.VISIBLE
        binding.mennyisegTomb.visibility = View.VISIBLE
        binding.raktarCelMozgas.visibility = View.VISIBLE
        binding.textView47.visibility = View.VISIBLE
        binding.constraintLayout6.visibility = View.VISIBLE
        binding.raktarCelMozgas.requestFocus()
        binding.mozgasRecycler.visibility = View.GONE
        binding.cikkTomb.text = viewModel.valasztasLista[0].mCikk
        binding.mennyisegTomb.text = viewModel.valasztasLista[0].mMennyiseg.toString()
        binding.textView47.text = viewModel.valasztasLista[0].mEgyseg
    }

    override fun onStop() {
        super.onStop()
        Log.d("MOZGAS", "onStop: ${viewModel.valasztasLista}")
    }
}