package com.fusetech.virtualkanban.Fragments

import android.content.Context
import android.content.res.Resources
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
import com.fusetech.virtualkanban.Activities.MainActivity
import com.fusetech.virtualkanban.Adapters.KontenerAdapter
import com.fusetech.virtualkanban.DataItems.KontenerItem
import com.fusetech.virtualkanban.R
import kotlinx.android.synthetic.main.fragment_kiszedesre_varo_igeny.view.*
import kotlinx.android.synthetic.main.kontenerben_lezaras_view.view.*
import kotlinx.android.synthetic.main.konteneres_view.view.*
import kotlinx.android.synthetic.main.konteneres_view.view.horizontalScrollView3

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private lateinit var childFrame: FrameLayout
private lateinit var childRecycler: RecyclerView
private var kontenerList: ArrayList<KontenerItem> = ArrayList()
private lateinit var progress: ProgressBar
private lateinit var exit3Btn: Button
private lateinit var mainActivity: MainActivity
private const val TAG = "KiszedesreVaroIgenyFrag"
private lateinit var sendContainerCode: KiszedesreVaroIgenyFragment.SendCode6
class KiszedesreVaroIgenyFragment : Fragment(),KontenerAdapter.onKontenerClickListener {
    private var param1: String? = null
    private var param2: String? = null

    interface SendCode6{
        fun containerCode(kontener: String)
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
        val view = inflater.inflate(R.layout.fragment_kiszedesre_varo_igeny, container, false)
        mainActivity = activity as MainActivity
        childFrame = view.data_frame3
        val child = layoutInflater.inflate(R.layout.konteneres_view,null)
        childFrame.addView(child)
        progress = child.konteneresProgress
        exit3Btn = child.exit3Button
        val horizontalScrollView: HorizontalScrollView = child.horizontalScrollView3
        horizontalScrollView.isFocusable = false
        horizontalScrollView.isFocusableInTouchMode = false
        exit3Btn.isFocusable = true
        exit3Btn.isFocusableInTouchMode = true
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
        Log.d(TAG, "onKontenerClick: MEGNYOMTAM")
        childRecycler.isFocusable = false
        childRecycler.isFocusableInTouchMode = false
        exit3Btn.isFocusable = false
        exit3Btn.isFocusableInTouchMode = false
        sendContainerCode.containerCode(kontenerList[position].kontner_id.toString())
        kontenerList.clear()
        childRecycler.adapter?.notifyDataSetChanged()
    }
    private fun loadData(){
        try {
            kontenerList.clear()
            val myList: ArrayList<KontenerItem> = arguments?.getSerializable("VAROLISTA") as ArrayList<KontenerItem>
            for(i in 0 until myList.size){
                kontenerList.add(KontenerItem(myList[i].kontener,myList[i].polc,myList[i].datum,myList[i].tetelszam,myList[i].kontner_id,myList[i].status))
            }
        }catch (e: Exception){
            Log.d(TAG, "loadData: $e")
        }
    }
    fun setProgressBarOff(){
        progress.visibility = View.GONE
    }
    fun setProgressBarOn(){
        progress.visibility = View.VISIBLE
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        sendContainerCode = if(context is SendCode6){
            context
        }else{
            throw RuntimeException(context.toString() + "must implement")
        }
    }
}