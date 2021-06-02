package com.fusetech.virtualkanban.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fusetech.virtualkanban.Adapters.KihelyezesKontenerAdapter
import com.fusetech.virtualkanban.DataItems.KihelyezesKontenerElemek
import com.fusetech.virtualkanban.R
import kotlinx.android.synthetic.main.kihelyezes_header.view.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class KihelyezesListaFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var recycler: RecyclerView
    val myList: ArrayList<KihelyezesKontenerElemek> = ArrayList()

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
        val view = inflater.inflate(R.layout.kihelyezes_header, container, false)
        recycler = view.recKihelyezesLista
        recycler.adapter = KihelyezesKontenerAdapter(myList)
        recycler.layoutManager = LinearLayoutManager(view.context)
        recycler.setHasFixedSize(true)

        myList.add(KihelyezesKontenerElemek(1,"03011001","Doboz","Doboz2","DD","10 db","2db"))

        recycler.adapter?.notifyDataSetChanged()
        return view
    }

}