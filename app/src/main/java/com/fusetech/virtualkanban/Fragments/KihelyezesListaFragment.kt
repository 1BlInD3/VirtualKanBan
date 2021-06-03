package com.fusetech.virtualkanban.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fusetech.virtualkanban.Activities.MainActivity
import com.fusetech.virtualkanban.Adapters.KihelyezesKontenerAdapter
import com.fusetech.virtualkanban.DataItems.KihelyezesKontenerElemek
import com.fusetech.virtualkanban.R
import kotlinx.android.synthetic.main.kihelyezes_header.view.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

@Suppress("UNCHECKED_CAST")
class KihelyezesListaFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var recycler: RecyclerView
    val myList: ArrayList<KihelyezesKontenerElemek> = ArrayList()
    private lateinit var kihelyezes : Button
    private lateinit var mainActivity: MainActivity

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
        mainActivity = activity as MainActivity
        kihelyezes = view.kihelyezesBtn
        recycler = view.recKihelyezesLista
        recycler.adapter = KihelyezesKontenerAdapter(myList)
        recycler.layoutManager = LinearLayoutManager(view.context)
        recycler.setHasFixedSize(true)

        getData()

        kihelyezes.setOnClickListener {
            mainActivity.setAlert("Megnyomtam a kihelyezest")
        }

        return view
    }

    fun getData() {
        val myItems: ArrayList<KihelyezesKontenerElemek> =
            arguments?.getSerializable("KIHELYEZESLISTA") as ArrayList<KihelyezesKontenerElemek>
        for (i in 0 until myItems.size) {
            myList.add(
                KihelyezesKontenerElemek(
                    myItems[i].id,
                    myItems[i].vonalkod,
                    myItems[i].megjegyzes1,
                    myItems[i].megjegyzes2,
                    myItems[i].intrem,
                    myItems[i].igenyelve,
                    myItems[i].kiadva
                )
            )
            recycler.adapter?.notifyDataSetChanged()
        }
    }

}