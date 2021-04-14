package com.fusetech.virtualkanban.Fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fusetech.virtualkanban.Activities.MainActivity
import com.fusetech.virtualkanban.Adapters.KontenerbenLezarasAdapter
import com.fusetech.virtualkanban.DataItems.KontenerbenLezarasItem
import com.fusetech.virtualkanban.R
import kotlinx.android.synthetic.main.kontenerben_lezaras_view.view.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private lateinit var recycler: RecyclerView
private val kontItem: ArrayList<KontenerbenLezarasItem> = ArrayList()
private lateinit var exitBtn: Button
private lateinit var lezarBtn: Button
private lateinit var mainActivity: MainActivity
private lateinit var kontenerNev: TextView
private const val TAG = "IgenyKontenerLezarasCik"

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
        mainActivity = activity as MainActivity
        recycler = view.child_recycler2
        exitBtn = view.exit3CikkButton
        lezarBtn = view.lezar3Button
        kontenerNev = view.kontenerNameLezaras
        recycler.adapter = KontenerbenLezarasAdapter(kontItem)
        recycler.layoutManager = LinearLayoutManager(view.context)
        recycler.setHasFixedSize(true)
        recycler.requestFocus()
        kontItem.clear()
        kontenerNev.text = ""
        loadData()
        recycler.adapter?.notifyDataSetChanged()

        exitBtn.setOnClickListener {
            kontItem.clear()
            mainActivity.removeIgenyFragment()
            mainActivity.igenyKontenerCheck()
        }
        lezarBtn.setOnClickListener {
            mainActivity.closeContainerAndItem()
            kontItem.clear()
            mainActivity.loadMenuFragment(true)
        }
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
        try {
            val myList: ArrayList<KontenerbenLezarasItem> = arguments?.getSerializable("CIKKLEZAR") as ArrayList<KontenerbenLezarasItem>
            for(i in 0 until myList.size){
                kontItem.add(KontenerbenLezarasItem(myList[i].cikkszam,myList[i].megjegyzes1,myList[i].megjegyzes2,myList[i].intrem,myList[i].igeny,myList[i].kiadva))
            }
            kontenerNev.text = arguments?.getString("KONTENER_ID")
        }catch (e: Exception){
            Log.d(TAG, "loadData: $e")
        }

    }
}