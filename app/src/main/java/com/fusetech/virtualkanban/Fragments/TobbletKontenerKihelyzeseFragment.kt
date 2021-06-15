package com.fusetech.virtualkanban.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fusetech.virtualkanban.Adapters.KontenerAdapter
import com.fusetech.virtualkanban.DataItems.KontenerItem
import com.fusetech.virtualkanban.R
import kotlinx.android.synthetic.main.fragment_tobblet_kontener_kihelyzese.view.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class TobbletKontenerKihelyzeseFragment : Fragment(), KontenerAdapter.onKontenerClickListener {
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var recycler: RecyclerView
    private val kontenerItem: ArrayList<KontenerItem> = ArrayList()
    private lateinit var progress: ProgressBar

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
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_tobblet_kontener_kihelyzese, container, false)
        recycler = view.tobbletRecycler
        recycler.adapter = KontenerAdapter(kontenerItem,this)
        recycler.layoutManager = LinearLayoutManager(view.context)
        recycler.setHasFixedSize(true)
        kontenerItem.add(KontenerItem("256137","P20","2020",5,"000256",1))
        //setProgressBarOff()

        return view
    }

    override fun onKontenerClick(position: Int) {
        TODO("Not yet implemented")
    }
    fun setProgressBarOff(){
        progress.visibility = View.INVISIBLE
    }
    fun setProgressBarOn(){
        progress.visibility = View.VISIBLE
    }

}