package com.fusetech.virtualkanban.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fusetech.virtualkanban.Activities.MainActivity
import com.fusetech.virtualkanban.Adapters.SzerelohelyItemAdapter
import com.fusetech.virtualkanban.Activities.MainActivity.Companion.kihelyezesItems
import com.fusetech.virtualkanban.DataItems.SzerelohelyItem
import com.fusetech.virtualkanban.R
import kotlinx.android.synthetic.main.fragment_igeny_kontener_kiszedese.view.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class IgenyKontenerKiszedese : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var recycler : RecyclerView
    private lateinit var szallitoText: EditText
    private lateinit var mainActivity: MainActivity
    private lateinit var szerelohely: EditText
    //private val lista: ArrayList<SzerelohelyItem> = ArrayList()

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
        mainActivity = activity as MainActivity
        szallitoText = view.szallitoJarmuText
        szerelohely = view.szereloText
        szerelohely.isEnabled = false
        szallitoText.requestFocus()
        recycler = view.recyclerSzerelo
        recycler.adapter = SzerelohelyItemAdapter(kihelyezesItems)
        recycler.layoutManager = GridLayoutManager(view.context,3)
        recycler.setHasFixedSize(true)
        return view
    }

    fun setCode(code: String){
        kihelyezesItems.clear()
        if(szallitoText.text.isEmpty()){
            if(code.equals("SZ01")){
                szallitoText.setText(code)
                szallitoText.selectAll()
                mainActivity.getContainerList(code)
                szallitoText.isEnabled = false
                szerelohely.isEnabled = true
                szerelohely.requestFocus()
            }
        }else{

        }
    }
    fun updateList(){
        recycler.adapter?.notifyDataSetChanged()
    }
}