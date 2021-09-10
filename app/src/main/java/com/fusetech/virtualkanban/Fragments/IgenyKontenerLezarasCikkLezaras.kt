package com.fusetech.virtualkanban.fragments

import android.annotation.SuppressLint
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
import com.fusetech.virtualkanban.activities.MainActivity.Companion.kontItem
import com.fusetech.virtualkanban.activities.MainActivity
import com.fusetech.virtualkanban.adapters.KontenerbenLezarasAdapter
import com.fusetech.virtualkanban.dataItems.KontenerbenLezarasItem
import com.fusetech.virtualkanban.R
import kotlinx.android.synthetic.main.kontenerben_lezaras_view.view.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val TAG = "IgenyKontenerLezarasCik"
private lateinit var sendItemCode: IgenyKontenerLezarasCikkLezaras.CikkCode

@Suppress("UNCHECKED_CAST")
class IgenyKontenerLezarasCikkLezaras : Fragment(), KontenerbenLezarasAdapter.onItemClickListener {
    private var param1: String? = null
    private var param2: String? = null

    private var exitBtn: Button? = null
    private var lezarBtn: Button? = null
    private var mainActivity: MainActivity? = null
    private var kontenerNev: TextView? = null
    private var progress: ProgressBar? = null
    private var myView: View? = null
    private var recycler: RecyclerView? = null

    interface CikkCode {
        fun cikkCode(code: Int)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        sendItemCode = if (context is CikkCode) {
            context
        } else {
            throw RuntimeException(context.toString() + "must implement")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        myView = inflater.inflate(R.layout.kontenerben_lezaras_view, container, false)
        mainActivity = activity as MainActivity
        recycler = myView?.child_recycler2!!
        exitBtn = myView?.exit3CikkButton
        lezarBtn = myView?.lezar3Button
        kontenerNev = myView?.kontenerNameLezaras
        val horizontalScrollView: HorizontalScrollView = myView?.horizontalScrollView3!!
        horizontalScrollView.isFocusable = false
        horizontalScrollView.isFocusableInTouchMode = false
        progress = myView?.cikkLezarasProgress
        setProgressBarOff()
        recycler?.adapter = KontenerbenLezarasAdapter(kontItem, this)
        recycler?.layoutManager = LinearLayoutManager(myView?.context)
        recycler?.setHasFixedSize(true)
        kontItem.clear()
        kontenerNev?.text = ""
        loadData()
        recycler?.adapter?.notifyDataSetChanged()

        exitBtn?.setOnClickListener {
            exitBtn?.isFocusable = true
            exitBtn?.isFocusableInTouchMode = true
            kontItem.clear()
            mainActivity?.loadMenuFragment(true)
            if (mainActivity?.getFragment("CIKKLEZARASFRAGMENTHATOS")!!) {
                mainActivity?.removeFragment("CIKKLEZARASFRAGMENTHATOS")
                mainActivity?.kiszedesreVaro()
            } else {
                mainActivity?.removeFragment("CIKKLEZARASFRAGMENT")
                mainActivity?.igenyKontenerCheck()
            }
            if(mainActivity?.isWifiConnected()!!){
                MainActivity.wifiInfo = mainActivity?.getMacAndSignalStrength()!!
            }
        }
        lezarBtn?.setOnClickListener {
            if(mainActivity?.isWifiConnected()!!){
                MainActivity.wifiInfo = mainActivity?.getMacAndSignalStrength()!!
                lezarBtn?.isEnabled = false
                exitBtn?.isEnabled = false
                setProgressBarOn()
                mainActivity?.closeContainerAndItem()
                kontItem.clear()
              //  mainActivity?.removeFragment("CIKKLEZARASFRAGMENT")
              //  mainActivity?.removeFragment("IGENYLEZARAS")
                mainActivity?.loadMenuFragment(true)
            }else{
                mainActivity?.setAlert("A wifi nincs bekapcsolva")
            }
        }
        if (arguments?.getBoolean("LEZARBUTN")!!) {
            lezarBtn?.visibility = View.VISIBLE
        } else {
            lezarBtn?.visibility = View.GONE
        }

        return myView
    }

    override fun onResume() {
        super.onResume()
        recycler?.requestFocus()
    }

    fun onTimeout() {
        kontItem.clear()
        mainActivity?.removeFragment("CIKKLEZARASFRAGMENTHATOS")
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            IgenyKontenerLezarasCikkLezaras().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun loadData() {
        try {
            val myList: ArrayList<KontenerbenLezarasItem> =
                arguments?.getSerializable("CIKKLEZAR") as ArrayList<KontenerbenLezarasItem>
            for (i in 0 until myList.size) {
                kontItem.add(
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
                        myList[i].kontener_id,
                        myList[i].balance
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
        sendItemCode.cikkCode(kontItem[position].id)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        clearLeak()
    }
    fun clearLeak(){
        myView = null
        exitBtn = null
        lezarBtn = null
        recycler = null
        recycler?.adapter = null
        kontenerNev = null
        progress = null
        mainActivity?.igenyKiszedesCikkLezaras = null
        mainActivity = null
    }
}