package com.fusetech.virtualkanban.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import android.text.method.TextKeyListener
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fusetech.virtualkanban.fragments.IgenyKontenerKiszedesCikkKiszedes.Companion.isSent
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fusetech.virtualkanban.activities.MainActivity
import com.fusetech.virtualkanban.adapters.IgenyItemAdapter
import com.fusetech.virtualkanban.dataItems.IgenyItem
import com.fusetech.virtualkanban.R
import kotlinx.android.synthetic.main.fragment_tobblet_kontener_osszeallitasa.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList

private var igenyList: ArrayList<IgenyItem> = ArrayList()
private var igenyReveresed: ArrayList<IgenyItem> = ArrayList()
private const val TAG = "TobbletKontenerOsszeall"
private lateinit var sendBinCode2: TobbletKontenerOsszeallitasaFragment.SendBinCode2

@Suppress("UNCHECKED_CAST")
class TobbletKontenerOsszeallitasaFragment : Fragment(), IgenyItemAdapter.IgenyItemClick {

    private  var kontenerText: TextView? = null
    private  var progressBar: ProgressBar? = null
    private  var polcTextIgeny: EditText? = null
    private  var megjegyzes1_igeny: TextView? = null
    private  var megjegyzes2_igeny2: TextView? = null
    private  var intrem_igeny2: TextView? = null
    private  var unit_igeny2: TextView? = null
    private  var mainActivity: MainActivity? = null
    private  var cikkItem_igeny: EditText? = null
    private  var mennyiseg_igeny2: EditText? = null
    private  var lezarButton: Button? = null
    private  var kilepButton: Button? = null
    private var myView : View? = null
    private  var recyclerView: RecyclerView? = null
    private var javitButton: Button? = null
    private var id = ""

    interface SendBinCode2 {
        fun sendBinCode2(code: String, kontener: String)
        fun sendDetails2(
            cikkszam: String,
            mennyiseg: Double,
            term_rakhely: String,
            unit: String,
            kontener: String
        )
        fun setJavit(kontener: String)
        fun setRakhelyTetel(kontener: Int,code: String)
        fun closeContainer2(statusz: Int, datum: String, kontener: String)
    }

    @SuppressLint("SimpleDateFormat", "NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        myView =
            inflater.inflate(R.layout.fragment_tobblet_kontener_osszeallitasa, container, false)
        mainActivity = activity as MainActivity
        recyclerView = myView?.trecycler_igeny!!
        recyclerView?.isEnabled = false
        recyclerView?.adapter = IgenyItemAdapter(igenyReveresed, this)
        recyclerView?.layoutManager = LinearLayoutManager(myView?.context)
        recyclerView?.setHasFixedSize(true)
        lezarButton = myView?.tlezar_igeny
        kontenerText = myView?.tcontainer_igeny
        progressBar = myView?.tprogressBar_igeny
        polcTextIgeny = myView?.tbin_igeny
        megjegyzes1_igeny = myView?.tmegjegyzes_igeny
        megjegyzes2_igeny2 = myView?.tmegjegyzes2_igeny
        intrem_igeny2 = myView?.tintrem_igeny
        unit_igeny2 = myView?.tunit_igeny
        cikkItem_igeny = myView?.tcikk_igeny
        mennyiseg_igeny2 = myView?.tmennyiseg_igeny
        javitButton = myView?.javitTobblet
        mennyiseg_igeny2?.isFocusable = false
        mennyiseg_igeny2?.isFocusableInTouchMode = false
        cikkItem_igeny?.isFocusable = false
        kilepButton = myView?.tkilep_igeny_button
        mennyiseg_igeny2?.filters = arrayOf<InputFilter>(
            DecimalDigitsInputFilter(
                9,
                2
            )
        )
        kontenerText?.text = arguments?.getString("KONTENER")
        polcTextIgeny?.setText(arguments?.getString("TERMRAKH"))
        Log.d(TAG, "onCreateView: ${arguments?.getString("KONTENER")}")
        Log.d(TAG, "onCreateView: ${arguments?.getString("TERMRAKH")}")
        setBinFocusOn()
        if (polcTextIgeny?.text!!.isNotEmpty()) {
            polcTextIgeny?.isFocusable = false
            polcTextIgeny?.isFocusableInTouchMode = false
            cikkItem_igeny?.isEnabled = true
            cikkItem_igeny?.requestFocus()
            try {
                igenyReveresed.clear()
                getDataFromList()
            } catch (e: Exception) {
                Toast.makeText(myView!!.context, "Nincs felvett tétel", Toast.LENGTH_SHORT).show()
            }
        }
        megjegyzes1_igeny?.text = ""
        megjegyzes2_igeny2?.text = ""
        intrem_igeny2?.text = ""
        unit_igeny2?.text = ""
        polcTextIgeny?.filters = arrayOf<InputFilter>(InputFilter.AllCaps())
        setProgressBarOff()
        polcTextIgeny?.setOnClickListener {
            sendBinCode2.sendBinCode2(polcTextIgeny?.text.toString(), kontenerText?.text.toString())
        }
        cikkItem_igeny?.setOnClickListener {
            disableItemText()
            if(mainActivity?.isWifiConnected()!!){
                MainActivity.wifiInfo = mainActivity?.getMacAndSignalStrength()!!
            }
            if(cikkItem_igeny?.text?.isNotEmpty()!!){
                mainActivity?.isItem2(
                    cikkItem_igeny?.text.toString(),
                    polcTextIgeny?.text?.trim().toString()
                )
            }/*else{
                mennyiseg_igeny2?.isFocusable = false
                mennyiseg_igeny2?.isFocusableInTouchMode = false
            }*/
        }
        javitButton?.setOnClickListener {
            val kontener = kontenerText?.text
            if(kontener != ""){
                sendBinCode2.setJavit(kontener.toString())
                polcTextIgeny?.requestFocus()
            }
            polcTextIgeny?.setText("")
        }
        mennyiseg_igeny2?.setOnClickListener {
            if(mainActivity?.isWifiConnected()!!){
                MainActivity.wifiInfo = mainActivity?.getMacAndSignalStrength()!!
            }
            if(cikkItem_igeny?.text?.isNotEmpty()!! && mennyiseg_igeny2?.text?.trim().toString().toDouble() > 0){
                val konti = kontenerText!!.text.trim().substring(4, kontenerText!!.text.trim().length)
                igenyList.add(
                    IgenyItem(
                        cikkItem_igeny?.text.toString().trim(), megjegyzes1_igeny!!.text.toString().trim(),
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
                    cikkItem_igeny?.isFocusable = true
                    cikkItem_igeny?.isFocusableInTouchMode = true
                    cikkItem_igeny?.requestFocus()
                    cikkItem_igeny?.selectAll()
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
                    cikkItem_igeny?.isFocusable = true
                    cikkItem_igeny?.isFocusableInTouchMode = true
                    cikkItem_igeny?.requestFocus()
                    cikkItem_igeny?.selectAll()
                }
                sendBinCode2.sendDetails2(
                    cikkItem_igeny?.text.toString().trim(), mennyiseg_igeny2?.text.toString().toDouble(),
                    polcTextIgeny?.text.toString().trim(), unit_igeny2?.text.toString(),
                    konti
                )
            }else{
                mainActivity?.setAlert("Rendes mennyiséget vigyél fel!")
                mennyiseg_igeny2?.isFocusable = true
                mennyiseg_igeny2?.isFocusableInTouchMode = true
                cikkItem_igeny?.isFocusable = false
                cikkItem_igeny?.isFocusableInTouchMode = false
                mennyiseg_igeny2?.selectAll()
                //mennyiseg_igeny2?.requestFocus()
            }
        }

        kilepButton?.setOnClickListener {
            /*if (view != null) {
                val ihm =
                    activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                ihm.hideSoftInputFromWindow(view.windowToken, 0)
            }*/
            if(mainActivity?.isWifiConnected()!!){
                MainActivity.wifiInfo = mainActivity?.getMacAndSignalStrength()!!
            }
            clearAll()
            /*if (view != null) {
                val ihm =
                    activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                ihm.toggleSoftInputFromWindow(
                    view.applicationWindowToken,
                    InputMethodManager.SHOW_FORCED,
                    0
                )
            }*/
        }
        lezarButton?.setOnClickListener {
            if (igenyReveresed.size > 0) {
                val polc = polcTextIgeny!!.text.trim().toString()
                //setProgressBarOn()
                val currentDateAndTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
                var a = 0
                CoroutineScope(IO).launch {
                    for (i in 0 until igenyReveresed.size) {
                        isSent = false
                        if (igenyReveresed[i].mennyiseg.toDouble() != 0.0) {
                            async {
                                mainActivity?.sendKihelyezesXmlData(
                                    igenyReveresed[i].cikkszam, polc,
                                    igenyReveresed[i].mennyiseg.toDouble(),
                                    "01",
                                    "21",
                                    "BE"
                                )
                            }.await()
                            if (isSent) {
                                a++
                            }
                        }
                    }
                    if (a == igenyReveresed.size) {
                        Log.d(TAG, "onCreateView: $currentDateAndTime")
                        if (polcTextIgeny!!.text.isEmpty() && igenyReveresed.size == 0) {
                            sendBinCode2.closeContainer2(7, currentDateAndTime,id)
                            CoroutineScope(Main).launch {
                                //setProgressBarOff()
                                clearAll()
                                Log.d(TAG, "onCreateView: lezártam az üreset")
                            }
                            mainActivity?.loadMenuFragment(true)
                        } else {
                            val code = polcTextIgeny?.text
                            sendBinCode2.setRakhelyTetel(id.toInt(),code.toString())
                            sendBinCode2.closeContainer2(7, currentDateAndTime,id)
                            CoroutineScope(Main).launch {
                                //setProgressBarOff()
                                clearAll()
                                Log.d(TAG, "onCreateView: lezártam amibe volt adat")
                            }
                            mainActivity?.loadMenuFragment(true)
                        }
                    } else {
                        CoroutineScope(Main).launch {
                            mainActivity?.setAlert("Nem teljes a siker, nem mindegyik cikk van lezárva")
                        }
                    }
                }
            } else {
                mainActivity?.setAlert("Nem vettél fel cikkeket")
            }
            if(mainActivity?.isWifiConnected()!!){
                MainActivity.wifiInfo = mainActivity?.getMacAndSignalStrength()!!
            }
        }
        return myView
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun clearAll() {
        lezarButton?.requestFocus()
        kontenerText?.text = ""
        polcTextIgeny?.setText("")
        TextKeyListener.clear(polcTextIgeny!!.text)
        polcTextIgeny?.isEnabled = false
        polcTextIgeny?.isFocusable = false
        polcTextIgeny?.isFocusableInTouchMode = false
        igenyList.clear()
        megjegyzes1_igeny?.text = ""
        megjegyzes2_igeny2?.text = ""
        mennyiseg_igeny2?.setText("")
        TextKeyListener.clear(mennyiseg_igeny2!!.text)
        mennyiseg_igeny2?.isEnabled = false
        mennyiseg_igeny2?.isFocusable = false
        mennyiseg_igeny2?.isFocusableInTouchMode = false
        unit_igeny2?.text = ""
        intrem_igeny2?.text = ""
        cikkItem_igeny?.setText("")
        TextKeyListener.clear(cikkItem_igeny!!.text)
        cikkItem_igeny?.isFocusable = false
        cikkItem_igeny?.isFocusableInTouchMode = false
        igenyReveresed.clear()
        recyclerView?.adapter?.notifyDataSetChanged()
        mainActivity?.loadMenuFragment(true)
    }

    fun setProgressBarOff() {
        progressBar?.visibility = View.GONE
    }

    fun setProgressBarOn() {
        progressBar?.visibility = View.VISIBLE
    }

    fun setBinFocusOn() {
        polcTextIgeny?.requestFocus()
        polcTextIgeny?.selectAll()
    }

    fun setFocusToItem(code: String) {
        polcTextIgeny?.setText(code)
        cikkItem_igeny?.isFocusable = true
        cikkItem_igeny?.requestFocus()
        cikkItem_igeny?.selectAll()
        polcTextIgeny?.isFocusable = false
        polcTextIgeny?.isFocusableInTouchMode = false
    }

    fun setFocusToQuantity() {
        mennyiseg_igeny2?.isFocusable = true
        mennyiseg_igeny2?.isFocusableInTouchMode = true
        mennyiseg_igeny2?.requestFocus()
        mennyiseg_igeny2?.selectAll()
        cikkItem_igeny?.isFocusable = false
        cikkItem_igeny?.isFocusableInTouchMode = false
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        sendBinCode2 = if (context is SendBinCode2) {
            context //as SendBinCode
        } else {
            throw RuntimeException(context.toString() + "must implement")
        }
    }

    fun setInfo(megj: String, megj2: String, intRem: String, unit: String) {
        megjegyzes1_igeny?.text = megj
        megjegyzes2_igeny2?.text = megj2
        intrem_igeny2?.text = intRem
        unit_igeny2?.text = unit
    }

    override fun igenyClick(position: Int) {
        Log.d("igenyitem", "igenyClick: $position")
    }

    @SuppressLint("NotifyDataSetChanged")
    fun getDataFromList() {
        val myList: ArrayList<IgenyItem> =
            arguments?.getSerializable("TOBBLET") as ArrayList<IgenyItem>
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
            recyclerView!!.adapter?.notifyDataSetChanged()
        }
    }

    override fun onResume() {
        super.onResume()
        kontenerText?.text = arguments?.getString("KONTENER")
        polcTextIgeny?.setText(arguments?.getString("TERMRAKH"))
        id = arguments?.getString("KID")!!
        if (polcTextIgeny!!.text.isNotEmpty()) {
            cikkItem_igeny?.isFocusable = true
            cikkItem_igeny?.requestFocus()
        }
        else{
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

    fun setCode(code: String) {
        if (polcTextIgeny?.text!!.isEmpty()) {
            if(mainActivity?.isWifiConnected()!!){
                MainActivity.wifiInfo = mainActivity?.getMacAndSignalStrength()!!
            }
            //polcTextIgeny.setText(code)
            sendBinCode2.sendBinCode2(code,kontenerText?.text.toString())
        } else {
            if(mainActivity?.isWifiConnected()!!){
                MainActivity.wifiInfo = mainActivity?.getMacAndSignalStrength()!!
            }
            cikkItem_igeny?.setText(code)
            disableItemText()
            mainActivity?.isItem2(code, polcTextIgeny!!.text.trim().toString())
        }
    }

    fun onKilepPressed() {
        kilepButton?.performClick()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        myView = null
        kontenerText = null
        progressBar = null
        polcTextIgeny = null
        megjegyzes1_igeny = null
        megjegyzes2_igeny2 = null
        intrem_igeny2 = null
        unit_igeny2 = null
        mainActivity = null
        cikkItem_igeny = null
        mennyiseg_igeny2 = null
        lezarButton = null
        kilepButton = null
        recyclerView = null
        recyclerView?.adapter = null
        mennyiseg_igeny2?.filters = null
        mainActivity?.menuFragment = null
        mainActivity = null
    }
    fun setCikkszamBlank(){
        cikkItem_igeny?.requestFocus()
        cikkItem_igeny?.selectAll()
        mennyiseg_igeny2?.setText("")
        mennyiseg_igeny2?.isFocusable = false
        mennyiseg_igeny2?.isFocusableInTouchMode = false
    }
    fun setAfterUpdate(){
        cikkItem_igeny?.isEnabled = true
        cikkItem_igeny?.selectAll()
        cikkItem_igeny?.requestFocus()
        mennyiseg_igeny2?.setText("")
       // mennyiseg_igeny2?.isEnabled = false
        megjegyzes2_igeny2?.text = ""
        intrem_igeny2?.text = ""
        unit_igeny2?.text = ""
        megjegyzes1_igeny?.text = ""
    }
    /*fun setAfterCheck(){
        mennyiseg_igeny2?.isFocusable = true
        mennyiseg_igeny2?.isFocusableInTouchMode = true
        mennyiseg_igeny2?.requestFocus()
    }*/
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
}