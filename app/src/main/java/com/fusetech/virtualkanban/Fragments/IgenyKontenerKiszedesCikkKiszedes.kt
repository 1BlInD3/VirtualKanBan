package com.fusetech.virtualkanban.Fragments

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fusetech.virtualkanban.Activities.MainActivity
import com.fusetech.virtualkanban.Adapters.PolcLocationAdapter
import com.fusetech.virtualkanban.DataItems.PolcLocation
import com.fusetech.virtualkanban.R
import kotlinx.android.synthetic.main.fragment_igeny_kontener_kiszedes_cikk_kiszedes.*
import kotlinx.android.synthetic.main.fragment_igeny_kontener_kiszedes_cikk_kiszedes.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val TAG = "IgenyKontenerKiszedesCi"

class IgenyKontenerKiszedesCikkKiszedes : Fragment(), PolcLocationAdapter.PolcItemClickListener {
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var cikkEdit: EditText
    private lateinit var meg1: TextView
    private lateinit var meg2: TextView
    private lateinit var intrem: TextView
    private lateinit var unit: TextView
    private lateinit var igeny: EditText
    private lateinit var polc: EditText
    private lateinit var mennyiseg: EditText
    private lateinit var lezar: Button
    private lateinit var vissza: Button
    private lateinit var progress: ProgressBar
    private lateinit var mainActivity: MainActivity
    private lateinit var kontenerNumber: TextView
    private lateinit var cikkNumber: TextView
    private var igenyeltMennyiseg: Double = 0.0
    private var igenyeltMennyisegAmiNemValtozik: Double = 0.0
    private lateinit var locationRecycler: RecyclerView
    private val itemLocationList: ArrayList<PolcLocation> = ArrayList()
    private val tempLocations: ArrayList<PolcLocation> = ArrayList()
    private lateinit var xmlData: SendXmlData
    private var maxMennyiseg: Double = 0.0
    var isSaved = false
    var isUpdated = false

    interface SendXmlData {
        fun sendXmlData(cikk: String, polc: String?, mennyiseg: Double?)
    }

    companion object {
        var isSent = false
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
        val view = inflater.inflate(
            R.layout.fragment_igeny_kontener_kiszedes_cikk_kiszedes,
            container,
            false
        )
        mainActivity = activity as MainActivity
        locationRecycler = view.locationRecycler
        locationRecycler.adapter = PolcLocationAdapter(itemLocationList, this)
        locationRecycler.layoutManager = LinearLayoutManager(view.context)
        locationRecycler.setHasFixedSize(true)
        cikkEdit = view.kiszedesCikkEdit
        meg1 = view.kiszedesMegj1
        meg2 = view.kiszedesMegj2
        intrem = view.intrem
        unit = view.kiszedesUnit
        unit.isAllCaps = true
        igeny = view.kiszedesIgenyEdit
        polc = view.kiszedesPolc
        mennyiseg = view.kiszedesMennyiseg
        lezar = view.kiszedesLezar
        vissza = view.kiszedesVissza
        progress = view.kihelyezesProgress
        kontenerNumber = view.kontenerIDKiszedes
        cikkNumber = view.cikkIDKiszedes
        setProgressBarOff()
        cikkEdit.isFocusable = false
        cikkEdit.isFocusableInTouchMode = false
        igeny.isFocusable = false
        igeny.isFocusableInTouchMode = false
        mennyiseg.isFocusable = false
        mennyiseg.isFocusableInTouchMode = false
        polc.keyListener = null
        polc.isFocusable = true
        polc.isFocusableInTouchMode = true
        polc.isCursorVisible = true
        polc.requestFocus()
        //mennyiseg.requestFocus()
        loadData()
        locationRecycler.adapter?.notifyDataSetChanged()

        lezar.setOnClickListener {
            val builder = AlertDialog.Builder(view.context)
            builder.setTitle("Figyelem")
                .setMessage("Biztos le akarod így zárni?")
            builder.setPositiveButton("Igen") { dialog, which ->
                if(!polc.text.trim().toString().isEmpty() && (mennyiseg.text.trim().toString().isEmpty() || mennyiseg.text.trim().toString() == "0" || mennyiseg.text.trim().toString() == "0.0")) {
                    CoroutineScope(IO).launch {
                        async {
                            mainActivity.updateItemStatus(cikkNumber.text.trim().toString())
                        }.await()
                        if (isUpdated) {
                            mainActivity.updateItemAtvevo(cikkNumber.text.trim().toString())
                            mainActivity.checkIfContainerIsDone(
                                kontenerNumber.text.trim().toString(),
                                cikkNumber.text.trim().toString(),
                                "02",
                                polc.text.trim().toString()
                            )
                            mainActivity.loadMenuFragment(true)
                            mainActivity.loadKiszedesFragment()
                            mainActivity.checkIfContainerStatus(
                                kontenerIDKiszedes.text.trim().toString()
                            )
                        }
                    }
                }else{
                    Toast.makeText(
                        view.context,
                        "Nincs polchely, vagy van mennyiség beírva, így nem zárhatod le!",
                        Toast.LENGTH_LONG
                    ).show()
                }
                Log.d(TAG, "onCreateView: Megnyomtam az IGEN gombot")
            }
            builder.setNegativeButton("Nem") { dialog, which ->
                Log.d(TAG, "onCreateView: Megnyomtam a NEM gombot")
            }
            builder.create()
            builder.show()
        }
        vissza.setOnClickListener {
            mainActivity.cikkUpdate(cikkIDKiszedes.text.trim().toString().toInt())
            mainActivity.loadMenuFragment(true)
            mainActivity.loadKiszedesFragment()
            mainActivity.checkIfContainerStatus(kontenerIDKiszedes.text.trim().toString())
        }
        mennyiseg.setOnClickListener {
            var osszeadva = false
            isUpdated = false
            if (mennyiseg.text?.trim().toString().toDouble() <= maxMennyiseg) {
                if (mennyiseg.text.toString().toDouble() > szazalek(10)) {
                    mainActivity.setAlert("Túl sok ennyit nem vehetsz ki")
                } else /*if (mennyiseg.text.trim().toString().toDouble() <= igenyeltMennyiseg)*/ {
                    val a = mennyiseg.text?.trim().toString().toDouble()
                    val b = polc.text.trim().toString()
                    val c = cikkNumber.text.trim().toString()
                    val cikk = cikkEdit.text.trim().toString()
                    val d = kontenerNumber.text.trim().toString()
                    CoroutineScope(IO).launch {
                        async {
                            mainActivity.insertDataToRaktarTetel(
                                c,
                                a,
                                "02",
                                b
                            )
                        }.await()
                        if (isSaved) {
                            CoroutineScope(Main).launch {
                                igenyeltMennyiseg -= a
                                igeny.setText(igenyeltMennyiseg.toString())
                                for (i in 0 until itemLocationList.size) {
                                    if (itemLocationList[i].polc?.trim() == b) {
                                        itemLocationList[i].mennyiseg =
                                            (itemLocationList[i].mennyiseg.toString()
                                                .toDouble() - a).toString()
                                    }
                                }
                                //TÖMBBE ÍRÁS
                                if (tempLocations.size == 0) {
                                    tempLocations.add(
                                        PolcLocation(
                                            b,
                                            a.toString()
                                        )
                                    )
                                } else {
                                    for (i in 0 until tempLocations.size) {
                                        if (tempLocations[i].polc == b) {
                                            tempLocations[i].mennyiseg =
                                                (tempLocations[i].mennyiseg.toString()
                                                    .toDouble() + a).toString()
                                            osszeadva = true
                                        }
                                    }
                                    if (!osszeadva) {
                                        tempLocations.add(
                                            PolcLocation(
                                                b,
                                                a.toString()
                                            )
                                        )
                                    }
                                }
                                // megnézni, hogy kész e az igény
                                if (igenyeltMennyiseg == 0.0) {
                                    isUpdated = false
                                    CoroutineScope(IO).launch {
                                        async {
                                            Log.d(
                                                "IOTHREAD",
                                                "onCreateView: ${Thread.currentThread().name}"
                                            )
                                            for(i in 0 until tempLocations.size){
                                                isSent = false
                                                xmlData.sendXmlData(cikk,tempLocations[i].polc,tempLocations[i].mennyiseg?.toDouble())
                                            }
                                        }.await()
                                        if(isSent){
                                            mainActivity.checkIfContainerIsDone(d, c, "02", b)
                                            async {
                                                mainActivity.updateItemStatus(c)
                                            }.await()
                                            if (isUpdated) {
                                                mainActivity.updateItemAtvevo(c)
                                                mainActivity.checkIfContainerIsDone(d, c, "02", b)
                                                Log.d(
                                                    "IOTHREAD",
                                                    "onCreateView: ${Thread.currentThread().name}"
                                                )
                                                mainActivity.loadMenuFragment(true)
                                                mainActivity.loadKiszedesFragment()
                                                mainActivity.checkIfContainerStatus(
                                                    kontenerIDKiszedes.text.trim().toString()
                                                )
                                            }
                                        }else{
                                            CoroutineScope(Main).launch {
                                                mainActivity.setAlert("Hiba volt az XML feltöltésnél")
                                            }
                                        }
                                        Log.d(TAG, "onCreateView: LEFUTOTT")
                                        }

                                } else {
                                    Log.d(TAG, "onCreateView: Frissíteni a táblákat")
                                    CoroutineScope(IO).launch {
                                        mainActivity.checkIfContainerIsDone(d, c, "02", b)
                                    }
                                }
                                locationRecycler.adapter?.notifyDataSetChanged()
                                /*for (i in 0 until tempLocations.size) {
                                    Log.d(
                                        TAG,
                                        "NEM ${tempLocations[i].polc} + ${tempLocations[i].mennyiseg}"
                                    )
                                }*/
                            }
                        }
                    }
                }
            } else {
                CoroutineScope(Main).launch {
                    mainActivity.setAlert("Többet adtál meg mint ami a polcon van")
                }
            }
            mennyiseg.setText("")
            mennyiseg.isFocusable = false
            mennyiseg.isFocusableInTouchMode = false
            polc.isFocusable = true
            polc.isFocusableInTouchMode = true
            polc.setText("")
            polc.requestFocus()
        }
        return view
    }

    fun loadData() {
        itemLocationList.clear()
        val myList: ArrayList<PolcLocation> =
            arguments?.getSerializable("K_LIST") as ArrayList<PolcLocation>
        for (i in 0 until myList.size) {
            itemLocationList.add(PolcLocation(myList[i].polc, myList[i].mennyiseg))
        }
    }

    fun setProgressBarOff() {
        progress.visibility = View.GONE
    }

    fun setProgressBarOn() {
        progress.visibility = View.VISIBLE
    }

    fun performButton() {
        vissza.performClick()
    }

    override fun onResume() {
        super.onResume()
        tempLocations.clear()
        cikkEdit.setText(arguments?.getString("K_CIKK"))
        meg1.text = arguments?.getString("K_MEGJ1")
        meg2.text = arguments?.getString("K_MEGJ2")
        intrem.text = arguments?.getString("K_INT")
        igenyeltMennyiseg = arguments?.getDouble("K_IGENY")!!
        igenyeltMennyisegAmiNemValtozik = arguments?.getDouble("K_IGENY")!!
        igeny.setText(igenyeltMennyiseg.toString())
        Log.d(TAG, "onCreateView: ${arguments?.getString("K_IGENY").toString()}")
        unit.text = arguments?.getString("K_UNIT")
        kontenerNumber.text = arguments?.getInt("K_KONTENER").toString()
        cikkNumber.text = arguments?.getInt("K_ID").toString()
        val binNumber = arguments?.getSerializable("K_POLC") as ArrayList<PolcLocation>
        val tempTomb = arguments?.getSerializable("K_TOMB") as ArrayList<PolcLocation>
        if (binNumber.size > 0) {
            for (i in 0 until itemLocationList.size) {
                for (j in 0 until binNumber.size) {
                    if (itemLocationList[i].polc?.trim() == binNumber[j].polc?.trim()) {
                        itemLocationList[i].mennyiseg = (itemLocationList[i].mennyiseg.toString()
                            .toDouble() - binNumber[j].mennyiseg.toString().toDouble()).toString()
                    }
                }
            }
            locationRecycler.adapter?.notifyDataSetChanged()
        }
        if(tempTomb.size > 0){
            for (i in 0 until tempTomb.size){
                tempLocations.add(PolcLocation(tempTomb[i].polc,tempTomb[i].mennyiseg))
            }
            for (i in 0 until tempLocations.size){
                Log.d(TAG, "onResume: ${tempLocations[i].polc}, ${tempLocations[i].mennyiseg}")
            }
        }
    }

    fun szazalek(x: Int): Double {
        val ceiling: Int
        ceiling = ((igenyeltMennyisegAmiNemValtozik / mennyiseg.text.toString().toDouble()) * x).toInt()
        return igenyeltMennyisegAmiNemValtozik + ceiling
    }

    override fun polcItemClick(position: Int) {
        Log.d(TAG, "polcItemClick: MEGNYOMTAM")
    }

    fun setBin(polcName: String) {
        maxMennyiseg = 0.0
        if (polc.text.isEmpty()) {
            for (i in 0 until itemLocationList.size) {
                if (itemLocationList[i].polc?.trim() == polcName.trim()) {
                    polc.setText(polcName)
                    polc.isFocusable = false
                    polc.isFocusableInTouchMode = false
                    mennyiseg.isFocusable = true
                    mennyiseg.isFocusableInTouchMode = true
                    mennyiseg.requestFocus()
                    maxMennyiseg = itemLocationList[i].mennyiseg?.trim().toString().toDouble()
                }
            }
            if (polc.text.isEmpty()) {
                mainActivity.setAlert("Nincs a rakhelyen ilyen tétel")
            }
        }
    }

    override fun onPause() {
        super.onPause()
        polc.setText("")
        mennyiseg.setText("")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        xmlData = if (context is SendXmlData) {
            context
        } else {
            throw RuntimeException(context.toString() + "must implement")
        }
    }
}