package com.fusetech.virtualkanban.Fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fusetech.virtualkanban.Activities.MainActivity
import com.fusetech.virtualkanban.Adapters.KontenerAdapter
import com.fusetech.virtualkanban.DataItems.KontenerItem
import com.fusetech.virtualkanban.R
import kotlinx.android.synthetic.main.fragment_igeny_kontener_kiszedes.view.*
import kotlinx.android.synthetic.main.konteneres_view.view.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class IgenyKontenerKiszedesFragment : Fragment(),KontenerAdapter.onKontenerClickListener {

    private lateinit var childFrame: FrameLayout
    private lateinit var childRecycler: RecyclerView
    private var kontenerList: ArrayList<KontenerItem> = ArrayList()
    private lateinit var progress: ProgressBar
    private lateinit var megnyitottBtn : Button
    private lateinit var exit3Btn: Button
    private lateinit var mainActivity: MainActivity
    private val TAG = "IgenyKontenerKiszedesFr"
    private lateinit var kontenerKiszedes: sendKiszedesContainer
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var cimText: TextView

    interface sendKiszedesContainer{
        fun sendContainerKiszedes(kontener: String)
    }

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
        mainActivity = activity as MainActivity
        childFrame = view.data_frame2
        val child = layoutInflater.inflate(R.layout.konteneres_view,null)
        childFrame.addView(child)
        progress = child.konteneresProgress
        megnyitottBtn = child.megnyitottKontenerButton
        exit3Btn = child.exit3Button
        cimText = view.titleText
        cimText.text = arguments?.getString("CIM")
        setProgressBarOff()
        kontenerList.clear()
        childRecycler = child.child_recycler
        childRecycler.adapter = KontenerAdapter(kontenerList,this)
        childRecycler.layoutManager = LinearLayoutManager(child.context)
        childRecycler.setHasFixedSize(true)

        kontenerList.clear()
        loadData()
        childRecycler.adapter?.notifyDataSetChanged()

        megnyitottBtn.setOnClickListener {
            setProgressBarOn()
            mainActivity.igenyKontenerMegnyitott()
            setProgressBarOff()
        }
        exit3Btn.setOnClickListener {
            kontenerList.clear()
            mainActivity.loadMenuFragment(true)
        }

       return view
    }

    override fun onKontenerClick(position: Int) {
        Toast.makeText(view?.context, "itt mas jelenik meg", Toast.LENGTH_SHORT).show()
        kontenerKiszedes.sendContainerKiszedes(kontenerList[position].kontner_id.toString())
    }
    @Suppress("UNCHECKED_CAST")
    private fun loadData(){
        try {
            kontenerList.clear()
            val myList: ArrayList<KontenerItem> = arguments?.getSerializable("KISZEDESLISTA") as ArrayList<KontenerItem>
            for(i in 0 until myList.size){
                kontenerList.add(KontenerItem(myList[i].kontener,myList[i].polc,myList[i].datum,myList[i].tetelszam,myList[i].kontner_id))
            }
        }catch (e: Exception){
            Log.d(TAG, "loadData: ")
        }
    }
    fun setProgressBarOff(){
        progress.visibility = View.GONE
    }
    fun setProgressBarOn(){
        progress.visibility = View.VISIBLE
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        kontenerKiszedes = if (context is sendKiszedesContainer){
            context
        }else{
            throw RuntimeException(context.toString() + "must implement")
        }
    }
}