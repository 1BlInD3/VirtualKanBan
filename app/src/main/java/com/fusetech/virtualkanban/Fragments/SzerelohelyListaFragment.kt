package com.fusetech.virtualkanban.fragments

import android.os.Bundle
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


@Suppress("UNCHECKED_CAST")
class SzerelohelyListaFragment : Fragment() {

    private lateinit var recycler : RecyclerView
    //private val myList: ArrayList<SzerelohelyItem> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_szerelohely_lista, container, false)
        recycler = view.recyclerHely
        recycler.adapter = SzerelohelyItemAdapter(kihelyezesItems)
        recycler.layoutManager = GridLayoutManager(view.context,3)
        recycler.setHasFixedSize(true)
        recycler.isFocusable = false
        recycler.isFocusableInTouchMode = false
        getData()

        return view
    }

    fun getData(){
        kihelyezesItems.clear()
        val lista : ArrayList<SzerelohelyItem> = arguments?.getSerializable("KILISTA") as ArrayList<SzerelohelyItem>
        for (i in 0 until lista.size){
            kihelyezesItems.add(SzerelohelyItem(lista[i].szerelohely))
        }
       recycler.adapter?.notifyDataSetChanged()
    }
}