package com.fusetech.virtualkanban.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fusetech.virtualkanban.Adapters.SzerelohelyItemAdapter
import com.fusetech.virtualkanban.DataItems.SzerelohelyItem
import com.fusetech.virtualkanban.R
import kotlinx.android.synthetic.main.fragment_szerelohely_lista.view.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

@Suppress("UNCHECKED_CAST")
class SzerelohelyListaFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var recycler : RecyclerView
    private val myList: ArrayList<SzerelohelyItem> = ArrayList()

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
        val view = inflater.inflate(R.layout.fragment_szerelohely_lista, container, false)
        recycler = view.recyclerHely
        recycler.adapter = SzerelohelyItemAdapter(myList)
        recycler.layoutManager = GridLayoutManager(view.context,3)
        recycler.setHasFixedSize(true)

        getData()

        return view
    }

    fun getData(){
        val lista : ArrayList<SzerelohelyItem> = arguments?.getSerializable("KILISTA") as ArrayList<SzerelohelyItem>
        for (i in 0 until lista.size){
            myList.add(SzerelohelyItem(lista[i].szerelohely))
        }
       recycler.adapter?.notifyDataSetChanged()
    }
}