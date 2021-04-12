package com.fusetech.virtualkanban.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fusetech.virtualkanban.Adapters.KontenerbenLezarasAdapter
import com.fusetech.virtualkanban.DataItems.KontenerbenLezarasItem
import com.fusetech.virtualkanban.R
import kotlinx.android.synthetic.main.kontenerben_lezaras_view.view.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private lateinit var recycler: RecyclerView
private val kontItem: ArrayList<KontenerbenLezarasItem> = ArrayList()

class IgenyKontenerLezarasCikkLezaras : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

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
        val view = inflater.inflate(R.layout.kontenerben_lezaras_view,container,false)
        recycler = view.child_recycler2
        recycler.adapter = KontenerbenLezarasAdapter(kontItem)
        recycler.layoutManager = LinearLayoutManager(view.context)
        recycler.setHasFixedSize(true)
        recycler.requestFocus()
        kontItem.clear()
        loadData()

        recycler.adapter?.notifyDataSetChanged()

        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            IgenyKontenerLezarasCikkLezaras().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
    private fun loadData(){
        val myList: ArrayList<KontenerbenLezarasItem> = arguments?.getSerializable("CIKKLEZAR") as ArrayList<KontenerbenLezarasItem>
        for(i in 0 until myList.size){
            kontItem.add(KontenerbenLezarasItem(myList[i].cikkszam,myList[i].megjegyzes1,myList[i].megjegyzes2,myList[i].intrem,myList[i].igeny,myList[i].kiadva))
        }
    }
}