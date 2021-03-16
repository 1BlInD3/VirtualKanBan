package com.fusetech.virtualkanban.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fusetech.virtualkanban.Adapters.PolcItemAdapter
import com.fusetech.virtualkanban.Adapters.PolcLocationAdapter
import com.fusetech.virtualkanban.DataItems.PolcLocation
import com.fusetech.virtualkanban.R
import kotlinx.android.synthetic.main.fragment_polc_location.view.*

class PolcLocationFragment : Fragment() {
   private lateinit var recyclerView: RecyclerView
    private val myItems: ArrayList<PolcLocation> = ArrayList()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =  inflater.inflate(R.layout.fragment_polc_location, container, false)

        val frameLayout = view.myFrameLayout
        val child = layoutInflater.inflate(R.layout.polc_location_header,null)
        frameLayout.addView(child)

        recyclerView = view.polcRecycler
        myItems.clear()
        myItems.add(PolcLocation("H220","5"))
        myItems.add(PolcLocation("H221","51"))
        recyclerView.adapter = PolcLocationAdapter(myItems)
        recyclerView.layoutManager = LinearLayoutManager(view.context)
        recyclerView.setHasFixedSize(true)

        return view
    }
}