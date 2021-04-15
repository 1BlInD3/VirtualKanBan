package com.fusetech.virtualkanban.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fusetech.virtualkanban.Adapters.KontenerAdapter
import com.fusetech.virtualkanban.DataItems.KontenerItem
import com.fusetech.virtualkanban.R
import kotlinx.android.synthetic.main.fragment_igeny_kontener_kiszedes.view.*
import kotlinx.android.synthetic.main.fragment_load.*
import kotlinx.android.synthetic.main.konteneres_view.view.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private lateinit var childFrame: FrameLayout
private lateinit var childRecycler: RecyclerView
private var kontenerList: ArrayList<KontenerItem> = ArrayList()
private lateinit var progress: ProgressBar

class IgenyKontenerKiszedesFragment : Fragment(),KontenerAdapter.onKontenerClickListener {

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
       val view = inflater.inflate(R.layout.fragment_igeny_kontener_kiszedes, container, false)
        childFrame = view.data_frame2
        val child = layoutInflater.inflate(R.layout.konteneres_view,null)
        childFrame.addView(child)
        progress = child.konteneresProgress
        setProgressBarOff()
        kontenerList.clear()
        childRecycler = child.child_recycler
        childRecycler.adapter = KontenerAdapter(kontenerList,this)
        childRecycler.layoutManager = LinearLayoutManager(child.context)
        childRecycler.setHasFixedSize(true)

        //kontenerList.add(KontenerItem("255653","NNG02","2021.04.01 14:52:02",5,"546"))
        kontenerList.clear()
        loadData()
        childRecycler.adapter?.notifyDataSetChanged()


       return view
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            IgenyKontenerKiszedesFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onKontenerClick(position: Int) {
        Toast.makeText(view?.context, "itt mas jelenik meg", Toast.LENGTH_SHORT).show()
    }
    private fun loadData(){
        val myList: ArrayList<KontenerItem> = arguments?.getSerializable("KISZEDESLISTA") as ArrayList<KontenerItem>
        for(i in 0 until myList.size){
            kontenerList.add(KontenerItem(myList[i].kontener,myList[i].polc,myList[i].datum,myList[i].tetelszam,myList[i].kontner_id))
        }
    }
    fun setProgressBarOff(){
        progress.visibility = View.GONE
    }
    fun setProgressBarOn(){
        progress.visibility = View.VISIBLE
    }
}