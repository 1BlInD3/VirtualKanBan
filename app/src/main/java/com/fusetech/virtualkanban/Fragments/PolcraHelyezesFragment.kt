package com.fusetech.virtualkanban.Fragments

import android.content.Context
import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import android.text.method.TextKeyListener
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.fragment.app.Fragment
import com.fusetech.virtualkanban.Activities.MainActivity
import com.fusetech.virtualkanban.R
import kotlinx.android.synthetic.main.fragment_polcra_helyezes.*
import kotlinx.android.synthetic.main.fragment_polcra_helyezes.view.*
import java.util.regex.Pattern


class PolcraHelyezesFragment : Fragment() {
    private lateinit var cikkText: EditText
    private lateinit var mainActivity: MainActivity
    private lateinit var sendCode: SendCode
    private lateinit var mennyisegText: EditText
    private lateinit var polcText: EditText
    private lateinit var tranzitQtyText: TextView
    private lateinit var sideContainer: FrameLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var kilepButton: Button
    private val TAG = "PolcraHelyezesFragment"
    private var binSelected: Boolean = false
    private var binPos: Int = -1
    private var binValue: String? = ""
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
        var context: Context
        context = view.context
        mainActivity = activity as MainActivity
        kilepButton = view.kilep_polc_btn
        megjegyzes1Text = view.description1Txt
        megjegyzes2Text = view.description2Txt
        intremText = view.intremTxt
        unitText = view.unitTxt
        polcText = view.polcTxt
        tranzitQtyText = view.tranzitQtyTxt
        sideContainer = view.side_container
        setContainerOff()
        progressBar = view.polcProgressBar
        setProgressBarOff()
        tranzitQtyText.isFocusable = false
        mennyisegText = view.mennyisegTxt
        mennyisegText.filters = arrayOf<InputFilter>(DecimalDigitsInputFilter(9, 2))
        cikkText = view.cikkEditTxt
        cikkText.requestFocus()
        sideContainer.visibility = View.GONE
        mennyisegText.isEnabled = false
        polcText.isEnabled = false
        polcText.filters = arrayOf<InputFilter>(InputFilter.AllCaps())
        cikkText.setOnClickListener{
            if(!cikkText.text.isBlank()){
                cikkText.selectAll()
                sendCode.sendCode(cikkText.text.trim().toString())
            }
        }
        mennyisegText.setOnClickListener {
            if(sideContainer.visibility == View.VISIBLE){
                if(!mennyisegText.text.isBlank()) {
                    val trQty = tranzitQtyText.text.toString().toDouble()
                    val qty = mennyisegText.text.toString().toDouble()
                    if (trQty < qty) {
                        mainActivity.setAlert("Túl sokat akarsz feltenni")
                        mennyisegText.selectAll()
                    } else {
                        mennyisegText.isEnabled = false
                        sideContainer.requestFocus()
                        polcText.isEnabled = true
                        mainActivity.setRecOn()
                    }
                }
            }
            else{
                val trQty = tranzitQtyText.text.toString().toInt()
                val qty = mennyisegText.text.toString().toInt()
                if (trQty < qty) {
                    mainActivity.setAlert("Túl sokat akarsz feltenni")
                    mennyisegText.selectAll()
                } else {
                    mennyisegText.isEnabled = false
                    polcText.isEnabled = true
                    polcText.requestFocus()
                }
            }
        }
        polcText.setOnClickListener {
            if(!polcText.text.isBlank()){
              val bin = polcText.text.toString()
              val trQty = tranzitQtyText.text.toString().toDouble()
              val qty = mennyisegText.text.toString().toDouble()
                    if(mainActivity.checkList(bin)){
                        if(trQty>qty){
                        tranzitQtyTxt.setText((trQty-qty).toString())
                            polcText.setText("")
                            polcText.isEnabled = false
                            mennyisegText.isEnabled = true
                            mennyisegText.selectAll()
                            mennyisegText.requestFocus()
                            mainActivity.checkIfContainsBin(bin,qty)
                        }
                        else if(trQty == qty) {
                            mainActivity.checkIfContainsBin(bin,qty)
                            tranzitQtyTxt.setText("0")
                            mainActivity.setRecData(binPos,qty)
                            mennyisegText.setText("")
                            tranzitQtyText.text = ""
                            polcText.setText("")
                            megjegyzes1Text?.text = ""
                            megjegyzes2Text?.text = ""
                            intremText?.text = ""
                            unitText?.text = ""
                            mennyisegText.isEnabled = false
                            polcText.isEnabled = false
                            cikkText.isEnabled = true
                            cikkText.setText("")
                            cikkText.requestFocus()
                            setContainerOff()
                        }
                    }else{
                        mainActivity.polcCheckIO(bin)
                    }
                }
            }
        kilepButton.setOnClickListener {
            if(view != null){
                val ihm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                ihm.hideSoftInputFromWindow(view!!.windowToken,0)
            }
            //kilepButton.requestFocus()
            cikkText.requestFocus()
            //cikkText.text.clear()
            TextKeyListener.clear(cikkText.text)
            cikkText.isEnabled = false
            cikkText.isFocusable = false
            cikkText.isFocusableInTouchMode = false
            mennyisegText.requestFocus()
            TextKeyListener.clear(mennyisegText.text)
           // mennyisegText.text.clear()
            mennyisegText.isEnabled = false
            mennyisegText.isFocusable = false
            mennyisegText.isFocusableInTouchMode = false
            polcText.requestFocus()
            TextKeyListener.clear(polcText.text)
            //polcText.text.clear()
            polcText.isEnabled = false
            polcText.isFocusable = false
            polcText.isFocusableInTouchMode = false
            mennyisegText.text.clear()
            mennyisegText.filters = arrayOf<InputFilter>()
            megjegyzes1Text?.text = ""
            megjegyzes2Text?.text = ""
            intremText?.text = ""
            unitText?.text = ""
            polcText.text.clear()
            tranzitQtyText.text = ""
            if(view != null){
                val ihm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                ihm.toggleSoftInputFromWindow(view.applicationWindowToken,InputMethodManager.SHOW_FORCED,0)
            }
            mainActivity.loadMenuFragment(true)

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
            context //as SendCode
        }else{
            throw RuntimeException(context.toString() + "must implement")
        }
    }
    fun setContainerOn(){
        sideContainer.visibility = View.VISIBLE
        sideContainer.isEnabled = false
    }
    fun setContainerOff(){
        sideContainer.visibility = View.GONE
    }
    fun setProgressBarOn(){
        progressBar.visibility = View.VISIBLE
    }
    fun setProgressBarOff(){
        progressBar.visibility = View.GONE
    }
    fun setBinNumber(binNumber: String?){
        polcText.setText(binNumber)
        polcText.requestFocus()
        polcText.selectAll()
    }
    fun focusToQty(){
        mennyisegText.isEnabled = true
        mennyisegText.requestFocus()
        cikkText.isEnabled = false
    }
    fun focusToBin(){
        polcText.isEnabled = true
        polcText.selectAll()
        polcText.requestFocus()
    }
    fun getAll(selected: Boolean,pos: Int, value: String?){
        binSelected = selected
        binPos = pos
        binValue = value
    }
    fun polcCheck(){
        val bin = polcText.text.toString()
        val trQty = tranzitQtyText.text.toString().toDouble()
        val qty = mennyisegText.text.toString().toDouble()
        binValue = ""
        binPos = -1
        binSelected = false
        if(trQty>qty){
            // tranzitQtyTxt.setText(trQty-qty)
            try {
                mainActivity.checkIfContainsBin(bin,qty)
            }catch(e: Exception){
                Log.d(TAG, "polcCheck: $e")
            }
            tranzitQtyTxt.setText((trQty-qty).toString())
            polcText.setText("")
            polcText.isEnabled = false
            mennyisegText.isEnabled = true
            mennyisegText.selectAll()
            mennyisegText.requestFocus()
        }
        else if(trQty == qty) {
            tranzitQtyTxt.setText("0")
            //ide egy interface hogy letöröljük a listából
            try {
                mainActivity.checkIfContainsBin(polcText.text.toString(),qty)
            }catch(e: Exception){
                Log.d(TAG, "polcCheck: $e")
            }
            mennyisegText.setText("")
            tranzitQtyText.text = ""
            polcText.setText("")
            megjegyzes1Text?.text = ""
            megjegyzes2Text?.text = ""
            intremText?.text = ""
            unitText?.text = ""
            mennyisegText.isEnabled = false
            polcText.isEnabled = false
            cikkText.isEnabled = true
            cikkText.setText("")
            cikkText.requestFocus()
            setContainerOff()
        }
    }
    class DecimalDigitsInputFilter(digitsBeforeZero: Int, digitsAfterZero: Int) :
        InputFilter {
        var mPattern: Pattern
        override fun filter(
            source: CharSequence,
            start: Int,
            end: Int,
            dest: Spanned,
            dstart: Int,
            dend: Int
        ): CharSequence? {
            val matcher = mPattern.matcher(dest)
            return if (!matcher.matches()) "" else null
        }

        init {
            mPattern =
                Pattern.compile("[0-9]{0," + (digitsBeforeZero - 1) + "}+((\\.[0-9]{0," + (digitsAfterZero - 1) + "})?)||(\\.)?")
        }
    }
    fun onKilepPressed(){
        kilepButton.performClick()
    }
    fun setCode(code: String){
        if(cikkText.text.isEmpty()){
            cikkText.setText(code)
        }else{
            polcText.setText(code)
        }
    }
}