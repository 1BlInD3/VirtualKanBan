package com.fusetech.virtualkanban.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fusetech.virtualkanban.Adapters.SzerelohelyItemAdapter
import com.fusetech.virtualkanban.DataItems.SzerelohelyItem
import com.fusetech.virtualkanban.R
import kotlinx.android.synthetic.main.fragment_igeny_kontener_kiszedese.view.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class IgenyKontenerKiszedese : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var recycler : RecyclerView
    private val lista: ArrayList<SzerelohelyItem> = ArrayList()

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
        val view = inflater.inflate(R.layout.fragment_igeny_kontener_kiszedese, container, false)
        recycler = view.recyclerSzerelo
        recycler.adapter = SzerelohelyItemAdapter(lista)
        //recycler.layoutManager = LinearLayoutManager(view.context)
        recycler.layoutManager = GridLayoutManager(view.context,3)
        recycler.setHasFixedSize(true)
        lista.add(SzerelohelyItem("LM1"))
        lista.add(SzerelohelyItem("LM2"))
        lista.add(SzerelohelyItem("LM3"))
        lista.add(SzerelohelyItem("LM4"))
        recycler.adapter?.notifyDataSetChanged()
        return view
    }

    /*companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment IgenyKontenerKiszedese.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            IgenyKontenerKiszedese().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }*/
}