package com.fusetech.virtualkanban.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.*
import android.content.pm.PackageManager
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
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.fusetech.virtualkanban.dataItems.*
import com.fusetech.virtualkanban.fragments.*
import com.fusetech.virtualkanban.R
import com.fusetech.virtualkanban.retrofit.RetrofitFunctions
import com.fusetech.virtualkanban.utils.SQL
import com.fusetech.virtualkanban.utils.SaveFile
import com.fusetech.virtualkanban.utils.XML
import com.honeywell.aidc.*
import com.honeywell.aidc.BarcodeReader.BarcodeListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import java.lang.StringBuilder
import java.net.NetworkInterface
import java.sql.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import android.content.DialogInterface
import android.net.wifi.WifiManager
import java.io.File
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.random.Random

private const val TAG = "MainActivity"

@AndroidEntryPoint
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
    HatosCikkekFragment.Hatos,
    RaktarkoziMozgasFragment.LoadResult,
    MozgasResult.FileSave {
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
    * 8-as opció
    * a 4-es opcióhoz hasonló, csak a 02-es raktár polcaira helyezi vissza a cikkeket


      Új opció

        a kiindulási polcot leolvassuk. Felugrik egy ablak, és rákérdez, hogy az egész polcot szeretnéd-e mozgatni. Ha igen akkor csak a cél polcot kéri, különben a cikkeket kell kiválasztani egyesévekl
     */

    /*
        HA többször akarunk kivenni, akkor kérdezze meg hogy üres-e a polc vagy nem,
        csak így kell kivenni. HA üres akkor küldjön emailt, a polccal levett mennyiséggel és a polc mennyiséggel



        AZ 5ÖS KIHELYEZÉS MÉG MINDIG NEM JÓL MŰKÖDIK, HA TÖBB KONTÉNER VAN UGYAN AZON A NÉVEN



        Többlet dedikált gomb: Polchely kell legyen benne és nulla legyen a polcchleyen lévő mennyiség. VAn többlet m az x polcon? megadás után email. x polcon cikkszámoadatok, ennyivel több volt Mennyi többletmennyiség maradt a polcon?.
     */
    val EXTERNAL_STORAGE = 101
    private var manager: AidcManager? = null
    private var barcodeReader: BarcodeReader? = null
    private var barcodeData: String = ""
    var loginFragment: LoginFragment? = null
    private lateinit var connection: Connection
    var cikkItems: ArrayList<CikkItems> = ArrayList()
    var polcItems: ArrayList<PolcItems> = ArrayList()
    var polcHelyezesFragment = PolcraHelyezesFragment()
    var igenyFragment = IgenyKontenerOsszeallitasFragment()
    var igenyLezarasFragment: IgenyKontenerLezarasFragment? = null
    var igenyKiszedesFragment: IgenyKontenerKiszedesFragment? = null
    var polcResultFragment: PolcResultFragment? = null
    var cikkResultFragment: CikkResultFragment? = null
    private lateinit var igenyKiszedesCikk: IgenyKontnerKiszedesCikk
    var igenyKiszedesCikkLezaras: IgenyKontenerLezarasCikkLezaras? = null
    var kiszedesreVaroIgenyFragment: KiszedesreVaroIgenyFragment? = null
    var szallitoJarmuFragment: SzallitoJartmuFragment? = null
    var igenyKontenerKiszedesCikkKiszedes: IgenyKontenerKiszedesCikkKiszedes? = null
    var ellenorzoKodFragment: EllenorzoKodFragment? = null
    var cikklekerdezesFragment: CikklekerdezesFragment? = null
    private var polcLocation: ArrayList<PolcLocation>? = ArrayList()
    var kontener = ""
    var menuFragment: MenuFragment? = null
    var lezarandoKontener = ""
    var igenyLezarCikkVisible: Boolean = false
    var selectedContainer = ""
    val kontener1List: ArrayList<KontenerItem> = ArrayList()
    val myList: ArrayList<KontenerItem> = ArrayList()
    val kontenerList: ArrayList<KontenerItem> = ArrayList()
    val listIgenyItems: ArrayList<IgenyItem> = ArrayList()
    val xml = XML()
    val save = SaveFile()
    val retro = RetrofitFunctions()
    private val sql = SQL(this)
    var kihelyezes: IgenyKontenerKiszedese? = null
    var kihelyezesFragmentLista: KihelyezesListaFragment? = null
    var tobbletOsszeallitasFragment = TobbletKontenerOsszeallitasaFragment()
    var tobbletKontenerKihelyzeseFragment: TobbletKontenerKihelyzeseFragment? = null
    var tobbletCikkek: TobbletKontenerCikkekFragment? = null
    var tobbletCikkekPolcra: TobbletCikkekPolcraFragment? = null
    var koztesFragment: KoztesFragment? = null
    private lateinit var myTimer: CountDownTimer
    private lateinit var exitTimer: CountDownTimer
    var hatosFragment: HatosCikkekFragment? = null
    private var raktarkoziFragment: RaktarkoziMozgasFragment? = null
    private lateinit var logoutWhenCharging: BroadcastReceiver
    var loadFragment: LoadFragment? = null
    var a = 0
    var wifi: Boolean? = false
    lateinit var connManager: ConnectivityManager

    companion object {
        const val url =
            "jdbc:jtds:sqlserver://10.0.0.11;databaseName=Fusetech;user=scala_read;password=scala_read;loginTimeout=10"
        const val connectionString =
            "jdbc:jtds:sqlserver://10.0.0.11;databaseName=leltar;user=Raktarrendszer;password=PaNNoN0132;loginTimeout=10"
        lateinit var res: Resources
        var zarolt = false

        @SuppressLint("StaticFieldLeak")
        lateinit var progress: ProgressBar
        val kihelyezesItems: ArrayList<SzerelohelyItem> = ArrayList()
        val cikkItem4: ArrayList<KontenerbenLezarasItem> = ArrayList()
        val kontItem: ArrayList<KontenerbenLezarasItem> = ArrayList()
        val tobbletItem: ArrayList<KontenerbenLezarasItem> = ArrayList()
        val tempLocations: ArrayList<PolcLocation> = ArrayList()
        val tobbletKontener: ArrayList<KontenerItem> = ArrayList()
        var mainUrl = "http://10.0.1.69:8030/"
        val tobbletCikkekArray: ArrayList<tobblet> = ArrayList()
        //var mainUrl = "http://10.0.2.149:8030/"
        var backupURL = "http://10.0.1.199:8030/"
        var endPoint = """"""
        var logPath = ""
        var timeOut = 0L
        var hasRight = false
        var fusetech = ""
        var itLoginCodes: ArrayList<String> = ArrayList()
        var szallitoMap: HashMap<String, String> = HashMap()
        var dolgKod: String = ""// vissza ide
        var sz0x: String = ""
        var wifiInfo: String = ""
        lateinit var path: File
        var szallito = ""
        var sz01KiszedesDate = ""
        var emailMessage: String? = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val bundle: Bundle = intent.extras!!
        connManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        Log.d(TAG, "FIFI: $wifi")
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
        szallitoMap = bundle.getSerializable("szallitoMap") as HashMap<String, String>
        Log.d("MYBUNDLE", "onCreate: $szallitoMap")
        fusetech = bundle.getString("fusetech")!!
        Toast.makeText(this, "fusetech = $fusetech", Toast.LENGTH_SHORT).show()
        itLoginCodes = (bundle.getSerializable("it") as ArrayList<String>?)!!
        Log.d(TAG, "onCreate: LOGIN CODES $itLoginCodes")
        res = resources
        path = getExternalFilesDir(null)!!
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        supportActionBar?.hide()
        igenyFragment = IgenyKontenerOsszeallitasFragment.newInstance("", "")
        polcHelyezesFragment = PolcraHelyezesFragment()
        szallitoJarmuFragment = SzallitoJartmuFragment()
        ellenorzoKodFragment = EllenorzoKodFragment()
        igenyKiszedesCikk = IgenyKontnerKiszedesCikk()
        progress = progressBar2
        wifiInfo = getMacAndSignalStrength()
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
        requestStoragePermission()
        loadLoginFragment()

        exitTimer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                Log.d(TAG, "onTick: ")
            }

            override fun onFinish() {
                Log.d(TAG, "onFinish: EXITTIMER")
                loginFragment?.clearLeak()
                finishAndRemoveTask()
            }
        }

        myTimer = object : CountDownTimer(timeOut * 60 * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                //Some code
                a++
                //Toast.makeText(this@MainActivity, "$a", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "onMainTick: $a")
            }

            override fun onFinish() {
                when {
                    getFragment("MENU") -> {
                        menuFragment = null
                        loadLoginFragment()
                        Log.d(TAG, "onFinish: MENU")
                    }
                    getFragment("LOGIN") -> {
                        Log.d(TAG, "onFinish: Loginhoz vissza")
                    }
                    getFragment("POLC") -> { //1
                        polcHelyezesFragment.onTimeout()
                    }
                    getFragment("IGENY") -> { //2
                        igenyFragment.clearAll()
                        igenyFragment.clearLeak()
                        loadLoginFragment()
                    }
                    getFragment("CIKKLEZARASFRAGMENT") -> { //3-2
                        igenyKiszedesCikkLezaras?.onTimeout()
                        removeFragment("CIKKLEZARASFRAGMENT")
                        igenyKiszedesCikkLezaras?.clearLeak()
                        loadLoginFragment()
                    }
                    getFragment("IGENYLEZARAS") -> { //3-1
                        igenyFragment.clearLeak()
                        loadLoginFragment()
                    }
                    getFragment("KISZEDESCIKK") -> { //4-3
                        igenyKontenerKiszedesCikkKiszedes?.onTimeout()
                    }
                    getFragment("NEGYESCIKKEK") -> { //4-2
                        removeFragment("NEGYESCIKKEK")
                        loadLoginFragment()
                    }
                    getFragment("KISZEDES") -> { //4-1
                        igenyKiszedesFragment?.destroy()
                        loadLoginFragment()
                    }
                    getFragment("KIHELYEZESITEMS") -> { //5-3
                        removeFragment("KIHELYEZESITEMS")
                        kihelyezes?.exit()
                        kihelyezesFragmentLista = null
                        loadLoginFragment()
                    }
                    getFragment("KIHELYEZESLISTA") -> { //5-2
                        removeFragment("KIHELYEZESLISTA")
                        kihelyezes?.exit()
                        loadLoginFragment()
                    }
                    getFragment("KIHELYEZES") -> { //5-1
                        kihelyezes?.exit()
                        loadLoginFragment()
                    }
                    getFragment("CIKKLEZARASFRAGMENTHATOS") -> { //6-2
                        hatosFragment?.onTimeout()
                        removeFragment("CIKKLEZARASFRAGMENTHATOS")
                        hatosFragment = null
                        loadLoginFragment()
                    }
                    getFragment("VARAS") -> { //6-1
                        loadLoginFragment()
                    }
                    getFragment("TOBBLET") -> { //7
                        tobbletOsszeallitasFragment.onKilepPressed()
                        menuFragment = null
                        loadLoginFragment()
                    }
                    getFragment("CIKKEKPOLCRA") -> {//////////////////////// 8-3
                        tobbletCikkekPolcra?.onTimeout()
                        loadLoginFragment()
                    }
                    getFragment("TOBBLETKIHELYEZESCIKKEK") -> { //8-2
                        //setContainerBackToOpen(tobbletCikkek.kontenerID!!)// lehet hogy ez nem is fog kelleni?!
                        loadLoginFragment()
                        //loadTobbletKontenerKihelyezes()
                    }
                    getFragment("TKK") -> {  ////////////////////////////// 8-1
                        loadLoginFragment()
                    }
                    getFragment("PRF") -> { // 9-3
                        removeFragment("PRF")
                        polcResultFragment?.clearLeak()
                        loadLoginFragment()
                    }
                    getFragment("CRF") -> { // 9-2
                        removeFragment("CRF")
                        cikkResultFragment?.clearLeak()
                        loadLoginFragment()
                    }
                    getFragment("LRF") -> { //9-4
                        loadFragment?.clearLeak()
                        loadLoginFragment()
                    }
                    getFragment("CIKK") -> { // 9-1
                        loadLoginFragment()
                    }
                    getFragment("SZALLITO") -> {
                        loadLoginFragment()
                    }
                    getFragment("ELLENOR") -> {
                        loadLoginFragment()
                    }
                    getFragment("MOZGAS") -> {
                        Log.d(TAG, "onFinish: MOZGASFRAGMENT")
                        loadLoginFragment()
                    }
                    getFragment("RAKTARKOZI") -> {
                        Log.d(TAG, "onFinish: RAKTARKOZIFRAGMENT")
                        loadLoginFragment()
                    }
                    else -> {
                        loadLoginFragment()
                        Log.d(TAG, "onFinish: ELSE")
                    }
                }
            }
        }
        //myTimer.start()
        val mIntent = IntentFilter()
        mIntent.addAction(Intent.ACTION_POWER_CONNECTED)
        //mIntent.addAction(Intent.ACTION_POWER_DISCONNECTED)
        logoutWhenCharging = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    Intent.ACTION_POWER_CONNECTED -> {
                        Log.d(TAG, "onReceive: Rajta vagyok a recieveren")
                        when {
                            getFragment("MENU") -> {
                                menuFragment = null
                                finishAndRemoveTask()
                            }
                            getFragment("LOGIN") -> {
                                Log.d(TAG, "onFinish: Loginhoz vissza")
                                finishAndRemoveTask()
                            }
                            getFragment("POLC") -> { //1
                                polcHelyezesFragment.onTimeout()
                            }
                            getFragment("IGENY") -> { //2
                                igenyFragment.clearAll()
                                igenyFragment.clearLeak()
                                finishAndRemoveTask()
                            }
                            getFragment("CIKKLEZARASFRAGMENT") -> { //3-2
                                igenyKiszedesCikkLezaras?.onTimeout()
                                removeFragment("CIKKLEZARASFRAGMENT")
                                igenyKiszedesCikkLezaras?.clearLeak()
                                finishAndRemoveTask()
                            }
                            getFragment("IGENYLEZARAS") -> { //3-1
                                igenyFragment.clearLeak()
                                finishAndRemoveTask()
                            }
                            getFragment("KISZEDESCIKK") -> { //4-3
                                igenyKontenerKiszedesCikkKiszedes?.onTimeout()
                            }
                            getFragment("NEGYESCIKKEK") -> { //4-2
                                removeFragment("NEGYESCIKKEK")
                                finishAndRemoveTask()
                            }
                            getFragment("KISZEDES") -> { //4-1
                                igenyKiszedesFragment?.destroy()
                                finishAndRemoveTask()
                            }
                            getFragment("KIHELYEZESITEMS") -> { //5-3
                                removeFragment("KIHELYEZESITEMS")
                                kihelyezes?.exit()
                                kihelyezesFragmentLista = null
                                finishAndRemoveTask()
                            }
                            getFragment("KIHELYEZESLISTA") -> { //5-2
                                removeFragment("KIHELYEZESLISTA")
                                kihelyezes?.exit()
                                finishAndRemoveTask()
                            }
                            getFragment("KIHELYEZES") -> { //5-1
                                kihelyezes?.exit()
                                finishAndRemoveTask()
                            }
                            getFragment("CIKKLEZARASFRAGMENTHATOS") -> { //6-2
                                hatosFragment?.onTimeout()
                                removeFragment("CIKKLEZARASFRAGMENTHATOS")
                                hatosFragment = null
                                finishAndRemoveTask()
                            }
                            getFragment("VARAS") -> { //6-1
                                finishAndRemoveTask()
                            }
                            getFragment("TOBBLET") -> { //7
                                tobbletOsszeallitasFragment.onKilepPressed()
                                menuFragment = null
                                finishAndRemoveTask()
                            }
                            getFragment("SZALLITO") -> {
                                finishAndRemoveTask()
                            }
                            getFragment("ELLENOR") -> {
                                finishAndRemoveTask()
                            }
                            getFragment("CIKKEKPOLCRA") -> {//////////////////////// 8-3
                                tobbletCikkekPolcra?.onTimeout()
                                finishAndRemoveTask()
                            }
                            getFragment("TOBBLETKIHELYEZESCIKKEK") -> { //8-2
                                //setContainerBackToOpen(tobbletCikkek.kontenerID!!)// lehet hogy ez nem is fog kelleni?!
                                finishAndRemoveTask()
                                //loadTobbletKontenerKihelyezes()
                            }
                            getFragment("TKK") -> {  ////////////////////////////// 8-1
                                finishAndRemoveTask()
                            }
                            getFragment("PRF") -> { // 9-3
                                removeFragment("PRF")
                                polcResultFragment?.clearLeak()
                                finishAndRemoveTask()
                            }
                            getFragment("CRF") -> { // 9-2
                                removeFragment("CRF")
                                cikkResultFragment?.clearLeak()
                                finishAndRemoveTask()
                            }
                            getFragment("LRF") -> { //9-4
                                loadFragment?.clearLeak()
                                finishAndRemoveTask()
                            }
                            getFragment("CIKK") -> { // 9-1
                                finishAndRemoveTask()
                            }
                            getFragment("MOZGAS") -> {
                                finishAndRemoveTask()
                            }
                            getFragment("RAKTARKOZI") -> {
                                finishAndRemoveTask()
                            }
                        }
                    }
                }
            }
        }
        this.registerReceiver(logoutWhenCharging, IntentFilter(mIntent))
        hideSystemUI()
    }

    private fun cancelTimer() {
        a = 0
        myTimer.cancel()
    }

    fun startExitTimer() {
        exitTimer.start()
        Log.d(TAG, "startExitTimer: elindult")
    }

    fun cancelExitTimer() {
        exitTimer.cancel()
        Log.d(TAG, "cancelExitTimer: megállt")
    }

    fun loadRaktarkozi() {
        raktarkoziFragment = RaktarkoziMozgasFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_container, raktarkoziFragment!!, "RAKTARKOZI").addToBackStack(null)
            .commit()
    }

    fun loadLoginFragment() {
        loginFragment = LoginFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_container, loginFragment!!, "LOGIN").commit()
    }

    fun loadKoztes() {
        koztesFragment = KoztesFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_container, koztesFragment!!, "KOZTES").commit()
    }
    /*fun loadIgenyKontenerKiszedes(){
        igenyKiszedesFragment = IgenyKontenerKiszedesFragment()
        supportFragmentManager.beginTransaction().replace(R.id.frame_container,igenyKiszedesFragment!!).commit()
    }*/

    private fun getMenuFragment(): Boolean {
        val fragmentManager = supportFragmentManager
        val menuFragment = fragmentManager.findFragmentByTag("MENU")
        if ((menuFragment != null && menuFragment.isVisible)) {
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
            .replace(R.id.frame_container, menuFragment!!, "MENU").commit()
    }

    fun loadCikklekerdezesFragment() {
        cikklekerdezesFragment = CikklekerdezesFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_container, cikklekerdezesFragment!!, "CIKK").addToBackStack(null)
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
        szallitoJarmuFragment = SzallitoJartmuFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_container, szallitoJarmuFragment!!, "SZALLITO")
            .commit()
    }

    fun loadKiszedesFragment() {
        val kiszedes = IgenyKontenerKiszedesFragment()
        supportFragmentManager.beginTransaction().replace(R.id.frame_container, kiszedes).commit()
    }

    fun loadKihelyezesFragment() {
        kihelyezes = IgenyKontenerKiszedese()
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_container, kihelyezes!!, "KIHELYEZES").commit()
    }

    override fun onBarcodeEvent(p0: BarcodeReadEvent?) {
        runOnUiThread {
            cancelTimer()
            barcodeData = p0?.barcodeData!!
            when {
                getFragment("LOGIN") -> {
                    loginFragment?.setId(barcodeData)
                    dolgKod = barcodeData
                    loginFragment?.startSpinning()
                    CoroutineScope(IO).launch {
                        sql.checkRightSql(dolgKod, this@MainActivity)
                    }
                }
                getFragment("CIKK") -> {
                    if (getFragment("CRF")) {
                        cikkResultFragment?.clearLeak()
                    } else if (getFragment("PRF")) {
                        polcResultFragment?.clearLeak()
                    } else if (getFragment("LRF")) {
                        loadFragment?.clearLeak()
                    }
                    loadLoadFragment("Várom az eredményt")
                    cikkItems.clear()
                    polcItems.clear()
                    cikklekerdezesFragment?.setBinOrItem(barcodeData)
                    CoroutineScope(IO).launch {
                        sql.cikkPolcQuery(barcodeData, this@MainActivity)
                    }
                }
                getFragment("SZALLITO") -> {
                    szallitoJarmuFragment?.setJarmu(barcodeData)
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
                    ellenorzoKodFragment?.setCode(barcodeData)
                    checkEllenorzoKod(barcodeData)
                }
                getFragment("POLC") -> {
                    polcHelyezesFragment.setCode(barcodeData)
                }
                getFragment("IGENY") -> {
                    igenyFragment.setCode(barcodeData)
                }
                getFragment("KIHELYEZES") -> {
                    kihelyezes?.setCode(barcodeData)
                }
                getFragment("TOBBLET") -> {
                    tobbletOsszeallitasFragment.setCode(barcodeData)
                }
                getFragment("CIKKEKPOLCRA") -> {
                    tobbletCikkekPolcra?.setCode(barcodeData)
                }
                getFragment("MOZGAS") -> {
                    val fragment = supportFragmentManager.findFragmentByTag("MOZGAS")
                    (fragment as MozgasResult).setText(barcodeData)
                }
                getFragment("RAKTARKOZI") -> {
                    raktarkoziFragment?.getCode(barcodeData)
                }
            }
            //myTimer.start()
            wifiInfo = getMacAndSignalStrength()
        }
    }

    override fun onFailureEvent(p0: BarcodeFailureEvent?) {
        runOnUiThread {
            Toast.makeText(this@MainActivity, "Nem sikerült leolvasni", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        cancelTimer()
        if (getMenuFragment() && menuFragment?.hasRightToOpen()!!) {
            when (keyCode) {
                7 -> {
                    menuFragment?.kilepesClick()
                    finishAndRemoveTask()
                    if (isWifiConnected()) {
                        wifiInfo = getMacAndSignalStrength()
                    }
                } //0
                8 -> {
                    Log.d(TAG, "onKeyUp: ${getMacAndSignalStrength()}")
                    menuFragment?.polcHelyezesClick()
                    loadPolcHelyezesFragment()
                    if (isWifiConnected()) {
                        wifiInfo = getMacAndSignalStrength()
                    }
                } //1
                9 -> {
                    menuFragment?.igenyOsszeClick()
                    containerCheck(dolgKod)
                    if (isWifiConnected()) {
                        wifiInfo = getMacAndSignalStrength()
                    }
                }  //2
                10 -> {
                    menuFragment?.igenyLezarClick()
                    igenyKontenerCheck()
                    if (isWifiConnected()) {
                        wifiInfo = getMacAndSignalStrength()
                    }
                }  //3
                11 -> {
                    menuFragment?.igenyKiszedClick()
                    igenyKontenerKiszedes()
                    if (isWifiConnected()) {
                        wifiInfo = getMacAndSignalStrength()
                    }
                }  //4
                12 -> {
                    menuFragment?.igenyKihelyezClick()
                    loadKihelyezesFragment()
                    if (isWifiConnected()) {
                        wifiInfo = getMacAndSignalStrength()
                    }
                }  //5
                13 -> {
                    menuFragment?.kiszedesreVaroClick()
                    kiszedesreVaro()
                    if (isWifiConnected()) {
                        wifiInfo = getMacAndSignalStrength()
                    }
                }  //6
                14 -> {
                    menuFragment?.tobbletOsszeClick()
                    containerCheck7(dolgKod)
                    if (isWifiConnected()) {
                        wifiInfo = getMacAndSignalStrength()
                    }
                }  //7
                15 -> {
                    menuFragment?.tobbletKihelyezClick()
                    loadTobbletKontenerKihelyezes()
                    if (isWifiConnected()) {
                        wifiInfo = getMacAndSignalStrength()
                    }
                }  //8
                16 -> {
                    menuFragment?.cikklekerdezesClick()
                    loadCikklekerdezesFragment()
                    if (isWifiConnected()) {
                        wifiInfo = getMacAndSignalStrength()
                    }
                }  //9
                56 -> {
                    menuFragment?.rakhelyClick()
                    loadRaktarkozi()
                    if (isWifiConnected()) {
                        wifiInfo = getMacAndSignalStrength()
                    }
                } // .
                131 -> {
                    if (loginContains(dolgKod)) {
                        when (fusetech) {
                            "1" -> {
                                fusetech = "2"
                                Toast.makeText(
                                    applicationContext,
                                    "Raktár utcába vagyunk",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            else -> {
                                fusetech = "1"
                                Toast.makeText(
                                    applicationContext,
                                    "Guba Sándorba vagyunk",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }//f1
            }
        } else if (getMenuFragment()) {
            when (keyCode) {
                7 -> {
                    menuFragment?.kilepesClick()
                    finishAndRemoveTask()
                } //0
                16 -> {
                    menuFragment?.cikklekerdezesClick()
                    loadCikklekerdezesFragment()
                }  //9
            }
        } else if (getFragment("KIHELYEZES")) {
            kihelyezes?.deleteFocused()
        } else if (getFragment("KISZEDESCIKK")) {
            igenyKontenerKiszedesCikkKiszedes?.deleteFocused()
        } else if (getFragment("CIKKEKPOLCRA")) {
            tobbletCikkekPolcra?.deleteFocused()
        } else if (getFragment("IGENY")) {
            igenyFragment.deleteFocused()
        } else if (getFragment("TOBBLET")) {
            tobbletOsszeallitasFragment.deleteFocused()
        }
        //myTimer.start()
        return super.onKeyUp(keyCode, event)
    }

    private fun loginContains(login: String): Boolean {
        if (login != "") {
            for (i in 0 until itLoginCodes.size) {
                if (itLoginCodes[i] == login) {
                    return true
                }
            }
        }
        return false
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
        if (getFragment("KIHELYEZES")) {
            CoroutineScope(IO).launch {
                sql.getContainersFromVehicle(szallito, this@MainActivity)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        kihelyezesItems.clear()
        polcItems.clear()
        cikkItems.clear()
        Log.d(TAG, "onDestroy: ")
        if (barcodeReader != null) {
            barcodeReader?.release()
        }
        Log.d(TAG, "onPause: ")
    }

    override fun onDestroy() {
        super.onDestroy()
        if (barcodeReader != null) {
            barcodeReader?.removeBarcodeListener(this)
            barcodeReader?.close()
            manager = null
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(logoutWhenCharging)
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
            CoroutineScope(Main).launch {
                progress.visibility = View.VISIBLE
            }
            connection = DriverManager.getConnection(connectionString)
            val statement2 = connection.prepareStatement(resources.getString(R.string.isPolc21))
            statement2.setString(1, "21")
            statement2.setString(2, barcodeData)
            val resultSet = statement2.executeQuery()
            if (!resultSet.next()) {
                CoroutineScope(Main).launch {
                    setAlert("Nincs a tranzitraktárban!")
                    progress.visibility = View.GONE
                }
            } else {
                sz0x = barcodeData
                val statement =
                    connection.prepareStatement(resources.getString(R.string.updateContainerStatus))
                statement.setInt(1, 2)
                statement.setString(2, sz0x)
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
                CoroutineScope(Main).launch {
                    progress.visibility = View.GONE
                }
            }
            connection.close()
        } catch (e: Exception) {
            CoroutineScope(Main).launch {
                setAlert("Probléma a feltöltésben!\n $e")
                progress.visibility = View.GONE
                sql.writeLog(e.toString(), "updateKontenerKiszedesre")
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
        builder.setPositiveButton("OK") { _, _ ->
            hideSystemUI()
        }
        //builder.setCancelable(false)
        builder.setOnCancelListener {
            hideSystemUI()
        }
        builder.create()
        builder.show().getButton(DialogInterface.BUTTON_POSITIVE).requestFocus()
    }

    override fun setValue(value: String) {
        if (value.isNotEmpty()) {
            loadLoadFragment("Várom az eredményt")
            cikkItems.clear()
            polcItems.clear()
            cikklekerdezesFragment?.setBinOrItem(value)
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
            CoroutineScope(Main).launch {
                progress.visibility = View.VISIBLE
            }
            connection = DriverManager.getConnection(connectionString)
            val statement = connection.prepareStatement(resources.getString(R.string.is02Polc))
            statement.setString(1, bin)
            val resultSet = statement.executeQuery()
            CoroutineScope(Main).launch {
                progress.visibility = View.GONE
            }
            //connection.close()
            return resultSet.next()
        } catch (e: Exception) {
            CoroutineScope(Main).launch {
                setAlert("$e")
                progress.visibility = View.GONE
            }
            return false
        }
    }

    override fun sendBinCode(code: String, kontener: String) {
        CoroutineScope(IO).launch {
            sql.check01(code, this@MainActivity, kontener)
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

    override fun setJavit2(kontener: String) {
        CoroutineScope(IO).launch {
            sql.contenerJavit(kontener)
        }
    }

    override fun setRakhelyTetel2(kontener: Int, code: String) {
        CoroutineScope(IO).launch {
            sql.tetelJavit(kontener, code)
        }
    }

    override fun setJavit(kontener: String) {
        CoroutineScope(IO).launch {
            sql.contenerJavit(kontener)
        }
    }

    override fun setRakhelyTetel(kontener: Int, code: String) {
        CoroutineScope(IO).launch {
            sql.tetelJavit(kontener, code)
        }
    }

    override fun closeContainer(statusz: Int, datum: String, kontener: String) {
        CoroutineScope(IO).launch {
            sql.closeContainerSql(statusz, datum, this@MainActivity, kontener)
        }
    }

    override fun sendBinCode2(code: String, kontener: String) {
        CoroutineScope(IO).launch {
            sql.checkCode02(code, this@MainActivity, kontener)
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

    override fun closeContainer2(statusz: Int, datum: String, kontener: String) {
        sql.closeContainerSql7(statusz, datum, this@MainActivity, kontener)
    }

    override fun itemCloseOneByOne(id: String, status: Int): Boolean {
        return sql.closeItemByItem(id,status)
    }

    override fun getItemId(id: String) {
        sql.getItemIdList(id)
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
            igenyLezarasFragment = IgenyKontenerLezarasFragment()
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
                kontItem.clear()
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

    fun updateItemStatus(itemId: String, status: Int) {
        sql.updtaeItemStatusSql(itemId, this@MainActivity, status)
    }

    fun updateItemAtvevo(itemId: String) {
        sql.updateItemAtvevoSql(itemId, this@MainActivity)
    }

    fun checkIfContainerIsDone(container: String, itemId: String, raktar: String, polc: String) {
        sql.checkIfContainerIsDoneSql(container, itemId, raktar, polc, this@MainActivity)
    }

    private fun checkEllenorzoKod(code: String) {
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

    fun updateCikkAfterSend(code: Int): Boolean {
       return sql.closeCikkek(code, this@MainActivity)
    }

    fun closeItem(code: String) {
        sql.closeContainer(code, this@MainActivity)
    }

    fun checkCloseContainer() {
        sql.closeReloadContainer(this@MainActivity)
    }

    fun setContainerStatusAndGetItems(kontener_id: String?) {
        //selectedContainer = kontener_id!!
        CoroutineScope(IO).launch {
            sql.updateContainerAndOpenItems(kontener_id, this@MainActivity)
        }
    }

    private fun setContainerBackToOpen(kontener: String) {
        CoroutineScope(IO).launch {
            sql.statuszVisszairas(kontener, this@MainActivity)
            //loadTobbletKontenerKihelyezes()
        }
    }

    override fun onBackPressed() {
        try {
            when {
                getFragment("MENU") -> {
                    menuFragment = null
                    loadLoginFragment()
                }
                getFragment("POLC") -> { // 1
                    polcHelyezesFragment.onKilepPressed()
                }
                getFragment("IGENY") -> { //2
                    igenyFragment.clearAll()
                    loadMenuFragment(true)
                }
                getFragment("CIKKLEZARASFRAGMENT") -> { // 3-2
                    igenyKiszedesCikkLezaras?.buttonPerform()
                }
                getFragment("IGENYLEZARAS") -> { // 3-1
                    loadMenuFragment(true)
                }
                getFragment("KISZEDESCIKK") -> { // 4-3
                    /* loadMenuFragment(true)
                     igenyKontenerKiszedes()*/
                    igenyKontenerKiszedesCikkKiszedes?.performButton()
                }
                getFragment("NEGYESCIKKEK") -> { // 4-2
                    //loadMenuFragment(true)
                    //loadKiszedesFragment()
                    removeFragment("NEGYESCIKKEK")
                    igenyKontenerKiszedes()
                }
                getFragment("KISZEDES") -> { // 4-1
                    loadMenuFragment(true)
                }
                getFragment("KIHELYEZESITEMS") -> { // 5-3
                    //kihelyezes?.onBack()
                    kihelyezes?.onButtonPressed()
                    //getContainerList("SZ01")
                }
                /*getFragment("KIHELYEZESLISTA") -> { // 5-2
                    kihelyezes?.exit()
                    loadMenuFragment(true)
                }*/
                getFragment("KIHELYEZES") -> { // 5-1
                    kihelyezes?.onButtonPressed()
                    loadMenuFragment(true)
                }
                getFragment("CIKKLEZARASFRAGMENTHATOS") -> { // 6-2
                    hatosFragment?.buttonPerform()
                    removeFragment("CIKKLEZARASFRAGMENTHATOS")
                }
                getFragment("VARAS") -> { // 6-1
                    loadMenuFragment(true)
                }
                getFragment("TOBBLET") -> { // 7
                    tobbletOsszeallitasFragment.onKilepPressed()
                }
                getFragment("CIKKEKPOLCRA") -> { // 8-3
                    Log.d(TAG, "onBackPressed: CIKKEKPOLCRA")
                    //progress.visibility = View.VISIBLE
                    tobbletCikkekPolcra?.onButtonPressed()
                    // progress.visibility = View.GONE
                }
                getFragment("TOBBLETKIHELYEZESCIKKEK") -> { // 8-2
                    setContainerBackToOpen(tobbletCikkek?.kontenerID!!)// lehet hogy ez nem is fog kelleni?!
                    //loadTobbletKontenerKihelyezes()
                }
                getFragment("TKK") -> { // 8-1
                    loadMenuFragment(true)
                }
                getFragment("SZALLITO") -> {
                    /*loadMenuFragment(true)
                    igenyKontenerKiszedes()*/
                    Toast.makeText(applicationContext, "Húzd le a kódot!!!!", Toast.LENGTH_LONG)
                        .show()
                }
                getFragment("ELLENOR") -> {
                    //loadMenuFragment(true)
                    //igenyKontenerKiszedes()
                    Toast.makeText(applicationContext, "Ellenőrizd le!!!!", Toast.LENGTH_LONG)
                        .show()
                }
                getFragment("PRF") -> { // 9-3
                    removeFragment("PRF")
                    polcResultFragment?.clearLeak()
                    loadMenuFragment(hasRight)
                }
                getFragment("CRF") -> { // 9-2
                    removeFragment("CRF")
                    cikkResultFragment?.clearLeak()
                    loadMenuFragment(hasRight)
                }
                getFragment("LRF") -> { //9-4
                    loadFragment?.clearLeak()
                    loadMenuFragment(hasRight)
                }
                getFragment("CIKK") -> { // 9-1
                    loadMenuFragment(hasRight)
                }
                /*getFragment("TOBBLETOSSZE") -> {
                    loadMenuFragment(true)
                }*/
                getFragment("LOGIN") -> {
                    Log.d(TAG, "onBackPressed: LOGIN")
                    menuFragment = null
                    finishAndRemoveTask()
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
            sql.openNyolcHarmas(
                id,
                kontenerID,
                megjegyzes,
                megjegyzes2,
                intrem,
                unit,
                mennyiseg,
                cikkszam,
                this@MainActivity
            )
        }
    }

    fun raktarcheck(code: String) {
        CoroutineScope(IO).launch {
            sql.checkBinIn02(code, this@MainActivity)
        }
    }

    fun updateCikkandContainer(cikk: Int, kontener: Int) {
        sql.closeItemAndCheckContainer(cikk, kontener, this@MainActivity)
    }

    fun removeFragment(fragment1: String) {
        val fragment = supportFragmentManager.findFragmentByTag(fragment1)
        if (fragment != null) supportFragmentManager.beginTransaction().remove(fragment)
            .commit()
    }

    override fun hatosInfo(id: Int) {
        CoroutineScope(IO).launch {
            sql.cikkCodeSql(id, this@MainActivity)
        }
    }

    private fun requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        ) {
            AlertDialog.Builder(this)
                .setTitle("El kell az engedélyt fogadni")
                .setMessage("Különben nem fog működni")
                .setPositiveButton("OK") { _, _ ->
                    ActivityCompat.requestPermissions(
                        this@MainActivity,
                        arrayOf(
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ), EXTERNAL_STORAGE
                    )
                }
                .setNegativeButton("Nem") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ), EXTERNAL_STORAGE
            )
        }
    }

    override
    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == EXTERNAL_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Toast.makeText(this, "El van fogadva", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "onRequestPermissionsResult: El van fogadva")
            } else {
                //Toast.makeText(this, "Nincs elfogadva", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "onRequestPermissionsResult: Nincs elfogadva")
            }
        }
    }

    fun getMacAddr(): String {
        try {
            val all: List<NetworkInterface> =
                Collections.list(NetworkInterface.getNetworkInterfaces())
            for (nif in all) {
                if (!nif.name.equals("wlan0", ignoreCase = true)) continue
                val macBytes = nif.hardwareAddress ?: return ""
                val res1 = StringBuilder()
                for (b in macBytes) {
                    res1.append(String.format("%02X:", b))
                }
                if (res1.isNotEmpty()) {
                    res1.deleteCharAt(res1.length - 1)
                }
                return res1.toString()
            }
        } catch (ex: java.lang.Exception) {
            Log.d(ContentValues.TAG, "getMacAddr: $ex")
        }
        return "02:00:00:00:00:00"
    }

    fun getMacAndSignalStrength(): String {
        val wifimanage = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        val wifiinfo = wifimanage.connectionInfo
        return wifiinfo.bssid + "," + wifiinfo.rssi.toString().trim()
    }

    fun isWifiConnected(): Boolean {
        try {
            return connManager.getNetworkCapabilities(connManager.activeNetwork)
                ?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)!!
        } catch (e: java.lang.Exception) {
            return false
        }
    }

    fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window,frame_container).let { controller ->
            //controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.hide(WindowInsetsCompat.Type.navigationBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()
        }
    }

    override fun loadPolcItems(code: String) {
        val bundle = Bundle()
        bundle.putString("KIINDULAS", code)
        val mozgasFragment = MozgasResult()
        mozgasFragment.arguments = bundle
        supportFragmentManager.beginTransaction()
            .replace(R.id.resultFrame, mozgasFragment, "MOZGAS").commit()
    }

    @SuppressLint("SimpleDateFormat")
    override fun saveFile(
        cikk: String,
        mennyiseg: Double,
        kiinduloPolc: String,
        celPolc: String,
        rbol: String,
        rba: String
    ) {
        val path = getExternalFilesDir(null)
        val name = SimpleDateFormat("yyyyMMddHHmmss").format(Date()) + Random.nextInt(
            0,
            10000
        ) + ".xml"
        if (getFragment("MOZGAS")) {
            val fragment = supportFragmentManager.findFragmentByTag("MOZGAS")
            Log.d("IOTHREAD", "saveFile: 1,5")
            (fragment as MozgasResult).getFileFromActivity(
                File(path, name),
                cikk,
                mennyiseg,
                kiinduloPolc,
                celPolc,
                rbol,
                rba
            )
        }
    }

    override fun ujPolcFelvetele() {
        if (getFragment("MOZGAS")) {
            val fragment = supportFragmentManager.findFragmentByTag("MOZGAS")
            if (fragment != null) {
                supportFragmentManager.beginTransaction().remove(fragment).commit()
                if (getFragment("RAKTARKOZI")) {
                    val fragment1 = supportFragmentManager.findFragmentByTag("RAKTARKOZI")
                    (fragment1 as RaktarkoziMozgasFragment).removeBin()
                }
            }
        }
    }
}