package com.fusetech.virtualkanban.Activities

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.KeyEvent
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.fusetech.virtualkanban.DataItems.CikkItems
import com.fusetech.virtualkanban.DataItems.PolcItems
import com.fusetech.virtualkanban.DataItems.PolcLocation
import com.fusetech.virtualkanban.Fragments.*
import com.fusetech.virtualkanban.R
import com.honeywell.aidc.*
import com.honeywell.aidc.BarcodeReader.BarcodeListener
import kotlinx.android.synthetic.main.fragment_menu.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet

class MainActivity : AppCompatActivity(), BarcodeListener,CikklekerdezesFragment.SetItemOrBinManually,PolcraHelyezesFragment.SendCode,PolcLocationFragment.SetPolcLocation {
// 1-es pont beviszem a cikket, és megnézi hogy van e a tranzit raktárban (3as raktár)szabad(ha zárolt akkor szól, ha nincs akkor szól)
    //ha van és szabad is, nézzük meg hogy hol vannak ilyenek FIFO szerint, vagy választ a listából, vagy felvisz egy újat, lehetőség ha nem fér fel rá és
    // át kell rakni máshova
    //egyszerre csak egy ember dolgozhasson a cikk felrakásánál

    //TELKES timi gépét a tarcsira


    //2) megnézem, hogy van e konténer a "atado" és "statusz = 0"
    // amikor megnyitom az igény konténer összeállítását, akkor [Leltar].[dbo]. kontener-be beírom a atado(1GU),statusz(0),kontener_tipus(1)
    //kontener 0000+id (összvissz 10karakterig)
    //aztán megjelenítem a "kontener"

    //A polcál csak a 01-es raktárokat fogadja el (ilyen van a polcCheck stringbe) és ha jó akkor beírja a [Leltar].[dbo]. kontener-be termeles_raktar = 01, termeles_rakhely = polc
    // jön a cikk (megnézzük h van e), beírjuk a 4dolgot mint mindig
    // mennyiség elfogadása enterrel, kéri a következő cikket ÉS beleír a [Leltar].[dbo].kontener_tetel-be (fénykép)
    // a [Leltar].[dbo]. kontener beíródik a statusz = 1, igenyelve = datetime

    private var manager : AidcManager? = null
    private var barcodeReader : BarcodeReader? = null
    private lateinit var barcodeData : String
    private lateinit var loginFragment : LoginFragment
    private lateinit var dolgKod : String
    private lateinit var connection : Connection
    private var cikkItems: ArrayList<CikkItems> = ArrayList()
    private var polcItems: ArrayList<PolcItems> = ArrayList()
    private val polcHelyezesFragment = PolcraHelyezesFragment()
    private val TAG = "MainActivity"
    private val cikklekerdezesFragment = CikklekerdezesFragment()
    val polcLocationFragment = PolcLocationFragment()
    private var polcLocation: ArrayList<PolcLocation>? = ArrayList()
    private val url = "jdbc:jtds:sqlserver://10.0.0.11;databaseName=Fusetech;user=scala_read;password=scala_read;loginTimeout=10"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
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
    fun loadMenuFragment(hasRight : Boolean?){
        val menuFragment : MenuFragment = MenuFragment.newInstance(hasRight)
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

    fun loadPolcLocation(){
        val loadPolc = PolcLocationFragment()
        supportFragmentManager.beginTransaction().replace(R.id.side_container,loadPolc,"LOCATION").commit()
    }
    override fun onBarcodeEvent(p0: BarcodeReadEvent?) {
        runOnUiThread{
            barcodeData = p0?.barcodeData!!
            if (loginFragment != null && loginFragment.isVisible) {
                loginFragment.SetId(barcodeData)
                dolgKod = barcodeData
                loginFragment.StartSpinning()
                CoroutineScope(IO).launch {
                    checkRightSql()
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
               9 -> Log.d(TAG, "onKeyDown: $keyCode")
               10 -> Log.d(TAG, "onKeyDown: $keyCode")
               11 -> Log.d(TAG, "onKeyDown: $keyCode")
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

    private fun checkRightSql(){
        Class.forName("net.sourceforge.jtds.jdbc.Driver")
        try{
            connection = DriverManager.getConnection(url)
            val statement : PreparedStatement = connection.prepareStatement(resources.getString(R.string.jog))
            statement.setString(1,barcodeData)
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
                        polcHelyezesFragment.setContainerOn()
                    }
                    Log.d(TAG, "checkTrannzit: Nincs a 02-es raktárban")
                    //ezt aztán kitörölni
                    polcLocation?.add(PolcLocation("H221","151"))
                    polcLocation?.add(PolcLocation("H222","152"))
                    polcLocation?.add(PolcLocation("H223","153"))
                    polcLocation?.add(PolcLocation("H224","154"))
                    polcLocation?.add(PolcLocation("H225","155"))
                    polcLocation?.add(PolcLocation("H226","156"))
                    var bundle = Bundle()
                    bundle.putSerializable("02RAKTAR",polcLocation)
                    polcLocationFragment.arguments = bundle
                    supportFragmentManager.beginTransaction().replace(R.id.side_container,polcLocationFragment,"LOC").commit()
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
                    var bundle: Bundle = Bundle()
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

    private fun cikkPolcQuery(code : String) {
        val polcResultFragment = PolcResultFragment()
        val cikkResultFragment = CikkResultFragment()
        val bundle = Bundle()
        Class.forName("net.sourceforge.jtds.jdbc.Driver")
        try {
            connection = DriverManager.getConnection(url)
            if (connection != null){
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

    fun setRecData(position: Int, value: Int){
        polcLocationFragment.getDataFromList(position,value)
    }

    fun checkIfContainsBin(falseBin: String, value: Int){
        polcLocationFragment.checkBinIsInTheList(falseBin, value)
    }

    fun polcCheckIO(code: String){
        CoroutineScope(IO).launch {
            checkPolc(code)
        }
    }
}