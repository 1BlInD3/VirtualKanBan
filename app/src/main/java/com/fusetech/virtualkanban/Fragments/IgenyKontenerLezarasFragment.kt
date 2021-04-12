package com.fusetech.virtualkanban.Fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fusetech.virtualkanban.Activities.MainActivity
import com.fusetech.virtualkanban.Adapters.KontenerAdapter
import com.fusetech.virtualkanban.DataItems.KontenerItem
import com.fusetech.virtualkanban.R
import kotlinx.android.synthetic.main.fragment_igeny_kontener_lezaras.view.*
import kotlinx.android.synthetic.main.konteneres_view.view.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class IgenyKontenerLezarasFragment : Fragment(), KontenerAdapter.onKontenerClickListener {
    private lateinit var dataFrame: FrameLayout
    private lateinit var childRecycler: RecyclerView
    private var kontenerList: ArrayList<KontenerItem> = ArrayList()
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var mainActivity: MainActivity
    private lateinit var igenyKontener: IgenyKontnerLezaras
    private val TAG = "IgenyKontenerLezarasFra"

    interface IgenyKontnerLezaras{
        fun sendContainer(container: String)
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
        val view = inflater.inflate(R.layout.fragment_igeny_kontener_lezaras, container, false)
        mainActivity = activity as MainActivity
        dataFrame = view.data_frame1
        val child = layoutInflater.inflate(R.layout.konteneres_view,null)
        dataFrame.addView(child)
        childRecycler = child.child_recycler
        childRecycler.adapter = KontenerAdapter(kontenerList,this)
        childRecycler.layoutManager = LinearLayoutManager(child.context)
        childRecycler.setHasFixedSize(true)
        kontenerList.clear()
        loadData()
        childRecycler.adapter?.notifyDataSetChanged()

        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            IgenyKontenerLezarasFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onKontenerClick(position: Int) {
        Log.d(TAG, "onKontenerClick: ${kontenerList[position].kontner_id}")
        igenyKontener.sendContainer(kontenerList[position].kontner_id.toString())
    }
    private fun loadData(){
        val myList: ArrayList<KontenerItem> = arguments?.getSerializable("KONTENERLISTA") as ArrayList<KontenerItem>
        for(i in 0 until myList.size){
            kontenerList.add(KontenerItem(myList[i].kontener,myList[i].polc,myList[i].datum,myList[i].tetelszam,myList[i].kontner_id))
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        igenyKontener = if(context is IgenyKontnerLezaras){
            context
        }else{
            throw RuntimeException(context.toString() + "must implement")
        }
    }
}