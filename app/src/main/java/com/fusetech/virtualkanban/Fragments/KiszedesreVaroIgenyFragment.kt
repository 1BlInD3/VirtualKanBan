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
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ProgressBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fusetech.virtualkanban.activities.MainActivity
import com.fusetech.virtualkanban.adapters.KontenerAdapter
import com.fusetech.virtualkanban.dataItems.KontenerItem
import com.fusetech.virtualkanban.R
import kotlinx.android.synthetic.main.fragment_kiszedesre_varo_igeny.view.*
import kotlinx.android.synthetic.main.konteneres_view.view.*
import kotlinx.android.synthetic.main.konteneres_view.view.horizontalScrollView3

private var kontenerList: ArrayList<KontenerItem> = ArrayList()
private const val TAG = "KiszedesreVaroIgenyFrag"
private lateinit var sendContainerCode: KiszedesreVaroIgenyFragment.SendCode6

class KiszedesreVaroIgenyFragment : Fragment(), KontenerAdapter.onKontenerClickListener {
    private var progress: ProgressBar? = null
    private var exit3Btn: Button? = null
    private var mainActivity: MainActivity? = null
    private var childFrame: FrameLayout? = null
    private var childRecycler: RecyclerView? = null
    private var myView: View? = null
    private var child: View? = null

    interface SendCode6 {
        fun containerCode(kontener: String)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        myView = inflater.inflate(R.layout.fragment_kiszedesre_varo_igeny, container, false)
        mainActivity = activity as MainActivity
        childFrame = myView?.data_frame3
        child = layoutInflater.inflate(R.layout.konteneres_view, null)
        childFrame?.addView(child)
        progress = child?.konteneresProgress
        exit3Btn = child?.exit3Button
        val horizontalScrollView: HorizontalScrollView = child?.horizontalScrollView3!!
        horizontalScrollView.isFocusable = false
        horizontalScrollView.isFocusableInTouchMode = false
        exit3Btn?.isFocusable = true
        setProgressBarOff()
        childRecycler = child?.child_recycler
        childRecycler?.adapter = KontenerAdapter(kontenerList, this)
        childRecycler?.layoutManager = LinearLayoutManager(child?.context)
        childRecycler?.setHasFixedSize(true)
        kontenerList.clear()
        loadData()

        exit3Btn?.setOnClickListener {
            if(mainActivity?.isWifiConnected()!!){
                MainActivity.wifiInfo = mainActivity?.getMacAndSignalStrength()!!
            }
            kontenerList.clear()
            mainActivity?.loadMenuFragment(true)
        }

        /*Thread(Runnable {
            var oldId = -1
            while (true) {
                val newView: View? = getView()?.findFocus()
                if (newView != null && newView.id != oldId) {
                    oldId = newView.id
                    var idName: String = try {
                        resources.getResourceEntryName(newView.id)
                    } catch (e: Resources.NotFoundException) {
                        newView.id.toString()
                    }
                    Log.i(TAG, "Focused Id: \t" + idName + "\tClass: \t" + newView.javaClass)
                }
                try {
                    Thread.sleep(100)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }).start()*/
        return myView
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onKontenerClick(position: Int) {
        if(mainActivity?.isWifiConnected()!!){
            MainActivity.wifiInfo = mainActivity?.getMacAndSignalStrength()!!
        }
        Log.d(TAG, "onKontenerClick: MEGNYOMTAM")
        childRecycler?.isFocusable = false
        childRecycler?.isFocusableInTouchMode = false
        exit3Btn?.isFocusable = false
        sendContainerCode.containerCode(kontenerList[position].kontner_id.toString())
        kontenerList.clear()
        childRecycler?.adapter?.notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadData() {
        try {
            kontenerList.clear()
            val myList: ArrayList<KontenerItem> =
                arguments?.getSerializable("VAROLISTA") as ArrayList<KontenerItem>
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
            childRecycler?.adapter?.notifyDataSetChanged()
            if (kontenerList.size > 0) {
                childRecycler?.requestFocus()
            }
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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        sendContainerCode = if (context is SendCode6) {
            context
        } else {
            throw RuntimeException(context.toString() + "must implement")
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        myView = null
        child = null
        progress = null
        exit3Btn = null
        childFrame = null
        childRecycler = null
        childRecycler?.adapter = null
        mainActivity?.kiszedesreVaroIgenyFragment = null
        mainActivity = null
    }
}