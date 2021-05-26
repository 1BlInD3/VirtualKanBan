package com.fusetech.virtualkanban.Utils

import android.util.Log
import com.fusetech.virtualkanban.Activities.MainActivity
import com.fusetech.virtualkanban.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.sql.Connection
import java.sql.DriverManager
import com.fusetech.virtualkanban.Activities.MainActivity.Companion.connectionString
import com.fusetech.virtualkanban.Activities.MainActivity.Companion.res
import com.fusetech.virtualkanban.Fragments.PolcraHelyezesFragment.Companion.myItems
import com.fusetech.virtualkanban.DataItems.PolcLocation
import kotlinx.coroutines.Dispatchers.IO
import java.sql.PreparedStatement
import java.sql.ResultSet
private const val TAG = "SQL"
 class SQL (val sqlMessage: SQLAlert){

    interface SQLAlert{
        fun sendMessage(message: String)
    }
    fun deleteKontenerRaktarTetel(konenerTetelId: String){
        var connection: Connection
        CoroutineScope(IO).launch {
            Class.forName("net.sourceforge.jtds.jdbc.Driver")
            try{
                connection = DriverManager.getConnection(connectionString)
                val statement = connection.prepareStatement(res.getString(R.string.deleteEmptyRaktarTetel))
                statement.setString(1,konenerTetelId)
                statement.executeUpdate()
            }catch (e: Exception){
                sqlMessage.sendMessage("Hiba van az üres letörlésénél $e")
            }
        }
    }
    fun checkRightSql(code: String, context: MainActivity) {
        val connection: Connection
        Class.forName("net.sourceforge.jtds.jdbc.Driver")
        try {
            connection = DriverManager.getConnection(connectionString)
            val statement: PreparedStatement =
                connection.prepareStatement(res.getString(R.string.jog))
            statement.setString(1, code)
            val resultSet: ResultSet = statement.executeQuery()
            if (!resultSet.next()) {
                Log.d(TAG, "checkRightSql: hülyeséggel lép be")
                context.loadMenuFragment(false)
            } else {
                if (resultSet.getInt("Jog") == 1) {
                    context.loadMenuFragment(true)
                } else {
                    context.loadMenuFragment(false)
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "Nincs kapcsolat")
            CoroutineScope(Dispatchers.Main).launch {
                context.loginFragment.StopSpinning()
                context.loginFragment.SetId("Hiaba lépett fel a feldolgozás során")
            }
        }
    }
     fun checkTrannzit(code: String, context: MainActivity, polcLocation: ArrayList<PolcLocation>?) {
         val connection: Connection
         Class.forName("net.sourceforge.jtds.jdbc.Driver")
         try {
             CoroutineScope(Dispatchers.Main).launch {
                 context.polcHelyezesFragment.setProgressBarOn()
             }
             context.removeLocationFragment()
             polcLocation?.clear()
             connection = DriverManager.getConnection(MainActivity.url)
             val statement: PreparedStatement =
                 connection.prepareStatement(res.getString(R.string.tranzitCheck))
             statement.setString(1, code)
             val resultSet: ResultSet = statement.executeQuery()
             if (!resultSet.next()) {
                 Log.d(TAG, "checkTrannzit: Hülyeség nincs a tranzitban")
                 CoroutineScope(Dispatchers.Main).launch {
                     context.setAlert("A cikk vagy zárolt, vagy nincs a tranzit raktárban!")
                     context.polcHelyezesFragment.setProgressBarOff()
                 }
             } else {//ha van a tranzitba
                 val desc1: String? = resultSet.getString("Description1")
                 val desc2: String? = resultSet.getString("Description2")
                 val intRem: String? = resultSet.getString("InternRem1")
                 val unit: String? = resultSet.getString("Description")
                 val balance: Int = resultSet.getInt("BalanceQty")
                 Log.d(TAG, "checkTrannzit: 0")
                 CoroutineScope(Dispatchers.Main).launch {
                     context.polcHelyezesFragment.setTextViews(
                         desc1.toString(),
                         desc2.toString(),
                         intRem.toString(),
                         unit.toString(),
                         balance.toString()
                     )
                     context.polcHelyezesFragment.focusToQty()
                     Log.d(TAG, "checkTrannzit: 1")
                 }
                 Log.d(TAG, "checkTrannzit: 2")
                 val statement1: PreparedStatement =
                     connection.prepareStatement(res.getString(R.string.raktarCheck))
                 statement1.setString(1, code)
                 val resultSet1: ResultSet = statement1.executeQuery()
                 if (!resultSet1.next()) {
                     CoroutineScope(Dispatchers.Main).launch {
                         context.polcHelyezesFragment.setProgressBarOff()
                     }
                     Log.d(TAG, "checkTrannzit: Nincs a 02-es raktárban")
                 } else {
                     CoroutineScope(Dispatchers.Main).launch {
                         context.polcHelyezesFragment.setProgressBarOff()
                     }
                     do {
                         val binNumber: String? = resultSet1.getString("BinNumber")
                         val balanceQty: Int = resultSet1.getInt("BalanceQty")
                         myItems.add(PolcLocation(binNumber, balanceQty.toString()))
                     } while (resultSet1.next())
                     context.polcHelyezesFragment.reload()
                 }
             }
         } catch (e: Exception) {
             Log.d(TAG, "checkTrannzit: $e")
             CoroutineScope(Dispatchers.Main).launch {
                 context.polcHelyezesFragment.setProgressBarOff()
             }
         }
     }
     fun checkPolc(code: String,context: MainActivity) {
         val connection: Connection
         Class.forName("net.sourceforge.jtds.jdbc.Driver")
         try {
             connection = DriverManager.getConnection(MainActivity.url)
             val statement: PreparedStatement =
                 connection.prepareStatement(res.getString(R.string.isPolc))
             statement.setString(1, code)
             val resultSet: ResultSet = statement.executeQuery()
             if (!resultSet.next()) {
                 CoroutineScope(Dispatchers.Main).launch {
                     context.setAlert("Nem polc")
                     context.polcHelyezesFragment.focusToBin()
                 }
             } else {
                 CoroutineScope(Dispatchers.Main).launch {
                     context.polcHelyezesFragment.polcCheck()
                 }
             }
         } catch (e: Exception) {
             Log.d(TAG, "checkPolc: visszajött hibával")
         }

     }
}