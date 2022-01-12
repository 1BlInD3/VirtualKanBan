package com.fusetech.virtualkanban.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.InputFilter
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
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
import com.fusetech.virtualkanban.activities.MainActivity.Companion.sz0x
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
    private var szalagCim: TextView? = null
    private var cikkNumber: TextView? = null
    private var bejelentes: ImageView? = null
    private var background: ConstraintLayout? = null
    private var appHeader: ConstraintLayout? = null
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
    private val email = Email()

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

    @SuppressLint("NotifyDataSetChanged")
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
        mainActivity?.removeFragment("NEGYESCIKKEK")
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
        //progress = myView!!.kihelyezesProgress
        kontenerNumber = myView!!.kontenerIDKiszedes
        cikkNumber = myView!!.cikkIDKiszedes
        bejelentes = myView!!.bejelentesBtn
        szalagCim = myView!!.textView16
        bejelentes?.visibility = View.GONE
        background = myView!!.backGroundConstrait
        appHeader = myView!!.constraintLayout
        cikkEdit!!.isFocusable = false
        cikkEdit!!.isFocusableInTouchMode = false
        igeny!!.isFocusable = false
        igeny!!.isFocusableInTouchMode = false
        mennyiseg?.isFocusable = false
        mennyiseg?.isFocusableInTouchMode = false
        mennyiseg?.filters = arrayOf<InputFilter>(
            PolcraHelyezesFragment.DecimalDigitsInputFilter(
                9,
                2
            )
        )
        //mainActivity?.hideSystemUI()

        /*polc?.keyListener = null
         polc?.isFocusable = false
         polc?.isFocusableInTouchMode = false*/
        polc?.requestFocus()
        //mennyiseg.requestFocus()
        loadData()
        locationRecycler?.adapter?.notifyDataSetChanged()
        //itemLocationList.add(PolcLocation("ABCD","1234567.0000"))
        lezar!!.setOnClickListener {
            if (tempLocations.size > 0) {
                val builder = AlertDialog.Builder(myView!!.context)
                builder.setTitle("Figyelem")
                    .setMessage("Biztos le akarod így zárni?")
                builder.setPositiveButton("Igen") { _, _ ->
                    closeAnyways(3)
                    mainActivity?.hideSystemUI()
                }
                builder.setNegativeButton("Nem") { _, _ ->
                    Log.d(TAG, "onCreateView: Megnyomtam a NEM gombot")
                    mainActivity?.hideSystemUI()
                }
                builder.setOnCancelListener {
                    mainActivity?.hideSystemUI()
                }
                builder.create()
                builder.show().getButton(DialogInterface.BUTTON_POSITIVE).requestFocus()
            } else {
                CoroutineScope(IO).launch {
                    val b = polc!!.text.trim().toString()
                    val c = cikkNumber!!.text.trim().toString()
                    val d = kontenerNumber!!.text.trim().toString()
                    val k = kontenerIDKiszedes.text.trim().toString()
                    nullavalKiut(c)
                    mainActivity!!.checkIfContainerIsDone(d, c, "02", b)
                    mainActivity!!.updateItemStatus(c, 3)
                    mainActivity!!.updateItemAtvevo(c)
                    mainActivity!!.checkIfContainerIsDone(d, c, "02", b)
                    try {
                        mainActivity?.igenyKontenerKiszedesCikkKiszedes =
                            null
                        mainActivity!!.loadKoztes()
                        mainActivity!!.checkIfContainerStatus(
                            k
                        )
                    } catch (e: Exception) {
                        Log.d(TAG, "lezaras nullaval: $e")
                    }
                }
                //closeAnyways(3)
                // akkor ez a cikk 3-ös státuszt kell kapjon
            }
            if (mainActivity?.isWifiConnected()!!) {
                MainActivity.wifiInfo = mainActivity?.getMacAndSignalStrength()!!
            }
        }
        vissza!!.setOnClickListener {
            mainActivity!!.cikkUpdate(cikkIDKiszedes.text.trim().toString().toInt())
            mainActivity?.igenyKontenerKiszedesCikkKiszedes = null
            mainActivity!!.loadKoztes()
            mainActivity!!.checkIfContainerStatus(kontenerIDKiszedes.text.trim().toString())
            if (mainActivity?.isWifiConnected()!!) {
                MainActivity.wifiInfo = mainActivity?.getMacAndSignalStrength()!!
            }
        }
        mennyiseg?.setOnClickListener {
            isUpdated = false
            if (mennyiseg?.text?.trim().toString().isNotEmpty() && mennyiseg?.text?.trim()
                    .toString().toDouble() > 0
            ) {
                if (mennyiseg?.text?.trim().toString().toDouble() > getPolcValue(
                        polc!!.text.trim().toString()
                    )
                ) {
                    if (bejelentes?.visibility == View.VISIBLE) {
                        mainActivity?.setAlert("A jelentéshez nyomd meg hosszan a megafon ikont!")
                    } else {
                        mainActivity?.setAlert("Túl sok ennyit nem vehetsz ki erről a polcról")
                        mennyiseg?.selectAll()
                    }
                } else if ((mennyiseg?.text?.trim().toString()
                        .toDouble() < igenyeltMennyiseg) && (getPolcValue(
                        polc!!.text.trim().toString()
                    ) > mennyiseg?.text?.trim().toString().toDouble())
                ) {
                    val builder = AlertDialog.Builder(myView!!.context)
                    builder.setTitle("Kevesebb van a polcon?")
                    builder.setMessage("Azért nem vetted ki az egészet, mert nincs ennyi a polcon?")
                    builder.setPositiveButton("Igen") { _, _ ->
                        CoroutineScope(IO).launch {
                            val a = getName(MainActivity.dolgKod)
                            if (a != "") {
                                try {
                                    CoroutineScope(Main).launch {
                                        MainActivity.progress.visibility = View.VISIBLE
                                    }
                                    email.sendEmail(
                                        "KanBan@fusetech.hu",
                                        "keszlet.modositas@fusetech.hu",
                                        "Készletkorrekció",
                                        "A(z) ${polc!!.text} polcon mennyiségi eltérést észleltem. A Scala szerint ${
                                            getPolcValue(polc!!.text.trim().toString())
                                        } ${unit!!.text.trim()} volt rajta\nValójában ${
                                            mennyiseg?.text?.trim().toString().toDouble()
                                        }${unit!!.text.trim()} tudtam levenni\nAdatok:\nCikkszám: ${cikkEdit!!.text}\n${meg1!!.text}\n${meg2!!.text}\n${intrem!!.text}\nKüldte: $a\n\nKérlek a levélre ne válaszolj!"
                                    )
                                    sendLogic()
                                    CoroutineScope(Main).launch {
                                        mennyiseg?.setText("")
                                        mennyiseg?.isFocusable = false
                                        mennyiseg?.isFocusableInTouchMode = false
                                        polc?.isFocusable = true
                                        polc?.isFocusableInTouchMode = true
                                        removeFromList(polc!!.text.trim().toString())
                                        polc?.setText("")
                                        polc?.requestFocus()
                                        bejelentes?.visibility = View.GONE
                                        mainActivity?.setAlert("E-mail elküldve\n A polc már nincs a listában")
                                        MainActivity.progress.visibility = View.GONE
                                    }
                                } catch (e: Exception) {
                                    mainActivity?.setAlert("HIánynál fellépett a probléma\n $e")
                                }

                            } else {
                                mainActivity?.setAlert("Nem sikerült a nevet megszereezni")
                            }
                        }
                        mainActivity?.hideSystemUI()
                    }
                    builder.setNegativeButton("Nem") { _, _ ->
                        sendLogic()
                        mennyiseg?.setText("")
                        mennyiseg?.isFocusable = false
                        mennyiseg?.isFocusableInTouchMode = false
                        polc?.isFocusable = true
                        polc?.isFocusableInTouchMode = true
                        polc?.setText("")
                        polc?.requestFocus()
                        bejelentes?.visibility = View.GONE
                        mainActivity?.hideSystemUI()
                    }
                    builder.setOnCancelListener {
                        mainActivity?.hideSystemUI()
                    }
                    builder.create()
                    builder.show().getButton(DialogInterface.BUTTON_POSITIVE).requestFocus()

                } else {
                    sendLogic()
                    mennyiseg?.setText("")
                    mennyiseg?.isFocusable = false
                    mennyiseg?.isFocusableInTouchMode = false
                    polc?.isFocusable = true
                    polc?.isFocusableInTouchMode = true
                    polc?.setText("")
                    polc?.requestFocus()
                    bejelentes?.visibility = View.GONE
                }
            } else if (mennyiseg?.text?.trim().toString().isNotEmpty() && mennyiseg?.text?.trim().toString().toDouble() == 0.0) {
                val builder = AlertDialog.Builder(myView?.context!!)
                builder.setTitle("Üres polc?")
                builder.setMessage("A ${polc?.text?.trim()} polc valóban üres?")
                builder.setPositiveButton("Igen") { _, _ ->
                    CoroutineScope(IO).launch {
                        email.sendEmail(
                            "KanBan@fusetech.hu",
                            "keszlet.modositas@fusetech.hu",
                            "Üres polc bejelentés",
                            "A ${polc?.text?.trim()} elvileg üres. A Scala szerint a ${cikkEdit?.text.toString().trim()} cikkből ${
                                getPolcValue(polc?.text?.trim().toString())
                            } ${unit?.text?.trim()} van rajta "
                        )
                        CoroutineScope(Main).launch {
                            removeFromList(polc?.text?.trim().toString())
                            mennyiseg?.setText("")
                        }
                    }
                    mainActivity?.hideSystemUI()
                }
                builder.setNegativeButton("Nem") { _, _ ->
                    mainActivity?.hideSystemUI()
                }
                builder.setOnCancelListener {
                    mainActivity?.hideSystemUI()
                }
                builder.create()
                builder.show().getButton(DialogInterface.BUTTON_POSITIVE).requestFocus()
            }
            if (mainActivity?.isWifiConnected()!!) {
                MainActivity.wifiInfo = mainActivity?.getMacAndSignalStrength()!!
            }
        }
        bejelentes?.setOnLongClickListener {
            if (mennyiseg?.text?.trim().toString().isNotEmpty() && mennyiseg?.text?.trim()
                    .toString().toDouble() > 0
            ) {
                CoroutineScope(IO).launch {
                    try {
                        val a = getName(MainActivity.dolgKod)
                        if (a != "") {
                            email.sendEmail(
                                "KanBan@fusetech.hu",
                                "keszlet.modositas@fusetech.hu",
                                "Készletkorrekció",
                                "A(z) ${polc!!.text} polcon mennyiségi eltérést észleltem. A Scala szerint ${
                                    getPolcValue(polc!!.text.trim().toString())
                                } ${unit!!.text.trim()} volt rajta.\nValójában ${
                                    mennyiseg?.text?.trim().toString().toDouble()
                                }${unit!!.text.trim()} -t találtam\nAdatok:\nCikkszám: ${cikkEdit!!.text}\n${meg1!!.text}\n${meg2!!.text}\n${intrem!!.text}\nKüldte: $a\n\nKérlek a levélre ne válaszolj"
                            )
                            CoroutineScope(Main).launch {
                                // background?.setBackgroundColor(resources.getColor(R.color.pocakszin2))
                                // appHeader?.setBackgroundColor(resources.getColor(R.color.pocakszin4))
                                background?.setBackgroundColor(
                                    ContextCompat.getColor(
                                        myView!!.context,
                                        R.color.pocakszin2
                                    )
                                )
                                appHeader?.setBackgroundColor(
                                    ContextCompat.getColor(
                                        myView!!.context,
                                        R.color.pocakszin4
                                    )
                                )
                                lezar?.isVisible = true
                                vissza?.isVisible = true
                                szalagCim?.text = resources.getString(R.string.ikk)
                                bejelentes?.visibility = View.GONE
                                mennyiseg?.isFocusable = false
                                mennyiseg?.isFocusableInTouchMode = false
                                polc?.setText("")
                                polc?.requestFocus()
                                mennyiseg?.setText("")
                                mainActivity?.setAlert("E-mail sikeresen elküldve!")
                            }
                        } else {
                            CoroutineScope(Main).launch {
                                mainActivity?.setAlert("Nem lehet a nevet a többletnél megszerezni!")
                            }

                        }
                    } catch (e: Exception) {
                        CoroutineScope(Main).launch {
                            mainActivity?.setAlert("Hiba történt a többlet rendezésénél\n $e")
                        }

                    }
                }

            } else {
                mainActivity?.setAlert("Nem lehet a mennyiség üres!")
            }
            if (mainActivity?.isWifiConnected()!!) {
                MainActivity.wifiInfo = mainActivity?.getMacAndSignalStrength()!!
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

    fun performButton() {
        if (szalagCim?.text != resources.getString(R.string.bejelentes)) {
            vissza?.performClick()
        }
    }

    fun onTimeout() {
        mainActivity?.cikkUpdate(cikkIDKiszedes.text.trim().toString().toInt())
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
        mainActivity?.igenyKontenerKiszedesCikkKiszedes = null
        appHeader = null
        szalagCim = null
        background = null
        mainActivity?.loadLoginFragment()
    }

    @SuppressLint("NotifyDataSetChanged")
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

    override fun polcItemClick(position: Int) {
        val qty = getPolcValue(polc!!.text.trim().toString())
        val bin = polc?.text.toString().trim()
        val cikk = cikkEdit?.text.toString().trim()
        val unity = unit!!.text.trim()
        Log.d(TAG, "polcItemClick: MEGNYOMTAM")
        if(cikkEdit?.text.toString().isNotEmpty() && polc?.text.toString().trim().isNotEmpty()){
            val builder = AlertDialog.Builder(myView!!.context)
            builder.setTitle("Figyelem")
            builder.setMessage("A $cikk cikk nincs a $bin polcon?")
            builder.setPositiveButton("Igen"){_,_ ->
                CoroutineScope(IO).launch {
                    email.sendEmail("KanBan@fusetech.hu",
                        "keszlet.modositas@fusetech.hu",
                        "Készletkorrekció",
                        "A $bin polcon a $cikk cikk $qty $unity nem található")
                }
                removeFromList(bin)
            }
            builder.setNegativeButton("Nem"){_,_ ->
            }
            builder.create()
            builder.show()
        }
    }

    fun setBin(polcName: String) {
        if (mainActivity?.isWifiConnected()!!) {
            MainActivity.wifiInfo = mainActivity?.getMacAndSignalStrength()!!
        }
        if (mainActivity?.isWifiConnected()!!) {
            MainActivity.wifiInfo = mainActivity?.getMacAndSignalStrength()!!
        }
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
                    if (itemLocationList[i].mennyiseg?.trim().toString()
                            .toDouble() == 0.0 && bejelentes?.visibility == View.GONE
                    ) {
                        bejelentes?.visibility = View.VISIBLE
                        // appHeader?.setBackgroundColor(resources.getColor(R.color.darkRed))
                        appHeader?.setBackgroundColor(
                            ContextCompat.getColor(
                                myView!!.context,
                                R.color.darkRed
                            )
                        )
                        background?.setBackgroundColor(
                            ContextCompat.getColor(
                                myView!!.context,
                                R.color.mildRed
                            )
                        )
                        lezar?.isVisible = false
                        vissza?.isVisible = false
                        // background?.setBackgroundColor(resources.getColor(R.color.mildRed))
                        szalagCim?.text = resources.getString(R.string.bejelentes)
                        mainActivity?.setAlert("Beléptél a bejelentő módba. Kérlek add meg a többlet mennyiséget ami a polcon van")

                    }
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

        Log.d(TAG, "onDestroyView: LEFUTOTT")
        clearLeak()
        super.onDestroyView()
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
        appHeader = null
        background = null
        szalagCim = null
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
        try {
            CoroutineScope(Main).launch {
                MainActivity.progress.visibility = View.VISIBLE
            }
            var name = ""
            Class.forName("net.sourceforge.jtds.jdbc.Driver")
            val connection: Connection = DriverManager.getConnection(MainActivity.url)
            val statement =
                connection.prepareStatement(MainActivity.res.getString(R.string.nev))
            statement.setString(1, code)
            val resultSet = statement.executeQuery()
            if (!resultSet.next()) {
                mainActivity?.setAlert("Nem jó kód a névnél")
                CoroutineScope(Main).launch {
                    MainActivity.progress.visibility = View.GONE
                }
            } else {
                name = resultSet.getString("TextDescription")
                CoroutineScope(Main).launch {
                    MainActivity.progress.visibility = View.GONE
                }
            }
            return name

        } catch (e: Exception) {
            val name = ""
            CoroutineScope(Main).launch {
                MainActivity.progress.visibility = View.GONE
            }
            return name
        }
    }

    private fun nullavalKiut(id: String) {
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver")
            val connection = DriverManager.getConnection(MainActivity.connectionString)
            val statement =
                connection.prepareStatement(MainActivity.res.getString(R.string.updateMozgatottMennyiseg))
            statement.setString(1, id)
            statement.executeUpdate()
        } catch (e: Exception) {
            CoroutineScope(Main).launch {
                mainActivity?.setAlert("Itt a probléma\n $e")
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun removeFromList(bin: String) {
        for (i in 0 until itemLocationList.size) {
            if (bin.trim() == itemLocationList[i].polc?.trim()) {
                itemLocationList.remove(itemLocationList[i])
                break
            }
        }
        locationRecycler?.adapter?.notifyDataSetChanged()

        if (itemLocationList.size < 1) {
            lezaras()
        } else {
            polc?.setText("")
            polc?.isFocusable = true
            polc?.isFocusableInTouchMode = true
            polc?.requestFocus()
            mennyiseg?.isFocusable = false
        }
    }

    private fun lezaras() {
        CoroutineScope(IO).launch {
            async {
                mainActivity!!.updateItemStatus(cikkNumber!!.text.trim().toString(), 3)
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
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun sendLogic() {
        var osszeadva = false
        val a = mennyiseg?.text?.trim().toString().toDouble()
        val b = polc!!.text.trim().toString()
        val c = cikkNumber!!.text.trim().toString()
        val cikk = cikkEdit!!.text.trim().toString()
        val d = kontenerNumber!!.text.trim().toString()
        val k = kontenerIDKiszedes.text.trim().toString()
        CoroutineScope(IO).launch {
            CoroutineScope(Main).launch {
                progress?.visibility = View.VISIBLE
            }
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
                            MainActivity.progress.visibility = View.VISIBLE
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
                                        sz0x
                                    )
                                }
                            }.await()
                            if (isSent) {
                                try {
                                    mainActivity!!.checkIfContainerIsDone(d, c, "02", b)
                                    mainActivity!!.updateItemStatus(c, 3)
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
                                    MainActivity.progress.visibility = View.GONE
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
                }
            }
            CoroutineScope(Main).launch {
                progress?.visibility = View.GONE
            }
        }
    }

    private fun closeAnyways(status: Int) { // Lezárja úgy ahogy van, ha van neki a tömbbe értéke
        val b = polc!!.text.trim().toString()
        val c = cikkNumber!!.text.trim().toString()
        val cikk = cikkEdit!!.text.trim().toString()
        val d = kontenerNumber!!.text.trim().toString()
        val k = kontenerIDKiszedes.text.trim().toString()
        isUpdated = false
        CoroutineScope(IO).launch {
            async {
                Log.d(
                    "IOTHREAD",
                    "onCreateView: ${Thread.currentThread().name}"
                )
                for (i in 0 until tempLocations.size) { // a nullával kiütésnél, ha nicns ilyen akkor ezt még át kell gondolni
                    isSent = false
                    xmlData.sendXmlData(
                        cikk,
                        tempLocations[i].polc,
                        tempLocations[i].mennyiseg?.toDouble(),
                        "02",
                        "21",
                        sz0x
                    )
                }
            }.await()
            if (isSent) {
                try {
                    mainActivity!!.checkIfContainerIsDone(d, c, "02", b)
                    mainActivity!!.updateItemStatus(c, status)
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
                    MainActivity.progress.visibility = View.GONE
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
    }

    fun deleteFocused() {
        if (polc?.hasFocus()!!) {
            polc?.setText("")
        }
    }
}