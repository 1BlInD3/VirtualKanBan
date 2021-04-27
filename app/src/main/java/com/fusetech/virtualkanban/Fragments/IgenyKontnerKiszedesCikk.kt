package com.fusetech.virtualkanban.Fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fusetech.virtualkanban.Activities.MainActivity
import com.fusetech.virtualkanban.Adapters.KontenerbenLezarasAdapter
import com.fusetech.virtualkanban.DataItems.KontenerbenLezarasItem
import com.fusetech.virtualkanban.R
import kotlinx.android.synthetic.main.kontenerben_lezaras_view.view.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class IgenyKontnerKiszedesCikk : Fragment(),KontenerbenLezarasAdapter.onItemClickListener {
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var recycler : RecyclerView
    private lateinit var tovabbBtn: Button
    private lateinit var visszaBtn: Button
    private lateinit var kontenerNev: TextView
    private lateinit var progress: ProgressBar
    private val cikkItem: ArrayList<KontenerbenLezarasItem> = ArrayList()
    private lateinit var cikkAdatok: KiszedesAdatok
    private lateinit var mainAcitivity: MainActivity

    interface KiszedesAdatok{
        fun cikkAdatok(cikk: String?, megj1: String?, megj2: String?, intrem: String?, igeny: Double, unit: String?, id: Int, kontnerNumber: Int)
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
        val view = inflater.inflate(R.layout.kontenerben_lezaras_view, container, false)
        mainAcitivity = activity as MainActivity
        kontenerNev = view.kontenerNameLezaras
        recycler = view.child_recycler2
        tovabbBtn = view.lezar3Button
        visszaBtn = view.exit3CikkButton
        progress = view.cikkLezarasProgress
        progress.visibility = View.GONE
        kontenerNev.text = arguments?.getString("NEGYESNEV")
        tovabbBtn.text = "Tovább"
        tovabbBtn.visibility = View.GONE
        recycler.adapter = KontenerbenLezarasAdapter(cikkItem,this)
        recycler.layoutManager = LinearLayoutManager(view.context)
        recycler.setHasFixedSize(true)
        loadData()
        recycler.adapter?.notifyDataSetChanged()
        recycler.requestFocus()
        visszaBtn.setOnClickListener{
            mainAcitivity.loadMenuFragment(true)
            mainAcitivity.igenyKontenerKiszedes()
        }

        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            IgenyKontnerKiszedesCikk().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onItemClick(position: Int) {
        cikkAdatok.cikkAdatok(cikkItem[position].cikkszam,cikkItem[position].megjegyzes1,cikkItem[position].megjegyzes2,
        cikkItem[position].intrem,cikkItem[position].igeny.toString().toDouble(),cikkItem[position].unit,cikkItem[position].id,cikkItem[position].kontener_id)

    }
    private fun loadData(){
        cikkItem.clear()
        val myList: ArrayList<KontenerbenLezarasItem> = arguments?.getSerializable("NEGYESCIKKEK") as ArrayList<KontenerbenLezarasItem>
        for(i in 0 until myList.size){
            cikkItem.add(KontenerbenLezarasItem(myList[i].cikkszam,myList[i].megjegyzes1,myList[i].megjegyzes2,myList[i].intrem,myList[i].igeny,myList[i].kiadva,myList[i].statusz,myList[i].unit,myList[i].id,myList[i].kontener_id))
        }
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        cikkAdatok = if(context is KiszedesAdatok){
            context
        }else{
            throw RuntimeException(context.toString() + "must implement")
        }
    }

    override fun onResume() {
        super.onResume()
        recycler.requestFocus()
    }
    fun setProgressBarOff(){
        progress.visibility = View.GONE
    }
    fun setProgressBarOn(){
        progress.visibility = View.VISIBLE
    }
}