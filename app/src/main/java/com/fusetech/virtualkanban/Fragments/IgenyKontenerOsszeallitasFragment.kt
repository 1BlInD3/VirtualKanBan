package com.fusetech.virtualkanban.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fusetech.virtualkanban.activities.MainActivity
import com.fusetech.virtualkanban.adapters.IgenyItemAdapter
import com.fusetech.virtualkanban.dataItems.IgenyItem
import com.fusetech.virtualkanban.R
import kotlinx.android.synthetic.main.fragment_igeny_kontener_osszeallitas.*
import kotlinx.android.synthetic.main.fragment_igeny_kontener_osszeallitas.view.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private var igenyList: ArrayList<IgenyItem> = ArrayList()
private var igenyReveresed: ArrayList<IgenyItem> = ArrayList()
private const val TAG = "IgenyKontenerOsszeallit"
private lateinit var sendBinCode: IgenyKontenerOsszeallitasFragment.SendBinCode

class IgenyKontenerOsszeallitasFragment : Fragment(), IgenyItemAdapter.IgenyItemClick {
    private var recyclerView: RecyclerView? = null
    private var param1: String? = null
    private var param2: String? = null
    private var myView : View? = null
    private var kontenerText: TextView? = null
    private var progressBar: ProgressBar? = null
    private var polcTextIgeny: EditText? = null
    private var megjegyzes1_igeny: TextView? = null
    private var megjegyzes2_igeny2: TextView? = null
    private var intrem_igeny2: TextView? = null
    private var unit_igeny2: TextView? = null
    private var mainActivity: MainActivity? = null
    private var cikkItem_igeny: EditText? = null
    private var mennyiseg_igeny2: EditText? = null
    private var lezarButton: Button? = null
    private var kilepButton: Button? = null
    private var javitButton: Button? = null
    private var id1: Int = 0

    interface SendBinCode {
        fun sendBinCode(code: String, kontener: String)
        fun sendDetails(
            cikkszam: String,
            mennyiseg: Double,
            term_rakhely: String,
            unit: String,
            kontener: String
        )
        fun setJavit2(kontener: String)
        fun setRakhelyTetel2(kontener: Int,code: String)
        fun closeContainer(statusz: Int, datum: String, kontener: String)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    @SuppressLint("SimpleDateFormat", "NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        myView = inflater.inflate(R.layout.fragment_igeny_kontener_osszeallitas, container, false)
        mainActivity = activity as MainActivity
        recyclerView = myView?.recycler_igeny!!
        recyclerView?.isEnabled = false
        recyclerView?.adapter = IgenyItemAdapter(igenyReveresed, this)
        recyclerView?.layoutManager = LinearLayoutManager(myView?.context)
        recyclerView?.setHasFixedSize(true)
        lezarButton = myView?.lezar_igeny
        kontenerText = myView?.container_igeny
        progressBar = myView?.progressBar_igeny
        polcTextIgeny = myView?.bin_igeny
        megjegyzes1_igeny = myView?.megjegyzes_igeny
        megjegyzes2_igeny2 = myView?.megjegyzes2_igeny
        intrem_igeny2 = myView?.intrem_igeny
        unit_igeny2 = myView?.unit_igeny
        cikkItem_igeny = myView?.cikk_igeny
        mennyiseg_igeny2 = myView?.mennyiseg_igeny
        javitButton = myView?.javitButton
        mennyiseg_igeny2?.filters = arrayOf<InputFilter>(DecimalDigitsInputFilter(9, 2))
        mennyiseg_igeny2?.isFocusable = false
        mennyiseg_igeny2?.isFocusableInTouchMode = false
        kilepButton = myView?.kilep_igeny_button
        cikkItem_igeny?.isFocusable = false
        cikkItem_igeny?.isFocusableInTouchMode = false
        kontenerText?.text = arguments?.getString("KONTENER")
        val konti = kontenerText?.text?.trim()?.substring(4, kontenerText?.text?.trim()?.length!!)
        polcTextIgeny?.setText(arguments?.getString("TERMRAKH"))
        Log.d(TAG, "onCreateView: ${arguments?.getString("KONTENER")}")
        Log.d(TAG, "onCreateView: ${arguments?.getString("TERMRAKH")}")
        setBinFocusOn()
        /*if (polcTextIgeny?.text?.isNotEmpty()!!) {
            polcTextIgeny?.isFocusable = false
            polcTextIgeny?.isFocusableInTouchMode = false
            cikkItem_igeny?.isFocusable = true
            cikkItem_igeny?.isFocusableInTouchMode = true
            cikkItem_igeny?.requestFocus()
            try {
                igenyReveresed.clear()
                getDataFromList()
            } catch (e: Exception) {
                Toast.makeText(myView?.context, "Nincs felvett tétel", Toast.LENGTH_SHORT).show()
            }
        }*/
        megjegyzes1_igeny?.text = ""
        megjegyzes2_igeny2?.text = ""
        intrem_igeny2?.text = ""
        unit_igeny2?.text = ""
        polcTextIgeny?.filters = arrayOf<InputFilter>(InputFilter.AllCaps())
        setProgressBarOff()
        polcTextIgeny?.setOnClickListener {
            sendBinCode.sendBinCode(polcTextIgeny?.text.toString(),kontenerText?.text.toString())
            if(mainActivity?.isWifiConnected()!!){
                MainActivity.wifiInfo = mainActivity?.getMacAndSignalStrength()!!
            }
        }
        cikkItem_igeny?.setOnClickListener {
            mainActivity?.isItem(cikkItem_igeny?.text.toString())
            disableItemText()
            if(mainActivity?.isWifiConnected()!!){
                MainActivity.wifiInfo = mainActivity?.getMacAndSignalStrength()!!
            }
        }
        mennyiseg_igeny2?.setOnClickListener {
            if(cikkItem_igeny!!.text.isNotEmpty() && polcTextIgeny!!.text.isNotEmpty()){
                igenyList.add(
                    IgenyItem(
                        cikkItem_igeny?.text.toString().trim(), megjegyzes1_igeny?.text.toString().trim(),
                        mennyiseg_igeny2?.text.toString().trim()
                    )
                )
                if (igenyList.size == 1) {
                    igenyReveresed.clear()
                    igenyReveresed.add(
                        IgenyItem(
                            igenyList[0].cikkszam,
                            igenyList[0].megnevezes,
                            igenyList[0].mennyiseg
                        )
                    )
                    recyclerView?.adapter?.notifyDataSetChanged()
                } else if (igenyList.size > 1) {
                    igenyReveresed.clear()
                    for (i in igenyList.size downTo 1) {
                        igenyReveresed.add(
                            IgenyItem(
                                igenyList[i - 1].cikkszam,
                                igenyList[i - 1].megnevezes,
                                igenyList[i - 1].mennyiseg
                            )
                        )
                    }
                    recyclerView?.adapter?.notifyDataSetChanged()
                }
                sendBinCode.sendDetails(
                    cikkItem_igeny?.text.toString().trim(), mennyiseg_igeny2?.text.toString().toDouble(),
                    polcTextIgeny?.text.toString().trim(), unit_igeny2?.text.toString(),
                    konti!!
                )
                cikkItem_igeny?.isFocusable = true
                cikkItem_igeny?.isFocusableInTouchMode = true
                cikkItem_igeny?.setText("")
                cikkItem_igeny?.requestFocus()
                mennyiseg_igeny2?.setText("")
                mennyiseg_igeny2?.isFocusable = false
                mennyiseg_igeny2?.isFocusableInTouchMode = false
                megjegyzes2_igeny2?.text = ""
                intrem_igeny2?.text = ""
                unit_igeny2?.text = ""
                megjegyzes1_igeny?.text = ""
                if(mainActivity?.isWifiConnected()!!){
                    MainActivity.wifiInfo = mainActivity?.getMacAndSignalStrength()!!
                }
            }
        }

        kilepButton?.setOnClickListener {
            mainActivity?.listIgenyItems?.clear()
            clearAll()
            mainActivity?.loadMenuFragment(true)
            if(mainActivity?.isWifiConnected()!!){
                MainActivity.wifiInfo = mainActivity?.getMacAndSignalStrength()!!
            }
        }
        lezarButton?.setOnClickListener {
            if(polcTextIgeny!!.text.isNotEmpty()){
                if (igenyReveresed.size > 0) {
                    setProgressBarOn()
                    val currentDateAndTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
                    Log.d(TAG, "onCreateView: $currentDateAndTime")
                    if (polcTextIgeny?.text?.isEmpty()!! && igenyReveresed.size == 0) {
                        sendBinCode.closeContainer(5, currentDateAndTime, konti!!)
                        setProgressBarOff()
                        clearAll()
                        mainActivity?.loadMenuFragment(true)
                        Log.d(TAG, "onCreateView: lezártam az üreset")
                    } else {
                        val code = polcTextIgeny?.text
                        sendBinCode.setRakhelyTetel2(id1,code.toString())
                        sendBinCode.closeContainer(
                            1,
                            currentDateAndTime,
                            konti!!
                        ) // ezt 1esre kéne átírni
                        //setProgressBarOff()
                        //clearAll()
                        //mainActivity.loadMenuFragment(true)
                        Log.d(TAG, "onCreateView: lezártam amibe volt adat")
                    }
                } else {
                    mainActivity?.setAlert("Nincsenek tételek a konténerben")
                }
                if(mainActivity?.isWifiConnected()!!){
                    MainActivity.wifiInfo = mainActivity?.getMacAndSignalStrength()!!
                }
            }else{
                mainActivity?.setAlert("Nem lehet a polc üres!!!")
            }
        }

        javitButton?.setOnClickListener{
            val kontener = kontenerText?.text
            if(kontener != ""){
                sendBinCode.setJavit2(kontener.toString())
                polcTextIgeny?.requestFocus()
            }
            polcTextIgeny?.setText("")
        }

        return myView
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clearAll() {
        mainActivity?.listIgenyItems?.clear()
        kontenerText?.text = ""
        igenyList.clear()
        igenyReveresed.clear()
        recyclerView?.adapter?.notifyDataSetChanged()
        megjegyzes1_igeny?.text = ""
        megjegyzes2_igeny2?.text = ""
        unit_igeny2?.text = ""
        intrem_igeny2?.text = ""
        mennyiseg_igeny2?.setText("")
        mennyiseg_igeny2?.isFocusable = false
        mennyiseg_igeny2?.isFocusableInTouchMode = false
        cikkItem_igeny?.setText("")
        cikkItem_igeny?.isFocusable = false
        cikkItem_igeny?.isFocusableInTouchMode = false
        polcTextIgeny?.setText("")
        polcTextIgeny?.isFocusable = false
        polcTextIgeny?.isFocusableInTouchMode = false
        lezarButton?.requestFocus()
    }

    fun setProgressBarOff() {
        progressBar?.visibility = View.GONE
    }

    fun setProgressBarOn() {
        progressBar?.visibility = View.VISIBLE
    }

    fun setBinFocusOn() {
        polcTextIgeny?.setText("")
        polcTextIgeny?.requestFocus()
    }

    fun setFocusToItem() {
        cikkItem_igeny?.isFocusable = true
        cikkItem_igeny?.isFocusableInTouchMode = true
        cikkItem_igeny?.requestFocus()
        cikkItem_igeny?.setText("")
        polcTextIgeny?.isFocusable = false
        polcTextIgeny?.isFocusableInTouchMode = false
    }

    fun setFocusToQuantity() {
        mennyiseg_igeny2?.isFocusable = true
        mennyiseg_igeny2?.isFocusableInTouchMode = true
        mennyiseg_igeny2?.selectAll()
        mennyiseg_igeny2?.requestFocus()
        cikkItem_igeny?.isFocusable = false
        cikkItem_igeny?.isFocusableInTouchMode = false
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String?) =
            IgenyKontenerOsszeallitasFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        sendBinCode = if (context is SendBinCode) {
            context //as SendBinCode
        } else {
            throw RuntimeException(context.toString() + "must implement")
        }
    }

    fun setInfo(megj: String, megj2: String, intRem: String, unit: String) {
        megjegyzes_igeny.text = megj
        megjegyzes2_igeny2?.text = megj2
        intrem_igeny2?.text = intRem
        unit_igeny2?.text = unit
    }

    override fun igenyClick(position: Int) {
        Log.d("igenyitem", "igenyClick: $position")
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getDataFromList() {
        val myList: ArrayList<IgenyItem> =
            arguments?.getSerializable("IGENY") as ArrayList<IgenyItem>
        if (myList.size == 0) {
            return
        } else {
            for (i in 0 until myList.size) {
                igenyReveresed.add(
                    IgenyItem(
                        myList[i].cikkszam,
                        myList[i].megnevezes,
                        myList[i].mennyiseg
                    )
                )
            }
            for (i in igenyReveresed.size downTo 1) {
                igenyList.add(
                    IgenyItem(
                        igenyReveresed[i - 1].cikkszam,
                        igenyReveresed[i - 1].megnevezes, igenyReveresed[i - 1].mennyiseg
                    )
                )
            }
            recyclerView?.adapter?.notifyDataSetChanged()
        }
    }

    override fun onResume() {
        super.onResume()
        kontenerText?.text = arguments?.getString("KONTENER")
        polcTextIgeny?.setText(arguments?.getString("TERMRAKH"))
        id1 = requireArguments().getInt("ID")
        if(polcTextIgeny!!.text.isNotEmpty()){
            polcTextIgeny?.isFocusable = false
            polcTextIgeny?.isFocusableInTouchMode = false
            cikkItem_igeny?.isFocusable = true
            cikkItem_igeny?.isFocusableInTouchMode = true
            cikkItem_igeny?.requestFocus()
            try {
                igenyReveresed.clear()
                getDataFromList()
            } catch (e: Exception) {
                Toast.makeText(myView?.context, "Nincs felvett tétel", Toast.LENGTH_SHORT).show()
            }
        }else{
            try {
                igenyReveresed.clear()
                getDataFromList()
            } catch (e: Exception) {
                Toast.makeText(myView?.context, "Nincs felvett tétel", Toast.LENGTH_SHORT).show()
            }
        }
    }

    class DecimalDigitsInputFilter(digitsBeforeZero: Int, digitsAfterZero: Int) :
        InputFilter {
        private var mPattern: Pattern =
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

    fun setCode(code: String) {
        if(mainActivity?.isWifiConnected()!!){
            MainActivity.wifiInfo = mainActivity?.getMacAndSignalStrength()!!
        }
        if (polcTextIgeny?.text?.isEmpty()!!) {
            polcTextIgeny?.setText(code)
            sendBinCode.sendBinCode(code,kontenerText?.text.toString())
        } else if(cikkItem_igeny?.text?.isEmpty()!!){
            disableItemText()
            cikkItem_igeny?.setText(code)
            mainActivity?.isItem(code)
        }else{
            Log.d(TAG, "setCode: Mit akarsz?")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        clearLeak()

    }
    fun clearLeak(){
        myView = null
        recyclerView = null
        recyclerView?.adapter = null
        lezarButton = null
        kontenerText = null
        progressBar = null
        polcTextIgeny = null
        megjegyzes1_igeny = null
        megjegyzes2_igeny2 = null
        intrem_igeny2 = null
        unit_igeny2 = null
        cikkItem_igeny = null
        mennyiseg_igeny2 = null
        mennyiseg_igeny2?.filters = null
        kilepButton = null
        mainActivity = null
    }

    fun deleteFocused(){
        if(polcTextIgeny?.hasFocus()!!){
            polcTextIgeny?.setText("")
        }
    }
    fun disableItemText(){
        cikkItem_igeny?.isEnabled = false
    }
    fun enableItemText(){
        cikkItem_igeny?.isEnabled = true
    }
    fun disableLezaras(){
        lezarButton?.isVisible = false
    }
    fun enableLezaras(){
        lezarButton?.isVisible = true
    }
}