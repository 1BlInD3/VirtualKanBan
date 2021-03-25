package com.fusetech.virtualkanban.Fragments

import android.content.Context
import android.os.Bundle
import android.text.InputFilter
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fusetech.virtualkanban.Activities.MainActivity
import com.fusetech.virtualkanban.Adapters.IgenyItemAdapter
import com.fusetech.virtualkanban.DataItems.IgenyItem
import com.fusetech.virtualkanban.R
import kotlinx.android.synthetic.main.fragment_igeny_kontener_osszeallitas.*
import kotlinx.android.synthetic.main.fragment_igeny_kontener_osszeallitas.view.*
import org.w3c.dom.Text

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private lateinit var kontenerText: TextView
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
private lateinit var recyclerView: RecyclerView
private var igenyList: ArrayList<IgenyItem> = ArrayList()
private var igenyReveresed: ArrayList<IgenyItem> = ArrayList()
private lateinit var kilepButton: Button

class IgenyKontenerOsszeallitasFragment : Fragment(), IgenyItemAdapter.IgenyItemClick {
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
        kontenerText = view.container_igeny
        progressBar = view.progressBar_igeny
        polcTextIgeny = view.bin_igeny
        megjegyzes1_igeny = view.megjegyzes_igeny
        megjegyzes2_igeny2 = view.megjegyzes2_igeny
        intrem_igeny2 = view.intrem_igeny
        unit_igeny2 = view.unit_igeny
        cikkItem_igeny = view.cikk_igeny
        mennyiseg_igeny2 = view.mennyiseg_igeny
        kilepButton = view.kilep_igeny_button
        kontenerText.text = param1
        polcTextIgeny.setText(param2)
        setBinFocusOn()
        if(!polcTextIgeny.text.isEmpty()){
            polcTextIgeny.isEnabled = false
            cikkItem_igeny.isEnabled = true
            cikkItem_igeny.requestFocus()
            //getDataFromList()
        }
        megjegyzes1_igeny.text = ""
        megjegyzes2_igeny2.text = ""
        intrem_igeny2.text = ""
        unit_igeny2.text = ""
        polcTextIgeny.filters = arrayOf<InputFilter>(InputFilter.AllCaps())
        unit_igeny2.filters = arrayOf<InputFilter>(InputFilter.AllCaps())
        setProgressBarOff()
        recyclerView = view.recycler_igeny
        recyclerView.isEnabled = false
        recyclerView.adapter = IgenyItemAdapter(igenyReveresed,this)
        recyclerView.layoutManager = LinearLayoutManager(view.context)
        recyclerView.setHasFixedSize(true)


        polcTextIgeny.setOnClickListener {
           sendBinCode.sendBinCode(polcTextIgeny.text.toString())
        }
        cikkItem_igeny.setOnClickListener {
            mainActivity.isItem(cikkItem_igeny.text.toString())
        }
        mennyiseg_igeny2.setOnClickListener {
            igenyList.add(IgenyItem(cikkItem_igeny.text.toString().trim(), megjegyzes1_igeny.text.toString().trim(),
                mennyiseg_igeny2.text.toString().trim()))
            if(igenyList.size == 1){
                igenyReveresed.clear()
                igenyReveresed.add(IgenyItem(igenyList[0].cikkszam,igenyList[0].megnevezes,igenyList[0].mennyiseg))
                recyclerView.adapter?.notifyDataSetChanged()
            }
            else if(igenyList.size > 1){
                igenyReveresed.clear()
                for(i in igenyList.size downTo 1){
                    igenyReveresed.add(IgenyItem(igenyList[i-1].cikkszam,igenyList[i-1].megnevezes,igenyList[i-1].mennyiseg))
                }
                recyclerView.adapter?.notifyDataSetChanged()
            }
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
            clearAll()
        }
        return view
    }

    fun clearAll(){
        igenyList.clear()
        igenyReveresed.clear()
        recyclerView.adapter?.notifyDataSetChanged()
        megjegyzes1_igeny.text = ""
        megjegyzes2_igeny2.text = ""
        unit_igeny2.text = ""
        intrem_igeny2.text = ""
        polcTextIgeny.setText("")
        cikkItem_igeny.setText("")
        mainActivity.loadMenuFragment(true)
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
        mennyiseg_igeny2.isEnabled = true
        mennyiseg_igeny2.selectAll()
        mennyiseg_igeny2.requestFocus()
        cikkItem_igeny.isEnabled = false
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

    override fun igenyClick(position: Int) {
        Log.d("igenyitem", "igenyClick: $position")
    }
    fun getDataFromList(){
        val myList: ArrayList<IgenyItem> = arguments?.getSerializable("IGENY") as ArrayList<IgenyItem>
        for(i in 0 until myList.size){
            igenyReveresed.add(IgenyItem(myList[i].cikkszam,myList[i].megnevezes,myList[i].mennyiseg))
        }
        recyclerView.adapter?.notifyDataSetChanged()
    }
}