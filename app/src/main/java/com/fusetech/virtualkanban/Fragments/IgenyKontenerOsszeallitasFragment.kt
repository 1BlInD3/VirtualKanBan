package com.fusetech.virtualkanban.Fragments

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
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fusetech.virtualkanban.Activities.MainActivity
import com.fusetech.virtualkanban.Adapters.IgenyItemAdapter
import com.fusetech.virtualkanban.DataItems.IgenyItem
import com.fusetech.virtualkanban.R
import kotlinx.android.synthetic.main.fragment_igeny_kontener_osszeallitas.*
import kotlinx.android.synthetic.main.fragment_igeny_kontener_osszeallitas.view.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
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
private lateinit var recyclerView: RecyclerView
private lateinit var lezarButton: Button
private var igenyList: ArrayList<IgenyItem> = ArrayList()
private var igenyReveresed: ArrayList<IgenyItem> = ArrayList()
private lateinit var kilepButton: Button
private const val TAG = "IgenyKontenerOsszeallit"
private lateinit var sendBinCode: IgenyKontenerOsszeallitasFragment.SendBinCode

class IgenyKontenerOsszeallitasFragment : Fragment(), IgenyItemAdapter.IgenyItemClick {
    private var param1: String? = null
    private var param2: String? = null

    interface SendBinCode {
        fun sendBinCode(code: String)
        fun sendDetails(
            cikkszam: String,
            mennyiseg: Double,
            term_rakhely: String,
            unit: String,
            kontener: String
        )

        fun closeContainer(statusz: Int, datum: String)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    @SuppressLint("SimpleDateFormat")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_igeny_kontener_osszeallitas, container, false)
        mainActivity = activity as MainActivity
        recyclerView = view.recycler_igeny
        recyclerView.isEnabled = false
        recyclerView.adapter = IgenyItemAdapter(igenyReveresed, this)
        recyclerView.layoutManager = LinearLayoutManager(view.context)
        recyclerView.setHasFixedSize(true)
        lezarButton = view.lezar_igeny
        kontenerText = view.container_igeny
        progressBar = view.progressBar_igeny
        polcTextIgeny = view.bin_igeny
        megjegyzes1_igeny = view.megjegyzes_igeny
        megjegyzes2_igeny2 = view.megjegyzes2_igeny
        intrem_igeny2 = view.intrem_igeny
        unit_igeny2 = view.unit_igeny
        cikkItem_igeny = view.cikk_igeny
        mennyiseg_igeny2 = view.mennyiseg_igeny
        mennyiseg_igeny2.filters = arrayOf<InputFilter>(DecimalDigitsInputFilter(9, 2))
        mennyiseg_igeny2.isFocusable = false
        mennyiseg_igeny2.isFocusableInTouchMode = false
        kilepButton = view.kilep_igeny_button
        cikkItem_igeny.isFocusable = false
        cikkItem_igeny.isFocusableInTouchMode = false
        kontenerText.text = arguments?.getString("KONTENER")
        polcTextIgeny.setText(arguments?.getString("TERMRAKH"))
        Log.d(TAG, "onCreateView: ${arguments?.getString("KONTENER")}")
        Log.d(TAG, "onCreateView: ${arguments?.getString("TERMRAKH")}")
        setBinFocusOn()
        if (polcTextIgeny.text.isNotEmpty()) {
            polcTextIgeny.isFocusable = false
            polcTextIgeny.isFocusableInTouchMode = false
            cikkItem_igeny.isFocusable = true
            cikkItem_igeny.isFocusableInTouchMode = true
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
            sendBinCode.sendBinCode(polcTextIgeny.text.toString())
        }
        cikkItem_igeny.setOnClickListener {
            mainActivity.isItem(cikkItem_igeny.text.toString())
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
            sendBinCode.sendDetails(
                cikkItem_igeny.text.toString().trim(), mennyiseg_igeny2.text.toString().toDouble(),
                polcTextIgeny.text.toString().trim(), unit_igeny2.text.toString(),
                konti
            )
            cikkItem_igeny.isFocusable = true
            cikkItem_igeny.isFocusableInTouchMode = true
            cikkItem_igeny.selectAll()
            cikkItem_igeny.requestFocus()
            mennyiseg_igeny2.setText("")
            mennyiseg_igeny2.isFocusable = false
            mennyiseg_igeny2.isFocusableInTouchMode = false
            megjegyzes2_igeny2.text = ""
            intrem_igeny2.text = ""
            unit_igeny2.text = ""
            megjegyzes1_igeny.text = ""
        }

        kilepButton.setOnClickListener {
            mainActivity.listIgenyItems.clear()
            clearAll()
            mainActivity.loadMenuFragment(true)
        }
        lezarButton.setOnClickListener {
            setProgressBarOn()
            val currentDateAndTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
            Log.d(TAG, "onCreateView: $currentDateAndTime")
            if (polcTextIgeny.text.isEmpty() && igenyReveresed.size == 0) {
                sendBinCode.closeContainer(5, currentDateAndTime)
                setProgressBarOff()
                clearAll()
                mainActivity.loadMenuFragment(true)
                Log.d(TAG, "onCreateView: lezártam az üreset")
            } else {
                sendBinCode.closeContainer(1, currentDateAndTime) // ezt 1esre kéne átírni
                setProgressBarOff()
                clearAll()
                mainActivity.loadMenuFragment(true)
                Log.d(TAG, "onCreateView: lezártam amibe volt adat")
            }
        }
        return view
    }

    fun clearAll() {
        mainActivity.listIgenyItems.clear()
        kontenerText.text = ""
        igenyList.clear()
        igenyReveresed.clear()
        recyclerView.adapter?.notifyDataSetChanged()
        megjegyzes1_igeny.text = ""
        megjegyzes2_igeny2.text = ""
        unit_igeny2.text = ""
        intrem_igeny2.text = ""
        mennyiseg_igeny2.setText("")
        mennyiseg_igeny2.isFocusable = false
        mennyiseg_igeny2.isFocusableInTouchMode = false
        cikkItem_igeny.setText("")
        cikkItem_igeny.isFocusable = false
        cikkItem_igeny.isFocusableInTouchMode = false
        polcTextIgeny.setText("")
        polcTextIgeny.isFocusable = false
        polcTextIgeny.isFocusableInTouchMode = false
        lezarButton.requestFocus()
    }

    fun setProgressBarOff() {
        progressBar.visibility = View.GONE
    }

    fun setProgressBarOn() {
        progressBar.visibility = View.VISIBLE
    }

    fun setBinFocusOn() {
        polcTextIgeny.selectAll()
        polcTextIgeny.requestFocus()
    }

    fun setFocusToItem() {
        cikkItem_igeny.isFocusable = true
        cikkItem_igeny.isFocusableInTouchMode = true
        cikkItem_igeny.requestFocus()
        cikkItem_igeny.selectAll()
        polcTextIgeny.isFocusable = false
        polcTextIgeny.isFocusableInTouchMode = false
    }

    fun setFocusToQuantity() {
        mennyiseg_igeny2.isFocusable = true
        mennyiseg_igeny2.isFocusableInTouchMode = true
        mennyiseg_igeny2.selectAll()
        mennyiseg_igeny2.requestFocus()
        cikkItem_igeny.isFocusable = false
        cikkItem_igeny.isFocusableInTouchMode = false
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
        megjegyzes2_igeny2.text = megj2
        intrem_igeny2.text = intRem
        unit_igeny2.text = unit
    }

    override fun igenyClick(position: Int) {
        Log.d("igenyitem", "igenyClick: $position")
    }

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
            recyclerView.adapter?.notifyDataSetChanged()
        }
    }

    override fun onResume() {
        super.onResume()
        kontenerText.text = arguments?.getString("KONTENER")
        polcTextIgeny.setText(arguments?.getString("TERMRAKH"))
    }

    class DecimalDigitsInputFilter(digitsBeforeZero: Int, digitsAfterZero: Int) :
        InputFilter {
        private var mPattern: Pattern = Pattern.compile("[0-9]{0," + (digitsBeforeZero - 1) + "}+((\\.[0-9]{0," + (digitsAfterZero - 1) + "})?)||(\\.)?")
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
        if (polcTextIgeny.text.isEmpty()) {
            polcTextIgeny.setText(code)
            sendBinCode.sendBinCode(code)
        } else {
            cikkItem_igeny.setText(code)
            mainActivity.isItem(code)
        }
    }
}