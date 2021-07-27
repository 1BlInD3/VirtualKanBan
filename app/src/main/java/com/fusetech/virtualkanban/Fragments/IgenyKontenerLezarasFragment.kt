package com.fusetech.virtualkanban.fragments

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
import kotlinx.android.synthetic.main.fragment_igeny_kontener_lezaras.view.*
import kotlinx.android.synthetic.main.konteneres_view.view.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

@Suppress("UNCHECKED_CAST")
class IgenyKontenerLezarasFragment : Fragment(), KontenerAdapter.onKontenerClickListener {
    private var dataFrame: FrameLayout? = null
    private var childRecycler: RecyclerView? = null
    private var kontenerList: ArrayList<KontenerItem> = ArrayList()
    private var param1: String? = null
    private var param2: String? = null
    private var mainActivity: MainActivity? = null
    private var igenyKontener: IgenyKontnerLezaras? = null
    private val TAG = "IgenyKontenerLezarasFra"
    private var exitBtn: Button? = null
    private var progress: ProgressBar? = null
    private var myView : View? = null
    private var child : View? = null

    interface IgenyKontnerLezaras {
        fun sendContainer(container: String)
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
        myView = inflater.inflate(R.layout.fragment_igeny_kontener_lezaras, container, false)
        mainActivity = activity as MainActivity
        dataFrame = myView?.data_frame1
        child = layoutInflater.inflate(R.layout.konteneres_view, null)
        dataFrame?.addView(child)
        val horizontalScrollView: HorizontalScrollView = child?.horizontalScrollView3!!
        horizontalScrollView.isFocusable = false
        horizontalScrollView.isFocusableInTouchMode = false
        exitBtn = child?.exit3Button
        exitBtn?.isFocusable = true
        progress = child?.konteneresProgress
        setProgressBarOff()
        childRecycler = child?.child_recycler
        childRecycler?.adapter = KontenerAdapter(kontenerList, this)
        childRecycler?.layoutManager = LinearLayoutManager(child?.context)
        childRecycler?.setHasFixedSize(true)
        kontenerList.clear()
        loadData()

        exitBtn?.setOnClickListener {
            Log.d(TAG, "onButtonPressed")
            kontenerList.clear()
            mainActivity?.loadMenuFragment(true)
        }

        return myView
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            IgenyKontenerLezarasFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onKontenerClick(position: Int) {
        Log.d(TAG, "onKontenerClick: ${kontenerList[position].kontner_id}")
        igenyKontener?.sendContainer(kontenerList[position].kontner_id.toString())
        kontenerList.clear()
        childRecycler?.adapter?.notifyDataSetChanged()
        exitBtn?.isFocusable = false
        exitBtn?.isFocusableInTouchMode = false
    }

    private fun loadData() {
        val myList: ArrayList<KontenerItem> =
            arguments?.getSerializable("KONTENERLISTA") as ArrayList<KontenerItem>
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
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        igenyKontener = if (context is IgenyKontnerLezaras) {
            context
        } else {
            throw RuntimeException(context.toString() + "must implement")
        }
    }

    fun setProgressBarOff() {
        progress?.visibility = View.GONE
    }

    fun setProgressBarOn() {
        progress?.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView: KonténerLezárás")
        myView = null
        child = null
        dataFrame = null
        childRecycler = null
        childRecycler?.adapter = null
        mainActivity = null
        igenyKontener = null
        exitBtn = null
        progress = null
    }
}