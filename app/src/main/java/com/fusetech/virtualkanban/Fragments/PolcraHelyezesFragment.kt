package com.fusetech.virtualkanban.Fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isVisible
import com.fusetech.virtualkanban.Activities.MainActivity
import com.fusetech.virtualkanban.R
import kotlinx.android.synthetic.main.fragment_polcra_helyezes.view.*


class PolcraHelyezesFragment : Fragment() {
    private lateinit var cikkText: EditText
    private lateinit var mainActivity: MainActivity
    private lateinit var sendCode: SendCode
    private lateinit var mennyisegText: TextView
    private lateinit var tranzitQtyText: TextView
    private lateinit var sideContainer: FrameLayout
    private lateinit var progressBar: ProgressBar
    var megjegyzes1Text: TextView? = null
    var megjegyzes2Text: TextView? = null
    var intremText: TextView? = null
    var unitText: TextView? = null
    interface SendCode{
        fun sendCode(code: String)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_polcra_helyezes, container, false)
        mainActivity = activity as MainActivity
        megjegyzes1Text = view.description1Txt
        megjegyzes2Text = view.description2Txt
        intremText = view.intremTxt
        unitText = view.unitTxt
        tranzitQtyText = view.tranzitQtyTxt
        sideContainer = view.side_container
       // setContainerOff()
        progressBar = view.polcProgressBar
        setProgressBarOff()
        tranzitQtyText.isFocusable = false
        mennyisegText = view.mennyisegTxt
        cikkText = view.cikkEditTxt
        cikkText.requestFocus()

        cikkText.setOnClickListener{
            if(!cikkText.text.isBlank()){
                cikkText.selectAll()
                sendCode.sendCode(cikkText.text.trim().toString())
            }
        }
        mennyisegText.setOnClickListener {
           // mainActivity.loadPolcLocation()
        }
        return view
    }

    fun setTextViews(megjegyzes1: String,megjegyzes2: String,intrem: String,unit: String,mennyiseg: String){
        megjegyzes1Text?.text = megjegyzes1
        megjegyzes2Text?.text = megjegyzes2
        intremText?.text = intrem
        unitText?.text = unit
        tranzitQtyText.text = mennyiseg
        mennyisegText.requestFocus()
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        sendCode = if(context is SendCode){
            context as SendCode
        }else{
            throw RuntimeException(context.toString() + "must implement")
        }
    }
    fun setContainerOn(){
        sideContainer.setBackgroundResource(R.drawable.editbuttn2)
    }
    fun setContainerOff(){
        sideContainer.setBackgroundResource(R.color.pocakszin2)
    }
    fun setProgressBarOn(){
        progressBar.visibility = View.VISIBLE
    }
    fun setProgressBarOff(){
        progressBar.visibility = View.GONE
    }
}