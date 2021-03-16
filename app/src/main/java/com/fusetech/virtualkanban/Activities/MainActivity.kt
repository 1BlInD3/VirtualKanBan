package com.fusetech.virtualkanban.Activities

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.fusetech.virtualkanban.DataItems.CikkItems
import com.fusetech.virtualkanban.DataItems.PolcItems
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

class MainActivity : AppCompatActivity(), BarcodeListener,CikklekerdezesFragment.SetItemOrBinManually {

    private var manager : AidcManager? = null
    private var barcodeReader : BarcodeReader? = null
    private lateinit var barcodeData : String
    private lateinit var loginFragment : LoginFragment
    private lateinit var dolgKod : String
    private lateinit var connection : Connection
    private var cikkItems: ArrayList<CikkItems> = ArrayList()
    private var polcItems: ArrayList<PolcItems> = ArrayList()
    private val TAG = "MainActivity"
    private val cikklekerdezesFragment = CikklekerdezesFragment()
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
    private fun getMenuFragment(): Boolean
    {
        val fragmentManager = supportFragmentManager
        val menuFragment = fragmentManager.findFragmentByTag("MENU")
        if(menuFragment != null && menuFragment.isVisible)
        {
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
    private fun loadPolcHelyezesFragment(){
        val polcHelyezesFragment = PolcraHelyezesFragment()
        supportFragmentManager.beginTransaction().replace(R.id.frame_container,polcHelyezesFragment,"POLC").addToBackStack("POLC").commit()
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
                        val loadFragment = LoadFragment()
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
}