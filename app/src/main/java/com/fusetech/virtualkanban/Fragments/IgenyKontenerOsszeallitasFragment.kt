package com.fusetech.virtualkanban.Fragments

import android.content.Context
import android.os.Bundle
import android.text.InputFilter
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import com.fusetech.virtualkanban.Activities.MainActivity
import com.fusetech.virtualkanban.R
import kotlinx.android.synthetic.main.fragment_igeny_kontener_osszeallitas.*
import kotlinx.android.synthetic.main.fragment_igeny_kontener_osszeallitas.view.*

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private lateinit var progressBar: ProgressBar
private lateinit var polcTextIgeny: EditText
private lateinit var megjegyzes1_igeny:TextView
private lateinit var megjegyzes2_igeny2:TextView
private lateinit var intrem_igeny2:TextView
private lateinit var unit_igeny2:TextView
private lateinit var mainActivity: MainActivity
private lateinit var sendBinCode : IgenyKontenerOsszeallitasFragment.SendBinCode
private lateinit var cikkItem_igeny: EditText
private lateinit var mennyiseg_igeny2: EditText

class IgenyKontenerOsszeallitasFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    interface SendBinCode{
        fun sendBinCode(code: String)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_igeny_kontener_osszeallitas, container, false)
        mainActivity = activity as MainActivity
        progressBar = view.progressBar_igeny
        polcTextIgeny = view.bin_igeny
        megjegyzes1_igeny = view.megjegyzes_igeny
        megjegyzes2_igeny2 = view.megjegyzes2_igeny
        intrem_igeny2 = view.intrem_igeny
        unit_igeny2 = view.unit_igeny
        cikkItem_igeny = view.cikk_igeny
        mennyiseg_igeny2 = view.mennyiseg_igeny
        megjegyzes1_igeny.text = ""
        megjegyzes2_igeny2.text = ""
        intrem_igeny2.text = ""
        unit_igeny2.text = ""
        polcTextIgeny.filters = arrayOf<InputFilter>(InputFilter.AllCaps())
        setBinFocusOn()
        setProgressBarOff()

        polcTextIgeny.setOnClickListener {
           sendBinCode.sendBinCode(polcTextIgeny.text.toString())
        }
        cikkItem_igeny.setOnClickListener {
            mainActivity.isItem(cikkItem_igeny.text.toString())
        }
        return view
    }

    fun setProgressBarOff(){
        progressBar.visibility = View.GONE
    }
    fun setProgressBarOn(){
        progressBar.visibility = View.VISIBLE
    }
    fun setBinFocusOn(){
        polcTextIgeny.selectAll()
        polcTextIgeny.requestFocus()
    }
    fun setFocusToItem(){
        cikkItem_igeny.requestFocus()
        cikkItem_igeny.selectAll()
        polcTextIgeny.isEnabled = false
    }
    fun setFocusToQuantity(){
        mennyiseg_igeny2.requestFocus()
        mennyiseg_igeny2.selectAll()
        cikkItem_igeny.isEnabled = false
    }
    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            IgenyKontenerOsszeallitasFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        sendBinCode = if(context is SendBinCode){
            context as SendBinCode
        }else{
            throw RuntimeException(context.toString() + "must implement")
        }
    }
    fun setInfo(megj: String, megj2: String, intRem: String, unit: String){
        megjegyzes_igeny.text = megj
        megjegyzes2_igeny2.text = megj2
        intrem_igeny2.text = intRem
        unit_igeny2.text = unit
    }
}