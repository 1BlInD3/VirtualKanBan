package com.fusetech.virtualkanban.Fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fusetech.virtualkanban.Activities.MainActivity.Companion.tempLocations
import com.fusetech.virtualkanban.R
import kotlinx.android.synthetic.main.fragment_tobblet_cikkek_polcra.view.*
import com.fusetech.virtualkanban.Activities.MainActivity.Companion.tobbletItem
import com.fusetech.virtualkanban.Adapters.PolcLocationAdapter
import com.fusetech.virtualkanban.DataItems.PolcLocation

@Suppress("UNCHECKED_CAST")
class TobbletCikkekPolcraFragment : Fragment(), PolcLocationAdapter.PolcItemClickListener {

    private lateinit var kontenerID : TextView
    private lateinit var cikkID : TextView
    private lateinit var cikkNumber : EditText
    private lateinit var megjegyzes1: TextView
    private lateinit var megjegyzes2 : TextView
    private lateinit var intrem : TextView
    private lateinit var unit : TextView
    private lateinit var igeny : EditText
    private lateinit var polc : EditText
    private lateinit var mennyiseg : EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar : ProgressBar
    private lateinit var lezarasBtn : Button
    private lateinit var visszaBtn : Button
    private var cikkid = 0
    private var kontid = 0
    private var cikkkod = ""
    private var mmegjegyzes1 = ""
    private var mmegjegyzes2 = ""
    private var mintrem = ""
    private var munit = ""
    private var mcikkszam = ""
    private var mmennyiseg : String? = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tobblet_cikkek_polcra, container, false)
        kontenerID = view.tkontenerIDKiszedes
        cikkID = view.tcikkIDKiszedes
        cikkNumber = view.tkiszedesCikkEdit
        megjegyzes1 = view.tkiszedesMegj1
        megjegyzes2 = view.tkiszedesMegj2
        intrem = view.tintrem
        unit = view.tkiszedesUnit
        igeny = view.tkiszedesIgenyEdit
        polc = view.tkiszedesPolc
        mennyiseg = view.tkiszedesMennyiseg
        recyclerView = view.tlocationRecycler
        progressBar = view.tkihelyezesProgress
        lezarasBtn = view.tkiszedesLezar
        visszaBtn = view.tkiszedesVissza
        recyclerView.adapter = PolcLocationAdapter(tempLocations,this)
        recyclerView.layoutManager = LinearLayoutManager(view.context)
        recyclerView.setHasFixedSize(true)
        loadData()

        return view
    }

    override fun onResume() {
        super.onResume()
        cikkid = arguments?.getInt("IID")!!
        kontid = arguments?.getInt("KID")!!
        cikkkod = arguments?.getString("MCIKK")!!
        mmegjegyzes1 = arguments?.getString("MMEGJ1")!!
        mmegjegyzes2 = arguments?.getString("MMEGJ2")!!
        mintrem = arguments?.getString("IINT")!!
        munit = arguments?.getString("UUNIT")!!
        mcikkszam = arguments?.getString("MCIKK")!!
        fillWidgets()
    }
    private fun fillWidgets(){
        kontenerID.text = kontid.toString()
        cikkID.text = cikkid.toString()
        cikkNumber.setText(cikkkod)
        megjegyzes1.text = mmegjegyzes1
        megjegyzes2.text = mmegjegyzes2
        intrem.text = mintrem
        unit.text = munit
        mmennyiseg = arguments?.getString("MMENY")
        igeny.setText(mmennyiseg)

    }
    private fun loadData(){
        tempLocations.clear()
        val myList: ArrayList<PolcLocation> = arguments?.getSerializable("LOCATIONBIN") as ArrayList<PolcLocation>
        if(myList.size > 0) {
            for (i in 0 until myList.size){
                tempLocations.add(PolcLocation(myList[i].polc,myList[i].mennyiseg))
            }
            recyclerView.adapter?.notifyDataSetChanged()
        }
    }

    override fun polcItemClick(position: Int) {
        Log.d("TAG", "polcItemClick: ")
    }

}