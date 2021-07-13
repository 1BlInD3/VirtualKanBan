package com.fusetech.virtualkanban.Activities

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.res.Resources
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.fusetech.virtualkanban.DataItems.*
import com.fusetech.virtualkanban.Fragments.*
import com.fusetech.virtualkanban.R
import com.fusetech.virtualkanban.Retrofit.RetrofitFunctions
import com.fusetech.virtualkanban.Utils.SQL
import com.fusetech.virtualkanban.Utils.SaveFile
import com.fusetech.virtualkanban.Utils.XML
import com.honeywell.aidc.*
import com.honeywell.aidc.BarcodeReader.BarcodeListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import java.sql.*

class MainActivity : AppCompatActivity(),
    BarcodeListener,
    CikklekerdezesFragment.SetItemOrBinManually,
    PolcraHelyezesFragment.SendCode,
    IgenyKontenerOsszeallitasFragment.SendBinCode,
    TobbletKontenerOsszeallitasaFragment.SendBinCode2,
    IgenyKontenerLezarasFragment.IgenyKontnerLezaras,
    KiszedesreVaroIgenyFragment.SendCode6,
    IgenyKontnerKiszedesCikk.KiszedesAdatok,
    IgenyKontenerLezarasCikkLezaras.CikkCode,
    IgenyKontenerKiszedesCikkKiszedes.SendXmlData,
    SQL.SQLAlert,
    TobbletKontenerCikkekFragment.Tobblet,
    RetrofitFunctions.Trigger{
    /*
    // 1es opció pont beviszem a cikket, és megnézi hogy van e a tranzit raktárban (3as raktár)szabad(ha zárolt akkor szól, ha nincs akkor szól)
    //ha van és szabad is, nézzük meg hogy hol vannak ilyenek FIFO szerint, vagy választ a listából, vagy felvisz egy újat, lehetőség ha nem fér fel rá és
    // át kell rakni máshova
    //egyszerre csak egy ember dolgozhasson a cikk felrakásánál

    //2es opció  megnézem, hogy van e konténer a "atado" és "statusz = 0"
    // amikor megnyitom az igény konténer összeállítását, akkor [Leltar].[dbo]. kontener-be beírom a atado(1GU),statusz(0),kontener_tipus(1)
    //kontener 0000+id (összvissz 10karakterig)
    //aztán megjelenítem a "kontener"

    //A polcál csak a 01-es raktárokat fogadja el (ilyen van a polcCheck stringbe) és ha jó akkor beírja a [Leltar].[dbo]. kontener-be termeles_raktar = 01, termeles_rakhely = polc
    // jön a cikk (megnézzük h van e), beírjuk a 4dolgot mint mindig
    // mennyiség elfogadása enterrel, kéri a következő cikket ÉS beleír a [Leltar].[dbo].kontener_tetel-be (fénykép)
    // a [Leltar].[dbo]. kontener beíródik a statusz = 1, igenyelve = datetime
    * 3as opció
    * csak azok a konténerek legyenek megjelenítve, amelyek KanBan státusza 1 ÉS A státusza 0 (írja ki amit ki kell írni) illetve a tételek státusza is 0
    * a lezárás a másik fülön átírja a tételeknél a státuszt 1-re, a konténereknél a státusz is 1 lesz és beírja az igénylés dátumát
    *
    * 4es opció
    * bonyolult.. tudja amit a 6-os opció. ha rámegyek egy tételre átírja a státuszát 2-re. be kell iktatni egy szállítójármű bekérést, onnantól jöhetnek a tételek
    * kiszedés: ha nulla a mennyiség ( v dedikált gomb )akkor cikk lezárás és 3as státusz, ha felvette a megfelelő mennyiséget akkor is 3as státusz, ha nem vesz fel annyit akkor beírja a raktar
    * tetel adatbazisba és ha kilepek lezaras nelkul akkor visszairja 1-re. ha aztán megint megnyitom akkor abból kiolvassa az értéket és azt kivonja az igényelt mennyiségből
    * és a polcot amit be kell frissíteni. esetleg másik színezés???
    * amikor kiválasztom a konténert, akkor csak azok a cikkek jelenjenek meg amiknél az átvevő NULL vagy én nyitottam meg
    *
    * 6os opció
    * Kiírja az 1es és 2 státuszú konténereket, majd kattintással belemegy és kiírja a tételeket ami benne van, átszínezi a 2-es státuszú konténereket
    *
    * írjon bele ha belelépek ha már van szállító jármű h ki vette át
    *
    * a szállítójármű beolvasásnl olyan polc kell ami van a 21-es raktárban
    *
    * A SpringBoot fel van készítve és service-be is működik! csak át kell írni a uploadDir-t inputxml-re jar-t csinálni és feltelepíteni a service-t a 10.0.1.69-en
    * 5-ös opció.
    * Bekéri a szállító járművet és megjeleníti az összes szerelőhelyet ahol van konténer. Utána megnyitjuk szerelőhelyet vonalkóddal és betölti az elemeket
    * és pirossal kiírja aminek 0 van megadva mennyiségnek. A kihelyezés gombbal először elküldi a szervernek az XML-t majd frissíti a tétel táblában a státuszt 5re
    * Ha ezeket megcsinálta akkor a végén átírja a konténer táblát 5re és beírja a kihelyezés dátumát, illetve frissíti az "atvevo"-t arra aki kihelyezi
    *
    * 7-es opció
    * nyit egy konténert beírja az id-t, atado, kontener, statusz= 6, kontener_tipus = 2
    * a 2-es opció layoutját használhatom plusz xml küldésnél fordítva történik. 01 ből 21 be, a mozgatott mennyiségbe kell beírni a mennyiséget
    *
    */
    private var manager: AidcManager? = null
    private var barcodeReader: BarcodeReader? = null
    private lateinit var barcodeData: String
    var loginFragment = LoginFragment()
    var dolgKod: String = ""// vissza ide
    private lateinit var connection: Connection
    var cikkItems: ArrayList<CikkItems> = ArrayList()
    var polcItems: ArrayList<PolcItems> = ArrayList()
    var polcHelyezesFragment = PolcraHelyezesFragment()
    var igenyFragment = IgenyKontenerOsszeallitasFragment()
    var igenyLezarasFragment = IgenyKontenerLezarasFragment()
    var igenyKiszedesFragment = IgenyKontenerKiszedesFragment()
    private lateinit var igenyKiszedesCikk: IgenyKontnerKiszedesCikk
    var igenyKiszedesCikkLezaras = IgenyKontenerLezarasCikkLezaras()
    var kiszedesreVaroIgenyFragment = KiszedesreVaroIgenyFragment()
    private lateinit var szallitoJarmuFragment: SzallitoJartmuFragment
    var igenyKontenerKiszedesCikkKiszedes = IgenyKontenerKiszedesCikkKiszedes()
    var ellenorzoKodFragment = EllenorzoKodFragment()
    private val TAG = "MainActivity"
    private val cikklekerdezesFragment = CikklekerdezesFragment()
    private var polcLocation: ArrayList<PolcLocation>? = ArrayList()
    var kontener = ""
    var menuFragment = MenuFragment()
    var lezarandoKontener = ""
    var igenyLezarCikkVisible: Boolean = false
    var selectedContainer = ""
    val kontener1List: ArrayList<KontenerItem> = ArrayList()
    val myList: ArrayList<KontenerItem> = ArrayList()
    val kontenerList: ArrayList<KontenerItem> = ArrayList()
    val listIgenyItems: ArrayList<IgenyItem> = ArrayList()
    val xml = XML()
    val save = SaveFile()
    val retro = RetrofitFunctions(this)
    val sql = SQL(this)
    val kihelyezes = IgenyKontenerKiszedese()
    val kihelyezesFragmentLista = KihelyezesListaFragment()
    val tobbletOsszeallitasFragment = TobbletKontenerOsszeallitasaFragment()
    val tobbletKontenerKihelyzeseFragment = TobbletKontenerKihelyzeseFragment()
    val tobbletCikkek = TobbletKontenerCikkekFragment()
    val tobbletCikkekPolcra = TobbletCikkekPolcraFragment()
    val koztesFragment = KoztesFragment()
    private lateinit var myTimer: CountDownTimer
    val hatosFragment = HatosCikkekFragment()
    var a = 0

    companion object {
        const val url =
            "jdbc:jtds:sqlserver://10.0.0.11;databaseName=Fusetech;user=scala_read;password=scala_read;loginTimeout=10"
        const val connectionString =
            "jdbc:jtds:sqlserver://10.0.0.11;databaseName=leltar;user=Raktarrendszer;password=PaNNoN0132;loginTimeout=10"
        lateinit var res: Resources
        @SuppressLint("StaticFieldLeak")
        lateinit var progress: ProgressBar
        val kihelyezesItems: ArrayList<SzerelohelyItem> = ArrayList()
        val cikkItem4: ArrayList<KontenerbenLezarasItem> = ArrayList()
        val kontItem: ArrayList<KontenerbenLezarasItem> = ArrayList()
        val tobbletItem: ArrayList<KontenerbenLezarasItem> = ArrayList()
        val tempLocations: ArrayList<PolcLocation> = ArrayList()
        val tobbletKontener: ArrayList<KontenerItem> = ArrayList()
        var mainUrl = "http://10.0.2.149:8030/"
        var backupURL = "http://10.0.1.199:8030/"
        var endPoint = """"""
        var logPath = ""
        var timeOut = 0L
        var szallitoJarmu: ArrayList<String> = ArrayList()
        var ellenorzoKod: ArrayList<String> = ArrayList()
        var kivalasztottSzallitoJarmu = ""
        var kivalasztottSzallitoJarmuEllenorzo = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val bundle: Bundle = intent.extras!!
        mainUrl = bundle.getString("main")!!
        Log.d("MYBUNDLE", "onCreate: $mainUrl")
        backupURL = bundle.getString("backup")!!
        Log.d("MYBUNDLE", "onCreate: $backupURL")
        endPoint = bundle.getString("endpoint")!!
        Log.d("MYBUNDLE", "onCreate: $endPoint")
        logPath = bundle.getString("logPath")!!
        Log.d("MYBUNDLE", "onCreate: $logPath")
        timeOut = bundle.getLong("timeOut")
        Log.d("MYBUNDLE", "onCreate: $timeOut")
        szallitoJarmu = bundle.getStringArrayList("szallitoJarmu")!!
        Log.d("MYBUNDLE", "onCreate: $szallitoJarmu")
        ellenorzoKod = bundle.getStringArrayList("ellenorzokod")!!
        Log.d("MYBUNDLE", "onCreate: $ellenorzoKod")
        res = resources
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        supportActionBar?.hide()
        igenyFragment = IgenyKontenerOsszeallitasFragment.newInstance("", "")
        polcHelyezesFragment = PolcraHelyezesFragment()
        szallitoJarmuFragment = SzallitoJartmuFragment()
        ellenorzoKodFragment = EllenorzoKodFragment()
        igenyKiszedesCikk = IgenyKontnerKiszedesCikk()
        loginFragment = LoginFragment()
        progress = progressBar2
        progress.visibility = View.GONE
        AidcManager.create(this) { aidcManager ->
            manager = aidcManager
            try {
                barcodeReader = manager?.createBarcodeReader()
                barcodeReader?.claim()
            } catch (e: ScannerUnavailableException) {
                e.printStackTrace()
            } catch (e: InvalidScannerNameException) {
                e.printStackTrace()
            }
            try {
                barcodeReader?.setProperty(BarcodeReader.PROPERTY_CODE_39_ENABLED, true)
                barcodeReader?.setProperty(BarcodeReader.PROPERTY_DATAMATRIX_ENABLED, true)
                barcodeReader?.setProperty(
                    BarcodeReader.PROPERTY_TRIGGER_CONTROL_MODE,
                    BarcodeReader.TRIGGER_CONTROL_MODE_AUTO_CONTROL
                )
            } catch (e: UnsupportedPropertyException) {
                e.printStackTrace()
                Toast.makeText(
                    applicationContext,
                    "Failed to apply properties",
                    Toast.LENGTH_SHORT
                ).show()
            }
            barcodeReader?.addBarcodeListener(this@MainActivity)
        }

        loadLoginFragment()

        myTimer = object : CountDownTimer(timeOut * 60 * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                //Some code
                a++
                //Toast.makeText(this@MainActivity, "$a", Toast.LENGTH_SHORT).show()
            }

            override fun onFinish() {
                when {
                    getFragment("MENU") -> {
                        loadLoginFragment()
                    }
                    getFragment("LOGIN") -> {
                        Log.d(TAG, "onFinish: Loginhoz vissza")
                    }
                    getFragment("POLC") -> { //1
                        polcHelyezesFragment.onTimeout()
                    }
                    getFragment("IGENY") -> { //2
                        igenyFragment.clearAll()
                        loadLoginFragment()
                    }
                    getFragment("CIKKLEZARASFRAGMENT") -> { //3-2
                        igenyKiszedesCikkLezaras.onTimeout()
                        removeFragment("CIKKLEZARASFRAGMENT")
                        loadLoginFragment()
                    }
                    getFragment("IGENYLEZARAS") -> { //3-1
                        loadLoginFragment()
                    }
                    getFragment("KISZEDESCIKK") -> { //4-3
                        igenyKontenerKiszedesCikkKiszedes.onTimeout()
                    }
                    getFragment("NEGYESCIKKEK") -> { //4-2
                        removeFragment("NEGYESCIKKEK")
                        loadLoginFragment()
                    }
                    getFragment("KISZEDES") -> { //4-1
                        loadLoginFragment()
                    }
                    getFragment("KIHELYEZES") -> { //5-1
                        kihelyezes.exit()
                        loadLoginFragment()
                    }
                    getFragment("KIHELYEZESLISTA") -> { //5-2
                        kihelyezes.exit()
                        loadLoginFragment()
                    }
                    getFragment("KIHELYEZESITEMS") -> { //5-3
                        kihelyezes.exit()
                        loadLoginFragment()
                    }
                    getFragment("CIKKLEZARASFRAGMENTHATOS") -> { //6-2
                        hatosFragment.onTimeout()
                        removeFragment("CIKKLEZARASFRAGMENTHATOS")
                        loadLoginFragment()
                    }
                    getFragment("VARAS") -> { //6-1
                        loadLoginFragment()
                    }
                    getFragment("TOBBLET") -> { //7
                        tobbletOsszeallitasFragment.onKilepPressed()
                        loadLoginFragment()
                    }
                    getFragment("SZALLITO") -> {
                        loadLoginFragment()
                    }
                    getFragment("ELLENOR") -> {
                        loadLoginFragment()
                    }
                    getFragment("TKK") -> {  //////////////////////////////
                        loadLoginFragment()
                    }
                    getFragment("TOBBLETKIHELYEZESCIKKEK") -> {
                        //setContainerBackToOpen(tobbletCikkek.kontenerID!!)// lehet hogy ez nem is fog kelleni?!
                        loadLoginFragment()
                        //loadTobbletKontenerKihelyezes()
                    }
                    getFragment("CIKKEKPOLCRA") -> {////////////////////////
                        tobbletCikkekPolcra.onTimeout()
                        loadLoginFragment()
                    }
                    else -> {
                        //loadLoginFragment()
                        Log.d(TAG, "onFinish: ELSE")
                    }
                }
            }
        }
        myTimer.start()
    }

    private fun cancelTimer() {
        a = 0
        myTimer.cancel()
    }

    fun loadLoginFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_container, loginFragment, "LOGIN").commit()
    }
    fun loadKoztes(){
        supportFragmentManager.beginTransaction().replace(R.id.frame_container,koztesFragment,"KOZTES").commit()
    }

    private fun getMenuFragment(): Boolean {
        val fragmentManager = supportFragmentManager
        val menuFragment = fragmentManager.findFragmentByTag("MENU")
        if (menuFragment != null && menuFragment.isVisible) {
            return true
        }
        return false
    }

    fun getFragment(fragmentName: String): Boolean {
        val myFrag = supportFragmentManager.findFragmentByTag(fragmentName)
        if (myFrag != null && myFrag.isVisible) {
            return true
        }
        return false
    }

    fun loadMenuFragment(hasRight: Boolean?) {
        menuFragment = MenuFragment.newInstance(hasRight)
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_container, menuFragment, "MENU").commit()
    }

    fun loadCikklekerdezesFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_container, cikklekerdezesFragment, "CIKK").addToBackStack(null)
            .commit()
    }

    private fun loadLoadFragment(value: String) {
        val loadFragment = LoadFragment.newInstance(value)
        supportFragmentManager.beginTransaction().replace(R.id.cikk_container, loadFragment)
            .commit()
    }

    fun loadPolcHelyezesFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_container, polcHelyezesFragment, "POLC").addToBackStack(null)
            .commit()
    }

    fun loadSzallitoJarmu(kontener_id: String) {
        kontener = kontener_id
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_container, szallitoJarmuFragment, "SZALLITO").addToBackStack(null)
            .commit()
    }

    fun loadKiszedesFragment() {
        val kiszedes = IgenyKontenerKiszedesFragment()
        supportFragmentManager.beginTransaction().replace(R.id.frame_container, kiszedes)
            .addToBackStack(null).commit()
    }

    fun loadKihelyezesFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_container, kihelyezes, "KIHELYEZES").addToBackStack(null).commit()
    }

    override fun onBarcodeEvent(p0: BarcodeReadEvent?) {
        runOnUiThread {
            cancelTimer()
            barcodeData = p0?.barcodeData!!
            when {
                loginFragment.isVisible -> {
                    loginFragment.setId(barcodeData)
                    dolgKod = barcodeData
                    loginFragment.startSpinning()
                    CoroutineScope(IO).launch {
                        sql.checkRightSql(dolgKod, this@MainActivity)
                    }
                }
                cikklekerdezesFragment.isVisible -> {
                    loadLoadFragment("Várom az eredményt")
                    cikkItems.clear()
                    polcItems.clear()
                    cikklekerdezesFragment.setBinOrItem(barcodeData)
                    CoroutineScope(IO).launch {
                        sql.cikkPolcQuery(barcodeData, this@MainActivity)
                    }
                }
                getFragment("SZALLITO") -> {
                    szallitoJarmuFragment.setJarmu(barcodeData)
                    //JSON ból megnézni, hogy van e ilyen szállító és itt átadni az értékét
                    CoroutineScope(IO).launch {
                        updateKontenerKiszedesre(kontener)
                    }
                }
                getFragment("KISZEDESCIKK") -> {
                    CoroutineScope(IO).launch {
                        chechPolcAndSetBin(barcodeData)
                    }
                    //igenyKontenerKiszedesCikkKiszedes.setBin(barcodeData)
                }
                getFragment("ELLENOR") -> {
                    ellenorzoKodFragment.setCode(barcodeData)
                    checkEllenorzoKod(barcodeData)
                }
                getFragment("POLC") -> {
                    polcHelyezesFragment.setCode(barcodeData)
                }
                getFragment("IGENY") -> {
                    igenyFragment.setCode(barcodeData)
                }
                getFragment("KIHELYEZES") -> {
                    kihelyezes.setCode(barcodeData)
                }
                getFragment("TOBBLET") -> {
                    tobbletOsszeallitasFragment.setCode(barcodeData)
                }
                getFragment("CIKKEKPOLCRA") -> {
                    tobbletCikkekPolcra.setCode(barcodeData)
                }
            }
            myTimer.start()
        }
    }

    override fun onFailureEvent(p0: BarcodeFailureEvent?) {
        runOnUiThread {
            Toast.makeText(this@MainActivity, "Nem sikerült leolvasni", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        cancelTimer()
        if (getMenuFragment()) {
            when (keyCode) {
                7 -> finishAndRemoveTask() //0
                8 -> loadPolcHelyezesFragment() //1
                9 -> containerCheck(dolgKod)  //2
                10 -> igenyKontenerCheck()  //3
                11 -> igenyKontenerKiszedes()  //4
                12 -> loadKihelyezesFragment()  //5
                13 -> kiszedesreVaro()  //6
                14 -> containerCheck7(dolgKod)  //7
                15 -> loadTobbletKontenerKihelyezes()  //8
                16 -> loadCikklekerdezesFragment()  //91
            }
        }
        myTimer.start()
        return super.onKeyUp(keyCode, event)
    }

    override fun onResume() {
        super.onResume()
        if (barcodeReader != null) {
            try {
                barcodeReader?.claim()
            } catch (e: ScannerUnavailableException) {
                e.printStackTrace()
                Toast.makeText(this, "Scanner unavailable", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        /*myList.clear()
        kontener1List.clear()
        kontenerList.clear()
        listIgenyItems.clear()
        polcLocation?.clear()*/
        if (barcodeReader != null) {
            barcodeReader?.release()
        }
        Log.d(TAG, "onPause: ")
    }

    override fun onDestroy() {
        super.onDestroy()
        /*polcItems.clear()
        cikkItems.clear()*/
        if (barcodeReader != null) {
            barcodeReader?.removeBarcodeListener(this)
            barcodeReader?.close()
        }
    }

    private fun chechPolcAndSetBin(code: String) {
        sql.chekcPolcAndSetBinSql(code, this@MainActivity)
    }

    private fun chechIfPolcHasChanged(kontener: String): Boolean {
        Class.forName("net.sourceforge.jtds.jdbc.Driver")
        try {
            connection = DriverManager.getConnection(url)
            val statement =
                connection.prepareStatement(resources.getString(R.string.kontenerEllenorzes))
            statement.setString(1, kontener)
            statement.setInt(2, 0)
            val resultSet = statement.executeQuery()
            return resultSet.next()
        } catch (e: Exception) {
            return false
        }
    }

    private fun updateKontenerKiszedesre(kontener: String) {
        Class.forName("net.sourceforge.jtds.jdbc.Driver")
        try {
            connection = DriverManager.getConnection(connectionString)
            val statement2 = connection.prepareStatement(resources.getString(R.string.isPolc21))
            statement2.setString(1, "21")
            statement2.setString(2, barcodeData)
            val resultSet = statement2.executeQuery()
            if (!resultSet.next()) {
                CoroutineScope(Main).launch {
                    setAlert("Nincs a tranzitraktárban!")
                }
            } else {
                kivalasztottSzallitoJarmu = barcodeData
                for(i in 0 until szallitoJarmu.size){
                    if(kivalasztottSzallitoJarmu == szallitoJarmu[i]){
                        kivalasztottSzallitoJarmuEllenorzo = ellenorzoKod[i]
                    }
                }
                val statement =
                    connection.prepareStatement(resources.getString(R.string.updateContainerStatus))
                statement.setInt(1, 2)
                statement.setString(2, kivalasztottSzallitoJarmu)//JSON ból a szállítójármű
                statement.setString(3, dolgKod)//ide kell a bejelentkezős kód
                statement.setString(4, kontener)
                statement.executeUpdate()
                Log.d(TAG, "updateKontenerKiszedesre: Sikeres adatfrissítés!!!")
                /*CoroutineScope(Main).launch {
                    setAlert("Siker!")
                }*/
                //itt kéne beolvasni a 4es opciót
                loadKiszedesFragment()
                sql.checkIfContainerIsOpen(kontener, this@MainActivity)
            }
        } catch (e: Exception) {
            CoroutineScope(Main).launch {
                setAlert("Probléma a feltöltésben!\n $e")
            }
        }
    }

    private fun updateKontener(kontener_id: String) {
        sql.updateKontenerSql(kontener_id, this@MainActivity)
    }

    private fun updateCikk(kontener_id: String) {
        sql.updateCikkSql(kontener_id, this@MainActivity)
    }

    fun setAlert(text: String) {
        val builder = AlertDialog.Builder(this@MainActivity)
        builder.setTitle("Figyelem")
            .setMessage(text)
        builder.create()
        builder.show()
    }

    override fun setValue(value: String) {
        if (value.isNotEmpty()) {
            loadLoadFragment("Várom az eredményt")
            cikkItems.clear()
            polcItems.clear()
            cikklekerdezesFragment.setBinOrItem(value)
            CoroutineScope(IO).launch {
                sql.cikkPolcQuery(value, this@MainActivity)
            }
        } else {
            loadLoadFragment("")
        }
    }

    override fun sendCode(code: String) {
        sql.checkTrannzit(code, this@MainActivity, polcLocation)
    }

    override fun sendTranzitData(
        cikk: String,
        polc: String?,
        mennyiseg: Double?,
        raktarbol: String,
        raktarba: String,
        polcra: String
    ) {
        sql.scalaSend(cikk, polc, mennyiseg, raktarbol, raktarba, polcra, this@MainActivity)
    }

    fun removeLocationFragment() {
        val isLocFragment = supportFragmentManager.findFragmentByTag("LOC")
        if (isLocFragment != null && isLocFragment.isVisible) {
            supportFragmentManager.beginTransaction().remove(isLocFragment).commit()
        }
    }

    fun check02Polc(bin: String): Boolean {
        Class.forName("net.sourceforge.jtds.jdbc.Driver")
        try {
            connection = DriverManager.getConnection(connectionString)
            val statement = connection.prepareStatement(resources.getString(R.string.is02Polc))
            statement.setString(1, bin)
            val resultSet = statement.executeQuery()
            return resultSet.next()
        } catch (e: Exception) {
            CoroutineScope(Main).launch {
                setAlert("$e")
            }
            return false
        }
    }

    override fun sendBinCode(code: String) {
        CoroutineScope(IO).launch {
            sql.check01(code, this@MainActivity)
        }
    }

    override fun sendDetails(
        cikkszam: String,
        mennyiseg: Double,
        term_rakhely: String,
        unit: String,
        kontener: String
    ) {
        CoroutineScope(IO).launch {
            sql.uploadItem(cikkszam, mennyiseg, term_rakhely, unit, this@MainActivity, kontener)
        }
    }

    override fun closeContainer(statusz: Int, datum: String) {
        CoroutineScope(IO).launch {
            sql.closeContainerSql(statusz, datum, this@MainActivity)
        }
    }

    override fun sendBinCode2(code: String) {
        CoroutineScope(IO).launch {
            sql.checkCode02(code, this@MainActivity)
        }
    }

    override fun sendDetails2(
        cikkszam: String,
        mennyiseg: Double,
        term_rakhely: String,
        unit: String,
        kontener: String
    ) {
        CoroutineScope(IO).launch {
            sql.uploadItem7(cikkszam, mennyiseg, term_rakhely, unit, this@MainActivity, kontener)
        }
    }

    override fun closeContainer2(statusz: Int, datum: String) {
        sql.closeContainerSql7(statusz, datum, this@MainActivity)
    }

    fun isItem(code: String) {
        CoroutineScope(IO).launch {
            sql.checkItem(code, this@MainActivity)
        }
    }

    fun isItem2(code: String, bin: String) {
        CoroutineScope(IO).launch {
            sql.checkItem2(code, bin, this@MainActivity)
        }
    }

    fun containerCheck(id: String) {
        CoroutineScope(IO).launch {
            sql.containerManagement(id, this@MainActivity)
        }
    }

    fun containerCheck7(id: String) {
        CoroutineScope(IO).launch {
            sql.containerManagement7(id, this@MainActivity)
        }
    }

    fun igenyKontenerCheck() {
        CoroutineScope(IO).launch {
            sql.loadIgenyLezaras(this@MainActivity)
            Log.d(TAG, "igenyKontenerCheck: Lefutott")
        }
    }

    override fun sendContainer(container: String) {
        lezarandoKontener = container
        CoroutineScope(IO).launch {
            sql.loadKontenerCikkek(container, this@MainActivity)
        }
    }

    fun closeContainerAndItem() {
        CoroutineScope(IO).launch {
            if (chechIfPolcHasChanged(lezarandoKontener)) {
                updateCikk(lezarandoKontener)
                updateKontener(lezarandoKontener)
            } else {
                igenyKontenerCheck()
                CoroutineScope(Main).launch {
                    setAlert("A konténer státusza már megváltozott")
                }
            }
        }
    }

    fun loadKihelyezesItems(code: String) {
        CoroutineScope(IO).launch {
            sql.loadKihelyezesItemsSql(code, this@MainActivity)
        }
    }

    fun igenyKontenerKiszedes() {
        CoroutineScope(IO).launch {
            sql.loadIgenyKiszedes(this@MainActivity)
        }
    }

    fun kiszedesreVaro() {
        CoroutineScope(IO).launch {
            sql.loadKiszedesreVaro(this@MainActivity)
        }
    }

    fun checkIfContainerStatus(kontener: String) {
        selectedContainer = kontener
        CoroutineScope(IO).launch {
            sql.checkIfContainerIsOpen(kontener, this@MainActivity)
        }
    }

    override fun containerCode(kontener: String) {
        CoroutineScope(IO).launch {
            sql.loadKontenerCikkekHatos(kontener, this@MainActivity)
        }
    }

    fun cikkUpdate(cikk: Int) {
        CoroutineScope(IO).launch {
            sql.cikkUpdateSql(cikk, this@MainActivity)
        }
    }

    fun loadTobbletKontenerKihelyezes() {
        CoroutineScope(IO).launch {
            sql.tobbletKontenerElemek(this@MainActivity)
        }
    }

    override fun cikkAdatok(
        cikk: String?,
        megj1: String?,
        megj2: String?,
        intrem: String?,
        igeny: Double,
        unit: String?,
        id: Int,
        kontnerNumber: Int
    ) {
        CoroutineScope(IO).launch {
            sql.cikkAdataokSql(
                cikk,
                megj1,
                megj2,
                intrem,
                igeny,
                unit,
                id,
                kontnerNumber,
                this@MainActivity
            )
        }
    }

    override fun cikkCode(code: Int) {
        CoroutineScope(IO).launch {
            sql.cikkCodeSql(code, this@MainActivity)
        }
    }

    fun insertDataToRaktarTetel(cikk: String, mennyiseg: Double, raktarKod: String, polc: String) {
        sql.insertDataToRaktarTetelSql(cikk, mennyiseg, raktarKod, polc, this@MainActivity)
    }

    fun updateItemStatus(itemId: String) {
        sql.updtaeItemStatusSql(itemId, this@MainActivity)
    }

    fun updateItemAtvevo(itemId: String) {
        sql.updateItemAtvevoSql(itemId, this@MainActivity)
    }

    fun checkIfContainerIsDone(container: String, itemId: String, raktar: String, polc: String) {
        sql.checkIfContainerIsDoneSql(container, itemId, raktar, polc, this@MainActivity)
    }

    fun checkEllenorzoKod(code: String) {
        CoroutineScope(IO).launch {
            sql.checkEllenorzoKodSql(code, this@MainActivity)
        }
    }

    override fun sendXmlData(
        cikk: String,
        polc: String?,
        mennyiseg: Double?,
        raktarbol: String,
        raktarba: String,
        polcra: String
    ) {
        sql.scalaSend(cikk, polc, mennyiseg, raktarbol, raktarba, polcra, this@MainActivity)
    }

    fun sendKihelyezesXmlData(
        cikk: String,
        polc: String?,
        mennyiseg: Double?,
        raktarbol: String,
        raktarba: String,
        polcra: String
    ) {
        sql.scalaSend(cikk, polc, mennyiseg, raktarbol, raktarba, polcra, this@MainActivity)
    }

    override fun sendMessage(message: String) {
        CoroutineScope(Main).launch {
            setAlert(message)
        }
    }

    fun getContainerList(code: String) {
        CoroutineScope(IO).launch {
            sql.getContainersFromVehicle(code, this@MainActivity)
        }
    }

    fun updateCikkAfterSend(code: Int) {
        sql.closeCikkek(code, this@MainActivity)
    }

    fun closeItem(code: Int) {
        sql.closeContainer(code, this@MainActivity)
    }

    fun setContainerStatusAndGetItems(kontener_id: String?) {
        //selectedContainer = kontener_id!!
        CoroutineScope(IO).launch {
            sql.updateContainerAndOpenItems(kontener_id, this@MainActivity)
        }
    }

    fun setContainerBackToOpen(kontener: String) {
        CoroutineScope(IO).launch {
            sql.statuszVisszairas(kontener, this@MainActivity)
            //loadTobbletKontenerKihelyezes()
        }
    }
    override fun onBackPressed() {
        try {
            when {
                getFragment("MENU") -> {
                    loadLoginFragment()
                }
                getFragment("CIKKLEZARASFRAGMENT") -> {
                    igenyKiszedesCikkLezaras.buttonPerform()
                }
                getFragment("IGENY") -> { //2
                    igenyFragment.clearAll()
                    loadMenuFragment(true)
                }
                getFragment("CIKKLEZARASFRAGMENTHATOS") -> {
                    hatosFragment.buttonPerform()
                    removeFragment("CIKKLEZARASFRAGMENTHATOS")
                }
                getFragment("SZALLITO") -> {
                    loadMenuFragment(true)
                    igenyKontenerKiszedes()
                }
                getFragment("KISZEDESCIKK") -> {
                    /* loadMenuFragment(true)
                     igenyKontenerKiszedes()*/
                    igenyKontenerKiszedesCikkKiszedes.performButton()
                }
                getFragment("NEGYESCIKKEK") -> {
                    loadMenuFragment(true)
                    //loadKiszedesFragment()
                    igenyKontenerKiszedes()
                }
                getFragment("ELLENOR") -> {
                    loadMenuFragment(true)
                    igenyKontenerKiszedes()
                }
                getFragment("DUMMY") -> {
                    loadMenuFragment(true)
                }
                getFragment("POLC") -> {
                    polcHelyezesFragment.onKilepPressed()
                }
                getFragment("KISZEDES") -> {
                    loadMenuFragment(true)
                }
                getFragment("KIHELYEZESLISTA") -> {
                    kihelyezes.exit()
                    loadMenuFragment(true)
                }
                getFragment("KIHELYEZESITEMS") -> {
                    kihelyezes.onBack()
                    getContainerList("SZ01")
                }
                getFragment("KIHELYEZES") -> {
                    kihelyezes.exit()
                    loadMenuFragment(true)
                }
                getFragment("TOBBLETOSSZE") -> {
                    loadMenuFragment(true)
                }
                getFragment("TOBBLET") -> {
                    tobbletOsszeallitasFragment.onKilepPressed()
                }
                getFragment("VARAS") -> {
                    loadMenuFragment(true)
                }
                getFragment("TKK") -> {
                    loadMenuFragment(true)
                }
                getFragment("TOBBLETKIHELYEZESCIKKEK") -> {
                    setContainerBackToOpen(tobbletCikkek.kontenerID!!)// lehet hogy ez nem is fog kelleni?!
                    //loadTobbletKontenerKihelyezes()
                }
                getFragment("CIKKEKPOLCRA") -> {
                    tobbletCikkekPolcra.onButtonPressed()
                }
                getFragment("LOGIN") -> {
                    Log.d(TAG, "onBackPressed: LOGIN")
                }
                //getFragment()
                else -> {
                    super.onBackPressed()
                }
            }
        } catch (e: Exception) {

        }
    }

    override fun sendTobblet(
        id: Int,
        kontenerID: Int,
        megjegyzes: String,
        megjegyzes2: String,
        intrem: String,
        unit: String,
        mennyiseg: Double,
        cikkszam: String
    ) {
        CoroutineScope(IO).launch {
           sql.openNyolcHarmas(id,kontenerID,megjegyzes,megjegyzes2,intrem,unit,mennyiseg,cikkszam,this@MainActivity)
        }
    }
    fun raktarcheck(code: String){
        CoroutineScope(IO).launch {
            sql.checkBinIn02(code,this@MainActivity)
        }
    }
    fun updateCikkandContainer(cikk: Int, kontener: Int){
        sql.closeItemAndCheckContainer(cikk,kontener,this@MainActivity)
    }

    override fun triggerError() {
        TODO("Not yet implemented")
    }
    fun removeFragment(fragment1: String){
        val fragment = supportFragmentManager.findFragmentByTag(fragment1)
        if (fragment != null) supportFragmentManager.beginTransaction().remove(fragment)
            .commit()
    }
}