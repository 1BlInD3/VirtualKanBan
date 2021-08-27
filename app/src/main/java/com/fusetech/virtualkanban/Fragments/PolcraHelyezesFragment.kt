package com.fusetech.virtualkanban.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import android.text.method.TextKeyListener
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fusetech.virtualkanban.activities.MainActivity
import com.fusetech.virtualkanban.adapters.PolcLocationAdapter
import com.fusetech.virtualkanban.dataItems.PolcLocation
import com.fusetech.virtualkanban.R
import kotlinx.android.synthetic.main.fragment_polcra_helyezes.*
import kotlinx.android.synthetic.main.fragment_polcra_helyezes.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.regex.Pattern

class PolcraHelyezesFragment : Fragment(), PolcLocationAdapter.PolcItemClickListener {
    private var cikkText: EditText? = null
    private var mainActivity: MainActivity? = null
    private var sendCode: SendCode? = null
    private var mennyisegText: EditText?= null
    private var polcText: EditText? = null
    private var tranzitQtyText: TextView? = null
    private var sideContainer: FrameLayout? = null
    private var progressBar: ProgressBar? = null
    private var kilepButton: Button? = null
    private var recycler: RecyclerView? = null
    private var ujCikk: Button? = null
    private val TAG = "PolcraHelyezesFragment"
    private var binSelected: Boolean = false
    private var binPos: Int = -1
    private var binValue: String? = ""
    private var megjegyzes1Text: TextView? = null
    private var megjegyzes2Text: TextView? = null
    private var intremText: TextView? = null
    private var unitText: TextView? = null
    private var myView: View? = null


    companion object {
        val myItems: ArrayList<PolcLocation> = ArrayList()
        var isSentTranzit = false
    }

    interface SendCode {
        fun sendCode(code: String)
        fun sendTranzitData(
            cikk: String,
            polc: String?,
            mennyiseg: Double?,
            raktarbol: String,
            raktarba: String,
            polcra: String
        )
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        myView = inflater.inflate(R.layout.fragment_polcra_helyezes, container, false)
        mainActivity = activity as MainActivity
        kilepButton = myView?.kilep_polc_btn!!
        megjegyzes1Text = myView?.description1Txt
        megjegyzes2Text = myView?.description2Txt
        intremText = myView?.intremTxt
        unitText = myView?.unitTxt
        polcText = myView?.polcTxt!!
        tranzitQtyText = myView?.tranzitQtyTxt!!
        sideContainer = myView?.side_container!!
        sideContainer?.descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
        recycler = myView?.locationRecyclerOne!!
        progressBar = myView?.polcProgressBar!!
        ujCikk = myView?.ujCikkPolcHelyezes!!
        setProgressBarOff()
        tranzitQtyText?.isFocusable = false
        mennyisegText = myView?.mennyisegTxt!!
        mennyisegText?.filters = arrayOf<InputFilter>(DecimalDigitsInputFilter(9, 2))
        cikkText = myView?.cikkEditTxt!!
        cikkText?.requestFocus()
        mennyisegText?.isEnabled = false
        polcText?.isEnabled = false
        polcText?.filters = arrayOf<InputFilter>(InputFilter.AllCaps())

        recycler?.adapter = PolcLocationAdapter(myItems, this)
        recycler?.layoutManager = LinearLayoutManager(myView?.context)
        recycler?.setHasFixedSize(true)

        ujCikk?.setOnClickListener {
            cikkText?.setText("")
            mennyisegText?.setText("")
            tranzitQtyText?.text = ""
            megjegyzes1Text?.text = ""
            megjegyzes2Text?.text = ""
            intremText?.text = ""
            unitText?.text = ""
            myItems.clear()
            recycler?.adapter?.notifyDataSetChanged()
            mennyisegText?.isEnabled = false
            cikkText?.isEnabled = true
            cikkText?.requestFocus()
            polcText?.setText("")
        }

        cikkText?.setOnClickListener {
            if (cikkText?.text?.isNotBlank()!!) {
                cikkText?.selectAll()
                CoroutineScope(IO).launch {
                    sendCode?.sendCode(cikkText?.text?.trim().toString())
                }
            }
        }
        mennyisegText?.setOnClickListener {
            if (sideContainer?.visibility == View.VISIBLE) {
                if (mennyisegText?.text?.isNotBlank()!!) {
                    val trQty = tranzitQtyText?.text.toString().toDouble()
                    val qty = mennyisegText?.text.toString().toDouble()
                    if (trQty < qty) {
                        mainActivity?.setAlert("Túl sokat akarsz feltenni")
                        mennyisegText?.selectAll()
                    } else {
                        mennyisegText?.isEnabled = false
                        sideContainer?.descendantFocusability = ViewGroup.FOCUS_AFTER_DESCENDANTS
                        sideContainer?.requestFocus()
                        polcText?.isEnabled = true
                        recycler?.isEnabled = true
                    }
                }
            } else {
                val trQty = tranzitQtyText?.text.toString().toInt()
                val qty = mennyisegText?.text.toString().toInt()
                if (trQty < qty) {
                    mainActivity?.setAlert("Túl sokat akarsz feltenni")
                    mennyisegText?.selectAll()
                } else {
                    mennyisegText?.isEnabled = false
                    polcText?.isEnabled = true
                    polcText?.requestFocus()
                }
            }
        }
        polcText?.setOnClickListener {
            if (polcText?.text?.isNotBlank()!!) {
                val bin = polcText?.text.toString()
                val trQty = tranzitQtyText?.text.toString().toDouble()
                val qty = mennyisegText?.text.toString().toDouble()
                val cikk = cikkText?.text.toString().trim()
                isSentTranzit = false
                CoroutineScope(IO).launch {
                    async {
                        Log.d("IOTHREAD", "${Thread.currentThread().name} 1es opcio")
                        if (mainActivity?.check02Polc(bin)!!) {
                            sendCode?.sendTranzitData(cikk, "STD03", qty, "03", "02", bin)
                        } else {
                            CoroutineScope(Main).launch {
                                mainActivity?.setAlert("Nincs ilyen polc a 02 raktárban")
                            }
                        }
                    }.await()
                    if (isSentTranzit) {
                        if (trQty > qty) {
                            CoroutineScope(Main).launch {
                                tranzitQtyTxt.setText((trQty - qty).toString())
                                polcText?.setText("")
                                polcText?.isEnabled = false
                                mennyisegText?.isEnabled = true
                                mennyisegText?.setText("")
                                mennyisegText?.requestFocus()
                                checkBinIsInTheList(bin, qty)
                            }
                        } else if (trQty == qty) {
                            CoroutineScope(Main).launch {
                                checkBinIsInTheList(bin, qty)
                                tranzitQtyTxt.setText("0")
                                getDataFromList(binPos, qty)
                                mennyisegText?.setText("")
                                tranzitQtyText?.text = ""
                                polcText?.setText("")
                                megjegyzes1Text?.text = ""
                                megjegyzes2Text?.text = ""
                                intremText?.text = ""
                                unitText?.text = ""
                                mennyisegText?.isEnabled = false
                                polcText?.isEnabled = false
                                cikkText?.isEnabled = true
                                cikkText?.setText("")
                                cikkText?.requestFocus()
                                myItems.clear()
                                recycler?.adapter?.notifyDataSetChanged()
                                sideContainer?.descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
                            }
                        }
                    } else {
                        CoroutineScope(Main).launch {
                            mainActivity?.setAlert("Nem sikerült a tranzit XML-t elküldeni a Scala felé")
                        }
                    }
                }
            }
        }
        kilepButton?.setOnClickListener {
            ujCikk?.requestFocus()
            TextKeyListener.clear(cikkText?.text)
            cikkText?.isEnabled = false
            cikkText?.isFocusable = false
            cikkText?.isFocusableInTouchMode = false
            TextKeyListener.clear(mennyisegText?.text)
            mennyisegText?.isEnabled = false
            mennyisegText?.isFocusable = false
            mennyisegText?.isFocusableInTouchMode = false
            TextKeyListener.clear(polcText?.text)
            polcText?.isEnabled = false
            polcText?.isFocusable = false
            polcText?.isFocusableInTouchMode = false
            mennyisegText?.text?.clear()
            mennyisegText?.filters = arrayOf<InputFilter>()
            megjegyzes1Text?.text = ""
            megjegyzes2Text?.text = ""
            intremText?.text = ""
            unitText?.text = ""
            polcText?.text?.clear()
            tranzitQtyText?.text = ""
            myItems.clear()
            recycler?.adapter?.notifyDataSetChanged()

            mainActivity?.loadMenuFragment(true)
        }
        return myView as View
    }

    fun setTextViews(
        megjegyzes1: String,
        megjegyzes2: String,
        intrem: String,
        unit: String,
        mennyiseg: String
    ) {
        megjegyzes1Text?.text = megjegyzes1
        megjegyzes2Text?.text = megjegyzes2
        intremText?.text = intrem
        unitText?.text = unit
        tranzitQtyText?.text = mennyiseg
        mennyisegText?.requestFocus()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        sendCode = if (context is SendCode) {
            context
        } else {
            throw RuntimeException(context.toString() + "must implement")
        }
    }

    fun setProgressBarOn() {
        progressBar?.visibility = View.VISIBLE
    }

    fun setProgressBarOff() {
        progressBar?.visibility = View.INVISIBLE
    }

    private fun setBinNumber(binNumber: String?) {
        polcText?.setText(binNumber)
        polcText?.requestFocus()
        polcText?.selectAll()
    }

    fun focusToQty() {
        mennyisegText?.isEnabled = true
        mennyisegText?.requestFocus()
        cikkText?.isEnabled = false
    }

    fun focusToBin() {
        polcText?.isEnabled = true
        polcText?.selectAll()
        polcText?.requestFocus()
    }

    fun getAll(selected: Boolean, pos: Int, value: String?) {
        binSelected = selected
        binPos = pos
        binValue = value
    }

    class DecimalDigitsInputFilter(digitsBeforeZero: Int, digitsAfterZero: Int) :
        InputFilter {
        var mPattern: Pattern =
            Pattern.compile("[0-9]{0," + (digitsBeforeZero - 1) + "}+((\\.[0-9]{0," + (digitsAfterZero - 1) + "})?)||(\\.)?")

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

    }

    fun onKilepPressed() {
        kilepButton?.performClick()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun onTimeout() {
        ujCikk?.requestFocus()
        //cikkText.requestFocus()
        TextKeyListener.clear(cikkText?.text)
        cikkText?.isEnabled = false
        cikkText?.isFocusable = false
        cikkText?.isFocusableInTouchMode = false
        //mennyisegText.requestFocus()
        TextKeyListener.clear(mennyisegText?.text)
        mennyisegText?.isEnabled = false
        mennyisegText?.isFocusable = false
        mennyisegText?.isFocusableInTouchMode = false
        // polcText.requestFocus()
        TextKeyListener.clear(polcText?.text)
        polcText?.isEnabled = false
        polcText?.isFocusable = false
        polcText?.isFocusableInTouchMode = false
        mennyisegText?.text?.clear()
        mennyisegText?.filters = arrayOf<InputFilter>()
        megjegyzes1Text?.text = ""
        megjegyzes2Text?.text = ""
        intremText?.text = ""
        unitText?.text = ""
        polcText?.text?.clear()
        tranzitQtyText?.text = ""
        myItems.clear()
        recycler?.adapter?.notifyDataSetChanged()
        mainActivity?.loadLoginFragment()
        clearLeak()
    }

    fun setCode(code: String) {
        if (cikkText?.text?.isEmpty()!!) {
            cikkText?.setText(code)
            cikkText?.selectAll()
            CoroutineScope(IO).launch {
                sendCode?.sendCode(cikkText?.text?.trim().toString())
            }
        } else if(polcText?.hasFocus()!!){
            polcText?.setText(code)
            polcText?.selectAll()
            polcText?.performClick()
        }
    }

    override fun polcItemClick(position: Int) {
        if(mennyisegText?.text?.isNotBlank()!!){
            val value: String? = myItems[position].polc?.trim()
            val isSelected = true
            setBinNumber(value)
            getAll(isSelected, position, value)
            focusToBin()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getDataFromList(position: Int, value: Double) {
        val quantity = myItems[position].mennyiseg?.toDouble()
        myItems[position].mennyiseg = (quantity?.plus(value)).toString()
        recycler?.adapter?.notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun checkBinIsInTheList(falseBin: String, value: Double) {
        for (i in 0 until myItems.size) {
            if (myItems[i].polc?.trim() == falseBin) {
                val quantity = myItems[i].mennyiseg?.toDouble()
                myItems[i].mennyiseg = (quantity?.plus(value)).toString()
                Log.d(TAG, "checkBinIsInTheList: van ilyen")
                recycler?.adapter?.notifyDataSetChanged()
                break
            } else {
                Log.d(
                    TAG,
                    "checkBinIsInTheList: nincs ilyen ${myItems[i].polc}\tfasle bin =  $falseBin"
                )
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun reload() {
        CoroutineScope(Main).launch {
            recycler?.adapter?.notifyDataSetChanged()
        }
    }

    fun setCikkNumberBack() {
        cikkText?.setText("")
        cikkText?.requestFocus()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        clearLeak()

    }
    private fun clearLeak(){
        myView = null
        mainActivity = null
        kilepButton = null
        megjegyzes1Text = null
        megjegyzes2Text = null
        intremText = null
        unitText = null
        polcText = null
        tranzitQtyText = null
        sideContainer = null
        recycler = null
        recycler?.adapter = null
        progressBar = null
        ujCikk = null
        mennyisegText = null
        mennyisegText?.filters = null
        cikkText = null
        polcText?.filters = null
    }
}