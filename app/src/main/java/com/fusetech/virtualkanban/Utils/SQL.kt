package com.fusetech.virtualkanban.Utils

import android.content.Context
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
        var connection: Connection
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
}