package com.fusetech.virtualkanban.Fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fusetech.virtualkanban.Adapters.PolcLocationAdapter
import com.fusetech.virtualkanban.DataItems.PolcLocation
import com.fusetech.virtualkanban.R
import kotlinx.android.synthetic.main.fragment_polc_location.view.*


class KiszedesLocationFragment : Fragment(), PolcLocationAdapter.PolcItemClickListener {

    private val myLocations: ArrayList<PolcLocation> = ArrayList()
    private lateinit var recycler: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_polc_location, container, false)
        val child = layoutInflater.inflate(R.layout.polc_location_header,null)
        val frameLayout = view.myFrameLayout
        frameLayout.addView(child)
        recycler = view.polcRecycler
        recycler.adapter = PolcLocationAdapter(myLocations,this)
        recycler.layoutManager = LinearLayoutManager(view.context)
        recycler.setHasFixedSize(true)
        loadData()
        recycler.adapter?.notifyDataSetChanged()
        return view
    }

    private fun loadData(){
        myLocations.clear()
        val myLocationList: ArrayList<PolcLocation> = arguments?.getSerializable("K_LOCATION") as ArrayList<PolcLocation>
        for(i in 0 until myLocationList.size){
            myLocations.add(PolcLocation(myLocationList[i].polc,myLocationList[i].mennyiseg))
        }
    }

    override fun polcItemClick(position: Int) {
        Log.d("KiszedesLocationFragment", "polcItemClick: ")
    }
}