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
import com.fusetech.virtualkanban.R
import kotlinx.android.synthetic.main.fragment_tobblet_kontener_cikkek.view.*
import com.fusetech.virtualkanban.activities.MainActivity.Companion.tobbletItem
import com.fusetech.virtualkanban.dataItems.KontenerbenLezarasItem

private const val TAG = "TobbletKontenerCikkekFr"
@Suppress("UNCHECKED_CAST")
class TobbletKontenerCikkekFragment : Fragment(), KontenerbenLezarasAdapter.onItemClickListener {
    interface Tobblet {
        fun sendTobblet(
            id: Int,
            kontenerID: Int,
            megjegyzes: String,
            megjegyzes2: String,
            intrem: String,
            unit: String,
            mennyiseg: Double,
            cikkszam: String
        )
    }
    private  var recycler: RecyclerView? = null
    private  var vissza: Button? = null
    private  var kontener: TextView? = null
    private  var tobblet: Tobblet? = null
    private  var mainActivity: MainActivity? = null
    private  var progress82: ProgressBar? = null
    private var myView: View? = null

    var kontenerID: String? = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        myView = inflater.inflate(R.layout.fragment_tobblet_kontener_cikkek, container, false)
        mainActivity = activity as MainActivity
        progress82 = myView?.nyolckettesProgress
        nyolcaskettesProgressOff()
        recycler = myView?.kihelyezesRecycler
        vissza = myView?.visszaTobbletButton
        kontener = myView?.kontenerIDText
        recycler?.adapter = KontenerbenLezarasAdapter(tobbletItem, this)
        recycler?.layoutManager = LinearLayoutManager(myView?.context)
        recycler?.setHasFixedSize(true)
        loadData()
        kontener?.setText(kontenerID)
        vissza?.setOnClickListener {
            mainActivity?.run {
                //loadMenuFragment(true)
                loadTobbletKontenerKihelyezes()
            }
        }
        return myView
    }

    override fun onItemClick(position: Int) {
        tobblet?.sendTobblet(
            tobbletItem[position].id,
            tobbletItem[position].kontener_id,
            tobbletItem[position].megjegyzes1!!,
            tobbletItem[position].megjegyzes2!!,
            tobbletItem[position].intrem!!,
            tobbletItem[position].unit!!,
            tobbletItem[position].igeny.toString().toDouble(),
            tobbletItem[position].cikkszam!!
        )
    }

    fun loadData() {
        val myList: ArrayList<KontenerbenLezarasItem> =
            arguments?.getSerializable("TOBBLETESCIKKEK") as ArrayList<KontenerbenLezarasItem>
        kontenerID = arguments?.getString("KONTENERTOBBLETCIKK")
        for (i in 0 until myList.size) {
            tobbletItem.add(
                KontenerbenLezarasItem(
                    myList[i].cikkszam,
                    myList[i].megjegyzes1,
                    myList[i].megjegyzes2,
                    myList[i].intrem,
                    myList[i].igeny,
                    myList[i].kiadva,
                    myList[i].statusz,
                    myList[i].unit,
                    myList[i].id,
                    myList[i].kontener_id
                )
            )
        }
        recycler?.adapter?.notifyDataSetChanged()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        tobblet = if (context is Tobblet) {
            context
        } else {
            throw RuntimeException(context.toString() + "must implement")
        }
    }
    fun nyolcaskettesProgressOn(){
        progress82?.visibility = View.VISIBLE
    }
    fun nyolcaskettesProgressOff(){
        progress82?.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView: ")
        myView = null
        recycler = null
        recycler?.adapter = null
        vissza = null
        kontener = null
        tobblet = null
        progress82 = null
        mainActivity?.tobbletCikkek = null
        mainActivity = null
    }
}