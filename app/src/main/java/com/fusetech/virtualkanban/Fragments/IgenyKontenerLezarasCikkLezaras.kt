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
import android.widget.HorizontalScrollView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fusetech.virtualkanban.Activities.MainActivity.Companion.kontItem
import com.fusetech.virtualkanban.Activities.MainActivity
import com.fusetech.virtualkanban.Adapters.KontenerbenLezarasAdapter
import com.fusetech.virtualkanban.DataItems.KontenerbenLezarasItem
import com.fusetech.virtualkanban.R
import kotlinx.android.synthetic.main.kontenerben_lezaras_view.view.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private lateinit var recycler: RecyclerView
private lateinit var exitBtn: Button
private lateinit var lezarBtn: Button
private lateinit var mainActivity: MainActivity
private lateinit var kontenerNev: TextView
private lateinit var progress: ProgressBar
private const val TAG = "IgenyKontenerLezarasCik"
private lateinit var sendItemCode: IgenyKontenerLezarasCikkLezaras.CikkCode

@Suppress("UNCHECKED_CAST")
class IgenyKontenerLezarasCikkLezaras : Fragment(), KontenerbenLezarasAdapter.onItemClickListener {
    private var param1: String? = null
    private var param2: String? = null

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
        recycler.adapter = KontenerbenLezarasAdapter(kontItem, this)
        recycler.layoutManager = LinearLayoutManager(view.context)
        recycler.setHasFixedSize(true)
        kontItem.clear()
        kontenerNev.text = ""
        loadData()
        recycler.adapter?.notifyDataSetChanged()
        recycler.requestFocus()

        exitBtn.setOnClickListener {
            exitBtn.isFocusable = true
            exitBtn.isFocusableInTouchMode = true
            kontItem.clear()
            mainActivity.loadMenuFragment(true)
            if (mainActivity.getFragment("CIKKLEZARASFRAGMENTHATOS")) {
                mainActivity.kiszedesreVaro()
            } else {
                mainActivity.igenyKontenerCheck()
            }
        }
        lezarBtn.setOnClickListener {
            setProgressBarOn()
            mainActivity.closeContainerAndItem()
            kontItem.clear()
            mainActivity.loadMenuFragment(true)
        }
        if (arguments?.getBoolean("LEZARBUTN")!!) {
            lezarBtn.visibility = View.VISIBLE
        } else {
            lezarBtn.visibility = View.GONE
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
        }).start() */

        return view
    }

    fun onTimeout() {
        kontItem.clear()
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
        sendItemCode.cikkCode(kontItem[position].id)
    }
}