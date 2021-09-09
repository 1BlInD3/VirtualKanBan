package com.fusetech.virtualkanban.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fusetech.virtualkanban.adapters.SzerelohelyItemAdapter
import com.fusetech.virtualkanban.dataItems.SzerelohelyItem
import com.fusetech.virtualkanban.R
import kotlinx.android.synthetic.main.fragment_szerelohely_lista.view.*
import com.fusetech.virtualkanban.activities.MainActivity.Companion.kihelyezesItems
import kotlinx.android.synthetic.main.fragment_igeny_kontener_kiszedese.*

private const val TAG = "SzerelohelyListaFragmen"

@Suppress("UNCHECKED_CAST")
class SzerelohelyListaFragment : Fragment() {

    private var recycler : RecyclerView? = null
    private var myView: View? = null
    private val mySzereloList: ArrayList<SzerelohelyItem> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        myView = inflater.inflate(R.layout.fragment_szerelohely_lista, container, false)
        recycler = myView?.recyclerHely
        mySzereloList.clear()
        recycler?.adapter = SzerelohelyItemAdapter(mySzereloList)
        recycler?.layoutManager = GridLayoutManager(myView?.context,3)
        recycler?.setHasFixedSize(true)
        recycler?.isFocusable = false
        recycler?.isFocusableInTouchMode = false

        return myView
    }

    @SuppressLint("NotifyDataSetChanged")
    fun getData(){
        //kihelyezesItems.clear()
        val lista : ArrayList<SzerelohelyItem>? = arguments?.getSerializable("KILISTA") as ArrayList<SzerelohelyItem>
        for (i in 0 until lista?.size!!){
            mySzereloList.add(SzerelohelyItem(lista[i].szerelohely))
        }
       recycler?.adapter?.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView: ")
        myView = null
        recycler = null
        recycler?.adapter = null
        
    }

    override fun onResume() {
        getData()
        super.onResume()
    }
}