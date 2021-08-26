package com.fusetech.virtualkanban.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fusetech.virtualkanban.activities.MainActivity
import com.fusetech.virtualkanban.activities.MainActivity.Companion.tempLocations
import com.fusetech.virtualkanban.activities.MainActivity.Companion.progress
import com.fusetech.virtualkanban.fragments.IgenyKontenerKiszedesCikkKiszedes.Companion.isSent
import com.fusetech.virtualkanban.R
import kotlinx.android.synthetic.main.fragment_tobblet_cikkek_polcra.view.*
import com.fusetech.virtualkanban.adapters.PolcLocationAdapter
import com.fusetech.virtualkanban.dataItems.PolcLocation
import kotlinx.android.synthetic.main.fragment_load.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

private const val TAG = "TobbletCikkekPolcraFrag"

@Suppress("UNCHECKED_CAST")
class TobbletCikkekPolcraFragment : Fragment(), PolcLocationAdapter.PolcItemClickListener {

    private var kontenerID: TextView? = null
    private var cikkID: TextView? = null
    private var cikkNumber: EditText? = null
    private var megjegyzes1: TextView? = null
    private var megjegyzes2: TextView? = null
    private var intrem: TextView? = null
    private var unit: TextView? = null
    private var igeny: EditText? = null
    private var polc: EditText? = null
    private var recyclerView: RecyclerView? = null
    private var progressBar: ProgressBar? = null
    private var visszaBtn: Button? = null
    private var mainActivity: MainActivity? = null
    private var tsideContainer: FrameLayout? = null
    private var myView: View? = null
    private var cikkid = 0
    private var kontid = 0
    private var cikkkod = ""
    private var mmegjegyzes1 = ""
    private var mmegjegyzes2 = ""
    private var mintrem = ""
    private var munit = ""
    private var mcikkszam = ""
    private var mmennyiseg: String? = ""

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        myView = inflater.inflate(R.layout.fragment_tobblet_cikkek_polcra, container, false)
        mainActivity = activity as MainActivity
        kontenerID = myView?.tkontenerIDKiszedes!!
        tsideContainer = myView?.tside_container2!!
        tsideContainer?.descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
        cikkID = myView?.tcikkIDKiszedes!!
        cikkNumber = myView?.tkiszedesCikkEdit!!
        cikkNumber?.isFocusable = false
        cikkNumber?.isFocusableInTouchMode = false
        megjegyzes1 = myView?.tkiszedesMegj1!!
        megjegyzes2 = myView?.tkiszedesMegj2!!
        intrem = myView?.tintrem!!
        unit = myView?.tkiszedesUnit!!
        igeny = myView?.tkiszedesIgenyEdit!!
        polc = myView?.tkiszedesPolc!!
        //mennyiseg = view.tkiszedesMennyiseg
        recyclerView = myView?.tlocationRecycler!!
        progressBar = myView?.tkihelyezesProgress!!
        visszaBtn = myView?.tkiszedesVissza!!
        recyclerView?.adapter = PolcLocationAdapter(tempLocations, this)
        recyclerView?.layoutManager = LinearLayoutManager(myView?.context)
        recyclerView?.setHasFixedSize(true)
        igeny?.isFocusable = false
        igeny?.isFocusableInTouchMode = false
        polc?.isFocusable = false
        polc?.isFocusableInTouchMode = false
        recyclerView?.isFocusable = false
        recyclerView?.isFocusableInTouchMode = false
        loadData()
        progrssOff()

        visszaBtn?.setOnClickListener {
            mainActivity?.progressBar?.visibility = View.VISIBLE
            cikkNumber?.setText("")
            kontenerID?.text = ""
            cikkID?.text = ""
            tempLocations.clear()
            polc?.setText("")
            recyclerView?.adapter?.notifyDataSetChanged()
            mainActivity?.run {
                setContainerStatusAndGetItems(kontid.toString())
            }
            mainActivity?.progressBar?.visibility = View.GONE
        }
        return myView
    }

    override fun onResume() {
        super.onResume()
        cikkid = arguments?.getInt("IID")!!
        kontid = arguments?.getInt("KID")!!
        cikkkod = arguments?.getString("MCIKK")!!
        mmegjegyzes1 = arguments?.getString("MMEGJ1")!!
        mmegjegyzes2 = arguments?.getString("MMEGJ2")!!
        mintrem = arguments?.getString("IINT")!!
        munit = arguments?.getString("UUNIT")!!
        mcikkszam = arguments?.getString("MCIKK")!!
        mmennyiseg = arguments?.getString("MMENNY")
        fillWidgets()
    }

    private fun fillWidgets() {
        kontenerID?.text = kontid.toString()
        cikkID?.text = cikkid.toString()
        cikkNumber?.setText(cikkkod)
        megjegyzes1?.text = mmegjegyzes1
        megjegyzes2?.text = mmegjegyzes2
        intrem?.text = mintrem
        unit?.text = munit
        igeny?.setText(mmennyiseg)

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadData() {
        tempLocations.clear()
        val myList: ArrayList<PolcLocation> =
            arguments?.getSerializable("LOCATIONBIN") as ArrayList<PolcLocation>
        if (myList.size > 0) {
            for (i in 0 until myList.size) {
                tempLocations.add(PolcLocation(myList[i].polc, myList[i].mennyiseg))
            }
            recyclerView?.adapter?.notifyDataSetChanged()
        }
    }

    override fun polcItemClick(position: Int) {
        Log.d("TAG", "polcItemClick: ")
    }

    fun progrssOff() {
        progressBar?.visibility = View.GONE
    }

    fun progrssOn() {
        progressBar?.visibility = View.VISIBLE
    }

    fun setCode(code: String) {
        isSent = false
        if (polc?.text?.isEmpty()!!) {
            mainActivity?.raktarcheck(code)
            polc?.setText(code)
            progress.visibility = View.VISIBLE
            CoroutineScope(IO).launch {
                async {
                    Log.d(
                        "IOTHREAD",
                        "onCreateView: ${Thread.currentThread().name}"
                    )
                    mainActivity?.sendKihelyezesXmlData(
                        cikkkod,
                        "BE",//SZ01
                        mmennyiseg.toString().toDouble(),
                        "21",
                        "02",
                        code
                    )
                }.await()
                if (isSent) {
                    CoroutineScope(Main).launch {
                        mainActivity?.setAlert("BRAVOOO")
                        cikkNumber?.setText("")
                        kontenerID?.text = ""
                        cikkID?.text = ""
                        polc?.setText("")
                        tempLocations.clear()
                        recyclerView?.adapter?.notifyDataSetChanged()
                    }
                    CoroutineScope(Main).launch {
                        progress.visibility = View.GONE
                    }
                    mainActivity?.updateCikkandContainer(cikkid, kontid)
                } else {
                    CoroutineScope(Main).launch {
                        mainActivity?.setAlert("A picsába")
                        progress.visibility = View.GONE
                    }
                }
            }
        } else {
            mainActivity?.setAlert("Egy nagy faaaaszt")
        }
    }

    fun clearPocl() {
        polc?.setText("")
    }

    fun setPolc() {
        /* mennyiseg.isEnabled = true
         mennyiseg.requestFocus()*/

    }

    fun onButtonPressed() {
        visszaBtn?.performClick()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun onTimeout() {
        cikkNumber?.setText("")
        kontenerID?.text = ""
        cikkID?.text = ""
        tempLocations.clear()
        polc?.setText("")
        recyclerView?.adapter?.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView: ")
        myView = null
        kontenerID = null
        cikkID = null
        cikkNumber = null
        megjegyzes1 = null
        megjegyzes2 = null
        intrem = null
        unit = null
        igeny = null
        polc = null
        recyclerView = null
        recyclerView?.adapter = null
        progressBar = null
        visszaBtn = null
        tsideContainer = null
        mainActivity?.tobbletCikkekPolcra = null
        mainActivity = null
    }
}