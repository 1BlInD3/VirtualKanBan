package com.fusetech.virtualkanban.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fusetech.virtualkanban.adapters.CikkItemAdapter
import com.fusetech.virtualkanban.dataItems.CikkItems
import com.fusetech.virtualkanban.R
import com.fusetech.virtualkanban.activities.MainActivity
import kotlinx.android.synthetic.main.fragment_cikk_result.view.*


class CikkResultFragment : Fragment() {

    private var recyclerView: RecyclerView? = null
    private var megjegyzes1Txt: TextView? = null
    private var megjegyzes2Txt: TextView? = null
    private var unitTxt: TextView? = null
    private var intRemTxt: TextView? = null
    var myCikkItems: ArrayList<CikkItems> = ArrayList()
    private var myView: View? = null
    private var frameLayout: FrameLayout? = null
    private var child: View? = null
    private var mainActivity: MainActivity? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mainActivity = activity as MainActivity
        myView = inflater.inflate(R.layout.fragment_cikk_result, container, false)

        frameLayout = myView?.cikkHeaderFrame
        child = layoutInflater.inflate(R.layout.cikk_header,null)
        frameLayout?.addView(child)

        recyclerView = myView?.cikkRecycler
        megjegyzes1Txt = myView?.megjegyzes1CikkText
        megjegyzes2Txt = myView?.megjegyzes2CikkText
        intRemTxt = myView?.intRemCikkText
        unitTxt = myView?.unitCikkText

        megjegyzes1Txt?.text = arguments?.getString("megjegyzes")
        megjegyzes2Txt?.text = arguments?.getString("megjegyzes2")
        unitTxt?.text = arguments?.getString("unit")
        intRemTxt?.text = arguments?.getString("intrem")

        myCikkItems.clear()
        loadCikkItems()

        recyclerView?.adapter = CikkItemAdapter(myCikkItems)
        recyclerView?.layoutManager = LinearLayoutManager(myView?.context)
        recyclerView?.setHasFixedSize(true)
        recyclerView?.isFocusable = false
        frameLayout?.isFocusable = false

        return myView
    }
    private fun loadCikkItems() {
        val myList: ArrayList<CikkItems> = arguments?.getSerializable("cikk") as ArrayList<CikkItems>
        if(myList.size>0){
            for (i in 0 until myList.size) {
                myCikkItems.add(CikkItems(myList[i].mMennyiseg,myList[i].mPolc,myList[i].mRaktar,myList[i].mAllapot))
            }
        }
    }
    fun clearLeak(){
        myView = null
        frameLayout = null
        child = null
        recyclerView = null
        recyclerView?.adapter = null
        megjegyzes1Txt = null
        megjegyzes2Txt = null
        unitTxt = null
        intRemTxt = null
        mainActivity?.removeFragment("CRF")
        mainActivity?.cikkResultFragment = null
    }

}