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


private const val TAG = "MainActivity"

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
    RetrofitFunctions.Trigger,
    HatosCikkekFragment.Hatos {
    /*
    // 1es opci?? pont beviszem a cikket, ??s megn??zi hogy van e a tranzit rakt??rban (3as rakt??r)szabad(ha z??rolt akkor sz??l, ha nincs akkor sz??l)
    //ha van ??s szabad is, n??zz??k meg hogy hol vannak ilyenek FIFO szerint, vagy v??laszt a list??b??l, vagy felvisz egy ??jat, lehet??s??g ha nem f??r fel r?? ??s
    // ??t kell rakni m??shova
    //egyszerre csak egy ember dolgozhasson a cikk felrak??s??n??l

    //2es opci??  megn??zem, hogy van e kont??ner a "atado" ??s "statusz = 0"
    // amikor megnyitom az ig??ny kont??ner ??ssze??ll??t??s??t, akkor [Leltar].[dbo]. kontener-be be??rom a atado(1GU),statusz(0),kontener_tipus(1)
    //kontener 0000+id (??sszvissz 10karakterig)
    //azt??n megjelen??tem a "kontener"

    //A polc??l csak a 01-es rakt??rokat fogadja el (ilyen van a polcCheck stringbe) ??s ha j?? akkor be??rja a [Leltar].[dbo]. kontener-be termeles_raktar = 01, termeles_rakhely = polc
    // j??n a cikk (megn??zz??k h van e), be??rjuk a 4dolgot mint mindig
    // mennyis??g elfogad??sa enterrel, k??ri a k??vetkez?? cikket ??S bele??r a [Leltar].[dbo].kontener_tetel-be (f??nyk??p)
    // a [Leltar].[dbo]. kontener be??r??dik a statusz = 1, igenyelve = datetime
    * 3as opci??
    * csak azok a kont??nerek legyenek megjelen??tve, amelyek KanBan st??tusza 1 ??S A st??tusza 0 (??rja ki amit ki kell ??rni) illetve a t??telek st??tusza is 0
    * a lez??r??s a m??sik f??l??n ??t??rja a t??telekn??l a st??tuszt 1-re, a kont??nerekn??l a st??tusz is 1 lesz ??s be??rja az ig??nyl??s d??tum??t
    *
    * 4es opci??
    * bonyolult.. tudja amit a 6-os opci??. ha r??megyek egy t??telre ??t??rja a st??tusz??t 2-re. be kell iktatni egy sz??ll??t??j??rm?? bek??r??st, onnant??l j??hetnek a t??telek
    * kiszed??s: ha nulla a mennyis??g ( v dedik??lt gomb )akkor cikk lez??r??s ??s 3as st??tusz, ha felvette a megfelel?? mennyis??get akkor is 3as st??tusz, ha nem vesz fel annyit akkor be??rja a raktar
    * tetel adatbazisba ??s ha kilepek lezaras nelkul akkor visszairja 1-re. ha azt??n megint megnyitom akkor abb??l kiolvassa az ??rt??ket ??s azt kivonja az ig??nyelt mennyis??gb??l
    * ??s a polcot amit be kell friss??teni. esetleg m??sik sz??nez??s???
    * amikor kiv??lasztom a kont??nert, akkor csak azok a cikkek jelenjenek meg amikn??l az ??tvev?? NULL vagy ??n nyitottam meg
    *
    * 6os opci??
    * Ki??rja az 1es ??s 2 st??tusz?? kont??nereket, majd kattint??ssal belemegy ??s ki??rja a t??teleket ami benne van, ??tsz??nezi a 2-es st??tusz?? kont??nereket
    *
    * ??rjon bele ha belel??pek ha m??r van sz??ll??t?? j??rm?? h ki vette ??t
    *
    * a sz??ll??t??j??rm?? beolvas??snl olyan polc kell ami van a 21-es rakt??rban
    *
    * A SpringBoot fel van k??sz??tve ??s service-be is m??k??dik! csak ??t kell ??rni a uploadDir-t inputxml-re jar-t csin??lni ??s feltelep??teni a service-t a 10.0.1.69-en
    * 5-??s opci??.
    * Bek??ri a sz??ll??t?? j??rm??vet ??s megjelen??ti az ??sszes szerel??helyet ahol van kont??ner. Ut??na megnyitjuk szerel??helyet vonalk??ddal ??s bet??lti az elemeket
    * ??s pirossal ki??rja aminek 0 van megadva mennyis??gnek. A kihelyez??s gombbal el??sz??r elk??ldi a szervernek az XML-t majd friss??ti a t??tel t??bl??ban a st??tuszt 5re
    * Ha ezeket megcsin??lta akkor a v??g??n ??t??rja a kont??ner t??bl??t 5re ??s be??rja a kihelyez??s d??tum??t, illetve friss??ti az "atvevo"-t arra aki kihelyezi
    *
    * 7-es opci??
    * nyit egy kont??nert be??rja az id-t, atado, kontener, statusz= 6, kontener_tipus = 2
    * a 2-es opci?? layoutj??t haszn??lhatom plusz xml k??ld??sn??l ford??tva t??rt??nik. 01 b??l 21 be, a mozgatott mennyis??gbe kell be??rni a mennyis??get
    *
    * 8-as opci??
    * a 4-es opci??hoz hasonl??, csak a 02-es rakt??r polcaira helyezi vissza a cikkeket

    /*
        HA t??bbsz??r akarunk kivenni, akkor k??rdezze meg hogy ??res-e a polc vagy nem,
        csak ??gy kell kivenni. HA ??res akkor k??ldj??n emailt, a polccal levett mennyis??ggel ??s a polc mennyis??ggel


        T??bblet dedik??lt gomb: Polchely kell legyen benne ??s nulla legyen a polcchleyen l??v?? mennyis??g. VAn t??bblet m az x polcon? megad??s ut??n email. x polcon cikksz??moadatok, ennyivel t??bb volt Mennyi t??bbletmennyis??g maradt a polcon?.
     */
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

    //var igenyKiszedesFragment = IgenyKontenerKiszedesFragment()
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
    val retro = RetrofitFunctions(this)
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

        @SuppressLint("StaticFieldLeak")
        lateinit var progress: ProgressBar
        val kihelyezesItems: ArrayList<SzerelohelyItem> = ArrayList()
        val cikkItem4: ArrayList<KontenerbenLezarasItem> = ArrayList()
        val kontItem: ArrayList<KontenerbenLezarasItem> = ArrayList()
        val tobbletItem: ArrayList<KontenerbenLezarasItem> = ArrayList()
        val tempLocations: ArrayList<PolcLocation> = ArrayList()
        val tobbletKontener: ArrayList<KontenerItem> = ArrayList()
        var mainUrl = "http://10.0.1.69:8030/"
        var backupURL = "http://10.0.1.199:8030/"
        var endPoint = """"""
        var logPath = ""
        var timeOut = 0L
        var hasRight = false

        //var szallitoJarmu: ArrayList<String> = ArrayList()
        // var ellenorzoKod: ArrayList<String> = ArrayList()
        var szallitoMap: HashMap<String, String> = HashMap()
        var dolgKod: String = ""// vissza ide
        var sz0x: String = ""
        var wifiInfo: String = ""
        lateinit var path: File
        var szallito = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val bundle: Bundle = intent.extras!!
        connManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        //wifi = connManager.getNetworkCapabilities(connManager.activeNetwork)?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
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
        Log.d("MYBUNDLE", "onCreate: ${szallitoMap.get("SZ01")}")
        /*szallitoJarmu = bundle.getStringArrayList("szallitoJarmu")!!
        Log.d("MYBUNDLE", "onCreate: $szallitoJarmu")
        ellenorzoKod = bundle.getStringArrayList("ellenorzokod")!!
        Log.d("MYBUNDLE", "onCreate: $ellenorzoKod")*/
        res = resources
        path = getExternalFilesDir(null)!!
        // mFile = File(path,name)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        supportActionBar?.hide()
        igenyFragment = IgenyKontenerOsszeallitasFragment.newInstance("", "")
        //igenyKiszedesFragment = IgenyKontenerKiszedesFragment()
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
                loginFragment?.clearLeak()
                finishAndRemoveTask()
            }
        }

        myTimer = object : CountDownTimer(timeOut * 60 * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                //Some code
                a++
                //Toast.makeText(this@MainActivity, "$a", Toast.LENGTH_SHORT).show()
            }

            override fun onFinish() {
                when {
                    getFragment("MENU") -> {
                        menuFragment = null
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
                    else -> {
                        //loadLoginFragment()
                        Log.d(TAG, "onFinish: ELSE")
                    }
                }
            }
        }
        myTimer.start()
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
        Log.d(TAG, "cancelExitTimer: meg??llt")
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
                    loadLoadFragment("V??rom az eredm??nyt")
                    cikkItems.clear()
                    polcItems.clear()
                    cikklekerdezesFragment?.setBinOrItem(barcodeData)
                    CoroutineScope(IO).launch {
                        sql.cikkPolcQuery(barcodeData, this@MainActivity)
                    }
                }
                getFragment("SZALLITO") -> {
                    szallitoJarmuFragment?.setJarmu(barcodeData)
                    //JSON b??l megn??zni, hogy van e ilyen sz??ll??t?? ??s itt ??tadni az ??rt??k??t
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
            }
            myTimer.start()
            wifiInfo = getMacAndSignalStrength()
        }
    }

    override fun onFailureEvent(p0: BarcodeFailureEvent?) {
        runOnUiThread {
            Toast.makeText(this@MainActivity, "Nem siker??lt leolvasni", Toast.LENGTH_SHORT).show()
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
        if(getFragment("KIHELYEZES")){
            CoroutineScope(IO).launch {
                sql.getContainersFromVehicle(szallito,this@MainActivity)
            }
        }
        //if(getFragment())
    }

    override fun onPause() {
        super.onPause()
        /*myList.clear()
        kontener1List.clear()
        kontenerList.clear()
        listIgenyItems.clear()
        polcLocation?.clear()*/
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
                    setAlert("Nincs a tranzitrakt??rban!")
                    progress.visibility = View.GONE
                }
            } else {
                sz0x = barcodeData
                val statement =
                    connection.prepareStatement(resources.getString(R.string.updateContainerStatus))
                statement.setInt(1, 2)
                statement.setString(2, sz0x)//JSON b??l a sz??ll??t??j??rm??
                statement.setString(3, dolgKod)//ide kell a bejelentkez??s k??d
                statement.setString(4, kontener)
                statement.executeUpdate()
                Log.d(TAG, "updateKontenerKiszedesre: Sikeres adatfriss??t??s!!!")
                /*CoroutineScope(Main).launch {
                    setAlert("Siker!")
                }*/
                //itt k??ne beolvasni a 4es opci??t
                loadKiszedesFragment()
                sql.checkIfContainerIsOpen(kontener, this@MainActivity)
                CoroutineScope(Main).launch {
                    progress.visibility = View.GONE
                }
            }
        } catch (e: Exception) {
            CoroutineScope(Main).launch {
                setAlert("Probl??ma a felt??lt??sben!\n $e")
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
            loadLoadFragment("V??rom az eredm??nyt")
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
            return resultSet.next()
        } catch (e: Exception) {
            CoroutineScope(Main).launch {
                setAlert("$e")
                progress.visibility = View.GONE
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

    override fun closeContainer(statusz: Int, datum: String, kontener: String) {
        CoroutineScope(IO).launch {
            sql.closeContainerSql(statusz, datum, this@MainActivity, kontener)
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

    override fun closeContainer2(statusz: Int, datum: String, kontener: String) {
        sql.closeContainerSql7(statusz, datum, this@MainActivity, kontener)
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
            } else {
                igenyKontenerCheck()
                CoroutineScope(Main).launch {
                    setAlert("A kont??ner st??tusza m??r megv??ltozott")
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

    fun updateCikkAfterSend(code: Int) {
        sql.closeCikkek(code, this@MainActivity)
    }

    fun closeItem(code: String) {
        sql.closeContainer(code, this@MainActivity)
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
                    Toast.makeText(applicationContext, "H??zd le a k??dot!!!!", Toast.LENGTH_LONG)
                        .show()
                }
                getFragment("ELLENOR") -> {
                    //loadMenuFragment(true)
                    //igenyKontenerKiszedes()
                    Toast.makeText(applicationContext, "Ellen??rizd le!!!!", Toast.LENGTH_LONG)
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

    override fun triggerError() {
        Log.d(TAG, "triggerError: ")
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
                .setTitle("El kell az enged??lyt fogadni")
                .setMessage("K??l??nben nem fog m??k??dni")
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
                if (res1.length > 0) {
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
        try{
            return connManager.getNetworkCapabilities(connManager.activeNetwork)
                ?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)!!
        }catch (e: java.lang.Exception){
            return false
        }
    }
    fun hideSystemUI() {
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.

        this.window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
               // or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }
}