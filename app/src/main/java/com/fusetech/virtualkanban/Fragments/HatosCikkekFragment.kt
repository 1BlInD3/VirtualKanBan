package com.fusetech.virtualkanban.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fusetech.virtualkanban.activities.MainActivity
import com.fusetech.virtualkanban.adapters.KontenerbenLezarasAdapter
import com.fusetech.virtualkanban.dataItems.KontenerbenLezarasItem
import com.fusetech.virtualkanban.R
import kotlinx.android.synthetic.main.kontenerben_lezaras_view.view.*

private const val TAG = "HatosCikkekFragment"

class HatosCikkekFragment : Fragment(), KontenerbenLezarasAdapter.onItemClickListener {
    private var recycler: RecyclerView? = null
    private var exitBtn: Button? = null
    private var lezarBtn: Button? = null
    private var mainActivity: MainActivity? = null
    private var kontenerNev: TextView? = null
    private var progress: ProgressBar? = null
    private var hatos: Hatos? = null
    private var myView: View? = null
    private var horizontalScrollView: HorizontalScrollView? = null

    interface Hatos {
        fun hatosInfo(id: Int)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        hatos = if (context is Hatos) {
            context
        } else {
            throw RuntimeException(context.toString() + "must implement")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        myView = inflater.inflate(R.layout.kontenerben_lezaras_view, container, false)
        mainActivity = activity as MainActivity
        recycler = myView?.child_recycler2
        exitBtn = myView?.exit3CikkButton
        lezarBtn = myView?.lezar3Button
        kontenerNev = myView?.kontenerNameLezaras
        horizontalScrollView = myView?.horizontalScrollView3!!
        horizontalScrollView?.isFocusable = false
        horizontalScrollView?.isFocusableInTouchMode = false
        progress = myView?.cikkLezarasProgress
        setProgressBarOff()
        recycler?.adapter = KontenerbenLezarasAdapter(MainActivity.kontItem, this)
        recycler?.layoutManager = LinearLayoutManager(myView?.context)
        recycler?.setHasFixedSize(true)
        MainActivity.kontItem.clear()
        kontenerNev?.text = ""
        loadData()
        recycler?.adapter?.notifyDataSetChanged()
        recycler?.requestFocus()

        exitBtn?.setOnClickListener {
            exitBtn?.isFocusable = true
            exitBtn?.isFocusableInTouchMode = true
            MainActivity.kontItem.clear()
           // mainActivity?.loadMenuFragment(true)
            mainActivity?.kiszedesreVaro()
            mainActivity?.removeFragment("CIKKLEZARASFRAGMENTHATOS")
            mainActivity?.hatosFragment = null
        }
        lezarBtn?.setOnClickListener {
            setProgressBarOn()
            mainActivity?.closeContainerAndItem()
            MainActivity.kontItem.clear()
            mainActivity?.loadMenuFragment(true)
        }
        if (arguments?.getBoolean("LEZARBUTN")!!) {
            lezarBtn?.visibility = View.VISIBLE
        } else {
            lezarBtn?.visibility = View.GONE
        }

        return myView
    }

    fun onTimeout() {
        MainActivity.kontItem.clear()
    }

    private fun loadData() {
        try {
            val myList: ArrayList<KontenerbenLezarasItem> =
                arguments?.getSerializable("CIKKLEZAR") as ArrayList<KontenerbenLezarasItem>
            for (i in 0 until myList.size) {
                MainActivity.kontItem.add(
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
            kontenerNev?.text = arguments?.getString("KONTENER_ID")
        } catch (e: Exception) {
            Log.d(TAG, "loadData: $e")
        }
    }

    fun setProgressBarOff() {
        progress?.visibility = View.GONE
    }

    fun setProgressBarOn() {
        progress?.visibility = View.VISIBLE
    }

    fun buttonPerform() {
        exitBtn?.performClick()
    }

    override fun onItemClick(position: Int) {
        hatos?.hatosInfo(MainActivity.kontItem[position].id)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        myView = null
        recycler = null
        recycler?.adapter = null
        exitBtn = null
        lezarBtn = null
        mainActivity = null
        kontenerNev = null
        progress = null
        hatos = null
        horizontalScrollView = null
    }
}