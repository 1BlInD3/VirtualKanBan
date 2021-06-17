package com.fusetech.virtualkanban.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fusetech.virtualkanban.Activities.MainActivity
import com.fusetech.virtualkanban.Adapters.KontenerAdapter
import com.fusetech.virtualkanban.DataItems.KontenerItem
import com.fusetech.virtualkanban.R
import kotlinx.android.synthetic.main.fragment_tobblet_kontener_kihelyzese.view.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

@Suppress("UNCHECKED_CAST")
class TobbletKontenerKihelyzeseFragment : Fragment(), KontenerAdapter.onKontenerClickListener {
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var recycler: RecyclerView
    private val kontenerItem: ArrayList<KontenerItem> = ArrayList()
    private lateinit var progress: ProgressBar
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
        val view = inflater.inflate(R.layout.fragment_tobblet_kontener_kihelyzese, container, false)
        mainActivity = activity as MainActivity
        recycler = view.tobbletRecycler
        progress = view.tobbletProgress
        recycler.adapter = KontenerAdapter(kontenerItem,this)
        recycler.layoutManager = LinearLayoutManager(view.context)
        recycler.setHasFixedSize(true)
        //kontenerItem.add(KontenerItem("256137","P20","2020",5,"000256",1))
        setProgressBar8Off()
        loadData()
        return view
    }

    override fun onKontenerClick(position: Int) {
        mainActivity.setContainerStatusAndGetItems(kontenerItem[position].kontner_id)
        //mainActivity.loadCTobbletCikkek()
    }
    fun setProgressBar8Off(){
        progress.visibility = View.GONE
    }
    fun setProgressBar8On(){
        progress.visibility = View.VISIBLE
    }
    fun loadData(){
        val myList: ArrayList<KontenerItem> = arguments?.getSerializable("TOBBLETKONTENEREK") as ArrayList<KontenerItem>
        for(i in 0 until myList.size){
            kontenerItem.add(KontenerItem(myList[i].kontener,myList[i].polc,"1900-01-01",myList[i].tetelszam,myList[i].kontner_id,myList[i].status))
        }
        recycler.adapter?.notifyDataSetChanged()
    }

}