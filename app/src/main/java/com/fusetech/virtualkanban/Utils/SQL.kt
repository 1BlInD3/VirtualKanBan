package com.fusetech.virtualkanban.utils

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.View
import com.fusetech.virtualkanban.activities.MainActivity
import com.fusetech.virtualkanban.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.fusetech.virtualkanban.activities.MainActivity.Companion.sz0x
import com.fusetech.virtualkanban.activities.MainActivity.Companion.connectionString
import com.fusetech.virtualkanban.activities.MainActivity.Companion.res
import com.fusetech.virtualkanban.activities.MainActivity.Companion.progress
import com.fusetech.virtualkanban.activities.MainActivity.Companion.tobbletKontener
import com.fusetech.virtualkanban.activities.MainActivity.Companion.tobbletItem
import com.fusetech.virtualkanban.activities.MainActivity.Companion.url
import com.fusetech.virtualkanban.activities.MainActivity.Companion.mainUrl
import com.fusetech.virtualkanban.activities.MainActivity.Companion.backupURL
import com.fusetech.virtualkanban.activities.MainActivity.Companion.endPoint
import com.fusetech.virtualkanban.activities.MainActivity.Companion.dolgKod
import com.fusetech.virtualkanban.activities.MainActivity.Companion.emailMessage
import com.fusetech.virtualkanban.activities.MainActivity.Companion.path
import com.fusetech.virtualkanban.activities.MainActivity.Companion.sz01KiszedesDate
import com.fusetech.virtualkanban.activities.MainActivity.Companion.wifiInfo
import com.fusetech.virtualkanban.dataItems.*
import com.fusetech.virtualkanban.fragments.*
import com.fusetech.virtualkanban.fragments.PolcraHelyezesFragment.Companion.myItems
import java.io.File
import java.sql.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.Date
import kotlin.collections.ArrayList
import kotlin.random.Random

private const val TAG = "SQL"

class SQL(private val sqlMessage: SQLAlert) {

    interface SQLAlert {
        fun sendMessage(message: String)
    }

    fun deleteKontenerRaktarTetel(konenerTetelId: String) {
        var connection: Connection
        CoroutineScope(Dispatchers.IO).launch {
            Class.forName("net.sourceforge.jtds.jdbc.Driver")
            try {
                connection = DriverManager.getConnection(connectionString)
                val statement =
                    connection.prepareStatement(res.getString(R.string.deleteEmptyRaktarTetel))
                statement.setString(1, konenerTetelId)
                statement.executeUpdate()
                connection.close()
            } catch (e: Exception) {
                sqlMessage.sendMessage("Hiba van az üres letörlésénél")
                writeLog(e.stackTraceToString(), "arg1 $konenerTetelId")
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
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
                val statement2 =
                    connection.prepareStatement("SELECT [Key1] FROM [Fusetech].[dbo].[DolgKodok] WHERE Key1 = ?")
                statement2.setString(1, code)
                val resultSet2 = statement2.executeQuery()
                if (!resultSet2.next()) {
                    CoroutineScope(Dispatchers.Main).launch {
                        context.setAlert("Nincs jogosultságod belépni")
                        context.loginFragment?.stopSpinning()
                    }
                } else {
                    context.loadMenuFragment(false)
                    context.loginFragment = null
                    MainActivity.hasRight = false
                    val statement1 = connection.prepareStatement(res.getString(R.string.kutyuLogin))
                    statement1.setString(1, code)
                    statement1.setString(2, context.getMacAddr())
                    statement1.executeUpdate()
                }
            } else {
                if (resultSet.getInt("Jog") == 1) {
                    context.loadMenuFragment(true)
                    context.loginFragment = null
                    MainActivity.hasRight = true
                    val statement1 = connection.prepareStatement(res.getString(R.string.kutyuLogin))
                    statement1.setString(1, code)
                    statement1.setString(2, context.getMacAddr())
                    statement1.executeUpdate()
                } else {
                    context.loadMenuFragment(false)
                    context.loginFragment = null
                    MainActivity.hasRight = false
                }
            }
            try {
                if (File(context.getExternalFilesDir(null), "LOG.txt").exists()) {
                    val email = Email()
                    val save = SaveFile()
                    email.sendEmail(
                        "KutyuLOG@fusetech.hu",
                        "attila.balind@fusetech.hu",
                        context.getMacAddr(),
                        save.readLog(File(context.getExternalFilesDir(null), "LOG.txt"))
                    )
                    File(context.getExternalFilesDir(null), "LOG.txt").delete()
                    connection.close()
                }
            } catch (e: Exception) {
                context.setAlert("$e")
                writeLog(e.stackTraceToString(), "arg1 $code")
                connection.close()

            }
        } catch (e: Exception) {
            Log.d(TAG, "Nincs kapcsolat")
            CoroutineScope(Dispatchers.Main).launch {
                context.loginFragment?.stopSpinning()
                context.loginFragment?.setId("Hiaba lépett fel a feldolgozás során $e")
                writeLog(e.stackTraceToString(), "arg1 $code")
            }
        }
    }

    fun checkTrannzit(
        code: String,
        context: MainActivity,
        polcLocation: ArrayList<PolcLocation>?
    ) {
        val connection: Connection
        Class.forName("net.sourceforge.jtds.jdbc.Driver")
        try {
            CoroutineScope(Dispatchers.Main).launch {
                progress.visibility = View.VISIBLE
            }
            context.removeLocationFragment()
            polcLocation?.clear()
            connection = DriverManager.getConnection(url)
            val statement: PreparedStatement =
                connection.prepareStatement(res.getString(R.string.tranzitCheck))
            statement.setString(1, code)
            val resultSet: ResultSet = statement.executeQuery()
            if (!resultSet.next()) {
                Log.d(TAG, "checkTrannzit: Hülyeség nincs a tranzitban")
                CoroutineScope(Dispatchers.Main).launch {
                    context.setAlert("A $code cikk vagy zárolt, vagy nincs a tranzit raktárban!")
                    context.polcHelyezesFragment.setCikkNumberBack()
                    progress.visibility = View.GONE
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
                statement1.setString(2,MainActivity.fusetech)
                val resultSet1: ResultSet = statement1.executeQuery()
                if (!resultSet1.next()) {
                    val statement2 = connection.prepareStatement(res.getString(R.string.emptyBins))
                    statement2.setString(1,MainActivity.fusetech)
                    val resultSet2 = statement2.executeQuery()
                    if (!resultSet2.next()) {
                        CoroutineScope(Dispatchers.Main).launch {
                            context.setAlert("Nincs a polcon és be sem jönnek az üres polcok")
                        }
                    } else {
                        do {
                            val polc = resultSet2.getString("BinNumber")
                            myItems.add(PolcLocation(polc, "0"))
                        } while (resultSet2.next())
                        context.polcHelyezesFragment.reload()
                    }
                    context.polcHelyezesFragment.reload()
                    CoroutineScope(Dispatchers.Main).launch {
                        progress.visibility = View.GONE
                    }
                    Log.d(TAG, "checkTrannzit: Nincs a 02-es raktárban")
                } else {
                    CoroutineScope(Dispatchers.Main).launch {
                        progress.visibility = View.GONE
                    }
                    do {
                        val binNumber: String? = resultSet1.getString("BinNumber")
                        val balanceQty: Int = resultSet1.getInt("BalanceQty")
                        myItems.add(PolcLocation(binNumber, balanceQty.toString()))
                    } while (resultSet1.next())
                    val statement3 = connection.prepareStatement(res.getString(R.string.emptyBins))
                    statement3.setString(1,MainActivity.fusetech)
                    val resultSet3 = statement3.executeQuery()
                    if (!resultSet3.next()) {
                        CoroutineScope(Dispatchers.Main).launch {
                            context.setAlert("Nincs a polcon és be sem jönnek az üres polcok")
                        }
                    } else {
                        do {
                            val polc = resultSet3.getString("BinNumber")
                            myItems.add(PolcLocation(polc, "0"))
                        } while (resultSet3.next())
                        context.polcHelyezesFragment.reload()
                    }
                    connection.close()
                    context.polcHelyezesFragment.reload()
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "checkTrannzit: $e")
            CoroutineScope(Dispatchers.Main).launch {
                progress.visibility = View.GONE
                context.setAlert("Tranzitos hiba $e")
                writeLog(e.stackTraceToString(), "arg1 $code arg2 $polcLocation")
            }
        }
    }

    fun contenerJavit(kontener: String){
        val connection: Connection
        Class.forName("net.sourceforge.jtds.jdbc.Driver")
        try {
            connection = DriverManager.getConnection(connectionString)
            val result = connection.prepareStatement(res.getString(R.string.javitPolc))
            result.setNull(1,Types.INTEGER)
            result.setString(2,kontener)
            result.executeUpdate()
            connection.close()
        }catch (e: Exception){
            Log.d(TAG, "contenerJavit: ${e.stackTraceToString()}")
        }
    }

    fun tetelJavit(kontener: Int,code: String){
        val connection: Connection
        Class.forName("net.sourceforge.jtds.jdbc.Driver")
        try {
            connection = DriverManager.getConnection(connectionString)
            val result = connection.prepareStatement(res.getString(R.string.javitTetel))
            result.setString(1,code)
            result.setInt(2,kontener)
            result.executeUpdate()
            connection.close()
        }catch (e: Exception){
            Log.d(TAG, "contenerJavit: ${e.stackTraceToString()}")
        }
    }

    //////////////////////////////////////////////////////////////////////////////////
    fun containerManagement(id: String, context: MainActivity) {
        val connection: Connection
        Class.forName("net.sourceforge.jtds.jdbc.Driver")
        try {
            CoroutineScope(Dispatchers.Main).launch {
                context.menuFragment?.setMenuProgressOn()
            }
            connection = DriverManager.getConnection(connectionString)
            val isContainer =
                connection.prepareStatement(res.getString(R.string.containerCheck))
            isContainer.setString(1, id)
            isContainer.setInt(2, 0)
            isContainer.setString(3,MainActivity.fusetech)
            val containerResult = isContainer.executeQuery()
            if (!containerResult.next()) {
                Log.d(TAG, "containerManagement: Nincs konténer")
                val insertContainer =
                    connection.prepareStatement(res.getString(R.string.openContainer))
                insertContainer.setString(1, id)
                insertContainer.setInt(2, 0)
                insertContainer.setInt(3, 1)
                insertContainer.setString(4, "01")
                insertContainer.setString(5,MainActivity.fusetech)
                insertContainer.executeUpdate()
                Log.d(TAG, "containerManagement: Konténer létrehozva")
                try {
                    Log.d(TAG, "containerManagement: Betöltöm az adatot")
                    val getName =
                        connection.prepareStatement(res.getString(R.string.containerCheck2))
                    getName.setString(1, id)
                    getName.setInt(2, 0)
                   // getName.setString(3,MainActivity.fusetech)
                    val getNameResult = isContainer.executeQuery()// isContainer volt
                    if (!getNameResult.next()) {
                        CoroutineScope(Dispatchers.Main).launch {
                            context.setAlert("Valami nagy hiba van")
                            context.menuFragment?.setMenuProgressOff()
                        }
                    } else {
                        val ajdee = getNameResult.getInt("id")
                        var nullasKontener: String = getNameResult.getInt("id").toString()
                        var zeroString = ""
                        if (nullasKontener.length < 10) {
                            val charLength = 10 - nullasKontener.length
                            for (i in 0 until charLength) {
                                zeroString += "0"
                            }
                            nullasKontener = """$zeroString$nullasKontener"""
                        }
                        val updateContainer =
                            connection.prepareStatement(res.getString(R.string.updateContainerValue))
                        updateContainer.setString(1, nullasKontener)
                        updateContainer.setInt(2, ajdee)
                        updateContainer.executeUpdate()
                        Log.d(TAG, "containerManagement: visszaírtam a konténer értéket")
                        val bundle = Bundle()
                        bundle.putString("KONTENER", nullasKontener)
                        context.igenyFragment.arguments = bundle
                        context.supportFragmentManager.beginTransaction()
                            .replace(R.id.frame_container, context.igenyFragment, "IGENY")
                            .addToBackStack(null).commit()
                        CoroutineScope(Dispatchers.Main).launch {
                            context.menuFragment?.setMenuProgressOff()
                        }
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "containerManagement: $e")
                    CoroutineScope(Dispatchers.Main).launch {
                        context.menuFragment?.setMenuProgressOff()
                        writeLog(e.stackTraceToString(), "arg1 $id")
                    }
                }
            } else {
                Log.d(TAG, "containerManagement: van konténer")
                val id1 = containerResult.getInt("id")
                context.kontener = containerResult.getString("kontener")
                val rakhely: String? = containerResult.getString("termeles_rakhely")
                Log.d(TAG, "containerManagement: $rakhely")
                val igenyItemCheck =
                    connection.prepareStatement(res.getString(R.string.loadIgenyItemsToList))
                igenyItemCheck.setInt(1, id1)//ez a számot át kell írni majd a "kontener"-re
                val loadIgenyListResult = igenyItemCheck.executeQuery()
                if (!loadIgenyListResult.next()) {
                    Log.d(TAG, "containerManagement: Üres")
                    val bundle1 = Bundle()
                    bundle1.putString("KONTENER", context.kontener)
                    bundle1.putString("TERMRAKH", rakhely)
                    bundle1.putInt("ID",id1)
                    context.igenyFragment.arguments = bundle1
                    context.supportFragmentManager.beginTransaction()
                        .replace(R.id.frame_container, context.igenyFragment, "IGENY")
                        .addToBackStack(null)
                        .commit()
                    CoroutineScope(Dispatchers.Main).launch {
                        context.menuFragment?.setMenuProgressOff()
                    }
                } else {
                    do {
                        val cikk = loadIgenyListResult.getString("cikkszam")
                        val megjegyzes = loadIgenyListResult.getString("megjegyzes")
                        val darabszam = loadIgenyListResult.getDouble("igenyelt_mennyiseg")
                        context.listIgenyItems.add(IgenyItem(cikk, megjegyzes, darabszam.toString()))
                    } while (loadIgenyListResult.next())
                    val bundle = Bundle()
                    bundle.putSerializable("IGENY", context.listIgenyItems)
                    bundle.putString("KONTENER", context.kontener)
                    bundle.putString("TERMRAKH", rakhely)
                    bundle.putInt("ID",id1)
                    context.igenyFragment.arguments = bundle
                    context.supportFragmentManager.beginTransaction()
                        .replace(R.id.frame_container, context.igenyFragment, "IGENY")
                        .addToBackStack(null)
                        .commit()
                    CoroutineScope(Dispatchers.Main).launch {
                        context.menuFragment?.setMenuProgressOff()
                    }
                }
                //context.listIgenyItems.clear()
                connection.close()
            }
        } catch (e: Exception) {
            CoroutineScope(Dispatchers.Main).launch {
                context.setAlert("Baj van az igény konténer betöltésénél / létrehozásánál")
                context.menuFragment?.setMenuProgressOff()
                writeLog(e.stackTraceToString(), "arg1 $id")
            }
        }
    }

    fun containerManagement7(id: String, context: MainActivity) {
        val connection: Connection
        Class.forName("net.sourceforge.jtds.jdbc.Driver")
        try {
            CoroutineScope(Dispatchers.Main).launch {
                context.menuFragment?.setMenuProgressOn()
            }
            connection = DriverManager.getConnection(connectionString)
            val isContainer =
                connection.prepareStatement(res.getString(R.string.containerCheck))
            isContainer.setString(1, id)
            isContainer.setInt(2, 6)
            isContainer.setString(3,MainActivity.fusetech)
            val containerResult = isContainer.executeQuery()
            if (!containerResult.next()) {
                Log.d(TAG, "containerManagement: Nincs konténer")
                val insertContainer =
                    connection.prepareStatement(res.getString(R.string.openContainer))
                insertContainer.setString(1, id)
                insertContainer.setInt(2, 6)
                insertContainer.setInt(3, 2)
                insertContainer.setString(4, "01")
                insertContainer.setString(5,MainActivity.fusetech)
                insertContainer.executeUpdate()
                Log.d(TAG, "containerManagement: Konténer létrehozva")
                try {
                    Log.d(TAG, "containerManagement: Betöltöm az adatot")
                    val getName =
                        connection.prepareStatement(res.getString(R.string.containerCheck2))
                    getName.setString(1, id)
                    getName.setInt(2, 6)
                    val getNameResult = isContainer.executeQuery()
                    if (!getNameResult.next()) {
                        CoroutineScope(Dispatchers.Main).launch {
                            context.setAlert("Valami nagy hiba van")
                            context.menuFragment?.setMenuProgressOff()
                        }
                    } else {
                        val ajdee = getNameResult.getInt("id")
                        var nullasKontener: String = getNameResult.getInt("id").toString()
                        val kontId: String = getNameResult.getInt("id").toString()
                        var zeroString = ""
                        if (nullasKontener.length < 10) {
                            val charLength = 10 - nullasKontener.length
                            for (i in 0 until charLength) {
                                zeroString += "0"
                            }
                            nullasKontener = """$zeroString$nullasKontener"""
                        }
                        val updateContainer =
                            connection.prepareStatement(res.getString(R.string.updateContainerValue))
                        updateContainer.setString(1, nullasKontener)
                        updateContainer.setInt(2, ajdee)
                        updateContainer.executeUpdate()
                        Log.d(TAG, "containerManagement: visszaírtam a konténer értéket")
                        val bundle = Bundle()
                        bundle.putString("KONTENER", nullasKontener)
                        bundle.putString("KID", kontId)
                        context.tobbletOsszeallitasFragment.arguments = bundle
                        context.supportFragmentManager.beginTransaction()
                            .replace(
                                R.id.frame_container,
                                context.tobbletOsszeallitasFragment,
                                "TOBBLET"
                            )
                            .addToBackStack(null).commit()
                        CoroutineScope(Dispatchers.Main).launch {
                            context.menuFragment?.setMenuProgressOff()
                        }
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "containerManagement: $e")
                    CoroutineScope(Dispatchers.Main).launch {
                        context.menuFragment?.setMenuProgressOff()
                        context.setAlert("Konténer nyitás\n$e")
                        writeLog(e.stackTraceToString(), "arg1 $id")
                    }
                }
            } else {
                Log.d(TAG, "containerManagement: van konténer")
                val id1 = containerResult.getInt("id").toString()
                context.kontener = containerResult.getString("kontener")
                val rakhely: String? = containerResult.getString("termeles_rakhely")
                Log.d(TAG, "containerManagement: $rakhely")
                val igenyItemCheck =
                    connection.prepareStatement(res.getString(R.string.loadIgenyItemsToList))
                igenyItemCheck.setInt(1, id1.trim().toInt())//ez a számot át kell írni majd a "kontener"-re
                val loadIgenyListResult = igenyItemCheck.executeQuery()
                if (!loadIgenyListResult.next()) {
                    Log.d(TAG, "containerManagement: Üres")
                    val bundle1 = Bundle()
                    bundle1.putString("KONTENER", context.kontener)
                    bundle1.putString("TERMRAKH", rakhely)
                    bundle1.putString("KID",id1)
                    context.tobbletOsszeallitasFragment.arguments = bundle1
                    context.supportFragmentManager.beginTransaction()
                        .replace(
                            R.id.frame_container,
                            context.tobbletOsszeallitasFragment,
                            "TOBBLET"
                        )
                        .addToBackStack(null)
                        .commit()
                    CoroutineScope(Dispatchers.Main).launch {
                        context.menuFragment?.setMenuProgressOff()
                    }
                } else {
                    context.listIgenyItems.clear()
                    do {
                        val cikk = loadIgenyListResult.getString("cikkszam")
                        val megjegyzes = loadIgenyListResult.getString("megjegyzes")
                        val darabszam = loadIgenyListResult.getDouble("igenyelt_mennyiseg")
                        context.listIgenyItems.add(IgenyItem(cikk, megjegyzes, darabszam.toString()))
                    } while (loadIgenyListResult.next())
                    val bundle = Bundle()
                    bundle.putSerializable("TOBBLET", context.listIgenyItems)
                    bundle.putString("KONTENER", context.kontener)
                    bundle.putString("TERMRAKH", rakhely)
                    bundle.putString("KID",id1)
                    context.tobbletOsszeallitasFragment.arguments = bundle
                    context.supportFragmentManager.beginTransaction()
                        .replace(
                            R.id.frame_container,
                            context.tobbletOsszeallitasFragment,
                            "TOBBLET"
                        )
                        .addToBackStack(null)
                        .commit()
                    CoroutineScope(Dispatchers.Main).launch {
                        context.menuFragment?.setMenuProgressOff()
                    }
                }
            }
            connection.close()
        } catch (e: Exception) {
            CoroutineScope(Dispatchers.Main).launch {
                context.setAlert("Valahol baj van $e")
                context.menuFragment?.setMenuProgressOff()
                writeLog(e.stackTraceToString(), "arg1 $id")
            }
        }
    }

    fun check01(code: String, context: MainActivity, kontener: String) {
        val connection: Connection
        CoroutineScope(Dispatchers.Main).launch {
            context.igenyFragment.setProgressBarOn()
        }
        Class.forName("net.sourceforge.jtds.jdbc.Driver")
        try {
            connection = DriverManager.getConnection(connectionString)
            val statement = connection.prepareStatement(res.getString(R.string.is01))
            statement.setString(1, code)
            statement.setString(2, "01")
            statement.setString(3, MainActivity.fusetech)
            val resultSet = statement.executeQuery()
            if (!resultSet.next()) {
                CoroutineScope(Dispatchers.Main).launch {
                    context.setAlert("A polc nem a 01 raktárban található")
                    context.igenyFragment.setBinFocusOn()
                    context.igenyFragment.setProgressBarOff()
                }
            } else {
                val statement1 =
                    connection.prepareStatement(res.getString(R.string.updateBin))
                statement1.setString(1, code)
                statement1.setString(2,kontener)
                statement1.executeUpdate()
                CoroutineScope(Dispatchers.Main).launch {
                    context.igenyFragment.setFocusToItem()
                    context.igenyFragment.setProgressBarOff()
                }
            }
            connection.close()
        } catch (e: Exception) {
            Log.d(TAG, "check01: $e")
            CoroutineScope(Dispatchers.Main).launch {
                context.igenyFragment.setBinFocusOn()
                context.igenyFragment.setProgressBarOff()
                context.setAlert("HIba történt a tranzit ellenőrzésekor")
                writeLog(e.stackTraceToString(), "arg1 $code")
            }
        }
    }

    fun checkCode02(code: String, context: MainActivity, kontener: String) {
        val connection: Connection
        CoroutineScope(Dispatchers.Main).launch {
            progress.visibility = View.VISIBLE
        }
        Class.forName("net.sourceforge.jtds.jdbc.Driver")
        try {
            connection = DriverManager.getConnection(connectionString)
            val statement = connection.prepareStatement(res.getString(R.string.is01))
            statement.setString(1, code)
            statement.setString(2, "01")
            statement.setString(3, MainActivity.fusetech)
            val resultSet = statement.executeQuery()
            if (!resultSet.next()) {
                CoroutineScope(Dispatchers.Main).launch {
                    context.setAlert("A polc nem a 01 raktárban található")
                    context.tobbletOsszeallitasFragment.setBinFocusOn()
                    progress.visibility = View.GONE
                }
            } else {
                val statement1 =
                    connection.prepareStatement(res.getString(R.string.updateBin))
                statement1.setString(1, code)
                statement1.setString(2,kontener)
                statement1.executeUpdate()
                CoroutineScope(Dispatchers.Main).launch {
                    context.tobbletOsszeallitasFragment.setFocusToItem(code)
                    progress.visibility = View.GONE
                }
            }
            connection.close()
        } catch (e: Exception) {
            Log.d(TAG, "check01: $e")
            CoroutineScope(Dispatchers.Main).launch {
                progress.visibility = View.GONE
                writeLog(e.stackTraceToString(), "arg1 $code")
            }
        }
    }

    fun uploadItem(
        cikk: String,
        menny: Double,
        term: String,
        unit: String,
        context: MainActivity,
        konti: String
    ) {
        val connection: Connection
        Class.forName("net.sourceforge.jtds.jdbc.Driver")
        try {
            CoroutineScope(Dispatchers.Main).launch {
                progress.visibility = View.VISIBLE
            }
            connection = DriverManager.getConnection(connectionString)
            val statement = connection.prepareStatement(res.getString(R.string.insertItem))
            statement.setString(1, konti)
            statement.setString(2, cikk)
            statement.setInt(3, 0) //ez a státusz
            statement.setDouble(4, menny)
            statement.setInt(5, 0)
            statement.setString(6, "01")
            statement.setString(7, term)
            statement.setString(8, unit)
            statement.executeUpdate()
            CoroutineScope(Dispatchers.Main).launch {
                progress.visibility = View.GONE
            }
            connection.close()
        } catch (e: Exception) {
            Log.d(TAG, "uploadItem: $e")
            CoroutineScope(Dispatchers.Main).launch {
                context.setAlert("Hiba történt, lépj vissza a 'Kilépés' gombbal a menübe, majd vissza, hogy megnézd mi lett utoljára felvéve")
                progress.visibility = View.GONE
                writeLog(e.stackTraceToString(), "arg1 $cikk arg2 $menny arg3 $term arg4 $unit arg5 $konti")
            }
        }
    }

    fun uploadItem7(
        cikk: String,
        menny: Double,
        term: String,
        unit: String,
        context: MainActivity,
        konti: String
    ) {
        val connection: Connection
        Class.forName("net.sourceforge.jtds.jdbc.Driver")
        try {
            CoroutineScope(Dispatchers.Main).launch {
                progress.visibility = View.VISIBLE
            }
            connection = DriverManager.getConnection(connectionString)
            val statement = connection.prepareStatement(res.getString(R.string.insertItem))
            statement.setString(1, konti)
            statement.setString(2, cikk)
            statement.setInt(3, 6) //ez a státusz
            statement.setDouble(4, menny)
            statement.setDouble(5, menny)
            statement.setString(6, "01")
            statement.setString(7, term)
            statement.setString(8, unit)
            statement.executeUpdate()
            CoroutineScope(Dispatchers.Main).launch {
                progress.visibility = View.GONE
                context.tobbletOsszeallitasFragment.setAfterUpdate()
            }
            connection.close()
        } catch (e: Exception) {
            Log.d(TAG, "uploadItem: $e")
            CoroutineScope(Dispatchers.Main).launch {
                context.setAlert("Hiba történt, lépj vissza a 'Kilépés' gombbal a menübe, majd vissza, hogy megnézd mi lett utoljára felvéve")
                progress.visibility = View.GONE
                writeLog(e.stackTraceToString(), "arg1 $cikk arg2 $menny arg3 $term arg4 $unit arg5 $konti")
            }
        }
    }

    fun closeContainerSql(statusz: Int, datum: String, context: MainActivity, kontener: String) {
        val connection: Connection
        Class.forName("net.sourceforge.jtds.jdbc.Driver")
        try {
            connection = DriverManager.getConnection(connectionString)
            val statement =
                connection.prepareStatement(res.getString(R.string.closeContainer))
            statement.setInt(1, statusz)
            statement.setString(2, datum)
            statement.setString(3, kontener)
            statement.executeUpdate()
            Log.d(TAG, "closeContainerSql: sikeres lezárás")
            val statement1 =
                connection.prepareStatement(res.getString(R.string.updateItemStatus))
            statement1.setInt(1, statusz)
            statement1.setString(2, kontener)
            try {
                statement1.executeUpdate()
                connection.close()
                CoroutineScope(Dispatchers.Main).launch {
                    context.setAlert("Sikeres konténer lezárás!")
                    context.igenyFragment.setProgressBarOff()
                    context.igenyFragment.clearAll()
                }
                context.loadMenuFragment(true)
            } catch (e: Exception) {
                connection.close()
                Log.d(TAG, "closeContainerSql: $e")
                CoroutineScope(Dispatchers.Main).launch {
                    context.setAlert("A cikk státuszok felülírásánál hiba lépett fel, gyere az IT-re")
                    writeLog(e.stackTraceToString(), "arg1 $statusz arg2 $datum arg3 $kontener")
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "closeContainerSql: $e")
            CoroutineScope(Dispatchers.Main).launch {
                context.setAlert("Hiba a konténer lezárásnál")
                writeLog(e.stackTraceToString(), "arg1 $statusz arg2 $datum arg3 $kontener")
            }
        }
    }

    fun closeContainerSql7(statusz: Int, datum: String, context: MainActivity, kontener: String) {
        val connection: Connection
        Class.forName("net.sourceforge.jtds.jdbc.Driver")
        try {
            CoroutineScope(Dispatchers.Main).launch {
                progress.visibility = View.VISIBLE
            }
            connection = DriverManager.getConnection(connectionString)
            val statement =
                connection.prepareStatement(res.getString(R.string.closeContainer7))
            statement.setInt(1, statusz)
            statement.setString(2, datum)
            statement.setString(3, kontener)
            statement.executeUpdate()
            Log.d(TAG, "closeContainerSql: sikeres lezárás")
            CoroutineScope(Dispatchers.Main).launch {
                context.setAlert("Sikeres konténer lezárás!")
                progress.visibility = View.GONE
            }
            val statement1 =
                connection.prepareStatement(res.getString(R.string.updateItemStatus))
            statement1.setInt(1, statusz)
            statement1.setString(2, kontener)
            try {
                CoroutineScope(Dispatchers.Main).launch {
                    progress.visibility = View.VISIBLE
                }
                statement1.executeUpdate()
                connection.close()
                CoroutineScope(Dispatchers.Main).launch {
                    progress.visibility = View.GONE
                }
            } catch (e: Exception) {
                connection.close()
                Log.d(TAG, "closeContainerSql: $e")
                CoroutineScope(Dispatchers.Main).launch {
                    context.setAlert("A cikk státuszok felülírásánál hiba lépett fel, gyere az IT-re")
                    progress.visibility = View.GONE
                    writeLog(e.stackTraceToString(), "arg1 $statusz arg2 $datum arg3 $kontener")
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "closeContainerSql: $e")
            CoroutineScope(Dispatchers.Main).launch {
                progress.visibility = View.GONE
                writeLog(e.stackTraceToString(), "arg1 $statusz arg2 $datum arg3 $kontener")
                context.setAlert("Hiba történt a feldolgozás során")
            }
        }
    }

    fun loadIgenyLezaras(context: MainActivity) {
        val connection: Connection
        Class.forName("net.sourceforge.jtds.jdbc.Driver")
        try {
            CoroutineScope(Dispatchers.Main).launch {
                if (context.menuFragment != null) {
                    context.menuFragment?.setMenuProgressOn()
                } else {
                    progress.visibility = View.VISIBLE
                }
            }
            connection = DriverManager.getConnection(url)
            val statement =
                connection.prepareStatement(res.getString(R.string.igenyKontenerLezarasKontenerBeolvas))
            statement.setString(1,MainActivity.fusetech)
            val resultSet = statement.executeQuery()
            if (!resultSet.next()) {
                Log.d(TAG, "loadIgenyLezaras: Nincs ilyen konténer")
                CoroutineScope(Dispatchers.Main).launch {
                    context.setAlert("Nincs lezárni való konténer!")
                    if (context.menuFragment != null) {
                        context.menuFragment?.setMenuProgressOff()
                    } else {
                        progress.visibility = View.GONE
                    }
                }
            } else {
                context.kontener1List.clear()
                do {
                    val kontener: String? = resultSet.getString("kontener")
                    val polc: String? = resultSet.getString("polc")
                    val datum: String = resultSet.getString("igenyelve").substring(0,16)
                    val tetelszam = resultSet.getInt("tetelszam")
                    val id: String? = resultSet.getString("id")
                    val status: Int = resultSet.getInt("statusz")
                    context.kontener1List.add(
                        KontenerItem(
                            kontener,
                            polc,
                            datum,
                            tetelszam,
                            id,
                            status
                        )
                    )
                } while (resultSet.next())
                val bundle = Bundle()
                bundle.putSerializable("KONTENERLISTA", context.kontener1List)
                context.igenyLezarasFragment?.arguments = bundle
                context.supportFragmentManager.beginTransaction()
                    .replace(R.id.frame_container, context.igenyLezarasFragment!!, "IGENYLEZARAS")
                    .addToBackStack(null).commit()
                CoroutineScope(Dispatchers.Main).launch {
                    if (context.menuFragment != null) {
                        context.menuFragment?.setMenuProgressOff()
                    } else {
                        progress.visibility = View.GONE
                    }
                }
            }
            connection.close()
        } catch (e: Exception) {
            Log.d(TAG, "loadIgenyLezaras: $e")
            CoroutineScope(Dispatchers.Main).launch {
                context.setAlert("Hálózati probléma! Próbáld újra")
                writeLog(e.stackTraceToString(), "loadIgenyLezaras")
                if (context.menuFragment != null) {
                    context.menuFragment?.setMenuProgressOff()
                } else {
                    progress.visibility = View.GONE
                }
            }
        }
    }

    fun loadKontenerCikkek(kontener_id: String, context: MainActivity) {
        context.igenyKiszedesCikkLezaras = IgenyKontenerLezarasCikkLezaras()
        val connection: Connection
        Class.forName("net.sourceforge.jtds.jdbc.Driver")
        try {
            CoroutineScope(Dispatchers.Main).launch {
                context.igenyLezarasFragment?.setProgressBarOn()
            }
            connection = DriverManager.getConnection(url)
            val statement =
                connection.prepareStatement(res.getString(R.string.igenyKontenerLezarasCikkLezarasNULL))
            statement.setInt(1, kontener_id.toInt())
            statement.setInt(2, 0)
            val resultSet = statement.executeQuery()
            if (!resultSet.next()) {
                Log.d(TAG, "loadKontenerCikkek: HIBA VAN")
                CoroutineScope(Dispatchers.Main).launch {
                    context.setAlert("A konténerben nincs 0 státuszú cikk")
                    context.igenyLezarasFragment?.setProgressBarOff()
                }
            } else {
                //val igenyKiszedesCikkLezaras = IgenyKontenerLezarasCikkLezaras()
                context.igenyLezarCikkVisible = true
                val kontenerCikkLezar: ArrayList<KontenerbenLezarasItem> = ArrayList()
                do {
                    val cikk = resultSet.getString("cikkszam")
                    val megj1 = resultSet.getString("Description1")
                    val megj2 = resultSet.getString("Description2")
                    val intrem = resultSet.getString("InternRem1")
                    val igeny = resultSet.getDouble("igenyelt_mennyiseg")
                        .toString() + " " + resultSet.getString("Unit")
                    val mozgatott = resultSet.getDouble("mozgatott_mennyiseg")
                        .toString() + " " + resultSet.getString("Unit")
                    val status = resultSet.getInt("statusz")
                    val unit = resultSet.getString("Unit")
                    val id = resultSet.getInt("id")
                    val kontenerId = resultSet.getInt("kontener_id")
                    val balance = resultSet.getDouble("StockBalance")
                    kontenerCikkLezar.add(
                        KontenerbenLezarasItem(
                            cikk,
                            megj1,
                            megj2,
                            intrem,
                            igeny,
                            mozgatott,
                            status,
                            unit,
                            id,
                            kontenerId,
                            balance
                        )
                    )
                } while (resultSet.next())
                val bundle = Bundle()
                bundle.putSerializable("CIKKLEZAR", kontenerCikkLezar)
                bundle.putString("KONTENER_ID", kontener_id)
                bundle.putBoolean("LEZARBUTN", true)
                context.igenyKiszedesCikkLezaras!!.arguments = bundle
                context.supportFragmentManager.beginTransaction()
                    .replace(
                        R.id.data_frame1,
                        context.igenyKiszedesCikkLezaras!!,
                        "CIKKLEZARASFRAGMENT"
                    )
                    /*.addToBackStack(null)*/.commit()
                CoroutineScope(Dispatchers.Main).launch {
                    context.igenyLezarasFragment?.setProgressBarOff()
                }
                //kontenerCikkLezar.clear()
            }
            connection.close()
        } catch (e: Exception) {
            Log.d(TAG, "loadKontenerCikkek: $e")
            CoroutineScope(Dispatchers.Main).launch {
                Log.d(TAG, "loadKontenerCikkek: $e")
                context.setAlert("Hiba lépett fel a tételek betöltésénél")
                context.igenyLezarasFragment?.setProgressBarOff()
                context.loadMenuFragment(true)
                writeLog(e.stackTraceToString(), "arg1 $kontener_id")
            }
        }
    }

    fun cikkPolcQuery(code: String, context: MainActivity) {
        val connection: Connection
        val bundle = Bundle()
        Class.forName("net.sourceforge.jtds.jdbc.Driver")
        try {
            connection = DriverManager.getConnection(url)
            val preparedStatement: PreparedStatement =
                connection.prepareStatement(res.getString(R.string.isPolc))
            preparedStatement.setString(1, code)
            val resultSet: ResultSet = preparedStatement.executeQuery()
            if (!resultSet.next()) {
                val preparedStatement1: PreparedStatement =
                    connection.prepareStatement(res.getString(R.string.cikkSql))
                preparedStatement1.setString(1, code)
                val resultSet1: ResultSet = preparedStatement1.executeQuery()
                if (!resultSet1.next()) {
                    val statement2 = connection.prepareStatement(res.getString(R.string.cikkSql4))
                    statement2.setString(1, code)
                    val resultSet2 = statement2.executeQuery()
                    if (!resultSet2.next()) {
                        context.loadFragment =
                            LoadFragment.newInstance("Nincs ilyen kód a rendszerben")
                        context.supportFragmentManager.beginTransaction()
                            .replace(R.id.cikk_container, context.loadFragment!!, "LRF").commit()
                    } else {
                        context.cikkResultFragment = CikkResultFragment()
                        val megjegyzes1: String? = resultSet2.getString("Description1")
                        val megjegyzes2: String? = resultSet2.getString("Description2")
                        val unit: String? = resultSet2.getString("Unit")
                        val intrem: String? = resultSet2.getString("IntRem")
                        bundle.putSerializable("cikk", context.cikkItems)
                        bundle.putString("megjegyzes", megjegyzes1)
                        bundle.putString("megjegyzes2", megjegyzes2)
                        bundle.putString("unit", unit)
                        bundle.putString("intrem", intrem)
                        context.cikkResultFragment?.arguments = bundle
                        context.supportFragmentManager.beginTransaction()
                            .replace(R.id.cikk_container, context.cikkResultFragment!!, "CRF")
                            .commit()
                    }
                } else {
                    context.cikkResultFragment = CikkResultFragment()
                    val megjegyzes1: String? = resultSet1.getString("Description1")
                    val megjegyzes2: String? = resultSet1.getString("Description2")
                    val unit: String? = resultSet1.getString("Unit")
                    val intrem: String? = resultSet1.getString("IntRem")
                    context.cikkItems.clear()
                    do {
                        context.cikkItems.add(
                            CikkItems(
                                resultSet1.getDouble("BalanceQty"),
                                resultSet1.getString("BinNumber"),
                                resultSet1.getString("Warehouse"),
                                resultSet1.getString("QcCategory")
                            )
                        )
                    } while (resultSet1.next())
                    bundle.putSerializable("cikk", context.cikkItems)
                    bundle.putString("megjegyzes", megjegyzes1)
                    bundle.putString("megjegyzes2", megjegyzes2)
                    bundle.putString("unit", unit)
                    bundle.putString("intrem", intrem)
                    context.cikkResultFragment?.arguments = bundle
                    context.supportFragmentManager.beginTransaction()
                        .replace(R.id.cikk_container, context.cikkResultFragment!!, "CRF").commit()
                }
            } else {
                val preparedStatement2: PreparedStatement =
                    connection.prepareStatement(res.getString(R.string.polcSql))
                preparedStatement2.setString(1, code)
                val resultSet2: ResultSet = preparedStatement2.executeQuery()
                if (!resultSet2.next()) {
                    val loadFragment = LoadFragment.newInstance("A polc üres")
                    context.supportFragmentManager.beginTransaction()
                        .replace(R.id.cikk_container, loadFragment).commit()
                } else {
                    context.polcResultFragment = PolcResultFragment()
                    do {
                        context.polcItems.add(
                            PolcItems(
                                resultSet2.getDouble("BalanceQty"),
                                resultSet2.getString("Unit"),
                                resultSet2.getString("Description1"),
                                resultSet2.getString("Description2"),
                                resultSet2.getString("IntRem"),
                                resultSet2.getString("QcCategory"),
                                resultSet2.getString("StockItem")
                            )
                        )

                    } while (resultSet2.next())
                    bundle.putSerializable("polc", context.polcItems)
                    context.polcResultFragment?.arguments = bundle
                    context.supportFragmentManager.beginTransaction()
                        .replace(R.id.cikk_container, context.polcResultFragment!!, "PRF").commit()
                }
            }
            connection.close()
        } catch (e: Exception) {
            Log.d(TAG, "$e")
            context.loadFragment = LoadFragment.newInstance("A feldolgozás során hiba lépett fel")
            context.supportFragmentManager.beginTransaction()
                .replace(R.id.cikk_container, context.loadFragment!!, "LRF")
                .commit()
            writeLog(e.stackTraceToString(), "arg1 $code")
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun scalaSend(
        cikkszam: String,
        polchely: String?,
        mennyisege: Double?,
        rbol: String,
        rba: String,
        polchelyre: String,
        context: MainActivity
    ) {
        val path = context.getExternalFilesDir(null)
        val name = SimpleDateFormat("yyyyMMddHHmmss").format(Date()) + Random.nextInt(
            0,
            10000
        ) + ".xml"
        val file = File(path, name)
        try {
            CoroutineScope(Dispatchers.Main).launch {
                progress.visibility = View.VISIBLE
            }
            val currentDate = SimpleDateFormat("yyyy-MM-dd").format(Date())
            context.save.saveFile(
                file,
                context.xml.createXml(
                    currentDate,
                    mennyisege,
                    cikkszam,
                    rbol,
                    polchely,
                    rba,
                    polchelyre,
                    dolgKod
                )
            )
            Log.d("IOTHREAD", "sendXmlData: ${Thread.currentThread().name}")
            try {
                context.retro.retrofitGet(file, endPoint)
                CoroutineScope(Dispatchers.Main).launch {
                    progress.visibility = View.GONE
                }
            } catch (e: Exception) {
                try {
                    val a = mainUrl
                    mainUrl = backupURL
                    context.retro.retrofitGet(file, endPoint)
                    mainUrl = a
                } catch (e: Exception) {
                    CoroutineScope(Dispatchers.Main).launch {
                        context.setAlert("Hálózati probléma")
                        writeLog(e.stackTraceToString(), "arg1 $cikkszam arg2 $polchely arg3 $mennyisege arg4 $rbol arg5 $rba arg6 $polchelyre")
                        progress.visibility = View.GONE
                        if (file.exists()) {
                            file.delete()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            CoroutineScope(Dispatchers.Main).launch {
                context.setAlert("Scalaba küldés hiba")
                writeLog(e.stackTraceToString(), "arg1 $cikkszam arg2 $polchely arg3 $mennyisege arg4 $rbol arg5 $rba arg6 $polchelyre")
                progress.visibility = View.GONE
                if (file.exists()) {
                    file.delete()
                }
                /* val catchFile = context.save.prepareFile(
                     context.getExternalFilesDir(null).toString(),
                     SimpleDateFormat("yyyyMMddHHmmss").format(Date()) + Random.nextInt(
                         0,
                         10000
                     ) + ".txt"
                 )
                 /*if (ContextCompat.checkSelfPermission(
                         context,
                         android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                     ) == PackageManager.PERMISSION_GRANTED
                 ) {*/
                 context.save.saveFile(catchFile, "myData")
                 context.retro.retrofitGet(catchFile, "//10.0.0.11/TeszWeb/bin")*/
                //}
            }
        }
    }

    fun checkItem(code: String, context: MainActivity) {
        val connection: Connection
        CoroutineScope(Dispatchers.Main).launch {
            context.igenyFragment.setProgressBarOn()
        }
        Class.forName("net.sourceforge.jtds.jdbc.Driver")
        try {
            connection = DriverManager.getConnection(url)
            val statement = connection.prepareStatement(res.getString(R.string.cikkSql))
            statement.setString(1, code)
            val resultSet = statement.executeQuery()
            if (!resultSet.next()) {
                val statement1 = connection.prepareStatement(res.getString(R.string.cikkSql4))
                statement1.setString(1, code)
                val resultSet1 = statement1.executeQuery()
                if (!resultSet1.next()) {
                    CoroutineScope(Dispatchers.Main).launch {
                        context.setAlert("Nincs ilyen cikk a rendszerben $code")
                        context.igenyFragment.setProgressBarOff()
                        context.igenyFragment.setFocusToItem()
                    }
                } else {
                    CoroutineScope(Dispatchers.Main).launch {
                        context.setAlert("A $code cikknek nincs mennyisége a rendszerben")
                        context.igenyFragment.setProgressBarOff()
                        context.igenyFragment.setFocusToItem()
                    }
                }
            } else {
                val megjegyzesIgeny: String = resultSet.getString("Description1")
                val megjegyzes2Igeny: String = resultSet.getString("Description2")
                val intremIgeny: String = resultSet.getString("IntRem")
                val unitIgeny: String = resultSet.getString("Unit")
                CoroutineScope(Dispatchers.Main).launch {
                    context.igenyFragment.setInfo(
                        megjegyzesIgeny,
                        megjegyzes2Igeny,
                        intremIgeny,
                        unitIgeny
                    )
                    context.igenyFragment.setProgressBarOff()
                    context.igenyFragment.setFocusToQuantity()
                }
            }
            connection.close()
        } catch (e: Exception) {
            Log.d(TAG, "checkItem: $e")
            CoroutineScope(Dispatchers.Main).launch {
                context.igenyFragment.setProgressBarOff()
                context.igenyFragment.setFocusToItem()
                context.setAlert("Hiba történt a cikk ellenőrzés közben")
                writeLog(e.stackTraceToString(), "arg1 $code")
            }
        }
    }

    fun checkItem2(code: String, bin: String, context: MainActivity) {
        val connection: Connection
        CoroutineScope(Dispatchers.Main).launch {
            //context.tobbletOsszeallitasFragment.setProgressBarOn()
            progress.visibility = View.VISIBLE
        }
        Class.forName("net.sourceforge.jtds.jdbc.Driver")
        try {
            connection = DriverManager.getConnection(url)
            val statement = connection.prepareStatement(res.getString(R.string.cikkSql2))
            statement.setString(1, code)
            statement.setString(2, bin)
            val resultSet = statement.executeQuery()
            if (!resultSet.next()) {
                val statement1 =
                    connection.prepareStatement(res.getString(R.string.cikkSql3ProdOnly))
                statement1.setString(1, code)
                val resultSet1 = statement1.executeQuery()
                if (!resultSet1.next()) {
                    CoroutineScope(Dispatchers.Main).launch {
                        context.setAlert("A $code cikk nincs sem a polcon sem a termelésben")
                        context.tobbletOsszeallitasFragment.setCikkszamBlank()
                        progress.visibility = View.GONE
                        context.tobbletOsszeallitasFragment.setFocusToItem(bin)
                    }
                } else {
                    var message = ""
                    //val igenyTermeles: ArrayList<IgenyItem> = ArrayList()
                    do {
                        val balance: Double = resultSet1.getString("BalanceQty").toDouble()
                        message += resultSet1.getString("BinNumber") + "\t" + balance.toString() + "\n"
                        /*val mennyiseg = resultSet1.getString("BalanceQty")
                        val polc = resultSet1.getString("BinNumber")*/
                        //igenyTermeles.add(IgenyItem(polc,mennyiseg,"Termelés"))
                    } while (resultSet1.next())
                    CoroutineScope(Dispatchers.Main).launch {
                        context.setAlert("A cikk ezeken a polcokon található a termelésben: \n\n$message")
                        context.tobbletOsszeallitasFragment.setCikkszamBlank()
                        progress.visibility = View.GONE
                        context.tobbletOsszeallitasFragment.setFocusToItem(bin)
                    }
                }
            } else {
                val megjegyzesIgeny: String = resultSet.getString("Description1")
                val megjegyzes2Igeny: String = resultSet.getString("Description2")
                val intremIgeny: String = resultSet.getString("IntRem")
                val unitIgeny: String = resultSet.getString("Unit")
                CoroutineScope(Dispatchers.Main).launch {
                    context.tobbletOsszeallitasFragment.setInfo(
                        megjegyzesIgeny,
                        megjegyzes2Igeny,
                        intremIgeny,
                        unitIgeny
                    )
                    progress.visibility = View.GONE
                    context.tobbletOsszeallitasFragment.setFocusToQuantity()
                }
            }
            connection.close()
        } catch (e: Exception) {
            Log.d(TAG, "checkItem: $e")
            CoroutineScope(Dispatchers.Main).launch {
                progress.visibility = View.GONE
                context.setAlert("Nincs hálózati kapcsolat?")
                writeLog(e.stackTraceToString(), "arg1 $code arg2 $bin")
            }
        }
    }

    fun loadIgenyKiszedes(context: MainActivity) {
        val connection: Connection
        context.igenyKiszedesFragment = IgenyKontenerKiszedesFragment()
        Class.forName("net.sourceforge.jtds.jdbc.Driver")
        try {
            CoroutineScope(Dispatchers.Main).launch {
                if (context.menuFragment != null) {
                    context.menuFragment?.setMenuProgressOn()
                } else {
                    progress.visibility = View.VISIBLE
                }
            }
            connection = DriverManager.getConnection(url)
            val statement =
                connection.prepareStatement(res.getString(R.string.igenyKontenerKiszedese))
            statement.setString(1,MainActivity.fusetech)
            val resultSet = statement.executeQuery()
            if (!resultSet.next()) {
                CoroutineScope(Dispatchers.Main).launch {
                    if (context.menuFragment != null) {
                        context.menuFragment?.setMenuProgressOff()
                    } else {
                        progress.visibility = View.GONE
                    }
                    context.setAlert("Nincs igény konténer")
                    context.loadMenuFragment(true)
                }
                /*context.supportFragmentManager.beginTransaction()
                    .replace(R.id.frame_container, context.igenyKiszedesFragment!!, "KISZEDES")
                    .commit()
                context.menuFragment = null*/
            } else {
                context.kontenerList.clear()
                do {
                    val kontener: String? = resultSet.getString("kontener")
                    val polc: String? = resultSet.getString("polc")
                    val datum: String = resultSet.getString("igenyelve").substring(0,16)
                    val tetelszam = resultSet.getInt("tetelszam")
                    val id: String = resultSet.getString("id")
                    val status: Int = resultSet.getInt("statusz")
                    context.kontenerList.add(
                        KontenerItem(
                            kontener,
                            polc,
                            datum,
                            tetelszam,
                            id,
                            status
                        )
                    )
                } while (resultSet.next())
                val bundle = Bundle()
                context.menuFragment = null
                bundle.putSerializable("KISZEDESLISTA", context.kontenerList)
                context.igenyKiszedesFragment?.arguments = bundle
                context.supportFragmentManager.beginTransaction()
                    .replace(R.id.frame_container, context.igenyKiszedesFragment!!, "KISZEDES")
                    .commit()
                CoroutineScope(Dispatchers.Main).launch {

                    if (context.menuFragment != null) {
                        context.menuFragment?.setMenuProgressOff()
                    } else {
                        progress.visibility = View.GONE
                    }
                }
            }
            connection.close()
        } catch (e: Exception) {
            Log.d(TAG, "loadIgenyKiszedes: $e")
            writeLog(e.stackTraceToString(), "loadIgenyKiszedes")
            CoroutineScope(Dispatchers.Main).launch {
                if (context.menuFragment != null) {
                    context.menuFragment?.setMenuProgressOff()
                } else {
                    progress.visibility = View.GONE
                }
                context.setAlert("Probléma van az igény kiszedéséért")
            }
        }
    }

    fun checkIfContainerIsOpen(   ///////////////////////////////////4-1
        kontener: String,
        context: MainActivity
    ) {////////////////////////////////////////////////////////////////////////////////////////////////////
        val connection: Connection
        context.koztesFragment = null
        Class.forName("net.sourceforge.jtds.jdbc.Driver")
        try {
            CoroutineScope(Dispatchers.Main).launch {
                progress.visibility = View.VISIBLE
            }
            connection = DriverManager.getConnection(connectionString)
            val statement =
                connection.prepareStatement(res.getString(R.string.kontenerEllenorzes))
            statement.setString(1, kontener)
            statement.setInt(2, 2)
            val resultSet = statement.executeQuery()
            if (!resultSet.next()) {
                context.loadSzallitoJarmu(kontener)
                CoroutineScope(Dispatchers.Main).launch {
                    progress.visibility = View.GONE
                    context.igenyKiszedesFragment?.destroy()
                }
            } else {
                sz0x = resultSet.getString("SzallitoJarmu")
                val statement2 =
                    connection.prepareStatement(res.getString(R.string.atvevoBeiras))
                statement2.setString(1, dolgKod)
                statement2.setString(2, kontener)
                statement2.executeUpdate()
                Log.d(TAG, "checkIfContainerIsOpen: Sikeres update")
                val statment3 =
                    connection.prepareStatement(res.getString(R.string.kontenerCikkAdatok))
                statment3.setInt(1, kontener.toInt())
                statment3.setString(2, dolgKod)
                val resultSet1 = statment3.executeQuery()
                if (!resultSet1.next()) {
                    CoroutineScope(Dispatchers.Main).launch {
                        //setAlert("A konténer üres")
                        progress.visibility = View.GONE
                    }
                    if(emailMessage?.isNotEmpty()!!) {
                        val email = Email()
                        email.sendEmail("KanBan@fusetech.hu","keszlet.modositas@fusetech.hu","Meghiúsult kiszolgálás",emailMessage!!)
                        emailMessage = ""
                    }
                    //context.igenyKiszedesFragment?.destroy()
                    context.ellenorzoKodFragment = EllenorzoKodFragment()
                    context.supportFragmentManager.beginTransaction()
                        .replace(R.id.frame_container, context.ellenorzoKodFragment!!, "ELLENOR")
                        .commit()
                } else {
                    val fragment = IgenyKontnerKiszedesCikk()
                    //context.igenyKiszedesFragment?.destroy()
                    val konteneresCikkek: ArrayList<KontenerbenLezarasItem> = ArrayList()
                    do {
                        val cikk = resultSet1.getString("cikkszam")
                        val megj1 = resultSet1.getString("Description1")
                        val megj2 = resultSet1.getString("Description2")
                        val intrem = resultSet1.getString("InternRem1")
                        val igeny = resultSet1.getDouble("igenyelt_mennyiseg").toString()
                        val mozgatott = resultSet1.getDouble("mozgatott_mennyiseg").toString()
                        val status = resultSet1.getInt("statusz")
                        val unit = resultSet1.getString("Unit")
                        val id = resultSet1.getInt("id")
                        val kontenerId = resultSet1.getInt("kontener_id")
                        val balance = resultSet1.getDouble("StockBalance")
                        konteneresCikkek.add(
                            KontenerbenLezarasItem(
                                cikk,
                                megj1,
                                megj2,
                                intrem,
                                igeny,
                                mozgatott,
                                status,
                                unit,
                                id,
                                kontenerId,
                                balance
                            )
                        )
                    } while (resultSet1.next())
                    val bundle = Bundle()
                    bundle.putSerializable("NEGYESCIKKEK", konteneresCikkek)
                    bundle.putSerializable("NEGYESNEV", kontener)
                    fragment.arguments = bundle
                    // igenyKiszedesCikk.arguments = bundle
                    context.supportFragmentManager.beginTransaction()
                        .replace(R.id.data_frame2, fragment, "NEGYESCIKKEK").commit()
                    //context.igenyKiszedesFragment = null
                    CoroutineScope(Dispatchers.Main).launch {
                        progress.visibility = View.GONE
                    }
                }
            }
            connection.close()
        } catch (e: Exception) {
            CoroutineScope(Dispatchers.Main).launch {
                context.setAlert("Hiba a négyescikkek betöltésénél")
                writeLog(e.stackTraceToString(), "arg1 $kontener")
                Log.d(TAG, "checkIfContainerIsOpen: $e")
                progress.visibility = View.GONE
            }
        }
    }

    fun loadKiszedesreVaro(context: MainActivity) {
        context.kiszedesreVaroIgenyFragment = KiszedesreVaroIgenyFragment()
        val connection: Connection
        Class.forName("net.sourceforge.jtds.jdbc.Driver")
        try {
            CoroutineScope(Dispatchers.Main).launch {
                if (context.menuFragment != null) {
                    context.menuFragment?.setMenuProgressOn()
                } else {
                    progress.visibility = View.VISIBLE
                }
            }
            connection = DriverManager.getConnection(url)
            val statement =
                connection.prepareStatement(res.getString(R.string.igenyKontenerKiszedese))
            statement.setString(1,MainActivity.fusetech)
            val resultSet = statement.executeQuery()
            if (!resultSet.next()) {
                CoroutineScope(Dispatchers.Main).launch {
                    if (context.menuFragment != null) {
                        context.menuFragment?.setMenuProgressOff()
                    } else {
                        progress.visibility = View.GONE
                    }
                }
                context.supportFragmentManager.beginTransaction()
                    .replace(R.id.frame_container, context.kiszedesreVaroIgenyFragment!!, "VARAS")
                    .addToBackStack(null).commit()
            } else {
                context.myList.clear()
                do {
                    val kontener: String? = resultSet.getString("kontener")
                    val polc: String? = resultSet.getString("polc")
                    val datum = resultSet.getString("igenyelve").substring(0,16)
                    val tetelszam = resultSet.getInt("tetelszam")
                    val id: String = resultSet.getString("id")
                    val status: Int = resultSet.getInt("statusz")
                    context.myList.add(KontenerItem(kontener, polc, datum, tetelszam, id, status))
                } while (resultSet.next())
                val bundle = Bundle()
                bundle.putSerializable("VAROLISTA", context.myList)
                context.kiszedesreVaroIgenyFragment!!.arguments = bundle
                context.supportFragmentManager.beginTransaction()
                    .replace(R.id.frame_container, context.kiszedesreVaroIgenyFragment!!, "VARAS")
                    /*.addToBackStack(null)*/.commit()
                CoroutineScope(Dispatchers.Main).launch {
                    if (context.menuFragment != null) {
                        context.menuFragment?.setMenuProgressOff()
                    } else {
                        progress.visibility = View.GONE
                    }
                    context.menuFragment = null
                }
            }
            connection.close()
        }catch (e: Exception) {
            Log.d(TAG, "loadIgenyKiszedes: $e")
            writeLog(e.stackTraceToString(), "loadKiszedesreVaro")
            CoroutineScope(Dispatchers.Main).launch {
                //context.menuFragment?.setMenuProgressOff()
                if (context.menuFragment != null) {
                    context.menuFragment?.setMenuProgressOff()
                } else {
                    progress.visibility = View.GONE
                }
                context.setAlert("Probléma van :\n $e")
            }
        }
    }

    fun loadKontenerCikkekHatos(kontener_id: String, context: MainActivity) {
        val connection: Connection
        context.hatosFragment = HatosCikkekFragment()
        Class.forName("net.sourceforge.jtds.jdbc.Driver")
        try {
            CoroutineScope(Dispatchers.Main).launch {
                //context.kiszedesreVaroIgenyFragment?.setProgressBarOn()
                progress.visibility = View.VISIBLE
            }
            connection = DriverManager.getConnection(url)
            val statement =
                connection.prepareStatement(res.getString(R.string.igenyKontenerLezarasCikkLezarasNezegetos))
            statement.setInt(1, kontener_id.toInt())
            //statement.setInt(2,1)
            val resultSet = statement.executeQuery()
            if (!resultSet.next()) {
                Log.d(TAG, "loadKontenerCikkek: HIBA VAN")
                CoroutineScope(Dispatchers.Main).launch {
                    context.setAlert("A konténerben nincs 1 státuszú cikk")
                    //context.kiszedesreVaroIgenyFragment?.setProgressBarOff()
                    progress.visibility = View.GONE
                    context.kiszedesreVaro()
                }
            } else {
                //val igenyKiszedesCikkLezaras = IgenyKontenerLezarasCikkLezaras()
                context.igenyLezarCikkVisible = true
                val kontenerCikkLezar: ArrayList<KontenerbenLezarasItem> = ArrayList()
                do {
                    val cikk = resultSet.getString("cikkszam")
                    val megj1 = resultSet.getString("Description1")
                    val megj2 = resultSet.getString("Description2")
                    val intrem = resultSet.getString("InternRem1")
                    val igeny = resultSet.getDouble("igenyelt_mennyiseg")
                        .toString() + " " + resultSet.getString("Unit")
                    val mozgatott = resultSet.getDouble("mozgatott_mennyiseg")
                        .toString() + " " + resultSet.getString("Unit")
                    val status = resultSet.getInt("statusz")
                    val unit = resultSet.getString("Unit")
                    val id = resultSet.getInt("id")
                    val kontenerId = resultSet.getInt("kontener_id")
                    val balance = resultSet.getDouble("StockBalance")
                    kontenerCikkLezar.add(
                        KontenerbenLezarasItem(
                            cikk,
                            megj1,
                            megj2,
                            intrem,
                            igeny,
                            mozgatott,
                            status,
                            unit,
                            id,
                            kontenerId,
                            balance
                        )
                    )
                } while (resultSet.next())
                val bundle = Bundle()
                bundle.putSerializable("CIKKLEZAR", kontenerCikkLezar)
                bundle.putString("KONTENER_ID", kontener_id)
                bundle.putBoolean("LEZARBUTN", false)
                context.hatosFragment?.arguments = bundle
                context.supportFragmentManager.beginTransaction()
                    .replace(
                        R.id.data_frame3,
                        context.hatosFragment!!,
                        "CIKKLEZARASFRAGMENTHATOS"
                    )
                    .commit()
                CoroutineScope(Dispatchers.Main).launch {
                    progress.visibility = View.GONE
                }
            }
            connection.close()
        } catch (e: Exception) {
            writeLog(e.stackTraceToString(), "arg1 $kontener_id")
            Log.d(TAG, "loadKontenerCikkek: $e")
            CoroutineScope(Dispatchers.Main).launch {
                progress.visibility = View.GONE
                context.setAlert("Hatos cikkek betlöltés hiba")
            }
        }
    }

    fun cikkAdataokSql(/////////////////////////////////4-2
        cikk: String?,
        megj1: String?,
        megj2: String?,
        intrem: String?,
        igeny: Double,
        unit: String?,
        id: Int,
        kontnerNumber: Int,
        context: MainActivity
    ) {
        val connection: Connection
        Class.forName("net.sourceforge.jtds.jdbc.Driver")
        try {
            CoroutineScope(Dispatchers.Main).launch {
                progress.visibility = View.VISIBLE
            }
            connection = DriverManager.getConnection(connectionString)
            val statement =
                connection.prepareStatement(res.getString(R.string.cikkCheck))// ezt is ki kell javítani, hogy 1 v kettő legyen jó státusz
            statement.setInt(1, id)
            statement.setString(2, dolgKod)
            val resultSet = statement.executeQuery()
            if (!resultSet.next()) {
                CoroutineScope(Dispatchers.Main).launch {
                    context.setAlert("Nem tudod megnyitni, mert már valaki dolgozik benne")
                    progress.visibility = View.GONE
                }
            } else {
                val listOfBin: ArrayList<PolcLocation> = ArrayList()
                val statement1 =
                    connection.prepareStatement(res.getString(R.string.cikkUpdate))
                statement1.setInt(1, 2)
                statement1.setString(2, dolgKod)
                statement1.setInt(3, id)
                statement1.executeUpdate()
                val tempPolcLocations: ArrayList<PolcLocation> = ArrayList()
                val statement6 =
                    connection.prepareStatement(res.getString(R.string.fillArray))
                statement6.setInt(1, id)
                val resultSet6 = statement6.executeQuery()
                if (!resultSet6.next()) {
                    Log.d(TAG, "cikkAdatok: nincsenek ilyen rekordok")
                    CoroutineScope(Dispatchers.Main).launch {
                        progress.visibility = View.GONE
                    }
                } else {
                    do {
                        val bin = resultSet6.getString("kiado_rakhely")
                        val sum = resultSet6.getString("mozgatott_mennyiseg")
                        tempPolcLocations.add(PolcLocation(bin, sum))
                    } while (resultSet6.next())
                }
                //ide kell hogy megnézze mi van a raktar_kontenerben
                val statement5 =
                    connection.prepareStatement(res.getString(R.string.raktarTetelIdeiglenes))
                statement5.setInt(1, id)
                val resultSet5 = statement5.executeQuery()
                if (!resultSet5.next()) {
                    //HA NINCS AZ ÁTMENETI ADATTÁBLÁBA ÉRTÉK
                    val statement2 =
                        connection.prepareStatement(res.getString(R.string.raktarCheck))
                    statement2.setString(1, cikk)
                    statement2.setString(2,MainActivity.fusetech)
                    val resultSet2 = statement2.executeQuery()
                    if (!resultSet2.next()) {
                        val statement3 = connection.prepareStatement(res.getString(R.string.cikkSomewhere))
                        statement3.setString(1,cikk)
                        val resultSet3 = statement3.executeQuery()
                        if(!resultSet3.next()){
                            CoroutineScope(Dispatchers.Main).launch {
                                val builder = AlertDialog.Builder(context)
                                builder.setTitle("Nincs készleten")
                                builder.setMessage("Nincs raktárkészleten az adott cikk, ezért ez 0 mennyiséggel lezárásra kerül.\nFolytatja?")
                                builder.setPositiveButton("Igen") { _, _ ->
                                    CoroutineScope(Dispatchers.IO).launch {
                                        context.checkIfContainerIsDone(
                                            kontnerNumber.toString(),
                                            id.toString(),
                                            "02",
                                            ""
                                        )
                                        context.updateItemStatus(id.toString(), 3)
                                        context.updateItemAtvevo(id.toString())
                                        context.checkIfContainerIsDone(
                                            kontnerNumber.toString(),
                                            id.toString(),
                                            "02",
                                            ""
                                        )
                                        context.loadKoztes()
                                        context.checkIfContainerStatus(kontnerNumber.toString())
                                        context.removeFragment("NEGYESCIKKEK")
                                    }
                                }
                                builder.setNegativeButton("Nem") { _, _ ->
                                    context.hideSystemUI()
                                }
                                builder.setOnCancelListener {
                                    context.hideSystemUI()
                                }
                                builder.create()
                                builder.show().getButton(DialogInterface.BUTTON_POSITIVE).requestFocus()
                                //context.setAlert("Nincs készleten")
                                progress.visibility = View.GONE
                            }
                        }else{
                            var message = ""
                            do {
                                val balance = resultSet3.getString("BalanceQty").toDouble()
                                val binNumber = resultSet3.getString("BinNumber")
                                val unit1 = resultSet3.getString("Unit")
                                message += "$balance\t$unit1\t\t$binNumber\n"
                            }while (resultSet3.next())
                            CoroutineScope(Dispatchers.Main).launch { //ide kell írni hogy ha nincs a készleten zárja le nullával
                                val builder = AlertDialog.Builder(context)
                                builder.setTitle("Nincs készleten")
                                builder.setMessage("Nincs raktárkészleten az adott cikk, ezért ez 0 mennyiséggel lezárásra kerül.\nFolytatja?")
                                builder.setPositiveButton("Igen") { _, _ ->
                                    //val email = Email()
                                    CoroutineScope(Dispatchers.IO).launch {
                                        //email.sendEmail("KanBan@fusetech.hu","keszlet.modositas@fusetech.hu","Meghiúsult kiszolgálás","A $cikk\t$megj1\t$megj2\t$intrem\tnincs a 02 raktárban, viszont megtalálhatók: \n$message")
                                        emailMessage += "A $cikk\t$megj1\t$megj2\t$intrem\tnincs a 02 raktárban, viszont megtalálhatók: \n$message\n"
                                            context.checkIfContainerIsDone(
                                            kontnerNumber.toString(),
                                            id.toString(),
                                            "02",
                                            ""
                                        )
                                        context.updateItemStatus(id.toString(), 3)
                                        context.updateItemAtvevo(id.toString())
                                        context.checkIfContainerIsDone(
                                            kontnerNumber.toString(),
                                            id.toString(),
                                            "02",
                                            ""
                                        )
                                        context.loadKoztes()
                                        context.checkIfContainerStatus(kontnerNumber.toString())
                                        context.removeFragment("NEGYESCIKKEK")
                                    }
                                }
                                builder.setNegativeButton("Nem") { _, _ ->
                                    context.hideSystemUI()
                                }
                                builder.setOnCancelListener {
                                    context.hideSystemUI()
                                }
                                builder.create()
                                builder.show().getButton(DialogInterface.BUTTON_POSITIVE).requestFocus()
                                //context.setAlert("Nincs készleten")
                                progress.visibility = View.GONE
                            }
                        }

                    } else {
                        context.igenyKontenerKiszedesCikkKiszedes =
                            IgenyKontenerKiszedesCikkKiszedes()
                        val myList: ArrayList<PolcLocation> = ArrayList()
                        do {
                            val polc = resultSet2.getString("BinNumber")
                            val mennyiseg = resultSet2.getDouble("BalanceQty").toString()
                            myList.add(PolcLocation(polc, mennyiseg))
                        } while (resultSet2.next())
                        val bundle = Bundle()
                        bundle.putString("K_CIKK", cikk)
                        bundle.putString("K_MEGJ1", megj1)
                        bundle.putString("K_MEGJ2", megj2)
                        bundle.putString("K_INT", intrem)
                        bundle.putDouble("K_IGENY", igeny)
                        bundle.putString("K_UNIT", unit)
                        bundle.putInt("K_KONTENER", kontnerNumber)
                        bundle.putInt("K_ID", id)
                        bundle.putSerializable("K_LIST", myList)
                        bundle.putSerializable("K_POLC", listOfBin)
                        bundle.putSerializable("K_TOMB", tempPolcLocations)
                        context.igenyKontenerKiszedesCikkKiszedes?.arguments = bundle
                        context.supportFragmentManager.beginTransaction().replace(
                            R.id.frame_container,
                            context.igenyKontenerKiszedesCikkKiszedes!!,
                            "KISZEDESCIKK"
                        ).commit()
                    }
                    CoroutineScope(Dispatchers.Main).launch {
                        progress.visibility = View.GONE
                    }
                } else {
                    context.igenyKontenerKiszedesCikkKiszedes = IgenyKontenerKiszedesCikkKiszedes()
                    //HA VAN AZ ÁTMENETI ADATTÁBLÁBA ÉRTÉK
                    var a = 0.0
                    do {
                        a += resultSet5.getDouble("mozgatott_mennyiseg")
                        listOfBin.add(
                            PolcLocation(
                                resultSet5.getString("kiado_rakhely"),
                                resultSet5.getDouble("mozgatott_mennyiseg").toString()
                            )
                        )
                    } while (resultSet5.next())
                    val ujIgeny = igeny - a
                    val statement2 =
                        connection.prepareStatement(res.getString(R.string.raktarCheck))
                    statement2.setString(1, cikk)
                    statement2.setString(2,MainActivity.fusetech)
                    val resultSet2 = statement2.executeQuery()
                    if (!resultSet2.next()) {
                        val statement3 = connection.prepareStatement(res.getString(R.string.cikkSomewhere))
                        statement3.setString(1,cikk)
                        val resultSet3 = statement3.executeQuery()
                        if(!resultSet3.next()){
                            CoroutineScope(Dispatchers.Main).launch {
                                val builder = AlertDialog.Builder(context)
                                builder.setTitle("Nincs készleten")
                                builder.setMessage("Nincs raktárkészleten az adott cikk, ezért ez 0 mennyiséggel lezárásra kerül.\nFolytatja?")
                                builder.setPositiveButton("Igen") { _, _ ->
                                    CoroutineScope(Dispatchers.IO).launch {
                                        context.checkIfContainerIsDone(
                                            kontnerNumber.toString(),
                                            id.toString(),
                                            "02",
                                            ""
                                        )
                                        context.updateItemStatus(id.toString(), 3)
                                        context.updateItemAtvevo(id.toString())
                                        context.checkIfContainerIsDone(
                                            kontnerNumber.toString(),
                                            id.toString(),
                                            "02",
                                            ""
                                        )
                                        context.loadKoztes()
                                        context.checkIfContainerStatus(kontnerNumber.toString())
                                        context.removeFragment("NEGYESCIKKEK")
                                    }
                                    builder.setNegativeButton("Nem") { _, _ ->
                                        context.hideSystemUI()
                                    }
                                    builder.setOnCancelListener {
                                       context.hideSystemUI()
                                    }
                                    builder.create()
                                    builder.show().getButton(DialogInterface.BUTTON_POSITIVE)
                                        .requestFocus()
                                } // ide kell írni hogy zárja le nullával automatikusan
                                progress.visibility = View.GONE
                            }
                        }else {
                            var message = ""
                            do {
                                val balance = resultSet3.getString("BalanceQty").toDouble()
                                val binNumber = resultSet3.getString("BinNumber")
                                val unit1 = resultSet3.getString("Unit")
                                message += "$balance\t$unit1\t\t$binNumber\n"
                            } while (resultSet3.next())
                            CoroutineScope(Dispatchers.Main).launch {
                                val builder = AlertDialog.Builder(context)
                                builder.setTitle("Nincs készleten")
                                builder.setMessage("Nincs raktárkészleten az adott cikk, ezért ez 0 mennyiséggel lezárásra kerül.\nFolytatja?")
                                builder.setPositiveButton("Igen") { _, _ ->
                                //    val email = Email()
                                CoroutineScope(Dispatchers.IO).launch {
                                    emailMessage += "A $cikk\t$megj1\t$megj2\t$intrem\tnincs a 02 raktárban, viszont megtalálhatók: \n$message\n"
                                   // email.sendEmail("KanBan@fusetech.hu","keszlet.modositas@fusetech.hu","Meghiúsult kiszolgálás","A $cikk\t$megj1\t$megj2\t$intrem\tnincs a 02 raktárban, viszont megtalálhatók: \n$message")
                                    context.checkIfContainerIsDone(
                                            kontnerNumber.toString(),
                                            id.toString(),
                                            "02",
                                            ""
                                        )
                                        context.updateItemStatus(id.toString(), 3)
                                        context.updateItemAtvevo(id.toString())
                                        context.checkIfContainerIsDone(
                                            kontnerNumber.toString(),
                                            id.toString(),
                                            "02",
                                            ""
                                        )
                                        context.loadKoztes()
                                        context.checkIfContainerStatus(kontnerNumber.toString())
                                        context.removeFragment("NEGYESCIKKEK")
                                    }
                                    builder.setNegativeButton("Nem") { _, _ ->
                                        context.hideSystemUI()
                                    }
                                    builder.setOnCancelListener {
                                        context.hideSystemUI()
                                    }
                                    builder.create()
                                    builder.show().getButton(DialogInterface.BUTTON_POSITIVE)
                                        .requestFocus()
                                } // ide kell írni hogy zárja le nullával automatikusan
                                progress.visibility = View.GONE
                            }
                        }
                    } else {
                        val myList: ArrayList<PolcLocation> = ArrayList()
                        do {
                            val polc = resultSet2.getString("BinNumber")
                            val mennyiseg = resultSet2.getDouble("BalanceQty").toString()
                            myList.add(PolcLocation(polc, mennyiseg))
                        } while (resultSet2.next())
                        val bundle = Bundle()
                        bundle.putString("K_CIKK", cikk)
                        bundle.putString("K_MEGJ1", megj1)
                        bundle.putString("K_MEGJ2", megj2)
                        bundle.putString("K_INT", intrem)
                        bundle.putDouble("K_IGENY", ujIgeny)
                        bundle.putString("K_UNIT", unit)
                        bundle.putInt("K_KONTENER", kontnerNumber)
                        bundle.putInt("K_ID", id)
                        bundle.putSerializable("K_LIST", myList)
                        bundle.putSerializable("K_POLC", listOfBin)
                        bundle.putSerializable("K_TOMB", tempPolcLocations)
                        context.igenyKontenerKiszedesCikkKiszedes?.arguments = bundle
                        context.supportFragmentManager.beginTransaction().replace(
                            R.id.frame_container,
                            context.igenyKontenerKiszedesCikkKiszedes!!,
                            "KISZEDESCIKK"
                        ).commit()
                    }
                }
                CoroutineScope(Dispatchers.Main).launch {
                    progress.visibility = View.GONE
                }
            }
            connection.close()
        } catch (e: Exception) {
            CoroutineScope(Dispatchers.Main).launch {
                context.setAlert("Cikk adatok hiba")
                writeLog(e.stackTraceToString(), "arg1 $cikk arg2 $megj1 arg3 $megj2 arg4 $intrem arg5 $igeny arg6 $unit arg7 $id arg8 $kontnerNumber")
            }
        }
        Log.d(TAG, "cikkAdatok: ")
    }

    fun cikkCodeSql(code: Int, context: MainActivity) {
        val connection: Connection
        Class.forName("net.sourceforge.jtds.jdbc.Driver")
        try {
            CoroutineScope(Dispatchers.Main).launch {
                progress.visibility = View.VISIBLE
            }
            connection = DriverManager.getConnection(url)
            val statement = connection.prepareStatement(res.getString(R.string.getAtvevo))
            statement.setInt(1, code)
            val resultSet = statement.executeQuery()
            if (!resultSet.next()) {
                CoroutineScope(Dispatchers.Main).launch {
                    context.setAlert("Nincs neki átvevője")
                    progress.visibility = View.GONE
                }
            } else {
                val atvevo = resultSet.getString("atvevo")
                val statement1 = connection.prepareStatement(res.getString(R.string.nev))
                statement1.setString(1, atvevo)
                val resultSet1 = statement1.executeQuery()
                if (!resultSet1.next()) {
                    CoroutineScope(Dispatchers.Main).launch {
                        context.setAlert("Nem fogja senki")
                        progress.visibility = View.GONE
                    }
                } else {
                    val nev = resultSet1.getString("TextDescription")
                    CoroutineScope(Dispatchers.Main).launch {
                        context.setAlert("$nev fogja a cikket")
                        progress.visibility = View.GONE
                    }
                }
            }
            connection.close()
        } catch (e: Exception) {
            CoroutineScope(Dispatchers.Main).launch {
                context.setAlert("Probléma a nevekkel $e")
                progress.visibility = View.GONE
                writeLog(e.stackTraceToString(), "arg1 $code")
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun checkEllenorzoKodSql(code: String, context: MainActivity) {
        val connection: Connection
        Class.forName("net.sourceforge.jtds.jdbc.Driver")
        try {
            CoroutineScope(Dispatchers.Main).launch {
                context.ellenorzoKodFragment?.setProgressBarOn()
            }
            connection = DriverManager.getConnection(connectionString)
            val statement =
                connection.prepareStatement(res.getString(R.string.kontenerBinDesciption))
            statement.setString(1, context.selectedContainer)
            val resultSet = statement.executeQuery()
            if (!resultSet.next()) {
                CoroutineScope(Dispatchers.Main).launch {
                    context.setAlert("Gáz van")
                    context.ellenorzoKodFragment?.setProgressBarOff()
                }
            } else {
                val ellKod = resultSet.getString("BinDescript2")
                if (code.trim() == ellKod) {
                    val state =
                        connection.prepareStatement(res.getString(R.string.kontenerKiszedve))
                    val myDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
                    state.setString(1, myDate)
                    state.setInt(2, 3) //5 hogy ne látszódjon kint a raktárba
                    state.setString(3, context.selectedContainer)
                    state.executeUpdate()
                    context.igenyKontenerKiszedes()
                    CoroutineScope(Dispatchers.Main).launch {
                        //context.setAlert("ITT kell lezárni a konténert")
                        context.ellenorzoKodFragment?.setProgressBarOff()
                    }
                } else {
                    CoroutineScope(Dispatchers.Main).launch {
                        context.setAlert("Nem egyezik a kód a szállító járművel")
                        context.ellenorzoKodFragment?.setProgressBarOff()
                    }
                }
            }
            connection.close()
        } catch (e: Exception) {
            CoroutineScope(Dispatchers.Main).launch {
                context.setAlert("Ellenőrző kódnál hiba")
                context.ellenorzoKodFragment?.setProgressBarOff()
                writeLog(e.stackTraceToString(), "arg1 $code")
            }
        }
    }

    fun checkIfContainerIsDoneSql(
        container: String,
        itemId: String,
        raktar: String,
        polc: String,
        context: MainActivity
    ) {
        val connection: Connection
        Class.forName("net.sourceforge.jtds.jdbc.Driver")
        val mozgatott: Double
        val szallito: String
        try {
            CoroutineScope(Dispatchers.Main).launch {
                progress.visibility = View.VISIBLE
            }
            connection = DriverManager.getConnection(connectionString)
            val statement1 =
                connection.prepareStatement(res.getString(R.string.getMozgatottMennyiseg))
            statement1.setString(1, itemId)
            val resultSet1 = statement1.executeQuery()
            if (!resultSet1.next()) {
                Log.d(TAG, "checkIfContainerIsDone: nincs mozgatott mennyiség (hazugság)")
                CoroutineScope(Dispatchers.Main).launch {
                    progress.visibility = View.GONE
                }
            } else {
                mozgatott = resultSet1.getDouble("mozgatott_mennyiseg")
                val statement2 =
                    connection.prepareStatement(res.getString(R.string.getSzallitoJarmu))
                statement2.setString(1, container)
                val resultSet2 = statement2.executeQuery()
                if (!resultSet2.next()) {
                    CoroutineScope(Dispatchers.Main).launch {
                        progress.visibility = View.GONE
                    }
                    Log.d(TAG, "checkIfContainerIsDone: nincs szállítójármű")
                } else {
                    szallito = resultSet2.getString("SzallitoJarmu")
                    val statement3 =
                        connection.prepareStatement(res.getString(R.string.updateKontenerTetel))
                    statement3.setDouble(1, mozgatott)
                    statement3.setString(2, raktar)
                    statement3.setString(3, polc)
                    statement3.setString(4, szallito)
                    statement3.setString(5, itemId)
                    statement3.executeUpdate()
                    Log.d(TAG, "checkIfContainerIsDone: Sikeres update")
                    CoroutineScope(Dispatchers.Main).launch {
                        progress.visibility = View.GONE
                    }
                }
            }
            connection.close()
        } catch (e: Exception) {
            CoroutineScope(Dispatchers.Main).launch {
                context.setAlert("Probléma a konténer ellenőrzésével $e")
                progress.visibility = View.GONE
                writeLog(e.stackTraceToString(), "arg1 $container arg2 $itemId arg3 $raktar arg4 $polc")
            }
        }
    }

    fun updateItemAtvevoSql(itemId: String, context: MainActivity) {
        val connection: Connection
        Class.forName("net.sourceforge.jtds.jdbc.Driver")
        try {
            CoroutineScope(Dispatchers.Main).launch {
                progress.visibility = View.VISIBLE
            }
            connection = DriverManager.getConnection(connectionString)
            val statement =
                connection.prepareStatement(res.getString(R.string.updateCikkAtvevo))
            statement.setString(1, dolgKod)
            statement.setString(2, itemId)
            statement.executeUpdate()
            CoroutineScope(Dispatchers.Main).launch {
                progress.visibility = View.GONE
            }
            connection.close()
        } catch (e: Exception) {
            CoroutineScope(Dispatchers.Main).launch {
                context.setAlert("Nem tudom az átvevőt kinullázni")
                progress.visibility = View.GONE
                writeLog(e.stackTraceToString(), "arg1 $itemId")
            }
        }
    }

    fun updtaeItemStatusSql(itemId: String, context: MainActivity, status: Int) {
        val connection: Connection
        Class.forName("net.sourceforge.jtds.jdbc.Driver")
        try {
            CoroutineScope(Dispatchers.Main).launch {
                progress.visibility = View.VISIBLE
            }
            connection = DriverManager.getConnection(connectionString)
            val statement =
                connection.prepareStatement(res.getString(R.string.updateKontenerTeletStatusz))
            statement.setInt(1, status)
            statement.setString(2, itemId)
            statement.executeUpdate()
            context.igenyKontenerKiszedesCikkKiszedes?.isUpdated = true
            CoroutineScope(Dispatchers.Main).launch {
                progress.visibility = View.GONE
            }
            connection.close()
        } catch (e: Exception) {
            CoroutineScope(Dispatchers.Main).launch {
                context.setAlert("Probléma a tétel 3-ra írásával")
                progress.visibility = View.GONE
                writeLog(e.stackTraceToString(), "arg1 $itemId arg2 $status")
            }
        }
    }

    fun insertDataToRaktarTetelSql(
        cikk: String,
        mennyiseg: Double,
        raktarKod: String,
        polc: String,
        context: MainActivity
    ) {
        val connection: Connection
        Class.forName("net.sourceforge.jtds.jdbc.Driver")
        try {
            connection = DriverManager.getConnection(connectionString)
            val statement =
                connection.prepareStatement(res.getString(R.string.insertTemporary))
            statement.setString(1, cikk)
            statement.setDouble(2, mennyiseg)
            statement.setString(3, raktarKod)
            statement.setString(4, polc)
            statement.executeUpdate()
            context.igenyKontenerKiszedesCikkKiszedes?.isSaved = true
            connection.close()
        } catch (e: Exception) {
            CoroutineScope(Dispatchers.Main).launch {
                context.setAlert("Probléma a ratar_tetel feltöltésnél")
                writeLog(e.stackTraceToString(), "arg1 $cikk arg2 $mennyiseg arg3 $raktarKod arg4 $polc")
            }
        }
    }

    fun cikkUpdateSql(cikk: Int, context: MainActivity) {
        val connection: Connection
        Class.forName("net.sourceforge.jtds.jdbc.Driver")
        try {
            CoroutineScope(Dispatchers.Main).launch {
                progress.visibility = View.VISIBLE
            }
            connection = DriverManager.getConnection(connectionString)
            val statement =
                connection.prepareStatement(res.getString(R.string.cikkUpdate))
            statement.setInt(1, 1)
            statement.setNull(2, Types.INTEGER)
            statement.setInt(3, cikk)
            statement.executeUpdate()
            Log.d(TAG, "cikkUpdate: sikeres")
            CoroutineScope(Dispatchers.Main).launch {
                progress.visibility = View.GONE
            }
            connection.close()
        } catch (e: Exception) {
            CoroutineScope(Dispatchers.Main).launch {
                context.setAlert("CikkUpdateHiba")
                progress.visibility = View.GONE
                writeLog(e.stackTraceToString(), "arg1 $cikk")
            }
        }
    }

    fun updateCikkSql(kontener_id: String, context: MainActivity) {
        val connection: Connection
        Class.forName("net.sourceforge.jtds.jdbc.Driver")
        try {
            connection = DriverManager.getConnection(connectionString)
            val statement =
                connection.prepareStatement(res.getString(R.string.updateItemStatus))
            statement.setInt(1,1)
            statement.setString(2, kontener_id)
            statement.executeUpdate()
            Log.d(TAG, "updateCikkAndKontener: Cikkek lezárva")
            connection.close()
        } catch (e: Exception) {
            Log.d(TAG, "updateCikkAndKontener: $e")
            CoroutineScope(Dispatchers.Main).launch {
                context.setAlert("Probléma van")
            }
            writeLog(e.stackTraceToString(), "arg1 $kontener_id")
        }
    }

    fun updateKontenerSql(kontener_id: String, context: MainActivity) {
        val connection: Connection
        Class.forName("net.sourceforge.jtds.jdbc.Driver")
        try {
            connection = DriverManager.getConnection(connectionString)
            val statment =
                connection.prepareStatement(res.getString(R.string.updateContainerStatus))
            statment.setInt(1, 1)
            statment.setNull(2, Types.INTEGER)
            statment.setNull(3, Types.INTEGER)
            //statment.setString(3, dolgKod)//ide kell a bejelentkezős kód
            statment.setString(4, kontener_id)
            statment.executeUpdate()
            loadIgenyLezaras(context)
            Log.d(TAG, "updateCikkAndKontener: Konténer lezárva")
            context.lezarandoKontener = ""
            connection.close()
        } catch (e: Exception) {
            Log.d(TAG, "updateKontener: $e")
            CoroutineScope(Dispatchers.Main).launch {
                context.setAlert("Probléma van a konténer 1-re átírásánál")
            }
            writeLog(e.stackTraceToString(), "arg1 $kontener_id")
        }
    }

    fun chekcPolcAndSetBinSql(code: String, context: MainActivity) {
        val connection: Connection
        Class.forName("net.sourceforge.jtds.jdbc.Driver")
        try {
            CoroutineScope(Dispatchers.Main).launch {
                progress.visibility = View.VISIBLE
            }
            connection = DriverManager.getConnection(url)
            val statement = connection.prepareStatement(res.getString(R.string.isPolc))
            statement.setString(1, code)
            val resultSet = statement.executeQuery()
            if (!resultSet.next()) {
                CoroutineScope(Dispatchers.Main).launch {
                    context.setAlert("Nincs ilyen polc")
                    progress.visibility = View.GONE
                }
            } else {
                CoroutineScope(Dispatchers.Main).launch {
                    context.igenyKontenerKiszedesCikkKiszedes?.setBin(code)
                    progress.visibility = View.GONE
                }
            }
            connection.close()
        } catch (e: Exception) {
            CoroutineScope(Dispatchers.Main).launch {
                context.setAlert("Probléma")
                progress.visibility = View.GONE
                writeLog(e.stackTraceToString(), "arg1 $code")
            }
        }
    }

    fun getContainersFromVehicle(code: String, context: MainActivity) {
        try {
            val fragment = SzerelohelyListaFragment()
            CoroutineScope(Dispatchers.Main).launch {
                progress.visibility = View.VISIBLE
            }
            Class.forName("net.sourceforge.jtds.jdbc.Driver")
            val connection: Connection = DriverManager.getConnection(connectionString)
            val statement =
                connection.prepareStatement(res.getString(R.string.igenyKontenerKihelyezesLista))
            statement.setString(1, code)
            statement.setString(2, sz01KiszedesDate)
            val resultSet = statement.executeQuery()
            if (!resultSet.next()) {
                CoroutineScope(Dispatchers.Main).launch {
                    context.setAlert("Nem jó szállítójármű")
                    context.kihelyezes?.mindentVissza()
                    progress.visibility = View.GONE
                }
            } else {
                sz0x = code
                val myList: ArrayList<SzerelohelyItem> = ArrayList()
                do {
                    val szerelohely = resultSet.getString("termeles_rakhely")
                    myList.add(SzerelohelyItem(szerelohely.uppercase(Locale.ROOT)))
                } while (resultSet.next())
                CoroutineScope(Dispatchers.Main).launch {
                    progress.visibility = View.GONE
                }
                val bundle = Bundle()
                bundle.putSerializable("KILISTA", myList)
                fragment.arguments = bundle
                context.supportFragmentManager.beginTransaction()
                    .replace(R.id.kihelyezesFrame, fragment, "KIHELYEZESLISTA").commit()
            }
            connection.close()
        } catch (e: Exception) {
            CoroutineScope(Dispatchers.Main).launch {
                context.setAlert("Nem lehetett a kihelyezéslistát megnyitni")
                context.kihelyezes?.mindentVissza()
                progress.visibility = View.GONE
                writeLog(e.stackTraceToString(), "arg1 $code")
            }
        }
    }

    fun loadKihelyezesItemsSql(code: String, context: MainActivity) {
        try {
            context.kihelyezesFragmentLista = KihelyezesListaFragment()
            CoroutineScope(Dispatchers.Main).launch {
                progress.visibility = View.VISIBLE
            }
            val myList: ArrayList<KihelyezesKontenerElemek> = ArrayList()
            Class.forName("net.sourceforge.jtds.jdbc.Driver")
            val connection: Connection = DriverManager.getConnection(connectionString)
            val statement =
                connection.prepareStatement(res.getString(R.string.igenyKontenerKihelyezesElemekLista))
            statement.setString(1, code)
            statement.setString(2, sz01KiszedesDate)
            val resultSet = statement.executeQuery()
            if (!resultSet.next()) {
                CoroutineScope(Dispatchers.Main).launch {
                    context.setAlert("Üres a konténer")
                    context.kihelyezes?.setFocusToBin()
                    progress.visibility = View.GONE
                }
            } else {
                do {
                    val id = resultSet.getInt("id")
                    val cikk = resultSet.getString("cikkszam")
                    val megj1 = resultSet.getString("Description1")
                    val megj2 = resultSet.getString("Description2")
                    val intrem = resultSet.getString("InternRem1")
                    val igeny = resultSet.getDouble("igenyelt_mennyiseg")
                    val igenyelve = igeny.toString() + " " + resultSet.getString("Unit")
                    val kiadva = resultSet.getInt("mozgatott_mennyiseg")
                    val kontenerID = resultSet.getInt("kontener_id")
                    myList.add(
                        KihelyezesKontenerElemek(
                            id,
                            cikk,
                            megj1,
                            megj2,
                            intrem,
                            igenyelve,
                            kiadva,
                            kontenerID
                        )
                    )
                } while (resultSet.next())
                CoroutineScope(Dispatchers.Main).launch {
                    progress.visibility = View.GONE
                }
                val bundle = Bundle()
                bundle.putSerializable("KIHELYEZESLISTA", myList)
                bundle.putString("KIHELYEZESHELY", code)
                context.kihelyezesFragmentLista?.arguments = bundle
                context.supportFragmentManager.beginTransaction().replace(
                    R.id.kihelyezesFrame,
                    context.kihelyezesFragmentLista!!,
                    "KIHELYEZESITEMS"
                ).commit()
            }
            connection.close()
        } catch (e: Exception) {
            CoroutineScope(Dispatchers.Main).launch {
                context.setAlert("Probléma a kihelyezésnél")
                context.kihelyezes?.setFocusToBin()
                progress.visibility = View.GONE
                writeLog(e.stackTraceToString(), "arg1 $code")
            }
        }
    }

    fun closeCikkek(code: Int, context: MainActivity) {
        try {
            CoroutineScope(Dispatchers.Main).launch {
                progress.visibility = View.VISIBLE
            }
            Class.forName("net.sourceforge.jtds.jdbc.Driver")
            val connection: Connection = DriverManager.getConnection(connectionString)
            val statement = connection.prepareStatement(res.getString(R.string.cikkLezarva))
            statement.setInt(1, code)
            statement.executeUpdate()
            CoroutineScope(Dispatchers.Main).launch {
                progress.visibility = View.GONE
            }
            connection.close()
        } catch (e: Exception) {
            CoroutineScope(Dispatchers.Main).launch {
                context.setAlert("Probléma a cikkek lezárásánál")
                progress.visibility = View.GONE
                writeLog(e.stackTraceToString(), "arg1 $code")
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun closeContainer(code: String, context: MainActivity) {
        try {
            CoroutineScope(Dispatchers.Main).launch {
                context.kihelyezes?.progressBarOn()
            }
            Class.forName("net.sourceforge.jtds.jdbc.Driver")
            val connection: Connection = DriverManager.getConnection(connectionString)
            val statement = connection.prepareStatement(res.getString(R.string.kontenerKihelyezve))
            val datum = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
            statement.setString(1, datum)
            statement.setInt(2, 5)
            statement.setInt(3, code.toInt())
            statement.executeUpdate()
            connection.close()
        } catch (e: Exception) {
            CoroutineScope(Dispatchers.Main).launch {
                context.setAlert("Konténer lezárás hiba")
                context.kihelyezes?.progressBarOff()
                writeLog(e.stackTraceToString(), "arg1 $code")
            }
        }
    }

    fun closeReloadContainer(context: MainActivity){
        CoroutineScope(Dispatchers.Main).launch {
            context.kihelyezes?.progressBarOff()
            context.kihelyezes?.onBack()
            context.kihelyezesFragmentLista = null
            CoroutineScope(Dispatchers.IO).launch {
                CoroutineScope(Dispatchers.Main).launch {
                    context.removeFragment("KIHELYEZESITEMS")
                    context.getContainerList(sz0x)
                }
            }
        }
    }

    fun tobbletKontenerElemek(context: MainActivity) {
        try {
            context.tobbletKontenerKihelyzeseFragment = TobbletKontenerKihelyzeseFragment()
            //context.menuFragment = MenuFragment()
            val kontenerItem: ArrayList<KontenerItem> = ArrayList()
            CoroutineScope(Dispatchers.Main).launch {
                if (context.menuFragment != null) {
                    context.menuFragment?.setMenuProgressOn()
                } else {
                    progress.visibility = View.VISIBLE
                }
            }
            Class.forName("net.sourceforge.jtds.jdbc.Driver")
            val connection: Connection = DriverManager.getConnection(connectionString)
            val statement =
                connection.prepareStatement(res.getString(R.string.tobbletKontenerLista))
            statement.setString(1,MainActivity.fusetech)
            val resultSet = statement.executeQuery()
            if (!resultSet.next()) {
                CoroutineScope(Dispatchers.Main).launch {
                    if (context.menuFragment != null) {
                        context.menuFragment?.setMenuProgressOff()
                        context.setAlert("Nincs többlet konténer")
                    } else {
                        progress.visibility = View.GONE
                    }
                    context.loadMenuFragment(true)
                }
            } else {
                tobbletKontener.clear()
                do { //Itt kéne a dátumot beírni
                    val id: String? = resultSet.getString("id")
                    val kontener = resultSet.getString("kontener")
                    val statusz = resultSet.getInt("statusz")
                    val igenyel: String? = resultSet.getString("igenyelve")
                    val tetelszam = resultSet.getInt("tetelszam")
                    val polc: String? = resultSet.getString("polc")
                    kontenerItem.add(
                        KontenerItem(
                            kontener,
                            polc,
                            igenyel,
                            tetelszam,
                            id,
                            statusz
                        )
                    )
                } while (resultSet.next())
                val bundle = Bundle()
                bundle.putSerializable("TOBBLETKONTENEREK", kontenerItem)
                context.tobbletKontenerKihelyzeseFragment?.arguments = bundle
                context.supportFragmentManager.beginTransaction().replace(
                    R.id.frame_container,
                    context.tobbletKontenerKihelyzeseFragment!!,
                    "TKK"
                ).commit()
                CoroutineScope(Dispatchers.Main).launch {
                    if (context.menuFragment != null) {
                        context.menuFragment?.setMenuProgressOff()
                    } else {
                        progress.visibility = View.GONE
                    }
                }
                //context.menuFragment = null
            }
            connection.close()
        } catch (e: Exception) {
            CoroutineScope(Dispatchers.Main).launch {
                context.setAlert("Többlet konténer elemek hiba")
                writeLog(e.stackTraceToString(), "tobbletKontenerElemek")
                if (context.menuFragment != null) {
                    context.menuFragment?.setMenuProgressOff()
                } else {
                    progress.visibility = View.GONE
                }
            }
        }
    }

    fun updateContainerAndOpenItems(code: String?, context: MainActivity) {
        try {
            context.tobbletCikkek = TobbletKontenerCikkekFragment()
            CoroutineScope(Dispatchers.Main).launch {
                /* if (context.tobbletKontenerKihelyzeseFragment != null) {
                     context.tobbletKontenerKihelyzeseFragment?.setProgressBar8On()
                 } else {
                     progress.visibility = View.VISIBLE
                 }*/
                progress.visibility = View.VISIBLE
            }
            Class.forName("net.sourceforge.jtds.jdbc.Driver") //EZT KÉNE KIIKTATNI HOGY ÁTÍRJA AZ ADATBÁZIST
            val connection = DriverManager.getConnection(connectionString)
            /*val statement =
                connection.prepareStatement(res.getString(R.string.updateContainerStatus))
            statement.setInt(1, 8)
            statement.setString(2, "SZ01")
            statement.setString(3, context.dolgKod)
            statement.setString(4, code)
            statement.executeUpdate()*/
            val statement2 =
                connection.prepareStatement(res.getString(R.string.tobbletKontnerCikkek))
            statement2.setString(1, code)
            val resultSet = statement2.executeQuery()
            if (!resultSet.next()) {
                CoroutineScope(Dispatchers.Main).launch {
                    context.setAlert("Nincsenek elemek")
                    context.loadMenuFragment(true)
                    /*if (context.tobbletKontenerKihelyzeseFragment != null) {
                        context.tobbletKontenerKihelyzeseFragment?.setProgressBar8Off()
                    } else {
                        progress.visibility = View.GONE
                    }*/
                    progress.visibility = View.GONE
                }
            } else {
                val tobbletCikkek: ArrayList<KontenerbenLezarasItem> = ArrayList()
                tobbletItem.clear()
                do {
                    val cikk = resultSet.getString("cikkszam")
                    val megj1 = resultSet.getString("Description1")
                    val megj2 = resultSet.getString("Description2")
                    val intrem = resultSet.getString("InternRem1")
                    val igeny = resultSet.getDouble("igenyelt_mennyiseg").toString()
                    val mozgatott = resultSet.getDouble("mozgatott_mennyiseg").toString()
                    val status = resultSet.getInt("statusz")
                    val unit = resultSet.getString("Unit")
                    val id = resultSet.getInt("id")
                    val kontenerId = resultSet.getInt("kontener_id")
                    val balance = resultSet.getDouble("StockBalance")
                    tobbletCikkek.add(
                        KontenerbenLezarasItem(
                            cikk,
                            megj1,
                            megj2,
                            intrem,
                            igeny,
                            mozgatott,
                            status,
                            unit,
                            id,
                            kontenerId,
                            balance
                        )
                    )
                } while (resultSet.next())
                val bundle = Bundle()
                bundle.putSerializable("TOBBLETESCIKKEK", tobbletCikkek)
                bundle.putString("KONTENERTOBBLETCIKK", code)
                context.tobbletCikkek?.arguments = bundle
                CoroutineScope(Dispatchers.Main).launch {
                    progress.visibility = View.GONE
                }
                context.supportFragmentManager.beginTransaction()
                    .replace(
                        R.id.frame_container,
                        context.tobbletCikkek!!,
                        "TOBBLETKIHELYEZESCIKKEK"
                    )
                    .commit()
            }
            connection.close()
        } catch (e: Exception) {
            CoroutineScope(Dispatchers.Main).launch {
                context.setAlert("8as nem tudta lezárni a konténert és megnyitni a másikat")
                writeLog(e.stackTraceToString(), "arg1 $code")
                progress.visibility = View.GONE
            }
        }
    }

    fun statuszVisszairas(code: String, context: MainActivity) {
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver")
            val connection = DriverManager.getConnection(connectionString)
            val statement =
                connection.prepareStatement(res.getString(R.string.updateContainerStatusJust))
            statement.setInt(1, 7)
            statement.setString(2, code)
            statement.executeUpdate()
            context.loadTobbletKontenerKihelyezes()
            connection.close()
        } catch (e: Exception) {
            CoroutineScope(Dispatchers.Main).launch {
                context.setAlert("Hiba a visszaíráskor")
                writeLog(e.stackTraceToString(), "arg1 $code")
            }
        }
    }

    fun openNyolcHarmas(
        id: Int,
        kontenerID: Int,
        megjegyzes: String,
        megjegyzes2: String,
        intrem: String,
        unit: String,
        mennyiseg: Double,
        cikkszam: String,
        context: MainActivity
    ) {
        try {
            //context.tobbletCikkek = TobbletKontenerCikkekFragment()
            context.tobbletCikkekPolcra = TobbletCikkekPolcraFragment()
            CoroutineScope(Dispatchers.Main).launch {
                progress.visibility = View.VISIBLE
            }
            val raktarBin: ArrayList<PolcLocation> = ArrayList()
            Class.forName("net.sourceforge.jtds.jdbc.Driver")
            val connection = DriverManager.getConnection(connectionString)
            val statement = connection.prepareStatement(res.getString(R.string.raktarCheck))
            statement.setString(1, cikkszam)
            statement.setString(2,MainActivity.fusetech)
            val resultSet = statement.executeQuery()
            if (!resultSet.next()) {
                val statement1 = connection.prepareStatement(res.getString(R.string.emptyBins))
                statement1.setString(1,MainActivity.fusetech)
                val resultSet1 = statement1.executeQuery()
                if (!resultSet1.next()) {
                    CoroutineScope(Dispatchers.Main).launch {
                        context.setAlert("Nincs a raktárban!")
                        progress.visibility = View.GONE
                    }
                } else {
                   // raktarBin.add(PolcLocation("STD02","0"))
                    do {
                        val binNumber = resultSet1.getString("BinNumber")
                        raktarBin.add(PolcLocation(binNumber, "0"))
                    } while (resultSet1.next())
                    val bundle = Bundle()
                    bundle.putSerializable("LOCATIONBIN", raktarBin)
                    bundle.putInt("IID", id)
                    bundle.putInt("KID", kontenerID)
                    bundle.putString("MMEGJ1", megjegyzes)
                    bundle.putString("MMEGJ2", megjegyzes2)
                    bundle.putString("IINT", intrem)
                    bundle.putString("UUNIT", unit)
                    bundle.putString("MMENNY", mennyiseg.toString())
                    bundle.putString("MCIKK", cikkszam)
                    context.tobbletCikkekPolcra?.arguments = bundle
                    CoroutineScope(Dispatchers.Main).launch {
                        progress.visibility = View.GONE
                    }
                    context.supportFragmentManager.beginTransaction()
                        .replace(
                            R.id.frame_container,
                            context.tobbletCikkekPolcra!!,
                            "CIKKEKPOLCRA"
                        )
                        .commit()
                }
            } else {
                //raktarBin.add(PolcLocation("STD02","0"))
                do {
                    val binNumber = resultSet.getString("BinNumber")
                    val mennyiseg2 = resultSet.getDouble("BalanceQty")
                    raktarBin.add(PolcLocation(binNumber, mennyiseg2.toString()))
                } while (resultSet.next())
                val statement3 = connection.prepareStatement(res.getString(R.string.emptyBins))
                statement3.setString(1,MainActivity.fusetech)
                val resultSet3 = statement3.executeQuery()
                if (!resultSet3.next()) {
                    CoroutineScope(Dispatchers.Main).launch {
                        context.setAlert("Nincs a raktárban!")
                        progress.visibility = View.GONE
                    }
                } else {
                    //raktarBin.add(PolcLocation("STD02","0"))
                    do {
                        val binNumber = resultSet3.getString("BinNumber")
                        raktarBin.add(PolcLocation(binNumber, "0"))
                    } while (resultSet3.next())
                }
                val bundle = Bundle()
                bundle.putSerializable("LOCATIONBIN", raktarBin)
                bundle.putInt("IID", id)
                bundle.putInt("KID", kontenerID)
                bundle.putString("MMEGJ1", megjegyzes)
                bundle.putString("MMEGJ2", megjegyzes2)
                bundle.putString("IINT", intrem)
                bundle.putString("UUNIT", unit)
                bundle.putString("MMENNY", mennyiseg.toString())
                bundle.putString("MCIKK", cikkszam)
                context.tobbletCikkekPolcra?.arguments = bundle
                CoroutineScope(Dispatchers.Main).launch {
                    progress.visibility = View.GONE
                }
                context.supportFragmentManager.beginTransaction()
                    .replace(R.id.frame_container, context.tobbletCikkekPolcra!!, "CIKKEKPOLCRA")
                    .commit()
            }
            connection.close()
        } catch (e: Exception) {
            CoroutineScope(Dispatchers.Main).launch {
                context.setAlert("Open nyolcas hiba")
                writeLog(e.stackTraceToString(), "arg1 $id arg2 $kontenerID arg3 $megjegyzes arg4 $megjegyzes2 arg5 $intrem arg6 $unit arg7 $mennyiseg arg8 $cikkszam")
                progress.visibility = View.GONE
            }
        }
    }

    fun checkBinIn02(code: String, context: MainActivity) {
        try {
            CoroutineScope(Dispatchers.Main).launch {
                context.tobbletCikkekPolcra?.progrssOn()
            }
            Class.forName("net.sourceforge.jtds.jdbc.Driver")
            val connection = DriverManager.getConnection(url)
            val statement = connection.prepareStatement(res.getString(R.string.is02Polc))
            statement.setString(1, code)
            val resultSet = statement.executeQuery()
            if (!resultSet.next()) {
                context.tobbletCikkekPolcra?.clearPocl()
                CoroutineScope(Dispatchers.Main).launch {
                    context.setAlert("Nem olyan polc ami a raktárba ")
                    context.tobbletCikkekPolcra?.progrssOff()
                }
            } else {
                CoroutineScope(Dispatchers.Main).launch {
                    context.tobbletCikkekPolcra?.setPolc()
                    context.tobbletCikkekPolcra?.progrssOff()
                }
            }
            connection.close()
        } catch (e: Exception) {
            CoroutineScope(Dispatchers.Main).launch {
                context.setAlert("A polc ellenőrzésénél hiba lépett fel")
                context.tobbletCikkekPolcra?.clearPocl()
                context.tobbletCikkekPolcra?.progrssOff()
                writeLog(e.stackTraceToString(), "arg1 $code")
            }
        }
    }

    fun closeItemAndCheckContainer(cikk: Int, kontener: Int, context: MainActivity) {
        try {
            context.tobbletCikkek = TobbletKontenerCikkekFragment()
            Class.forName("net.sourceforge.jtds.jdbc.Driver")
            val connection = DriverManager.getConnection(connectionString)
            val statement = connection.prepareStatement(res.getString(R.string.cikkUpdate))
            statement.setInt(1, 9)//9 v 8
            statement.setString(2, dolgKod)
            statement.setInt(3, cikk)
            statement.executeUpdate()
            val statement2 =
                connection.prepareStatement(res.getString(R.string.tobbletKontnerCikkek))
            statement2.setInt(1, kontener)
            val resultSet = statement2.executeQuery()
            if (!resultSet.next()) {
                CoroutineScope(Dispatchers.Main).launch {
                    context.setAlert("A konténer üres")
                }
                val statement3 =
                    connection.prepareStatement(res.getString(R.string.updateContainerStatusJust))
                statement3.setInt(1, 9)//
                statement3.setInt(2, kontener)
                statement3.executeUpdate()
                tobbletKontenerElemek(context)
            } else {
                val tobbletCikkek: ArrayList<KontenerbenLezarasItem> = ArrayList()
                tobbletItem.clear()
                do {
                    val cikk1 = resultSet.getString("cikkszam")
                    val megj1 = resultSet.getString("Description1")
                    val megj2 = resultSet.getString("Description2")
                    val intrem = resultSet.getString("InternRem1")
                    val igeny = resultSet.getDouble("igenyelt_mennyiseg").toString()
                    val mozgatott = resultSet.getDouble("mozgatott_mennyiseg").toString()
                    val status = resultSet.getInt("statusz")
                    val unit = resultSet.getString("Unit")
                    val id = resultSet.getInt("id")
                    val kontenerId = resultSet.getInt("kontener_id")
                    val balance = resultSet.getDouble("StockBalance")
                    tobbletCikkek.add(
                        KontenerbenLezarasItem(
                            cikk1,
                            megj1,
                            megj2,
                            intrem,
                            igeny,
                            mozgatott,
                            status,
                            unit,
                            id,
                            kontenerId,
                            balance
                        )
                    )
                } while (resultSet.next())
                val bundle = Bundle()
                bundle.putSerializable("TOBBLETESCIKKEK", tobbletCikkek)
                bundle.putString("KONTENERTOBBLETCIKK", kontener.toString())
                context.tobbletCikkek?.arguments = bundle
                context.supportFragmentManager.beginTransaction()
                    .replace(
                        R.id.frame_container,
                        context.tobbletCikkek!!,
                        "TOBBLETKIHELYEZESCIKKEK"
                    )
                    .commit()
            }
            connection.close()
        } catch (e: Exception) {
            CoroutineScope(Dispatchers.Main).launch {
                context.setAlert("Visszaírási hiba")
                writeLog(e.stackTraceToString(), "arg1 $cikk arg2 $kontener")
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun setDate(): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
    }

    fun writeLog(hiba: String, argumentumok: String) {
        val save = SaveFile()
        save.writeLog(
            File(path, "LOG.txt"), """${setDate()};${argumentumok};${hiba};${wifiInfo}
                    |
                """.trimMargin()
        )
    }
}