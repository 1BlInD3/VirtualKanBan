package com.fusetech.virtualkanban.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ProgressBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fusetech.virtualkanban.activities.MainActivity
import com.fusetech.virtualkanban.adapters.KontenerAdapter
import com.fusetech.virtualkanban.dataItems.KontenerItem
import com.fusetech.virtualkanban.R
import kotlinx.android.synthetic.main.fragment_igeny_kontener_kiszedes.view.*
import kotlinx.android.synthetic.main.konteneres_view.view.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

private var kontenerList: ArrayList<KontenerItem> = ArrayList()
private const val TAG = "IgenyKontenerKiszedesFr"

@Suppress("UNCHECKED_CAST")
class IgenyKontenerKiszedesFragment : Fragment(), KontenerAdapter.onKontenerClickListener {

    private var param1: String? = null
    private var param2: String? = null
    private var progress: ProgressBar? = null
    private var exit3Btn: Button? = null
    private var mainActivity: MainActivity? = null
    private var childFrame: FrameLayout? = null
    private var childRecycler: RecyclerView? = null
    private var myView: View? = null
    private var child: View? = null
    private var constraint: ConstraintLayout? = null
    private var horizontalScrollView: HorizontalScrollView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }


    @SuppressLint("InflateParams", "NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        myView = inflater.inflate(R.layout.fragment_igeny_kontener_kiszedes, container, false)
        mainActivity = activity as MainActivity
        mainActivity!!.ellenorzoKodFragment = null
        childFrame = myView?.data_frame2
        child = layoutInflater.inflate(R.layout.konteneres_view, null)
        childFrame?.addView(child)
        horizontalScrollView = child?.horizontalScrollView3!!
        horizontalScrollView?.isFocusable = false

        progress = child?.konteneresProgress
        exit3Btn = child?.exit3Button
        constraint = child?.constant
        exit3Btn?.isFocusable = true
        setProgressBarOff()


        exit3Btn?.setOnClickListener {
            kontenerList.clear()
            mainActivity?.loadMenuFragment(true)
        }

        return myView
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            IgenyKontenerKiszedesFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onKontenerClick(position: Int) {
        if (mainActivity?.isWifiConnected()!!) {
            MainActivity.wifiInfo = mainActivity?.getMacAndSignalStrength()!!
            mainActivity?.checkIfContainerStatus(kontenerList[position].kontner_id.toString())
            exit3Btn?.isFocusable = false
            exit3Btn?.isFocusableInTouchMode = false
            kontenerList.clear()
            childRecycler?.adapter?.notifyDataSetChanged()
        } else {
            mainActivity?.setAlert("Nincs a wifi bekapcsolva")
        }
    }

    private fun loadData() {
        try {
            kontenerList.clear()
            val myList: ArrayList<KontenerItem> =
                arguments?.getSerializable("KISZEDESLISTA") as ArrayList<KontenerItem>
            for (i in 0 until myList.size) {
                kontenerList.add(
                    KontenerItem(
                        myList[i].kontener,
                        myList[i].polc,
                        myList[i].datum,
                        myList[i].tetelszam,
                        myList[i].kontner_id,
                        myList[i].status
                    )
                )
            }
        } catch (e: Exception) {
            Log.d(TAG, "loadData: ")
        }
    }

    fun setProgressBarOff() {
        progress?.visibility = View.GONE
    }

    fun setProgressBarOn() {
        progress?.visibility = View.VISIBLE
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        childRecycler = child?.child_recycler
        childRecycler?.adapter = KontenerAdapter(kontenerList, this)
        childRecycler?.layoutManager = LinearLayoutManager(child?.context)
        childRecycler?.setHasFixedSize(true)
        kontenerList.clear()
        loadData()
        childRecycler?.adapter?.notifyDataSetChanged()
        super.onResume()
        childRecycler?.requestFocus()
    }

    /* override fun onPause() {
         super.onPause()
         exit3Btn.isFocusable = false
         exit3Btn.isFocusableInTouchMode = false
     }*/
    override fun onDestroyView() {
        Log.d(TAG, "onDestroyView: KISZEDES")
        super.onDestroyView()
        kontenerList.clear()
        constraint = null
        myView = null
        child = null
        childFrame = null
        childRecycler = null
        childRecycler?.adapter = null
        progress = null
        exit3Btn = null
        mainActivity?.igenyKiszedesFragment = null
        mainActivity = null
        horizontalScrollView = null
    }

    fun destroy() {
        myView = null
        child = null
        childRecycler = null
        childRecycler?.adapter = null
        progress = null
        exit3Btn = null
        mainActivity!!.igenyKiszedesFragment = null
        mainActivity = null
        horizontalScrollView = null
    }
}