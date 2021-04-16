package com.fusetech.virtualkanban.Fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fusetech.virtualkanban.Activities.MainActivity
import com.fusetech.virtualkanban.Adapters.KontenerAdapter
import com.fusetech.virtualkanban.DataItems.KontenerItem
import com.fusetech.virtualkanban.R
import kotlinx.android.synthetic.main.fragment_igeny_kontener_kiszedes_megnyitott.view.*


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private lateinit var recycler: RecyclerView
private lateinit var exitBtn: Button
private lateinit var mainAcitivity: MainActivity
private val kontenerList: ArrayList<KontenerItem> =  ArrayList()
private const val TAG = "IgenyKontenerKiszedesMe"

class IgenyKontenerKiszedesMegnyitottFragment : Fragment(),KontenerAdapter.onKontenerClickListener {
    private var param1: String? = null
    private var param2: String? = null

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
        val view = inflater.inflate(R.layout.fragment_igeny_kontener_kiszedes_megnyitott,container,false)
        mainAcitivity = activity as MainActivity
        exitBtn = view.exit4Button
        recycler = view.child_recycler_open
        recycler.adapter = KontenerAdapter(kontenerList,this)
        recycler.layoutManager = LinearLayoutManager(view.context)
        recycler.setHasFixedSize(true)
        kontenerList.clear()
        loadData()
        recycler.adapter?.notifyDataSetChanged()

        exitBtn.setOnClickListener {
            kontenerList.clear()
            mainAcitivity.loadMenuFragment(true)
            mainAcitivity.igenyKontenerKiszedes("6. Kiszedésre váró igénykonténerek")
        }

        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            IgenyKontenerKiszedesMegnyitottFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
    override fun onKontenerClick(position: Int) {
        mainAcitivity.setAlert("A ${kontenerList[position].kontner_id}-t már megynitották")
    }
    private fun loadData(){
        try{
            val myList: ArrayList<KontenerItem> = arguments?.getSerializable("MEGNYITOTTLISTA") as ArrayList<KontenerItem>
            for(i in 0 until myList.size){
                kontenerList.add(KontenerItem(myList[i].kontener,myList[i].polc,myList[i].datum,myList[i].tetelszam,myList[i].kontner_id))
            }
        }catch (e: Exception){
            Log.d(TAG, "loadData: $e")
        }
    }
    fun performButton(){
        exitBtn.performClick()
    }
}