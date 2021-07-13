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
import android.view.inputmethod.InputMethodManager
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

private lateinit var recyclerView: RecyclerView
private var igenyList: ArrayList<IgenyItem> = ArrayList()
private var igenyReveresed: ArrayList<IgenyItem> = ArrayList()
private const val TAG = "TobbletKontenerOsszeall"
private lateinit var sendBinCode2: TobbletKontenerOsszeallitasaFragment.SendBinCode2

@Suppress("UNCHECKED_CAST")
class TobbletKontenerOsszeallitasaFragment : Fragment(), IgenyItemAdapter.IgenyItemClick {

    private lateinit var kontenerText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var polcTextIgeny: EditText
    private lateinit var megjegyzes1_igeny: TextView
    private lateinit var megjegyzes2_igeny2: TextView
    private lateinit var intrem_igeny2: TextView
    private lateinit var unit_igeny2: TextView
    private lateinit var mainActivity: MainActivity
    private lateinit var cikkItem_igeny: EditText
    private lateinit var mennyiseg_igeny2: EditText
    private lateinit var lezarButton: Button
    private lateinit var kilepButton: Button

    interface SendBinCode2 {
        fun sendBinCode2(code: String)
        fun sendDetails2(
            cikkszam: String,
            mennyiseg: Double,
            term_rakhely: String,
            unit: String,
            kontener: String
        )

        fun closeContainer2(statusz: Int, datum: String)
    }

    @SuppressLint("SimpleDateFormat")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =
            inflater.inflate(R.layout.fragment_tobblet_kontener_osszeallitasa, container, false)
        mainActivity = activity as MainActivity
        recyclerView = view.trecycler_igeny
        recyclerView.isEnabled = false
        recyclerView.adapter = IgenyItemAdapter(igenyReveresed, this)
        recyclerView.layoutManager = LinearLayoutManager(view.context)
        recyclerView.setHasFixedSize(true)
        lezarButton = view.tlezar_igeny
        kontenerText = view.tcontainer_igeny
        progressBar = view.tprogressBar_igeny
        polcTextIgeny = view.tbin_igeny
        megjegyzes1_igeny = view.tmegjegyzes_igeny
        megjegyzes2_igeny2 = view.tmegjegyzes2_igeny
        intrem_igeny2 = view.tintrem_igeny
        unit_igeny2 = view.tunit_igeny
        cikkItem_igeny = view.tcikk_igeny
        mennyiseg_igeny2 = view.tmennyiseg_igeny
        mennyiseg_igeny2.isFocusable = false
        cikkItem_igeny.isFocusable = false
        kilepButton = view.tkilep_igeny_button
        mennyiseg_igeny2.filters = arrayOf<InputFilter>(
            DecimalDigitsInputFilter(
                9,
                2
            )
        )
        kontenerText.text = arguments?.getString("KONTENER")
        polcTextIgeny.setText(arguments?.getString("TERMRAKH"))
        Log.d(TAG, "onCreateView: ${arguments?.getString("KONTENER")}")
        Log.d(TAG, "onCreateView: ${arguments?.getString("TERMRAKH")}")
        setBinFocusOn()
        if (polcTextIgeny.text.isNotEmpty()) {
            polcTextIgeny.isFocusable = false
            polcTextIgeny.isFocusableInTouchMode = false
            cikkItem_igeny.isEnabled = true
            cikkItem_igeny.requestFocus()
            try {
                igenyReveresed.clear()
                getDataFromList()
            } catch (e: Exception) {
                Toast.makeText(view.context, "Nincs felvett tétel", Toast.LENGTH_SHORT).show()
            }
        }
        megjegyzes1_igeny.text = ""
        megjegyzes2_igeny2.text = ""
        intrem_igeny2.text = ""
        unit_igeny2.text = ""
        polcTextIgeny.filters = arrayOf<InputFilter>(InputFilter.AllCaps())
        setProgressBarOff()
        polcTextIgeny.setOnClickListener {
            sendBinCode2.sendBinCode2(polcTextIgeny.text.toString())
        }
        cikkItem_igeny.setOnClickListener {
            mainActivity.isItem2(
                cikkItem_igeny.text.toString(),
                polcTextIgeny.text.trim().toString()
            )
        }
        mennyiseg_igeny2.setOnClickListener {
            val konti = kontenerText.text.trim().substring(4, kontenerText.text.trim().length)
            igenyList.add(
                IgenyItem(
                    cikkItem_igeny.text.toString().trim(), megjegyzes1_igeny.text.toString().trim(),
                    mennyiseg_igeny2.text.toString().trim()
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
                recyclerView.adapter?.notifyDataSetChanged()
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
                recyclerView.adapter?.notifyDataSetChanged()
            }
            sendBinCode2.sendDetails2(
                cikkItem_igeny.text.toString().trim(), mennyiseg_igeny2.text.toString().toDouble(),
                polcTextIgeny.text.toString().trim(), unit_igeny2.text.toString(),
                konti
            )
            cikkItem_igeny.isEnabled = true
            cikkItem_igeny.selectAll()
            cikkItem_igeny.requestFocus()
            mennyiseg_igeny2.setText("")
            mennyiseg_igeny2.isEnabled = false
            megjegyzes2_igeny2.text = ""
            intrem_igeny2.text = ""
            unit_igeny2.text = ""
            megjegyzes1_igeny.text = ""
        }

        kilepButton.setOnClickListener {
            if (view != null) {
                val ihm =
                    activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                ihm.hideSoftInputFromWindow(view.windowToken, 0)
            }
            clearAll()
            if (view != null) {
                val ihm =
                    activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                ihm.toggleSoftInputFromWindow(
                    view.applicationWindowToken,
                    InputMethodManager.SHOW_FORCED,
                    0
                )
            }
        }
        lezarButton.setOnClickListener {
            if (igenyReveresed.size > 0) {
                val polc = polcTextIgeny.text.trim().toString()
                setProgressBarOn()
                val currentDateAndTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
                var a = 0
                CoroutineScope(IO).launch {
                    for (i in 0 until igenyReveresed.size) {
                        isSent = false
                        if (igenyReveresed[i].mennyiseg.toDouble() != 0.0) {
                            async {
                                mainActivity.sendKihelyezesXmlData(
                                    igenyReveresed[i].cikkszam, polc,
                                    igenyReveresed[i].mennyiseg.toDouble(),
                                    "01",
                                    "21",
                                    "SZ01"
                                )
                            }.await()
                            if (isSent) {
                                a++
                            }
                        }
                    }
                    if (a == igenyReveresed.size) {
                        Log.d(TAG, "onCreateView: $currentDateAndTime")
                        if (polcTextIgeny.text.isEmpty() && igenyReveresed.size == 0) {
                            sendBinCode2.closeContainer2(7, currentDateAndTime)
                            CoroutineScope(Main).launch {
                                setProgressBarOff()
                                clearAll()
                                Log.d(TAG, "onCreateView: lezártam az üreset")
                            }
                            mainActivity.loadMenuFragment(true)
                        } else {
                            sendBinCode2.closeContainer2(7, currentDateAndTime)
                            CoroutineScope(Main).launch {
                                setProgressBarOff()
                                clearAll()
                                Log.d(TAG, "onCreateView: lezártam amibe volt adat")
                            }
                            mainActivity.loadMenuFragment(true)
                        }
                    } else {
                        CoroutineScope(Main).launch {
                            mainActivity.setAlert("Nem teljes a siker")
                        }
                    }
                }
            } else {
                mainActivity.setAlert("Nincs semmilyen cikk felvéve igénynek")
            }
        }

        return view
    }

    private fun clearAll() {
        lezarButton.requestFocus()
        kontenerText.text = ""
        polcTextIgeny.setText("")
        TextKeyListener.clear(polcTextIgeny.text)
        polcTextIgeny.isEnabled = false
        polcTextIgeny.isFocusable = false
        polcTextIgeny.isFocusableInTouchMode = false
        igenyList.clear()
        megjegyzes1_igeny.text = ""
        megjegyzes2_igeny2.text = ""
        mennyiseg_igeny2.setText("")
        TextKeyListener.clear(mennyiseg_igeny2.text)
        mennyiseg_igeny2.isEnabled = false
        mennyiseg_igeny2.isFocusable = false
        mennyiseg_igeny2.isFocusableInTouchMode = false
        unit_igeny2.text = ""
        intrem_igeny2.text = ""
        cikkItem_igeny.setText("")
        TextKeyListener.clear(cikkItem_igeny.text)
        cikkItem_igeny.isFocusable = false
        cikkItem_igeny.isFocusableInTouchMode = false
        igenyReveresed.clear()
        recyclerView.adapter?.notifyDataSetChanged()
        mainActivity.loadMenuFragment(true)
    }

    fun setProgressBarOff() {
        progressBar.visibility = View.GONE
    }

    fun setProgressBarOn() {
        progressBar.visibility = View.VISIBLE
    }

    fun setBinFocusOn() {
        polcTextIgeny.requestFocus()
    }

    fun setFocusToItem(code: String) {
        polcTextIgeny.setText(code)
        cikkItem_igeny.isFocusable = true
        cikkItem_igeny.requestFocus()
        cikkItem_igeny.selectAll()
        polcTextIgeny.isFocusable = false
        polcTextIgeny.isFocusableInTouchMode = false
    }

    fun setFocusToQuantity() {
        mennyiseg_igeny2.isEnabled = true
        mennyiseg_igeny2.selectAll()
        mennyiseg_igeny2.requestFocus()
        cikkItem_igeny.isFocusable = false
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
        megjegyzes1_igeny.text = megj
        megjegyzes2_igeny2.text = megj2
        intrem_igeny2.text = intRem
        unit_igeny2.text = unit
    }

    override fun igenyClick(position: Int) {
        Log.d("igenyitem", "igenyClick: $position")
    }

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
            recyclerView.adapter?.notifyDataSetChanged()
        }
    }

    override fun onResume() {
        super.onResume()
        kontenerText.text = arguments?.getString("KONTENER")
        polcTextIgeny.setText(arguments?.getString("TERMRAKH"))
        if (polcTextIgeny.text.isNotEmpty()) {
            cikkItem_igeny.isFocusable = true
            cikkItem_igeny.requestFocus()
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
        if (polcTextIgeny.text.isEmpty()) {
            //polcTextIgeny.setText(code)
            sendBinCode2.sendBinCode2(code)
        } else {
            cikkItem_igeny.setText(code)
            mainActivity.isItem2(code, polcTextIgeny.text.trim().toString())
        }
    }

    fun onKilepPressed() {
        kilepButton.performClick()
    }
}