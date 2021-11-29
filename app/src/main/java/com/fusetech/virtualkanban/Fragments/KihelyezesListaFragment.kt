package com.fusetech.virtualkanban.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fusetech.virtualkanban.activities.MainActivity
import com.fusetech.virtualkanban.adapters.KihelyezesKontenerAdapter
import com.fusetech.virtualkanban.dataItems.KihelyezesKontenerElemek
import com.fusetech.virtualkanban.R
import kotlinx.android.synthetic.main.kihelyezes_header.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import com.fusetech.virtualkanban.fragments.IgenyKontenerKiszedesCikkKiszedes.Companion.isSent
import com.fusetech.virtualkanban.activities.MainActivity.Companion.sz0x
import java.lang.Exception

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val TAG = "KihelyezesListaFragment"

@Suppress("UNCHECKED_CAST")
class KihelyezesListaFragment : Fragment(), KihelyezesKontenerAdapter.KihelyezesListener {
    private var param1: String? = null
    private var param2: String? = null
    val myList: ArrayList<KihelyezesKontenerElemek> = ArrayList()

    private var recycler: RecyclerView? = null
    private var kihelyezes: Button? = null
    private var mainActivity: MainActivity? = null
    private var szerelohely: String? = null
    private var myView: View? = null

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
        myView = inflater.inflate(R.layout.kihelyezes_header, container, false)
        mainActivity = activity as MainActivity
        kihelyezes = myView?.kihelyezesBtn
        recycler = myView?.recKihelyezesLista
        recycler?.adapter = KihelyezesKontenerAdapter(myList, this)
        recycler?.layoutManager = LinearLayoutManager(myView?.context)
        recycler?.setHasFixedSize(true)
        myList.clear()
        getData()
        Log.d(TAG, "onCreateView: $myList")
        kihelyezes?.setOnClickListener {
            kihelyezes?.setBackgroundResource(R.drawable.disabled)
            kihelyezes?.isEnabled = false
            mainActivity?.kihelyezes?.progressBarOn()
            try {
                var a = 0
                CoroutineScope(IO).launch {
                    for (i in 0 until myList.size) {
                        isSent = false
                        if (myList[i].kiadva != 0) {
                            async {
                                mainActivity?.sendKihelyezesXmlData(
                                    myList[i].vonalkod,
                                    sz0x,
                                    myList[i].kiadva.toDouble(),
                                    "21",
                                    "01",
                                    szerelohely!!
                                )
                            }.await()
                            if (isSent) {
                                mainActivity?.updateCikkAfterSend(myList[i].id)
                                a++
                            }
                        }else{
                            a++
                        }
                    }
                    if (a == myList.size) {
                        val kontenerList: ArrayList<String> = ArrayList()
                        for(i in 0 until myList.size){
                           kontenerList.add(myList[i].kontenerID.toString())
                        }
                        val list = kontenerList.distinct()
                        Log.d(TAG, "onCreateView: $list")
                        for (i in 0 until list.size){
                            mainActivity?.closeItem(list[i])
                        }
                        mainActivity?.checkCloseContainer()
                        /*val unique : Set<String> = HashSet<String>(kontenerList)
                        for (code in unique){
                            mainActivity?.closeItem(code)
                        //list.add(kontenerList.stream().distinct().collect(Collectors.toList()).toString())
                        }*/
                      /*  for (temp in hset) {
                            println(temp)
                        }*/
                        /*for (i in 0 until list.size){

                        }*/
                        Log.d(TAG, "Minden cikk lefutott")
                    }
                }
            } catch (e: Exception) {
                mainActivity?.setAlert("${e.printStackTrace()}")
                mainActivity?.kihelyezes?.progressBarOff()
                kihelyezes?.isEnabled = true
            }
            mainActivity?.kihelyezes?.progressBarOff()
            kihelyezes?.isEnabled = true
            if(mainActivity?.isWifiConnected()!!){
                MainActivity.wifiInfo = mainActivity?.getMacAndSignalStrength()!!
            }
        }
        return myView
    }

    @SuppressLint("NotifyDataSetChanged")
    fun getData() {
        val myItems: ArrayList<KihelyezesKontenerElemek> =
            arguments?.getSerializable("KIHELYEZESLISTA") as ArrayList<KihelyezesKontenerElemek>
        for (i in 0 until myItems.size) {
            myList.add(
                KihelyezesKontenerElemek(
                    myItems[i].id,
                    myItems[i].vonalkod,
                    myItems[i].megjegyzes1,
                    myItems[i].megjegyzes2,
                    myItems[i].intrem,
                    myItems[i].igenyelve,
                    myItems[i].kiadva,
                    myItems[i].kontenerID
                )
            )
            recycler?.adapter?.notifyDataSetChanged()
        }
        szerelohely = arguments?.getString("KIHELYEZESHELY") as String
    }

    override fun kihelyezesClick(pos: Int) {
        Log.d(TAG, "kihelyezesClick: ${myList[pos]}")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView: ")
        myView = null
        recycler = null
        recycler?.adapter = null
        kihelyezes = null
        szerelohely = null
        mainActivity = null
    }
}