package com.fusetech.virtualkanban.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fusetech.virtualkanban.activities.MainActivity
import com.fusetech.virtualkanban.adapters.KontenerbenLezarasAdapter
import com.fusetech.virtualkanban.dataItems.KontenerbenLezarasItem
import com.fusetech.virtualkanban.R
import com.fusetech.virtualkanban.activities.MainActivity.Companion.cikkItem4
import kotlinx.android.synthetic.main.kontenerben_lezaras_view.view.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val TAG = "IgenyKontnerKiszedesCik"

class IgenyKontnerKiszedesCikk : Fragment(),KontenerbenLezarasAdapter.onItemClickListener{
    private var param1: String? = null
    private var param2: String? = null
    private var recycler : RecyclerView? = null
    private  var tovabbBtn: Button?= null
    private  var visszaBtn: Button?= null
    private  var kontenerNev: TextView?= null
    private  var progress: ProgressBar?= null
    private  var cikkAdatok: KiszedesAdatok?= null
    private  var mainAcitivity: MainActivity?= null
    private var myView: View? = null

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
        myView = inflater.inflate(R.layout.kontenerben_lezaras_view, container, false)
        mainAcitivity = activity as MainActivity
        //mainAcitivity!!.igenyKiszedesFragment = null
        kontenerNev = myView?.kontenerNameLezaras
        recycler = myView?.child_recycler2
        tovabbBtn = myView?.lezar3Button
        visszaBtn = myView?.exit3CikkButton
        progress = myView?.cikkLezarasProgress
        progress?.visibility = View.GONE
        kontenerNev?.text = arguments?.getString("NEGYESNEV")
        tovabbBtn?.text = getString(R.string.tovabb)
        tovabbBtn?.visibility = View.GONE
        recycler?.adapter = KontenerbenLezarasAdapter(cikkItem4,this)
        recycler?.layoutManager = LinearLayoutManager(myView?.context)
        recycler?.setHasFixedSize(true)
        loadData()
        recycler?.adapter?.notifyDataSetChanged()
        recycler?.requestFocus()
        visszaBtn?.setOnClickListener{
            myView = null
            recycler = null
            recycler?.adapter = null
            tovabbBtn = null
            visszaBtn = null
            kontenerNev = null
            progress = null
            cikkAdatok = null
            //mainAcitivity?.loadMenuFragment(true)
            //mainAcitivity?.menuFragment = null
            mainAcitivity?.removeFragment("NEGYESCIKKEK")
            mainAcitivity?.igenyKontenerKiszedes()
            mainAcitivity = null
        }

        return myView
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
        mainAcitivity?.igenyKontenerKiszedesCikkKiszedes = IgenyKontenerKiszedesCikkKiszedes()
        cikkAdatok?.cikkAdatok(cikkItem4[position].cikkszam,cikkItem4[position].megjegyzes1,cikkItem4[position].megjegyzes2,
        cikkItem4[position].intrem,cikkItem4[position].igeny.toString().toDouble(),cikkItem4[position].unit,cikkItem4[position].id,cikkItem4[position].kontener_id)
        myView = null
        recycler = null
        recycler?.adapter = null
        tovabbBtn = null
        visszaBtn = null
        kontenerNev = null
        progress = null
        cikkAdatok = null
        mainAcitivity?.removeFragment("NEGYESCIKKEK")
        mainAcitivity = null

    }
    private fun loadData(){
        cikkItem4.clear()
        val myList: ArrayList<KontenerbenLezarasItem> = arguments?.getSerializable("NEGYESCIKKEK") as ArrayList<KontenerbenLezarasItem>
        for(i in 0 until myList.size){
            cikkItem4.add(KontenerbenLezarasItem(myList[i].cikkszam,myList[i].megjegyzes1,myList[i].megjegyzes2,myList[i].intrem,myList[i].igeny,myList[i].kiadva,myList[i].statusz,myList[i].unit,myList[i].id,myList[i].kontener_id))
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
        recycler?.requestFocus()
    }
    fun setProgressBarOff(){
        progress?.visibility = View.GONE
    }
    fun setProgressBarOn(){
        progress?.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView: Cikkek megnyitása")
        myView = null
        recycler = null
        recycler?.adapter = null
        tovabbBtn = null
        visszaBtn = null
        kontenerNev = null
        progress = null
        cikkAdatok = null
        mainAcitivity?.removeFragment("NEGYESCIKKEK")
        mainAcitivity = null
    }
}