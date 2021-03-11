package com.fusetech.virtualkanban.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fusetech.virtualkanban.Adapters.CikkItemAdapter
import com.fusetech.virtualkanban.DataItems.CikkItems
import com.fusetech.virtualkanban.R
import kotlinx.android.synthetic.main.fragment_cikk_result.view.*


class CikkResultFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var megjegyzes1Txt: TextView
    private lateinit var megjegyzes2Txt: TextView
    private lateinit var unitTxt: TextView
    private lateinit var intRemTxt : TextView
    var myCikkItems: ArrayList<CikkItems> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_cikk_result, container, false)

        val frameLayout = view.cikkHeaderFrame
        val child = layoutInflater.inflate(R.layout.cikk_header,null)
        frameLayout.addView(child)

        recyclerView = view.cikkRecycler
        megjegyzes1Txt = view.megjegyzes1CikkText
        megjegyzes2Txt = view.megjegyzes2CikkText
        intRemTxt = view.intRemCikkText
        unitTxt = view.unitCikkText

        megjegyzes1Txt.text = arguments?.getString("megjegyzes")
        megjegyzes2Txt.text = arguments?.getString("megjegyzes2")
        unitTxt.text = arguments?.getString("unit")
        intRemTxt.text = arguments?.getString("intrem")

        myCikkItems.clear()
        loadCikkItems()

        recyclerView.adapter = CikkItemAdapter(myCikkItems)
        recyclerView.layoutManager = LinearLayoutManager(view.context)
        recyclerView.setHasFixedSize(true)


        return view
    }
    private fun loadCikkItems() {
        val myList: ArrayList<CikkItems> = arguments?.getSerializable("cikk") as ArrayList<CikkItems>
        for (i in 0 until myList.size) {
            myCikkItems.add(CikkItems(myList[i].mMennyiseg,myList[i].mPolc,myList[i].mRaktar,myList[i].mAllapot))
        }
    }

}