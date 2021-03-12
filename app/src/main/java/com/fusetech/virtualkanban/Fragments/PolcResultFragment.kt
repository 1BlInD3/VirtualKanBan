package com.fusetech.virtualkanban.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fusetech.virtualkanban.Adapters.PolcItemAdapter
import com.fusetech.virtualkanban.DataItems.PolcItems
import com.fusetech.virtualkanban.R
import kotlinx.android.synthetic.main.fragment_polc_result.view.*

class PolcResultFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private val myPolcList: ArrayList<PolcItems> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_polc_result, container, false)

        val frameLayout = view.polcHeaderFrame
        val child = layoutInflater.inflate(R.layout.polc_header,null)
        frameLayout.addView(child)

        recyclerView = view.polcRecycler

        myPolcList.clear()
        loadPolcItems()
        recyclerView.adapter = PolcItemAdapter(myPolcList)
        recyclerView.layoutManager = LinearLayoutManager(view.context)
        recyclerView.setHasFixedSize(true)
        recyclerView.isFocusable = false
        frameLayout.isFocusable = false

        return view
    }
    private fun loadPolcItems() {
        val myList: ArrayList<PolcItems> = arguments?.getSerializable("polc") as ArrayList<PolcItems>
        for(i in 0 until myList.size){
            myPolcList.add(
                PolcItems(myList[i].mMennyiseg, myList[i].mEgyseg,
                myList[i].mMegnevezes1,myList[i].mMegnevezes2, myList[i].mIntRem, myList[i].mAllapot)
            )
        }
    }

}