package com.fusetech.virtualkanban.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fusetech.virtualkanban.adapters.PolcItemAdapter
import com.fusetech.virtualkanban.dataItems.PolcItems
import com.fusetech.virtualkanban.R
import com.fusetech.virtualkanban.activities.MainActivity
import kotlinx.android.synthetic.main.fragment_polc_result.view.*

class PolcResultFragment : Fragment() {

    private var recyclerView: RecyclerView? = null
    var myView: View? = null
    var child: View? = null
    var frameLayout: FrameLayout? =null
    var mainActivity: MainActivity? = null
    var horizontalScrollView: HorizontalScrollView? = null
    var frame2: FrameLayout? = null
    var constraint: ConstraintLayout? = null
    private val myPolcList: ArrayList<PolcItems> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        myView = inflater.inflate(R.layout.fragment_polc_result, container, false)
        mainActivity = activity as MainActivity
        frameLayout = myView?.polcHeaderFrame
        child = layoutInflater.inflate(R.layout.polc_header,null)
        horizontalScrollView = child?.horizontalScrollView
        frameLayout?.addView(child)
        frame2 = child?.polcHeaderFrame
        constraint = child?.clayout
        recyclerView = myView?.polcRecycler
        myPolcList.clear()
        loadPolcItems()
        recyclerView?.adapter = PolcItemAdapter(myPolcList)
        recyclerView?.layoutManager = LinearLayoutManager(myView?.context)
        recyclerView?.setHasFixedSize(true)
        recyclerView?.isFocusable = false
        frameLayout?.isFocusable = false

        return myView
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

    override fun onDestroyView() {
        super.onDestroyView()
        myView = null
        horizontalScrollView = null
        frame2 = null
        constraint = null
        child = null
        frameLayout = null
        recyclerView = null
        recyclerView?.adapter = null
        mainActivity?.removeFragment("PRF")
        mainActivity?.polcResultFragment = null
        mainActivity?.cikklekerdezesFragment = null
        mainActivity = null
    }
}