package com.fusetech.virtualkanban.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fusetech.virtualkanban.Adapters.KontenerbenLezarasAdapter
import com.fusetech.virtualkanban.R
import kotlinx.android.synthetic.main.fragment_tobblet_kontener_cikkek.view.*
import com.fusetech.virtualkanban.Activities.MainActivity.Companion.tobbletItem
import com.fusetech.virtualkanban.DataItems.KontenerbenLezarasItem


@Suppress("UNCHECKED_CAST")
class TobbletKontenerCikkekFragment : Fragment(), KontenerbenLezarasAdapter.onItemClickListener {
    private lateinit var recycler: RecyclerView
    private lateinit var vissza: Button
    private lateinit var kontener: TextView
    var kontenerID: String? = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tobblet_kontener_cikkek, container, false)
        recycler = view.kihelyezesRecycler
        vissza = view.visszaTobbletButton
        kontener = view.kontenerIDText
        recycler.adapter = KontenerbenLezarasAdapter(tobbletItem, this)
        recycler.layoutManager = LinearLayoutManager(view.context)
        recycler.setHasFixedSize(true)
        loadData()
        kontener.setText(kontenerID)

        recycler.adapter?.notifyDataSetChanged()
        return view
    }

    override fun onItemClick(position: Int) {
        TODO("Not yet implemented")
    }

    fun loadData() {
        val myList: ArrayList<KontenerbenLezarasItem> =
            arguments?.getSerializable("TOBBLETESCIKKEK") as ArrayList<KontenerbenLezarasItem>
        kontenerID = arguments?.getString("KONTENERTOBBLETCIKK")
        for (i in 0 until myList.size) {
            tobbletItem.add(
                KontenerbenLezarasItem(
                    myList[i].cikkszam,
                    myList[i].megjegyzes1,
                    myList[i].megjegyzes2,
                    myList[i].intrem,
                    myList[i].igeny,
                    myList[i].kiadva,
                    myList[i].statusz,
                    myList[i].unit,
                    myList[i].id,
                    myList[i].kontener_id
                )
            )
        }
        recycler.adapter?.notifyDataSetChanged()
    }
    fun getContainer(): String{
        return kontenerID!!
    }

}