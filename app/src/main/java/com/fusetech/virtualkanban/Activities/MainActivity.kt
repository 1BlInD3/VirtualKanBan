package com.fusetech.virtualkanban.Activities

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.fusetech.virtualkanban.Fragments.CikklekerdezesFragment
import com.fusetech.virtualkanban.Fragments.LoginFragment
import com.fusetech.virtualkanban.Fragments.MenuFragment
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

    private lateinit var manager : AidcManager
    private lateinit var barcodeReader : BarcodeReader
    private lateinit var barcodeData : String
    private lateinit var loginFragment : LoginFragment
    private lateinit var dolgKod : String
    private lateinit var connection : Connection
    private val TAG = "MainActivity"
    private val url = "jdbc:jtds:sqlserver://10.0.0.11;databaseName=Fusetech;user=scala_read;password=scala_read;loginTimeout=10"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
        AidcManager.create(this) { aidcManager ->
            manager = aidcManager
            try {
                barcodeReader = manager.createBarcodeReader()
                barcodeReader.claim()
            } catch (e: ScannerUnavailableException) {
                e.printStackTrace()
            } catch (e: InvalidScannerNameException) {
                e.printStackTrace()
            }
            try {
                barcodeReader.setProperty(BarcodeReader.PROPERTY_CODE_39_ENABLED, true)
                barcodeReader.setProperty(BarcodeReader.PROPERTY_DATAMATRIX_ENABLED, true)
                barcodeReader.setProperty(
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
            barcodeReader.addBarcodeListener(this@MainActivity)
        }
        loginFragment = LoginFragment()
        supportFragmentManager.beginTransaction().replace(R.id.frame_container, loginFragment,"LOGIN").commit()

    }
    private fun getMenuFragment(): Boolean
    {
        val fragmentManager = supportFragmentManager
        var menuFragment = fragmentManager.findFragmentByTag("MENU")
        if(menuFragment != null && menuFragment?.isVisible!!)
        {
            return true
        }
        return false
    }
    fun loadMenuFragment(hasRight : Boolean?){
        val menuFragment : MenuFragment = MenuFragment.newInstance(hasRight)
        supportFragmentManager.beginTransaction().replace(R.id.frame_container, menuFragment,"MENU").commit()
    }
    fun loadCikklekerdezesFragment(){
        val cikklekerdezesFragment = CikklekerdezesFragment()
        supportFragmentManager.beginTransaction().replace(R.id.frame_container, cikklekerdezesFragment,"CIKK").addToBackStack(null).commit()
    }
    override fun onBarcodeEvent(p0: BarcodeReadEvent?) {
        runOnUiThread{
            barcodeData = p0?.barcodeData!!
            if (loginFragment.isVisible) {
                loginFragment.SetId(barcodeData)
                dolgKod = barcodeData
                loginFragment.StartSpinning()
                CoroutineScope(IO).launch {
                    checkRightSql()
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
               8 -> Log.d(TAG, "onKeyDown: $keyCode")
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

    override fun setValue(value: String) {

    }
    /*override fun onResume() {
       super.onResume()
           barcodeReader = manager.createBarcodeReader()
           try {
               barcodeReader.claim()
           } catch (e: ScannerUnavailableException) {
               e.printStackTrace()
               Toast.makeText(this, "Scanner unavailable", Toast.LENGTH_SHORT).show()
           }
   }*/
    /* override fun onPause() {
        super.onPause()
            barcodeReader.release()

    }*/
    /* override fun onDestroy() {
        super.onDestroy()
        barcodeReader.removeBarcodeListener(this)
        barcodeReader.close()
    }*/
    /*
     private fun sql()
     {
         Class.forName("net.sourceforge.jtds.jdbc.Driver")
         val connection = DriverManager.getConnection(URL)
         if(connection!=null){
             val statement : Statement = connection.createStatement()
             var resultSet : ResultSet = statement.executeQuery(resources.getString(R.string.allData))
             while (resultSet.next())
             {
                 var a = resultSet.getString("Cikkszam")
                 var b = resultSet.getString("Mennyiseg")
                 var c = resultSet.getString("Dolgozo")
                 var d = resultSet.getString("RaktHely")
                 myList.add(
                     ProbaClass(
                         a,
                         b,
                         c,
                         d
                     )
                 )

             }
             var bundle = Bundle()
             bundle.putSerializable("Lista",myList)
             val firstKotlinFragment  = FirstKotlinFragment()
             firstKotlinFragment.arguments = bundle
             supportFragmentManager.beginTransaction().replace(R.id.frame_container,firstKotlinFragment).commit()
         }
     }*/
}