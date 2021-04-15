package com.fusetech.virtualkanban.Activities

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.fusetech.virtualkanban.DataItems.*
import com.fusetech.virtualkanban.Fragments.*
import com.fusetech.virtualkanban.R
import com.honeywell.aidc.*
import com.honeywell.aidc.BarcodeReader.BarcodeListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet

class MainActivity : AppCompatActivity(), BarcodeListener,
    CikklekerdezesFragment.SetItemOrBinManually,
    PolcraHelyezesFragment.SendCode,
    PolcLocationFragment.SetPolcLocation,
    IgenyKontenerOsszeallitasFragment.SendBinCode,
    IgenyKontenerLezarasFragment.IgenyKontnerLezaras{
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
    /*
    * 3as opció
    * csak azok a konténerek legyenek megjelenítve, amelyek KanBan státusza 1 ÉS A státusza 0 (írja ki amit ki kell írni) illetve a tételek státusza is 0
    * a lezárás a másik fülön átírja a tételeknél a státuszt 1-re, a konténereknél a státusz is 1 lesz és beírja az igénylés dátumát
    * */
    private var manager : AidcManager? = null
    private var barcodeReader : BarcodeReader? = null
    private lateinit var barcodeData : String
    private lateinit var loginFragment : LoginFragment
    private lateinit var dolgKod : String
    private lateinit var connection : Connection
    private var cikkItems: ArrayList<CikkItems> = ArrayList()
    private var polcItems: ArrayList<PolcItems> = ArrayList()
    private val polcHelyezesFragment = PolcraHelyezesFragment()
    private lateinit var igenyFragment: IgenyKontenerOsszeallitasFragment
    private lateinit var igenyLezarasFragment: IgenyKontenerLezarasFragment
    private lateinit var igenyKiszedesFragment: IgenyKontenerKiszedesFragment
    private val TAG = "MainActivity"
    private val cikklekerdezesFragment = CikklekerdezesFragment()
    val polcLocationFragment = PolcLocationFragment()
    private var polcLocation: ArrayList<PolcLocation>? = ArrayList()
    private var kontener = ""
    private lateinit var menuFragment : MenuFragment
    private var lezarandoKontener = ""
    private var igenyLezarCikkVisible: Boolean = false
    private val url = "jdbc:jtds:sqlserver://10.0.0.11;databaseName=Fusetech;user=scala_read;password=scala_read;loginTimeout=10"
    private val connectionString ="jdbc:jtds:sqlserver://10.0.0.11;databaseName=leltar;user=Raktarrendszer;password=PaNNoN0132;loginTimeout=10"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        supportActionBar?.hide()
        igenyFragment = IgenyKontenerOsszeallitasFragment.newInstance("","")
        igenyLezarasFragment = IgenyKontenerLezarasFragment()
        igenyKiszedesFragment = IgenyKontenerKiszedesFragment()
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
        loginFragment = LoginFragment()
        supportFragmentManager.beginTransaction().replace(R.id.frame_container, loginFragment,"LOGIN").commit()

    }
    private fun getMenuFragment(): Boolean{
        val fragmentManager = supportFragmentManager
        val menuFragment = fragmentManager.findFragmentByTag("MENU")
        if(menuFragment != null && menuFragment.isVisible){
            return true
        }
        return false
    }
    private fun getIgenyLezaras(): Boolean{
        val myFrag = supportFragmentManager.findFragmentByTag("IGENYLEZARAS")
        if(myFrag != null && myFrag.isVisible){
            return true
        }
        return false
    }
    fun loadMenuFragment(hasRight : Boolean?){
        menuFragment = MenuFragment.newInstance(hasRight)
        supportFragmentManager.beginTransaction().replace(R.id.frame_container, menuFragment,"MENU").commit()
    }
    private fun loadCikklekerdezesFragment(){
        supportFragmentManager.beginTransaction().replace(R.id.frame_container, cikklekerdezesFragment,"CIKK").addToBackStack(null).commit()
    }
    private fun loadLoadFragment(value: String){
        val loadFragment = LoadFragment.newInstance(value)
        supportFragmentManager.beginTransaction().replace(R.id.cikk_container,loadFragment).commit()
    }

     fun loadPolcHelyezesFragment(){
        supportFragmentManager.beginTransaction().replace(R.id.frame_container,polcHelyezesFragment,"POLC").addToBackStack(null).commit()
    }
    fun loadIgenyOsszeallitasFragment(kontener: String, polc: String?){
        igenyFragment = IgenyKontenerOsszeallitasFragment.newInstance(kontener,polc)
        supportFragmentManager.beginTransaction().replace(R.id.frame_container,igenyFragment,"IGENY").addToBackStack(null).commit()
    }
    fun loadIgenyKiszedesFragment(){
        val igenyKiszedes = IgenyKontenerKiszedesFragment()
        supportFragmentManager.beginTransaction().replace(R.id.frame_container,igenyKiszedes,"IGENYKISZEDES").addToBackStack(null).commit()
    }
    fun removeIgenyFragment(){
        val fragment : Fragment? = supportFragmentManager.findFragmentByTag("IGENYLEZARAS")
        if (fragment != null) {
            supportFragmentManager.beginTransaction().remove(fragment).commit()
        }
    }
    override fun onBarcodeEvent(p0: BarcodeReadEvent?) {
        runOnUiThread{
            barcodeData = p0?.barcodeData!!
            if (loginFragment != null && loginFragment.isVisible) {
                loginFragment.SetId(barcodeData)
                dolgKod = barcodeData
                loginFragment.StartSpinning()
                CoroutineScope(IO).launch {
                    checkRightSql(dolgKod)
                }
            }else if(cikklekerdezesFragment != null && cikklekerdezesFragment.isVisible) {
                loadLoadFragment("Várom az eredményt")
                cikkItems.clear()
                polcItems.clear()
                cikklekerdezesFragment.setBinOrItem(barcodeData)
                CoroutineScope(IO).launch {
                    cikkPolcQuery(barcodeData)
                }
            }
        }
    }

    override fun onFailureEvent(p0: BarcodeFailureEvent?) {
        runOnUiThread{
            Toast.makeText(this@MainActivity, "Nem sikerült leolvasni",Toast.LENGTH_SHORT).show()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if(getMenuFragment())
        {
           when(keyCode){
               8 -> loadPolcHelyezesFragment()
               9 -> containerCheck("1GU")
               10 -> igenyKontenerCheck()
               11 -> igenyKontenerKiszedes()//Log.d(TAG, "onKeyDown: $keyCode")
               12 -> Log.d(TAG, "onKeyDown: $keyCode")
               13 -> Log.d(TAG, "onKeyDown: $keyCode")
               14 -> Log.d(TAG, "onKeyDown: $keyCode")
               15 -> Log.d(TAG, "onKeyDown: $keyCode")
               16 -> loadCikklekerdezesFragment()
           }
        }
        return super.onKeyDown(keyCode, event)
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
        if (barcodeReader != null) {
            barcodeReader?.release()
        }
        Log.d(TAG, "onPause: ")
    }

    override fun onDestroy() {
        super.onDestroy()
        polcItems.clear()
        cikkItems.clear()
        if(barcodeReader != null) {
            barcodeReader?.removeBarcodeListener(this)
            barcodeReader?.close()
        }
    }

    private fun chechIfPolcHasChanged(kontener: String): Boolean {
        Class.forName("net.sourceforge.jtds.jdbc.Driver")
        try {
            connection = DriverManager.getConnection(url)
            val statement = connection.prepareStatement(resources.getString(R.string.kontenerEllenorzes))
            statement.setString(1,kontener)
            statement.setInt(2,0)
            val resultSet = statement.executeQuery()
            return resultSet.next()
        }catch (e: Exception){
            return false
        }
    }

    private fun updateKontener(kontener_id: String){
        Class.forName("net.sourceforge.jtds.jdbc.Driver")
        try{
            connection = DriverManager.getConnection(connectionString)
            val statment = connection.prepareStatement(resources.getString(R.string.updateContainerStatus))
            statment.setString(1,kontener_id)
            statment.executeUpdate()
            Log.d(TAG, "updateCikkAndKontener: Konténer lezárva")
            lezarandoKontener = ""
        }catch (e: Exception){
            Log.d(TAG, "updateKontener: $e")
            setAlert("Probléma van\n $e")
        }
    }
    private fun updateCikk(kontener_id: String){
        Class.forName("net.sourceforge.jtds.jdbc.Driver")
        try{
            connection = DriverManager.getConnection(connectionString)
            val statement = connection.prepareStatement(resources.getString(R.string.updateItemStatus))
            statement.setString(1,kontener_id)
            statement.executeUpdate()
            Log.d(TAG, "updateCikkAndKontener: Cikkek lezárva")
        }catch (e: Exception){
            Log.d(TAG, "updateCikkAndKontener: $e")
            setAlert("Probléma van\n $e")
        }
    }
    private fun loadKontenerCikkek(kontener_id: String){
        Class.forName("net.sourceforge.jtds.jdbc.Driver")
        try {
            CoroutineScope(Main).launch {
                igenyLezarasFragment.setProgressBarOn()
            }
            connection = DriverManager.getConnection(url)
            val statement = connection.prepareStatement(resources.getString(R.string.igenyKontenerLezarasCikkLezaras))
            statement.setInt(1,kontener_id.toInt())
            val resultSet = statement.executeQuery()
            if(!resultSet.next()){
                Log.d(TAG, "loadKontenerCikkek: HIBA VAN")
                CoroutineScope(Main).launch {
                    setAlert("A konténerben nincs 0 státuszú cikk")
                    igenyLezarasFragment.setProgressBarOff()
                }
            }else{
                igenyLezarCikkVisible = true
                val kontenerCikkLezar: ArrayList<KontenerbenLezarasItem> = ArrayList()
                do {
                    val cikk = resultSet.getString("cikkszam")
                    val megj1 = resultSet.getString("Description1")
                    val megj2 = resultSet.getString("Description2")
                    val intrem = resultSet.getString("InternRem1")
                    val igeny = resultSet.getDouble("igenyelt_mennyiseg").toString() +" "+resultSet.getString("Unit")
                    val mozgatott = resultSet.getDouble("mozgatott_mennyiseg").toString()+" " + resultSet.getString("Unit")
                    kontenerCikkLezar.add(KontenerbenLezarasItem(cikk,megj1,megj2,intrem,igeny,mozgatott))
                }while (resultSet.next())
                val bundle = Bundle()
                bundle.putSerializable("CIKKLEZAR",kontenerCikkLezar)
                bundle.putString("KONTENER_ID",kontener_id)
                val cikkLezaras = IgenyKontenerLezarasCikkLezaras()
                cikkLezaras.arguments = bundle
                supportFragmentManager.beginTransaction().replace(R.id.data_frame1,cikkLezaras,"CIKKLEZARASFRAGMENT").addToBackStack(null).commit()
                CoroutineScope(Main).launch {
                    igenyLezarasFragment.setProgressBarOff()
                }
            }
        }catch (e: Exception){
            Log.d(TAG, "loadKontenerCikkek: $e")
            CoroutineScope(Main).launch {
                igenyLezarasFragment.setProgressBarOff()
            }
        }
    }
    private fun loadIgenyKiszedes(){
        Class.forName("net.sourceforge.jtds.jdbc.Driver")
        try{
            connection = DriverManager.getConnection(url)
            val statement = connection.prepareStatement(resources.getString(R.string.igenyKontenerKiszedese))
            statement.setInt(1,1)
            val resultSet = statement.executeQuery()
            if(!resultSet.next()){
                supportFragmentManager.beginTransaction().replace(R.id.frame_container,igenyKiszedesFragment,"KISZEDES").addToBackStack(null).commit()
                /*CoroutineScope(Main).launch {
                    setAlert("Nincs aktív konténer")
                }*/
            }else{
                val kontenerList: ArrayList<KontenerItem> = ArrayList()
                do{
                    val kontener: String? = resultSet.getString("kontener")
                    val polc: String? = resultSet.getString("polc")
                    val datum: String? = resultSet.getString("igenyelve")
                    val tetelszam = resultSet.getInt("tetelszam")
                    val id: String = resultSet.getString("id")
                    kontenerList.add(KontenerItem(kontener,polc,datum,tetelszam,id))
                }while(resultSet.next())
                val bundle = Bundle()
                bundle.putSerializable("KISZEDESLISTA",kontenerList)
                igenyKiszedesFragment.arguments = bundle
                supportFragmentManager.beginTransaction().replace(R.id.frame_container,igenyKiszedesFragment,"KISZEDES").addToBackStack(null).commit()
            }
        }catch (e: Exception){
            Log.d(TAG, "loadIgenyKiszedes: $e")
            setAlert("Probléma van :\n $e")
        }
    }
    private fun loadIgenyLezaras(){
        Class.forName("net.sourceforge.jtds.jdbc.Driver")
        try {
            CoroutineScope(Main).launch {
                menuFragment.setMenuProgressOn()
            }
            connection = DriverManager.getConnection(url)
            val statement = connection.prepareStatement(resources.getString(R.string.igenyKontenerLezarasKontenerBeolvas))
            val resultSet = statement.executeQuery()
            if(!resultSet.next()){
                Log.d(TAG, "loadIgenyLezaras: Nincs ilyen konténer")
            }else{
                val kontenerList: ArrayList<KontenerItem> = ArrayList()
                do {
                    val kontener: String? = resultSet.getString("kontener")
                    val polc: String? = resultSet.getString("polc")
                    val datum: String? = resultSet.getString("igenyelve")
                    val tetelszam = resultSet.getInt("tetelszam")
                    val id: String? = resultSet.getString("id")
                    kontenerList.add(KontenerItem(kontener,polc,datum,tetelszam,id))
                }while(resultSet.next())
                val bundle = Bundle()
                bundle.putSerializable("KONTENERLISTA",kontenerList)
                igenyLezarasFragment.arguments = bundle
                supportFragmentManager.beginTransaction().replace(R.id.frame_container,igenyLezarasFragment,"IGENYLEZARAS").addToBackStack(null).commit()
                CoroutineScope(Main).launch {
                    menuFragment.setMenuProgressOff()
                }
            }
        }catch (e: Exception){
            Log.d(TAG, "loadIgenyLezaras: $e")
            CoroutineScope(Main).launch {
                setAlert("Hálózati probléma! Próbáld újra")
                menuFragment.setMenuProgressOff()
            }
        }
    }
    private fun closeContainerSql(statusz: Int, datum: String){
        Class.forName("net.sourceforge.jtds.jdbc.Driver")
        try {
            connection = DriverManager.getConnection(connectionString)
            val statement = connection.prepareStatement(resources.getString(R.string.closeContainer))
            statement.setInt(1,statusz)
            statement.setString(2,datum)
            statement.setString(3,kontener)
            statement.executeUpdate()
            Log.d(TAG, "closeContainerSql: sikeres lezárás")
            CoroutineScope(Main).launch {
                setAlert("Sikeres konténer lezárás!")
            }
            val statement1 = connection.prepareStatement(resources.getString(R.string.updateItemStatus))
            statement1.setString(1,kontener)
            try{
                statement1.executeUpdate()
            }catch (e: Exception){
                Log.d(TAG, "closeContainerSql: $e")
                CoroutineScope(Main).launch {
                    setAlert("A cikk státuszok felülírásánál hiba lépett fel, gyere az IT-re")
                }
            }
        }catch (e: Exception){
            Log.d(TAG, "closeContainerSql: $e")
        }
    }
    private fun uploadItem(cikk: String, menny: Double, term: String, unit: String){
        Class.forName("net.sourceforge.jtds.jdbc.Driver")
        try{
            connection = DriverManager.getConnection(connectionString)
            val statement = connection.prepareStatement(resources.getString(R.string.insertItem))
            statement.setString(1,kontener)
            statement.setString(2,cikk)
            statement.setInt(3,0) //ez a státusz
            statement.setDouble(4,menny)
            statement.setInt(5,0)
            statement.setString(6,"01")
            statement.setString(7,term)
            statement.setString(8,unit)
            statement.executeUpdate()
        }catch (e: Exception){
            Log.d(TAG, "uploadItem: $e")
            CoroutineScope(Main).launch {
                setAlert("Hiba történt, lépj vissza a 'Kilépés' gombbal a menübe, majd vissza, hogy megnézd mi lett utoljára felvéve")
            }
        }
    }
    private fun checkItem(code: String){
        CoroutineScope(Main).launch {
            igenyFragment.setProgressBarOn()
        }
        Class.forName("net.sourceforge.jtds.jdbc.Driver")
        try{
            connection = DriverManager.getConnection(url)
            val statement = connection.prepareStatement(resources.getString(R.string.cikkSql))
            statement.setString(1,code)
            val resultSet = statement.executeQuery()
            if(!resultSet.next()){
                CoroutineScope(Main).launch {
                    setAlert("Nincs ilyen cikk a rendszerben")
                    igenyFragment.setProgressBarOff()
                    igenyFragment.setFocusToItem()
                }
            }else{
                val megjegyzesIgeny: String = resultSet.getString("Description1")
                val megjegyzes2Igeny: String = resultSet.getString("Description2")
                val intremIgeny: String = resultSet.getString("IntRem")
                val unitIgeny: String = resultSet.getString("Unit")
                CoroutineScope(Main).launch {
                    igenyFragment.setInfo(megjegyzesIgeny,megjegyzes2Igeny,intremIgeny,unitIgeny)
                    igenyFragment.setProgressBarOff()
                    igenyFragment.setFocusToQuantity()
                }
            }
        }catch (e: Exception){
            Log.d(TAG, "checkItem: $e")
            CoroutineScope(Main).launch {
                igenyFragment.setProgressBarOff()
            }
        }
    }

    private fun check01(code: String){
        CoroutineScope(Main).launch {
            igenyFragment.setProgressBarOn()
        }
        Class.forName("net.sourceforge.jtds.jdbc.Driver")
        try{
            connection = DriverManager.getConnection(connectionString)
            val statement = connection.prepareStatement(resources.getString(R.string.is01))
            statement.setString(1,code)
            val resultSet = statement.executeQuery()
            if(!resultSet.next()){
                CoroutineScope(Main).launch {
                    setAlert("A polc nem a 01 raktárban található")
                    igenyFragment.setBinFocusOn()
                    igenyFragment.setProgressBarOff()
                }
            }else{
                val statement1 = connection.prepareStatement(resources.getString(R.string.updateBin))
                statement1.setString(1,code)
                statement1.setString(2,dolgKod)
                statement1.setString(3,"0")
                statement1.executeUpdate()
                CoroutineScope(Main).launch {
                    igenyFragment.setFocusToItem()
                    igenyFragment.setProgressBarOff()
                }
            }
        }catch (e: Exception){
            Log.d(TAG, "check01: $e")
            CoroutineScope(Main).launch {
                igenyFragment.setProgressBarOff()
            }
        }
    }

    private fun checkPolc(code: String){
        Class.forName("net.sourceforge.jtds.jdbc.Driver")
        try{
            connection= DriverManager.getConnection(url)
            val statement: PreparedStatement = connection.prepareStatement(resources.getString(R.string.isPolc))
            statement.setString(1,code)
            val resultSet: ResultSet = statement.executeQuery()
            if(!resultSet.next()){
                CoroutineScope(Main).launch {
                    setAlert("Nem polc")
                    polcHelyezesFragment.focusToBin()
                }
            }else{
                CoroutineScope(Main).launch {
                    polcHelyezesFragment.polcCheck()
                }
            }
        }catch (e: Exception){
            Log.d(TAG, "checkPolc: visszajött hibával")
        }
    }

    private fun checkRightSql(code: String){
        Class.forName("net.sourceforge.jtds.jdbc.Driver")
        try{
            connection = DriverManager.getConnection(connectionString)
            val statement : PreparedStatement = connection.prepareStatement(resources.getString(R.string.jog))
            statement.setString(1,code)
            val resultSet : ResultSet = statement.executeQuery()
            if (!resultSet.next()){
                Log.d(TAG, "checkRightSql: hülyeséggel lép be")
                loadMenuFragment(false)
            }
            else{
            if (resultSet.getInt("Jog") == 1) {
                loadMenuFragment(true)
            } else {
                loadMenuFragment(false)
                }
            }
        }catch (e : Exception)
        {
            Log.d(TAG, "Nincs kapcsolat")
            CoroutineScope(Main).launch {
                loginFragment.StopSpinning()
                loginFragment.SetId("Hiaba lépett fel a feldolgozás során")
            }
        }
    }

    private fun checkTrannzit(code: String){
        Class.forName("net.sourceforge.jtds.jdbc.Driver")
        try {
            CoroutineScope(Main).launch {
                polcHelyezesFragment.setContainerOff()
                polcHelyezesFragment.setProgressBarOn()
            }
            removeLocationFragment()
            polcLocation?.clear()
            connection = DriverManager.getConnection(url)
            val statement: PreparedStatement = connection.prepareStatement(resources.getString(R.string.tranzitCheck))
            statement.setString(1,code)
            val resultSet: ResultSet = statement.executeQuery()
            if(!resultSet.next()){
                Log.d(TAG, "checkTrannzit: Hülyeség nincs a tranzitban")
                CoroutineScope(Main).launch {
                    setAlert("A cikk vagy zárolt, vagy nincs a tranzit raktárban!")
                    polcHelyezesFragment.setProgressBarOff()
                }
            }
            else{
                val desc1: String? = resultSet.getString("Description1")
                val desc2: String? = resultSet.getString("Description2")
                val intRem: String? = resultSet.getString("InternRem1")
                val unit: String? = resultSet.getString("Description")
                val balance: Int? = resultSet.getInt("BalanceQty")
                Log.d(TAG, "checkTrannzit: 0")
                CoroutineScope(Main).launch {
                   polcHelyezesFragment.setTextViews(desc1.toString(),desc2.toString(),intRem.toString(),unit.toString(),balance.toString())
                   polcHelyezesFragment.focusToQty()
                   Log.d(TAG, "checkTrannzit: 1")
                }
                Log.d(TAG, "checkTrannzit: 2")
                val statement1: PreparedStatement = connection.prepareStatement(resources.getString(R.string.raktarCheck))
                statement1.setString(1,code)
                val resultSet1: ResultSet = statement1.executeQuery()
                if (!resultSet1.next()){
                    CoroutineScope(Main).launch {
                        polcHelyezesFragment.setProgressBarOff()
                       // polcHelyezesFragment.setContainerOn()
                    }
                    Log.d(TAG, "checkTrannzit: Nincs a 02-es raktárban")
                    //ezt aztán kitörölni
                    polcLocation?.add(PolcLocation("H221","151"))
                    polcLocation?.add(PolcLocation("H222","152"))
                    polcLocation?.add(PolcLocation("H223","153"))
                    polcLocation?.add(PolcLocation("H224","154"))
                    polcLocation?.add(PolcLocation("H225","155"))
                    polcLocation?.add(PolcLocation("H226","156"))
                    val bundle = Bundle()
                    bundle.putSerializable("02RAKTAR",polcLocation)
                    polcLocationFragment.arguments = bundle
                    //supportFragmentManager.beginTransaction().replace(R.id.side_container,polcLocationFragment,"LOC").commit()
                }
                else{
                    CoroutineScope(Main).launch {
                        polcHelyezesFragment.setContainerOn()
                        polcHelyezesFragment.setProgressBarOff()
                    }
                    do{
                        val binNumber: String? = resultSet1.getString("BinNumber")
                        val balanceQty: Int? = resultSet1.getInt("BalanceQty")
                        polcLocation?.add(PolcLocation(binNumber,balanceQty.toString()))
                    }while (resultSet1.next())
                    val bundle = Bundle()
                    bundle.putSerializable("02RAKTAR",polcLocation)
                    polcLocationFragment.arguments = bundle
                    supportFragmentManager.beginTransaction().replace(R.id.side_container,polcLocationFragment,"LOC").commit()
                }
            }
        }catch (e: java.lang.Exception){
            Log.d(TAG, "checkTrannzit: $e")
            CoroutineScope(Main).launch {
                polcHelyezesFragment.setProgressBarOff()
            }
        }
    }

    private fun containerManagement(id: String){
        Class.forName("net.sourceforge.jtds.jdbc.Driver")
        try{
            CoroutineScope(Main).launch {
                menuFragment.setMenuProgressOn()
            }
            connection = DriverManager.getConnection(connectionString)
            val isContainer = connection.prepareStatement(resources.getString(R.string.containerCheck))
            isContainer.setString(1,id)
            isContainer.setInt(2,0)
            val containerResult = isContainer.executeQuery()
            if(!containerResult.next()){
                Log.d(TAG, "containerManagement: Nincs konténer")
                val insertContainer = connection.prepareStatement(resources.getString(R.string.openContainer))
                insertContainer.setString(1,id)
                insertContainer.setInt(2,0)
                insertContainer.setInt(3,1)
                insertContainer.setString(4,"01")
                insertContainer.executeUpdate()
                Log.d(TAG, "containerManagement: Konténer létrehozva")
                try{
                    Log.d(TAG, "containerManagement: Betöltöm az adatot")
                    val getName = connection.prepareStatement(resources.getString(R.string.containerCheck))
                    getName.setString(1,id)
                    getName.setInt(2,0)
                    val getNameResult = isContainer.executeQuery()
                    if(!getNameResult.next()){
                        CoroutineScope(Main).launch {
                            setAlert("Valami nagy hiba van")
                        }
                    }else{
                        var nullasKontener: String = getNameResult.getInt("id").toString()
                        var zeroString = ""
                        if(nullasKontener.length<10){
                            val charLength = 10 - nullasKontener.length
                            for(i in 0 until charLength){
                                zeroString += "0"
                            }
                            nullasKontener = """$zeroString$nullasKontener"""
                        }
                        val updateContainer = connection.prepareStatement(resources.getString(R.string.updateContainerValue))
                        updateContainer.setString(1,nullasKontener)
                        updateContainer.setString(2,id)
                        updateContainer.setInt(3,0)
                        updateContainer.executeUpdate()
                        Log.d(TAG, "containerManagement: visszaírtam a konténer értéket")
                        val bundle = Bundle()
                        bundle.putString("KONTENER",nullasKontener)
                        igenyFragment.arguments = bundle
                        supportFragmentManager.beginTransaction().replace(R.id.frame_container,igenyFragment).addToBackStack(null).commit()
                        CoroutineScope(Main).launch {
                            menuFragment.setMenuProgressOff()
                        }
                    }
                }catch (e: Exception){
                    Log.d(TAG, "containerManagement: $e")
                    CoroutineScope(Main).launch {
                        menuFragment.setMenuProgressOff()
                    }
                }
            }else{
                Log.d(TAG, "containerManagement: van konténer")
                val id1 = containerResult.getInt("id")
                kontener = containerResult.getString("kontener")
                val rakhely:String? = containerResult.getString("termeles_rakhely")
                Log.d(TAG, "containerManagement: $rakhely")
                val igenyItemCheck = connection.prepareStatement(resources.getString(R.string.loadIgenyItemsToList))
                igenyItemCheck.setInt(1,id1)//ez a számot át kell írni majd a "kontener"-re
                val loadIgenyListResult = igenyItemCheck.executeQuery()
                if(!loadIgenyListResult.next()){
                    Log.d(TAG, "containerManagement: Üres")
                    val bundle1 = Bundle()
                    bundle1.putString("KONTENER",kontener)
                    bundle1.putString("TERMRAKH",rakhely)
                    igenyFragment.arguments = bundle1
                    supportFragmentManager.beginTransaction().replace(R.id.frame_container,igenyFragment).addToBackStack(null).commit()
                    CoroutineScope(Main).launch {
                        menuFragment.setMenuProgressOff()
                    }
                }else{
                    val listIgenyItems: ArrayList<IgenyItem> = ArrayList()
                    do {
                        val cikk = loadIgenyListResult.getString("cikkszam")
                        val megjegyzes = loadIgenyListResult.getString("megjegyzes")
                        val darabszam = loadIgenyListResult.getString("igenyelt_mennyiseg")
                        listIgenyItems.add(IgenyItem(cikk, megjegyzes, darabszam))
                    }while(loadIgenyListResult.next())
                    val bundle = Bundle()
                        bundle.putSerializable("IGENY",listIgenyItems)
                        bundle.putString("KONTENER", kontener)
                        bundle.putString("TERMRAKH", rakhely)
                    igenyFragment.arguments = bundle
                        supportFragmentManager.beginTransaction().replace(R.id.frame_container,igenyFragment).addToBackStack(null).commit()
                    CoroutineScope(Main).launch {
                        menuFragment.setMenuProgressOff()
                    }
                }
            }
        }catch (e: Exception){
            CoroutineScope(Main).launch{
                setAlert("Valahol baj van $e")
                menuFragment.setMenuProgressOff()
            }
        }
    }

    private fun cikkPolcQuery(code : String) {
        val polcResultFragment = PolcResultFragment()
        val cikkResultFragment = CikkResultFragment()
        val bundle = Bundle()
        Class.forName("net.sourceforge.jtds.jdbc.Driver")
        try {
            connection = DriverManager.getConnection(url)
            val preparedStatement: PreparedStatement = connection.prepareStatement(resources.getString(R.string.isPolc))
            preparedStatement.setString(1,code)
            val resultSet: ResultSet = preparedStatement.executeQuery()
            if(!resultSet.next()){
                val preparedStatement1: PreparedStatement = connection.prepareStatement(resources.getString(R.string.cikkSql))
                preparedStatement1.setString(1,code)
                val resultSet1: ResultSet = preparedStatement1.executeQuery()
                if(!resultSet1.next()){
                    val loadFragment = LoadFragment.newInstance("Nincs ilyen kód a rendszerben")
                    supportFragmentManager.beginTransaction().replace(R.id.cikk_container,loadFragment).commit()
                }else{
                    val megjegyzes1: String? = resultSet1.getString("Description1")
                    val megjegyzes2: String? = resultSet1.getString("Description2")
                    val unit: String? = resultSet1.getString("Unit")
                    val intrem: String? = resultSet1.getString("IntRem")
                    cikkItems.clear()
                    do{
                        cikkItems.add(CikkItems(resultSet1.getDouble("BalanceQty"),resultSet1.getString("BinNumber"), resultSet1.getString("Warehouse"), resultSet1.getString("QcCategory")))
                    }while (resultSet1.next())
                    bundle.putSerializable("cikk",cikkItems)
                    bundle.putString("megjegyzes", megjegyzes1)
                    bundle.putString("megjegyzes2", megjegyzes2)
                    bundle.putString("unit", unit)
                    bundle.putString("intrem", intrem)
                    cikkResultFragment.arguments = bundle
                    supportFragmentManager.beginTransaction().replace(R.id.cikk_container,cikkResultFragment).commit()
                }
            }
            else{
                val preparedStatement2: PreparedStatement = connection.prepareStatement(resources.getString(R.string.polcSql))
                preparedStatement2.setString(1,code)
                val resultSet2: ResultSet = preparedStatement2.executeQuery()
                if(!resultSet2.next()){
                    val loadFragment = LoadFragment.newInstance("A polc üres")
                    supportFragmentManager.beginTransaction().replace(R.id.cikk_container,loadFragment).commit()
                }else{
                    do {
                        polcItems.add(PolcItems(resultSet2.getDouble("BalanceQty"), resultSet2.getString("Unit"), resultSet2.getString("Description1"), resultSet2.getString("Description2"), resultSet2.getString("IntRem"), resultSet2.getString("QcCategory")))

                    }while (resultSet2.next())
                    bundle.putSerializable("polc",polcItems)
                    polcResultFragment.arguments = bundle
                    supportFragmentManager.beginTransaction().replace(R.id.cikk_container,polcResultFragment).commit()
                }
            }
        }catch (e: Exception){
            Log.d(TAG, "$e")
            val loadFragment = LoadFragment.newInstance("A feldolgozás során hiba lépett fel")
            supportFragmentManager.beginTransaction().replace(R.id.cikk_container,loadFragment).commit()
        }
    }

    fun setAlert(text: String){
        val builder = AlertDialog.Builder(this@MainActivity)
        builder.setTitle("Figyelem")
            .setMessage(text)
        builder.create()
        builder.show()
    }

    override fun setValue(value: String) {
        if(value.isNotEmpty()) {
            loadLoadFragment("Várom az eredményt")
            cikkItems.clear()
            polcItems.clear()
            cikklekerdezesFragment.setBinOrItem(value)
            CoroutineScope(IO).launch {
                cikkPolcQuery(value)
            }
        }else{
            loadLoadFragment("")
        }
    }

    override fun sendCode(code: String) {
        CoroutineScope(IO).launch {
            checkTrannzit(code)
        }
    }
    private fun removeLocationFragment(){
        val isLocFragment = supportFragmentManager.findFragmentByTag("LOC")
        if(isLocFragment != null && isLocFragment.isVisible){
            supportFragmentManager.beginTransaction().remove(isLocFragment).commit()
        }
    }

    override fun setPolcLocation(binNumber: String?,selected: Boolean,position: Int) {
        polcHelyezesFragment.setBinNumber(binNumber)
        polcHelyezesFragment.getAll(selected,position,binNumber)
        polcHelyezesFragment.focusToBin()
    }
    fun setRecOn(){
        polcLocationFragment.setRecyclerOn()
    }

    fun setRecData(position: Int, value: Double){
        polcLocationFragment.getDataFromList(position,value)
    }

    fun checkIfContainsBin(falseBin: String, value: Double){
            polcLocationFragment.checkBinIsInTheList(falseBin, value)
    }

    fun polcCheckIO(code: String){
        CoroutineScope(IO).launch {
            checkPolc(code)
        }
    }

    override fun sendBinCode(code: String) {
       CoroutineScope(IO).launch {
           check01(code)
       }
    }

    override fun sendDetails(
        cikkszam: String,
        mennyiseg: Double,
        term_rakhely: String,
        unit: String
    ) {
        CoroutineScope(IO).launch {
            uploadItem(cikkszam,mennyiseg,term_rakhely,unit)
        }
    }

    override fun closeContainer(statusz: Int, datum: String) {
        CoroutineScope(IO).launch {
            closeContainerSql(statusz,datum)
        }
    }

    fun isItem(code: String){
        CoroutineScope(IO).launch {
            checkItem(code)
        }
    }
    fun checkList(code: String):Boolean{
        return polcLocationFragment.checkList(code)
    }
    private fun containerCheck(id: String){
        CoroutineScope(IO).launch {
            containerManagement(id)
        }
    }
    fun igenyKontenerCheck(){
        CoroutineScope(IO).launch {
            loadIgenyLezaras()
            Log.d(TAG, "igenyKontenerCheck: Lefutott")
        }
    }

    override fun sendContainer(container: String) {
        lezarandoKontener = container
        CoroutineScope(IO).launch {
            loadKontenerCikkek(container)
        }
    }
    fun closeContainerAndItem(){
        CoroutineScope(IO).launch {
            if(chechIfPolcHasChanged(lezarandoKontener)) {
                updateCikk(lezarandoKontener)
                updateKontener(lezarandoKontener)
            }else{
                igenyKontenerCheck()
                CoroutineScope(Main).launch {
                    setAlert("A konténer státusza már megváltozott")
                }
            }
        }
    }

    private fun igenyKontenerKiszedes(){
        CoroutineScope(IO).launch {
            loadIgenyKiszedes()
        }
    }

    override fun onBackPressed() {

        if(getIgenyLezaras()) {
            if (igenyLezarCikkVisible) {
                igenyLezarCikkVisible = false
                loadMenuFragment(true)
                CoroutineScope(IO).launch {
                    loadIgenyLezaras()
                }
            }
        }
        super.onBackPressed()
    }
}