package com.fusetech.virtualkanban.fragments

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
import com.fusetech.virtualkanban.activities.MainActivity
import com.fusetech.virtualkanban.adapters.PolcLocationAdapter
import com.fusetech.virtualkanban.dataItems.PolcLocation
import com.fusetech.virtualkanban.R
import com.fusetech.virtualkanban.utils.SQL
import kotlinx.android.synthetic.main.fragment_igeny_kontener_kiszedes_cikk_kiszedes.*
import kotlinx.android.synthetic.main.fragment_igeny_kontener_kiszedes_cikk_kiszedes.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import com.fusetech.virtualkanban.activities.MainActivity.Companion.tempLocations
import com.fusetech.virtualkanban.utils.Email
import java.sql.Connection
import java.sql.DriverManager

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val TAG = "IGK"

@Suppress("UNCHECKED_CAST")
class IgenyKontenerKiszedesCikkKiszedes : Fragment(), PolcLocationAdapter.PolcItemClickListener,
    SQL.SQLAlert {
    private var param1: String? = null
    private var param2: String? = null
    private var cikkEdit: EditText? = null
    private var meg1: TextView? = null
    private var meg2: TextView? = null
    private var intrem: TextView? = null
    private var unit: TextView? = null
    private var igeny: EditText? = null
    private var polc: EditText? = null
    private var mennyiseg: EditText? = null
    private var lezar: Button? = null
    private var vissza: Button? = null
    private var progress: ProgressBar? = null
    private var mainActivity: MainActivity? = null
    private var kontenerNumber: TextView? = null
    private var cikkNumber: TextView? = null
    private var emptyBin: ImageView? = null
    private var igenyeltMennyiseg: Double = 0.0
    private var igenyeltMennyisegAmiNemValtozik: Double = 0.0
    private var locationRecycler: RecyclerView? = null
    private val itemLocationList: ArrayList<PolcLocation> = ArrayList()
    private lateinit var xmlData: SendXmlData
    private var maxMennyiseg: Double = 0.0
    private var myView: View? = null
    var isSaved = false
    var isUpdated = false
    private val sql = SQL(this)
    val email = Email()

    interface SendXmlData {
        fun sendXmlData(
            cikk: String,
            polc: String?,
            mennyiseg: Double?,
            raktarbol: String,
            raktarba: String,
            polcra: String
        )
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
        myView = inflater.inflate(
            R.layout.fragment_igeny_kontener_kiszedes_cikk_kiszedes,
            container,
            false
        )
        mainActivity = activity as MainActivity
        //mainActivity?.removeFragment("NEGYESCIKKEK")
        locationRecycler = myView!!.locationRecycler
        locationRecycler?.adapter = PolcLocationAdapter(itemLocationList, this)
        locationRecycler?.layoutManager = LinearLayoutManager(myView!!.context)
        locationRecycler?.setHasFixedSize(true)
        cikkEdit = myView!!.kiszedesCikkEdit
        val childFrame: FrameLayout = myView!!.side_container2
        childFrame.descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
        meg1 = myView!!.kiszedesMegj1
        meg2 = myView!!.kiszedesMegj2
        intrem = myView!!.intrem
        unit = myView!!.kiszedesUnit
        unit!!.isAllCaps = true
        igeny = myView!!.kiszedesIgenyEdit
        polc = myView!!.kiszedesPolc
        mennyiseg = myView?.kiszedesMennyiseg
        lezar = myView!!.kiszedesLezar
        vissza = myView!!.kiszedesVissza
        progress = myView!!.kihelyezesProgress
        kontenerNumber = myView!!.kontenerIDKiszedes
        cikkNumber = myView!!.cikkIDKiszedes
        setProgressBarOff()
        emptyBin = myView?.emptyBin
        cikkEdit!!.isFocusable = false
        cikkEdit!!.isFocusableInTouchMode = false
        igeny!!.isFocusable = false
        igeny!!.isFocusableInTouchMode = false
        mennyiseg?.isFocusable = false
        mennyiseg?.isFocusableInTouchMode = false
        polc?.keyListener = null
        polc?.isFocusable = false
        polc?.isFocusableInTouchMode = false
        //mennyiseg.requestFocus()
        loadData()
        locationRecycler?.adapter?.notifyDataSetChanged()

        lezar!!.setOnClickListener {
            val builder = AlertDialog.Builder(myView!!.context)
            builder.setTitle("Figyelem")
                .setMessage("Biztos le akarod így zárni?")
            builder.setPositiveButton("Igen") { dialog, which ->
                if (polc!!.text.trim().toString().isNotEmpty() && (mennyiseg?.text?.trim()
                        .toString()
                        .isEmpty() || mennyiseg?.text?.trim()
                        .toString() == "0" || mennyiseg?.text?.trim().toString() == "0.0")
                ) {
                    CoroutineScope(IO).launch {
                        async {
                            mainActivity!!.updateItemStatus(cikkNumber!!.text.trim().toString())
                        }.await()
                        if (isUpdated) {
                            mainActivity!!.updateItemAtvevo(cikkNumber!!.text.trim().toString())
                            mainActivity!!.checkIfContainerIsDone(
                                kontenerNumber!!.text.trim().toString(),
                                cikkNumber!!.text.trim().toString(),
                                "02",
                                polc!!.text.trim().toString()
                            )
                            mainActivity?.igenyKontenerKiszedesCikkKiszedes = null
                            mainActivity!!.loadKoztes()
                            mainActivity!!.checkIfContainerStatus(
                                kontenerIDKiszedes.text.trim().toString()
                            )
                        }
                    }
                } else {
                    mainActivity!!.setAlert("Nincs polchely, vagy van mennyiség beírva, így nem zárhatod le!")
                    /*Toast.makeText(
                        view.context,
                        "Nincs polchely, vagy van mennyiség beírva, így nem zárhatod le!",
                        Toast.LENGTH_LONG
                    ).show()*/
                }
                Log.d(TAG, "onCreateView: Megnyomtam az IGEN gombot")
            }
            builder.setNegativeButton("Nem") { dialog, which ->
                Log.d(TAG, "onCreateView: Megnyomtam a NEM gombot")
            }
            builder.create()
            builder.show()
        }
        vissza!!.setOnClickListener {
            mainActivity!!.cikkUpdate(cikkIDKiszedes.text.trim().toString().toInt())
            //mainActivity.loadMenuFragment(true)
            mainActivity?.igenyKontenerKiszedesCikkKiszedes = null
            mainActivity!!.loadKoztes()
            //mainActivity.loadKiszedesFragment()
            mainActivity!!.checkIfContainerStatus(kontenerIDKiszedes.text.trim().toString())
        }
        mennyiseg?.setOnClickListener {
            var osszeadva = false
            isUpdated = false
            if (mennyiseg?.text?.trim().toString().isNotEmpty()) {
                if (mennyiseg?.text?.trim().toString().toDouble() > getPolcValue(
                        polc!!.text.trim().toString()
                    )
                ) {
                    mainActivity?.setAlert("Túl sok ennyit nem vehetsz ki erről a polcról")
                    email.sendEmail(
                        "kutyu@fusetech.hu",
                        "attila.balind@fusetech.hu",
                        "Készletkorrekció",
                        "Készletet kéne korrigálni"
                    )
                } else /*if (mennyiseg.text.trim().toString().toDouble() <= igenyeltMennyiseg)*/ {
                    val a = mennyiseg?.text?.trim().toString().toDouble()
                    val b = polc!!.text.trim().toString()
                    val c = cikkNumber!!.text.trim().toString()
                    val cikk = cikkEdit!!.text.trim().toString()
                    val d = kontenerNumber!!.text.trim().toString()
                    val k = kontenerIDKiszedes.text.trim().toString()
                    CoroutineScope(IO).launch {
                        async {
                            mainActivity!!.insertDataToRaktarTetel(
                                c,
                                a,
                                "02",
                                b
                            )
                        }.await()
                        if (isSaved) {
                            CoroutineScope(Main).launch {
                                igenyeltMennyiseg -= a
                                igeny!!.setText(igenyeltMennyiseg.toString())
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
                                if (igenyeltMennyiseg <= 0.0) {
                                    CoroutineScope(Main).launch {
                                        setProgressBarOn()
                                    }
                                    isUpdated = false
                                    CoroutineScope(IO).launch {
                                        async {
                                            Log.d(
                                                "IOTHREAD",
                                                "onCreateView: ${Thread.currentThread().name}"
                                            )
                                            for (i in 0 until tempLocations.size) {
                                                isSent = false
                                                xmlData.sendXmlData(
                                                    cikk,
                                                    tempLocations[i].polc,
                                                    tempLocations[i].mennyiseg?.toDouble(),
                                                    "02",
                                                    "21",
                                                    "SZ01"
                                                )
                                            }
                                        }.await()
                                        if (isSent) {
                                            try {
                                                mainActivity!!.checkIfContainerIsDone(d, c, "02", b)
                                                mainActivity!!.updateItemStatus(c)
                                                mainActivity!!.updateItemAtvevo(c)
                                                mainActivity!!.checkIfContainerIsDone(d, c, "02", b)
                                            } catch (e: Exception) {
                                                CoroutineScope(Main).launch {
                                                    mainActivity!!.setAlert("isSent után\n $e")
                                                }
                                            }
                                            Log.d(
                                                "IOTHREAD",
                                                "onCreateView: ${Thread.currentThread().name}"
                                            )
                                            //mainActivity!!.loadMenuFragment(true)
                                            try {
                                                mainActivity?.igenyKontenerKiszedesCikkKiszedes =
                                                    null
                                                mainActivity!!.loadKoztes()
                                                mainActivity!!.checkIfContainerStatus(
                                                    k
                                                )
                                            } catch (e: Exception) {
                                                Log.d(TAG, "onCreateView: $e")
                                            }

                                        } else {
                                            //kitörölni az utolsó tranzakciót
                                            sql.deleteKontenerRaktarTetel(c)
                                            CoroutineScope(Main).launch {
                                                setProgressBarOff()
                                                mainActivity!!.setAlert("Hiba volt az XML feltöltésnél")
                                            }
                                            mainActivity?.igenyKontenerKiszedesCikkKiszedes = null
                                            mainActivity!!.loadKoztes()
                                            mainActivity!!.checkIfContainerStatus(
                                                k
                                            )
                                        }
                                        Log.d(TAG, "onCreateView: LEFUTOTT")
                                    }

                                } else {
                                    Log.d(TAG, "onCreateView: Frissíteni a táblákat")
                                    CoroutineScope(IO).launch {
                                        mainActivity!!.checkIfContainerIsDone(d, c, "02", b)
                                    }
                                }
                                locationRecycler?.adapter?.notifyDataSetChanged()
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
                mennyiseg?.setText("")
                mennyiseg?.isFocusable = false
                mennyiseg?.isFocusableInTouchMode = false
                polc?.isFocusable = true
                polc?.isFocusableInTouchMode = true
                polc?.setText("")
                polc?.requestFocus()
            }
        }

        emptyBin?.setOnLongClickListener {
            if (polc!!.text.trim().toString().isNotEmpty()) {
                val builder = AlertDialog.Builder(myView?.context)
                builder.setTitle("Üres polc?")
                    .setMessage("A polc valóban üres?")
                    .setPositiveButton("Igen") { dialog, which ->
                        CoroutineScope(IO).launch {
                            val a = getName(MainActivity.dolgKod)
                            if (a != "") {
                                email.sendEmail(
                                    "kutyu@fusetech.hu",
                                    "attila.balind@fusetech.hu",
                                    "Készletkorrekció",
                                    "A(z) ${polc!!.text} polc elvileg üres. A Scala szerint ${
                                        getPolcValue(
                                            polc!!.text.trim().toString()
                                        )
                                    } ${unit!!.text.trim()} van rajta\nAdatok:\nCikkszám: ${cikkEdit!!.text}\n${meg1!!.text}\n${meg2!!.text}\n${intrem!!.text}\nKüldte: $a"
                                )

                            }
                        }
                    }
                    .setNegativeButton("Nem") { dialog, which ->

                    }
                builder.create()
                builder.show()
            } else {
                val builder = AlertDialog.Builder(myView?.context)
                builder.setTitle("Üres polc?")
                builder.setMessage("Előbb válaszd ki a polcot!")
                builder.create()
                builder.show()
            }
            true
        }

        return myView
    }

    private fun loadData() {
        itemLocationList.clear()
        val myList: ArrayList<PolcLocation> =
            arguments?.getSerializable("K_LIST") as ArrayList<PolcLocation>
        for (i in 0 until myList.size) {
            itemLocationList.add(PolcLocation(myList[i].polc, myList[i].mennyiseg))
        }
    }

    fun setProgressBarOff() {
        progress?.visibility = View.GONE
    }

    fun setProgressBarOn() {
        progress?.visibility = View.VISIBLE
    }

    fun performButton() {
        vissza?.performClick()
    }

    fun onTimeout() {
        mainActivity?.cikkUpdate(cikkIDKiszedes.text.trim().toString().toInt())
        //clearLeak()
        myView = null
        cikkEdit = null
        meg1 = null
        meg2 = null
        intrem = null
        unit = null
        igeny = null
        polc = null
        mennyiseg = null
        lezar = null
        vissza = null
        progress = null
        kontenerNumber = null
        cikkNumber = null
        locationRecycler = null
        locationRecycler?.adapter = null
        //xmlData = null
        mainActivity?.igenyKontenerKiszedesCikkKiszedes = null
        mainActivity?.loadLoginFragment()
    }

    override fun onResume() {
        super.onResume()
        tempLocations.clear()
        cikkEdit?.setText(arguments?.getString("K_CIKK"))
        meg1?.text = arguments?.getString("K_MEGJ1")
        meg2?.text = arguments?.getString("K_MEGJ2")
        intrem?.text = arguments?.getString("K_INT")
        igenyeltMennyiseg = arguments?.getDouble("K_IGENY")!!
        igenyeltMennyisegAmiNemValtozik = arguments?.getDouble("K_IGENY")!!
        igeny?.setText(igenyeltMennyiseg.toString())
        Log.d(TAG, "onCreateView: ${arguments?.getString("K_IGENY").toString()}")
        unit?.text = arguments?.getString("K_UNIT")
        kontenerNumber?.text = arguments?.getInt("K_KONTENER").toString()
        cikkNumber?.text = arguments?.getInt("K_ID").toString()
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
            locationRecycler?.adapter?.notifyDataSetChanged()
        }
        if (tempTomb.size > 0) {
            for (i in 0 until tempTomb.size) {
                tempLocations.add(PolcLocation(tempTomb[i].polc, tempTomb[i].mennyiseg))
            }
            for (i in 0 until tempLocations.size) {
                Log.d(TAG, "onResume: ${tempLocations[i].polc}, ${tempLocations[i].mennyiseg}")
            }
        }
    }

    /*private fun szazalek(x: Int): Double {
        val ceiling: Int =
            ((igenyeltMennyisegAmiNemValtozik / mennyiseg?.text.toString().toDouble()) * x).toInt()
        return (igenyeltMennyisegAmiNemValtozik + ceiling)
    }*/

    override fun polcItemClick(position: Int) {
        Log.d(TAG, "polcItemClick: MEGNYOMTAM")
    }

    fun setBin(polcName: String) {
        maxMennyiseg = 0.0
        if (polc?.text?.isEmpty()!!) {
            for (i in 0 until itemLocationList.size) {
                if (itemLocationList[i].polc?.trim() == polcName.trim()) {
                    polc?.setText(polcName)
                    polc?.isFocusable = false
                    polc?.isFocusableInTouchMode = false
                    mennyiseg?.isFocusable = true
                    mennyiseg?.isFocusableInTouchMode = true
                    mennyiseg?.requestFocus()
                    maxMennyiseg = itemLocationList[i].mennyiseg?.trim().toString().toDouble()
                }
            }
            if (polc?.text?.isEmpty()!!) {
                mainActivity?.setAlert("Nincs a rakhelyen ilyen tétel")
            }
        }
    }

    override fun onPause() {
        super.onPause()
        polc?.setText("")
        mennyiseg?.setText("")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        xmlData = if (context is SendXmlData) {
            context
        } else {
            throw RuntimeException(context.toString() + "must implement")
        }
    }

    override fun sendMessage(message: String) {
        TODO("Not yet implemented")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView: LEFUTOTT")
        clearLeak()

    }

    fun clearLeak() {
        myView = null
        cikkEdit = null
        meg1 = null
        meg2 = null
        intrem = null
        unit = null
        igeny = null
        polc = null
        mennyiseg = null
        lezar = null
        vissza = null
        progress = null
        kontenerNumber = null
        cikkNumber = null
        locationRecycler = null
        locationRecycler?.adapter = null
        //xmlData = null
        mainActivity = null
    }

    private fun getPolcValue(polcName: String): Double {
        for (i in 0 until itemLocationList.size) {
            if (itemLocationList[i].polc?.trim().equals(polcName)) {
                return itemLocationList[i].mennyiseg?.trim().toString().toDouble()
            }
        }
        return 0.0
    }

    private fun getName(code: String): String {
        var name = ""
        val connection: Connection
        Class.forName("net.sourceforge.jtds.jdbc.Driver")
        connection = DriverManager.getConnection(MainActivity.url)
        val statement =
            connection.prepareStatement(MainActivity.res.getString(R.string.nev))
        statement.setString(1, code)
        val resultSet = statement.executeQuery()
        if (!resultSet.next()) {
            mainActivity?.setAlert("Nem jó kód a névnél")
        } else {
            name = resultSet.getString("TextDescription")
        }
        return name
    }
}