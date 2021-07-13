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

private lateinit var childRecycler: RecyclerView
private var kontenerList: ArrayList<KontenerItem> = ArrayList()
private const val TAG = "IgenyKontenerKiszedesFr"

@Suppress("UNCHECKED_CAST")
class IgenyKontenerKiszedesFragment : Fragment(),KontenerAdapter.onKontenerClickListener{

    private var param1: String? = null
    private var param2: String? = null
    private lateinit var progress: ProgressBar
    private lateinit var exit3Btn: Button
    private lateinit var mainActivity: MainActivity
    private lateinit var childFrame: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    @SuppressLint("InflateParams")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       val view = inflater.inflate(R.layout.fragment_igeny_kontener_kiszedes, container, false)
        mainActivity = activity as MainActivity
        childFrame = view.data_frame2
        val child = layoutInflater.inflate(R.layout.konteneres_view,null)
        childFrame.addView(child)
        val horizontalScrollView: HorizontalScrollView = child.horizontalScrollView3
        horizontalScrollView.isFocusable = false
        horizontalScrollView.isFocusableInTouchMode = false
        progress = child.konteneresProgress
        exit3Btn = child.exit3Button
        exit3Btn.isFocusable = true
        //exit3Btn.isFocusableInTouchMode = true
        setProgressBarOff()
        childRecycler = child.child_recycler
        childRecycler.adapter = KontenerAdapter(kontenerList,this)
        childRecycler.layoutManager = LinearLayoutManager(child.context)
        childRecycler.setHasFixedSize(true)
        kontenerList.clear()
        loadData()
        childRecycler.adapter?.notifyDataSetChanged()

        exit3Btn.setOnClickListener {
            kontenerList.clear()
            mainActivity.loadMenuFragment(true)
        }

       return view
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

    override fun onKontenerClick(position: Int) {
        mainActivity.checkIfContainerStatus(kontenerList[position].kontner_id.toString())
        exit3Btn.isFocusable = false
        exit3Btn.isFocusableInTouchMode = false
        kontenerList.clear()
        childRecycler.adapter?.notifyDataSetChanged()
    }
    private fun loadData(){
        try {
            kontenerList.clear()
            val myList: ArrayList<KontenerItem> = arguments?.getSerializable("KISZEDESLISTA") as ArrayList<KontenerItem>
            for(i in 0 until myList.size){
                kontenerList.add(KontenerItem(myList[i].kontener,myList[i].polc,myList[i].datum,myList[i].tetelszam,myList[i].kontner_id,myList[i].status))
            }
        }catch (e: Exception){
            Log.d(TAG, "loadData: ")
        }
    }
    fun setProgressBarOff(){
        progress.visibility = View.GONE
    }
    fun setProgressBarOn(){
        progress.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        childRecycler.requestFocus()
    }
   /* override fun onPause() {
        super.onPause()
        exit3Btn.isFocusable = false
        exit3Btn.isFocusableInTouchMode = false
    }*/

    override fun onDestroy() {
        kontenerList.clear()
        super.onDestroy()
    }
}