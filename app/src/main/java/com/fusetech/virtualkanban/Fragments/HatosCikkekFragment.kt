package com.fusetech.virtualkanban.Fragments

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
import com.fusetech.virtualkanban.Activities.MainActivity
import com.fusetech.virtualkanban.Adapters.KontenerbenLezarasAdapter
import com.fusetech.virtualkanban.DataItems.KontenerbenLezarasItem
import com.fusetech.virtualkanban.R
import kotlinx.android.synthetic.main.kontenerben_lezaras_view.view.*

private lateinit var recycler: RecyclerView
private const val TAG = "HatosCikkekFragment"

class HatosCikkekFragment : Fragment(), KontenerbenLezarasAdapter.onItemClickListener {
    private lateinit var exitBtn: Button
    private lateinit var lezarBtn: Button
    private lateinit var mainActivity: MainActivity
    private lateinit var kontenerNev: TextView
    private lateinit var progress: ProgressBar
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.kontenerben_lezaras_view, container, false)

        mainActivity = activity as MainActivity
        recycler = view.child_recycler2
        exitBtn = view.exit3CikkButton
        lezarBtn = view.lezar3Button
        kontenerNev = view.kontenerNameLezaras
        val horizontalScrollView: HorizontalScrollView = view.horizontalScrollView3
        horizontalScrollView.isFocusable = false
        horizontalScrollView.isFocusableInTouchMode = false
        progress = view.cikkLezarasProgress
        setProgressBarOff()
        recycler.adapter = KontenerbenLezarasAdapter(MainActivity.kontItem, this)
        recycler.layoutManager = LinearLayoutManager(view.context)
        recycler.setHasFixedSize(true)
        MainActivity.kontItem.clear()
        kontenerNev.text = ""
        loadData()
        recycler.adapter?.notifyDataSetChanged()
        recycler.requestFocus()

        exitBtn.setOnClickListener {
            exitBtn.isFocusable = true
            exitBtn.isFocusableInTouchMode = true
            MainActivity.kontItem.clear()
            mainActivity.loadMenuFragment(true)
            mainActivity.kiszedesreVaro()
            mainActivity.removeFragment("CIKKLEZARASFRAGMENTHATOS")
        }
        lezarBtn.setOnClickListener {
            setProgressBarOn()
            mainActivity.closeContainerAndItem()
            MainActivity.kontItem.clear()
            mainActivity.loadMenuFragment(true)
        }
        if (arguments?.getBoolean("LEZARBUTN")!!) {
            lezarBtn.visibility = View.VISIBLE
        } else {
            lezarBtn.visibility = View.GONE
        }

        return view
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
            kontenerNev.text = arguments?.getString("KONTENER_ID")
        } catch (e: Exception) {
            Log.d(TAG, "loadData: $e")
        }
    }

    fun setProgressBarOff() {
        progress.visibility = View.GONE
    }

    fun setProgressBarOn() {
        progress.visibility = View.VISIBLE
    }

    fun buttonPerform() {
        exitBtn.performClick()
    }

    override fun onItemClick(position: Int) {
        Log.d(TAG, "onItemClick: megy")
    }
}