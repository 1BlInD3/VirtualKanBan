package com.fusetech.virtualkanban.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fusetech.virtualkanban.activities.MainActivity
import com.fusetech.virtualkanban.adapters.KontenerAdapter
import com.fusetech.virtualkanban.dataItems.KontenerItem
import com.fusetech.virtualkanban.R
import com.fusetech.virtualkanban.activities.MainActivity.Companion.tobbletKontener
import kotlinx.android.synthetic.main.fragment_tobblet_kontener_kihelyzese.view.*

private const val TAG = "TobbletKontenerKihelyze"
@Suppress("UNCHECKED_CAST")
class TobbletKontenerKihelyzeseFragment : Fragment(), KontenerAdapter.onKontenerClickListener {

    private var recycler: RecyclerView? = null
    private var progress: ProgressBar? = null
    private var mainActivity: MainActivity? = null
    private var button: Button? = null
    private var myView: View? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        myView = inflater.inflate(R.layout.fragment_tobblet_kontener_kihelyzese, container, false)
        mainActivity = activity as MainActivity
        mainActivity?.menuFragment = null
        recycler = myView?.tobbletRecycler
        progress = myView?.tobbletProgress
        recycler?.adapter = KontenerAdapter(tobbletKontener, this)
        recycler?.layoutManager = LinearLayoutManager(myView?.context)
        recycler?.setHasFixedSize(true)
        button = myView?.kilepTobbletBtn
        //kontenerItem.add(KontenerItem("256137","P20","2020",5,"000256",1))
        setProgressBar8Off()
        loadData()
        recycler?.requestFocus()
        button?.setOnClickListener {
            mainActivity?.loadMenuFragment(true)
        }
        return myView
    }

    override fun onKontenerClick(position: Int) {
        /*if(tobbletKontener[position].status == 8){
            mainActivity.setAlert("Ezt már megnyitotta valaki")
        }else{
            mainActivity.setContainerStatusAndGetItems(tobbletKontener[position].kontner_id)
        }*/
        //mainActivity.loadCTobbletCikkek()
        mainActivity?.setContainerStatusAndGetItems(tobbletKontener[position].kontner_id)
    }

    fun setProgressBar8Off() {
        progress?.visibility = View.GONE
    }

    fun setProgressBar8On() {
        progress?.visibility = View.VISIBLE
    }

    fun loadData() {
        val myList: ArrayList<KontenerItem> =
            arguments?.getSerializable("TOBBLETKONTENEREK") as ArrayList<KontenerItem>
        for (i in 0 until myList.size) {
            tobbletKontener.add(
                KontenerItem(
                    myList[i].kontener,
                    myList[i].polc,
                    "1900-01-01",
                    myList[i].tetelszam,
                    myList[i].kontner_id,
                    myList[i].status
                )
            )
        }
        recycler?.adapter?.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView: ")
        myView = null
        recycler = null
        recycler?.adapter = null
        progress = null
        button = null
        mainActivity?.tobbletKontenerKihelyzeseFragment = null
        mainActivity = null
    }
}