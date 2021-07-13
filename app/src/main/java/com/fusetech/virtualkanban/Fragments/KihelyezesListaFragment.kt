package com.fusetech.virtualkanban.fragments

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
import java.lang.Exception

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val TAG = "KihelyezesListaFragment"

@Suppress("UNCHECKED_CAST")
class KihelyezesListaFragment : Fragment(), KihelyezesKontenerAdapter.KihelyezesListener {
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var recycler: RecyclerView
    val myList: ArrayList<KihelyezesKontenerElemek> = ArrayList()
    private lateinit var kihelyezes: Button
    private lateinit var mainActivity: MainActivity
    private lateinit var szerelohely: String

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
        val view = inflater.inflate(R.layout.kihelyezes_header, container, false)
        mainActivity = activity as MainActivity
        kihelyezes = view.kihelyezesBtn
        recycler = view.recKihelyezesLista
        recycler.adapter = KihelyezesKontenerAdapter(myList,this)
        recycler.layoutManager = LinearLayoutManager(view.context)
        recycler.setHasFixedSize(true)
        myList.clear()
        getData()
        kihelyezes.setOnClickListener {
            kihelyezes.setBackgroundResource(R.drawable.disabled)
            kihelyezes.isEnabled = false
            mainActivity.kihelyezes.progressBarOn()
            try{
                var a = 0
                CoroutineScope(IO).launch {
                    for (i in 0 until myList.size) {
                        isSent = false
                        if (myList[i].kiadva != 0) {
                            async {
                                mainActivity.sendKihelyezesXmlData(
                                    myList[i].vonalkod,
                                    "SZ01",
                                    myList[i].kiadva.toDouble(),
                                    "21",
                                    "01",
                                    szerelohely
                                )
                            }.await()
                            if (isSent) {
                                mainActivity.updateCikkAfterSend(myList[i].id)
                                a++
                            }
                        }
                    }
                    if(a == myList.size){
                        mainActivity.closeItem(myList[0].kontenerID)
                        Log.d(TAG, "Minden cikk lefutott")
                    }
                }
            }catch (e: Exception){
                mainActivity.setAlert("$e")
                mainActivity.kihelyezes.progressBarOff()
                kihelyezes.isEnabled = true
            }
            mainActivity.kihelyezes.progressBarOff()
            kihelyezes.isEnabled = true
        }

        return view
    }

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
            recycler.adapter?.notifyDataSetChanged()
        }
        szerelohely = arguments?.getString("KIHELYEZESHELY") as String
    }

    override fun kihelyezesClick(pos: Int) {
        Log.d(TAG, "kihelyezesClick: ${myList[pos]}")
    }

}