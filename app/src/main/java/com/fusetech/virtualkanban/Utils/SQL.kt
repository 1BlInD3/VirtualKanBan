package com.fusetech.virtualkanban.Utils

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import com.fusetech.virtualkanban.Activities.MainActivity
import com.fusetech.virtualkanban.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.fusetech.virtualkanban.Activities.MainActivity.Companion.connectionString
import com.fusetech.virtualkanban.Activities.MainActivity.Companion.res
import com.fusetech.virtualkanban.Activities.MainActivity.Companion.progress
import com.fusetech.virtualkanban.DataItems.*
import com.fusetech.virtualkanban.Fragments.*
import com.fusetech.virtualkanban.Fragments.PolcraHelyezesFragment.Companion.myItems
import kotlinx.coroutines.Dispatchers.IO
import java.io.File
import java.sql.*
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.collections.ArrayList

private const val TAG = "SQL"
 class SQL (val sqlMessage: SQLAlert) {

     interface SQLAlert {
         fun sendMessage(message: String)
     }

     fun deleteKontenerRaktarTetel(konenerTetelId: String) {
         var connection: Connection
         CoroutineScope(IO).launch {
             Class.forName("net.sourceforge.jtds.jdbc.Driver")
             try {
                 connection = DriverManager.getConnection(connectionString)
                 val statement =
                     connection.prepareStatement(res.getString(R.string.deleteEmptyRaktarTetel))
                 statement.setString(1, konenerTetelId)
                 statement.executeUpdate()
             } catch (e: Exception) {
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

     fun checkTrannzit(
         code: String,
         context: MainActivity,
         polcLocation: ArrayList<PolcLocation>?
     ) {
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
                     context.polcHelyezesFragment.setCikkNumberBack()
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

     fun checkPolc(code: String, context: MainActivity) {
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

     fun containerManagement(id: String, context: MainActivity) {
         val connection: Connection
         Class.forName("net.sourceforge.jtds.jdbc.Driver")
         try {
             CoroutineScope(Dispatchers.Main).launch {
                 context.menuFragment.setMenuProgressOn()
             }
             connection = DriverManager.getConnection(connectionString)
             val isContainer =
                 connection.prepareStatement(res.getString(R.string.containerCheck))
             isContainer.setString(1, id)
             isContainer.setInt(2, 0)
             val containerResult = isContainer.executeQuery()
             if (!containerResult.next()) {
                 Log.d(TAG, "containerManagement: Nincs konténer")
                 val insertContainer =
                     connection.prepareStatement(res.getString(R.string.openContainer))
                 insertContainer.setString(1, id)
                 insertContainer.setInt(2, 0)
                 insertContainer.setInt(3, 1)
                 insertContainer.setString(4, "01")
                 insertContainer.executeUpdate()
                 Log.d(TAG, "containerManagement: Konténer létrehozva")
                 try {
                     Log.d(TAG, "containerManagement: Betöltöm az adatot")
                     val getName =
                         connection.prepareStatement(res.getString(R.string.containerCheck))
                     getName.setString(1, id)
                     getName.setInt(2, 0)
                     val getNameResult = isContainer.executeQuery()
                     if (!getNameResult.next()) {
                         CoroutineScope(Dispatchers.Main).launch {
                             context.setAlert("Valami nagy hiba van")
                         }
                     } else {
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
                         updateContainer.setString(2, id)
                         updateContainer.setInt(3, 0)
                         updateContainer.executeUpdate()
                         Log.d(TAG, "containerManagement: visszaírtam a konténer értéket")
                         val bundle = Bundle()
                         bundle.putString("KONTENER", nullasKontener)
                         context.igenyFragment.arguments = bundle
                         context.supportFragmentManager.beginTransaction()
                             .replace(R.id.frame_container, context.igenyFragment, "IGENY")
                             .addToBackStack(null).commit()
                         CoroutineScope(Dispatchers.Main).launch {
                             context.menuFragment.setMenuProgressOff()
                         }
                     }
                 } catch (e: Exception) {
                     Log.d(TAG, "containerManagement: $e")
                     CoroutineScope(Dispatchers.Main).launch {
                         context.menuFragment.setMenuProgressOff()
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
                     context.igenyFragment.arguments = bundle1
                     context.supportFragmentManager.beginTransaction()
                         .replace(R.id.frame_container, context.igenyFragment).addToBackStack(null)
                         .commit()
                     CoroutineScope(Dispatchers.Main).launch {
                         context.menuFragment.setMenuProgressOff()
                     }
                 } else {
                     do {
                         val cikk = loadIgenyListResult.getString("cikkszam")
                         val megjegyzes = loadIgenyListResult.getString("megjegyzes")
                         val darabszam = loadIgenyListResult.getString("igenyelt_mennyiseg")
                         context.listIgenyItems.add(IgenyItem(cikk, megjegyzes, darabszam))
                     } while (loadIgenyListResult.next())
                     val bundle = Bundle()
                     bundle.putSerializable("IGENY", context.listIgenyItems)
                     bundle.putString("KONTENER", context.kontener)
                     bundle.putString("TERMRAKH", rakhely)
                     context.igenyFragment.arguments = bundle
                     context.supportFragmentManager.beginTransaction()
                         .replace(R.id.frame_container, context.igenyFragment).addToBackStack(null)
                         .commit()
                     CoroutineScope(Dispatchers.Main).launch {
                         context.menuFragment.setMenuProgressOff()
                     }
                 }
             }
         } catch (e: Exception) {
             CoroutineScope(Dispatchers.Main).launch {
                 context.setAlert("Valahol baj van $e")
                 context.menuFragment.setMenuProgressOff()
             }
         }
     }

     fun check01(code: String, context: MainActivity) {
         val connection: Connection
         CoroutineScope(Dispatchers.Main).launch {
             context.igenyFragment.setProgressBarOn()
         }
         Class.forName("net.sourceforge.jtds.jdbc.Driver")
         try {
             connection = DriverManager.getConnection(connectionString)
             val statement = connection.prepareStatement(res.getString(R.string.is01))
             statement.setString(1, code)
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
                 statement1.setString(2, context.dolgKod)
                 statement1.setString(3, "0")
                 statement1.executeUpdate()
                 CoroutineScope(Dispatchers.Main).launch {
                     context.igenyFragment.setFocusToItem()
                     context.igenyFragment.setProgressBarOff()
                 }
             }
         } catch (e: Exception) {
             Log.d(TAG, "check01: $e")
             CoroutineScope(Dispatchers.Main).launch {
                 context.igenyFragment.setProgressBarOff()
             }
         }
     }

     fun uploadItem(
         cikk: String,
         menny: Double,
         term: String,
         unit: String,
         context: MainActivity
     ) {
         val connection: Connection
         Class.forName("net.sourceforge.jtds.jdbc.Driver")
         try {
             connection = DriverManager.getConnection(connectionString)
             val statement = connection.prepareStatement(res.getString(R.string.insertItem))
             statement.setString(1, context.kontener)
             statement.setString(2, cikk)
             statement.setInt(3, 0) //ez a státusz
             statement.setDouble(4, menny)
             statement.setInt(5, 0)
             statement.setString(6, "01")
             statement.setString(7, term)
             statement.setString(8, unit)
             statement.executeUpdate()
         } catch (e: Exception) {
             Log.d(TAG, "uploadItem: $e")
             CoroutineScope(Dispatchers.Main).launch {
                 context.setAlert("Hiba történt, lépj vissza a 'Kilépés' gombbal a menübe, majd vissza, hogy megnézd mi lett utoljára felvéve")
             }
         }
     }

     fun closeContainerSql(statusz: Int, datum: String, context: MainActivity) {
         val connection: Connection
         Class.forName("net.sourceforge.jtds.jdbc.Driver")
         try {
             connection = DriverManager.getConnection(connectionString)
             val statement =
                 connection.prepareStatement(res.getString(R.string.closeContainer))
             statement.setInt(1, statusz)
             statement.setString(2, datum)
             statement.setString(3, context.kontener)
             statement.executeUpdate()
             Log.d(TAG, "closeContainerSql: sikeres lezárás")
             CoroutineScope(Dispatchers.Main).launch {
                 context.setAlert("Sikeres konténer lezárás!")
             }
             val statement1 =
                 connection.prepareStatement(res.getString(R.string.updateItemStatus))
             statement1.setString(1, context.kontener)
             try {
                 statement1.executeUpdate()
             } catch (e: Exception) {
                 Log.d(TAG, "closeContainerSql: $e")
                 CoroutineScope(Dispatchers.Main).launch {
                     context.setAlert("A cikk státuszok felülírásánál hiba lépett fel, gyere az IT-re")
                 }
             }
         } catch (e: Exception) {
             Log.d(TAG, "closeContainerSql: $e")
         }
     }

     fun loadIgenyLezaras(context: MainActivity) {
         val connection: Connection
         Class.forName("net.sourceforge.jtds.jdbc.Driver")
         try {
             CoroutineScope(Dispatchers.Main).launch {
                 context.menuFragment.setMenuProgressOn()
             }
             connection = DriverManager.getConnection(MainActivity.url)
             val statement =
                 connection.prepareStatement(res.getString(R.string.igenyKontenerLezarasKontenerBeolvas))
             val resultSet = statement.executeQuery()
             if (!resultSet.next()) {
                 Log.d(TAG, "loadIgenyLezaras: Nincs ilyen konténer")
                 CoroutineScope(Dispatchers.Main).launch {
                     context.setAlert("loadIgenyLezaras: Nincs ilyen konténer")
                 }
             } else {
                 context.kontener1List.clear()
                 do {
                     val kontener: String? = resultSet.getString("kontener")
                     val polc: String? = resultSet.getString("polc")
                     val datum: String? = resultSet.getString("igenyelve")
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
                 context.igenyLezarasFragment.arguments = bundle
                 context.supportFragmentManager.beginTransaction()
                     .replace(R.id.frame_container, context.igenyLezarasFragment, "IGENYLEZARAS")
                     .addToBackStack(null).commit()
                 CoroutineScope(Dispatchers.Main).launch {
                     context.menuFragment.setMenuProgressOff()
                 }
             }
         } catch (e: Exception) {
             Log.d(TAG, "loadIgenyLezaras: $e")
             CoroutineScope(Dispatchers.Main).launch {
                 context.setAlert("Hálózati probléma! Próbáld újra\n $e")
                 context.menuFragment.setMenuProgressOff()
             }
         }
     }

     fun loadKontenerCikkek(kontener_id: String, context: MainActivity) {
         val connection: Connection
         Class.forName("net.sourceforge.jtds.jdbc.Driver")
         try {
             CoroutineScope(Dispatchers.Main).launch {
                 context.igenyLezarasFragment.setProgressBarOn()
             }
             connection = DriverManager.getConnection(MainActivity.url)
             val statement =
                 connection.prepareStatement(res.getString(R.string.igenyKontenerLezarasCikkLezarasNULL))
             statement.setInt(1, kontener_id.toInt())
             statement.setInt(2, 0)
             val resultSet = statement.executeQuery()
             if (!resultSet.next()) {
                 Log.d(TAG, "loadKontenerCikkek: HIBA VAN")
                 CoroutineScope(Dispatchers.Main).launch {
                     context.setAlert("A konténerben nincs 0 státuszú cikk")
                     context.igenyLezarasFragment.setProgressBarOff()
                 }
             } else {
                 val igenyKiszedesCikkLezaras = IgenyKontenerLezarasCikkLezaras()
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
                             kontenerId
                         )
                     )
                 } while (resultSet.next())
                 val bundle = Bundle()
                 bundle.putSerializable("CIKKLEZAR", kontenerCikkLezar)
                 bundle.putString("KONTENER_ID", kontener_id)
                 bundle.putBoolean("LEZARBUTN", true)
                 igenyKiszedesCikkLezaras.arguments = bundle
                 context.supportFragmentManager.beginTransaction()
                     .replace(R.id.data_frame1, igenyKiszedesCikkLezaras, "CIKKLEZARASFRAGMENT")
                     .addToBackStack(null).commit()
                 CoroutineScope(Dispatchers.Main).launch {
                     context.igenyLezarasFragment.setProgressBarOff()
                 }
             }
         } catch (e: Exception) {
             Log.d(TAG, "loadKontenerCikkek: $e")
             CoroutineScope(Dispatchers.Main).launch {
                 Log.d(TAG, "loadKontenerCikkek: $e")
                 context.setAlert("$e")
                 context.igenyLezarasFragment.setProgressBarOff()
             }
         }
     }

     fun cikkPolcQuery(code: String, context: MainActivity) {
         val connection: Connection
         val polcResultFragment = PolcResultFragment()
         val cikkResultFragment = CikkResultFragment()
         val bundle = Bundle()
         Class.forName("net.sourceforge.jtds.jdbc.Driver")
         try {
             connection = DriverManager.getConnection(MainActivity.url)
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
                     val loadFragment = LoadFragment.newInstance("Nincs ilyen kód a rendszerben")
                     context.supportFragmentManager.beginTransaction()
                         .replace(R.id.cikk_container, loadFragment).commit()
                 } else {
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
                     cikkResultFragment.arguments = bundle
                     context.supportFragmentManager.beginTransaction()
                         .replace(R.id.cikk_container, cikkResultFragment).commit()
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
                     do {
                         context.polcItems.add(
                             PolcItems(
                                 resultSet2.getDouble("BalanceQty"),
                                 resultSet2.getString("Unit"),
                                 resultSet2.getString("Description1"),
                                 resultSet2.getString("Description2"),
                                 resultSet2.getString("IntRem"),
                                 resultSet2.getString("QcCategory")
                             )
                         )

                     } while (resultSet2.next())
                     bundle.putSerializable("polc", context.polcItems)
                     polcResultFragment.arguments = bundle
                     context.supportFragmentManager.beginTransaction()
                         .replace(R.id.cikk_container, polcResultFragment).commit()
                 }
             }
         } catch (e: Exception) {
             Log.d(TAG, "$e")
             val loadFragment = LoadFragment.newInstance("A feldolgozás során hiba lépett fel")
             context.supportFragmentManager.beginTransaction()
                 .replace(R.id.cikk_container, loadFragment)
                 .commit()
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
         try {
             val currentDate = SimpleDateFormat("yyyy-MM-dd").format(Date())
             if (ContextCompat.checkSelfPermission(
                     context,
                     android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                 ) == PackageManager.PERMISSION_GRANTED
             ) {
                 val path = context.getExternalFilesDir(null)
                 val name = SimpleDateFormat("yyyyMMddHHmmss").format(Date()) + polchely + ".xml"
                 val file = File(path, name)
                 context.save.saveXml(
                     file,
                     context.xml.createXml(
                         currentDate,
                         mennyisege,
                         cikkszam,
                         rbol,
                         polchely,
                         rba,
                         polchelyre,
                         context.dolgKod
                     )
                 )
                 Log.d("IOTHREAD", "sendXmlData: ${Thread.currentThread().name}")
                 context.retro.retrofitGet(file)
             }
         } catch (e: Exception) {
             CoroutineScope(Dispatchers.Main).launch {
                 context.setAlert("Scala send $e")
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
             connection = DriverManager.getConnection(MainActivity.url)
             val statement = connection.prepareStatement(res.getString(R.string.cikkSql))
             statement.setString(1, code)
             val resultSet = statement.executeQuery()
             if (!resultSet.next()) {
                 CoroutineScope(Dispatchers.Main).launch {
                     context.setAlert("Nincs ilyen cikk a rendszerben")
                     context.igenyFragment.setProgressBarOff()
                     context.igenyFragment.setFocusToItem()
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
         } catch (e: Exception) {
             Log.d(TAG, "checkItem: $e")
             CoroutineScope(Dispatchers.Main).launch {
                 context.igenyFragment.setProgressBarOff()
             }
         }
     }

     fun loadIgenyKiszedes(context: MainActivity) {
         val connection: Connection
         Class.forName("net.sourceforge.jtds.jdbc.Driver")
         try {
             CoroutineScope(Dispatchers.Main).launch {
                 context.menuFragment.setMenuProgressOn()
             }
             connection = DriverManager.getConnection(MainActivity.url)
             val statement =
                 connection.prepareStatement(res.getString(R.string.igenyKontenerKiszedese))
             val resultSet = statement.executeQuery()
             if (!resultSet.next()) {
                 CoroutineScope(Dispatchers.Main).launch {
                     context.menuFragment.setMenuProgressOff()
                 }
                 context.supportFragmentManager.beginTransaction()
                     .replace(R.id.frame_container, context.igenyKiszedesFragment, "KISZEDES")
                     .addToBackStack(null).commit()
             } else {
                 context.kontenerList.clear()
                 do {
                     val kontener: String? = resultSet.getString("kontener")
                     val polc: String? = resultSet.getString("polc")
                     val datum: String? = resultSet.getString("igenyelve")
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
                 bundle.putSerializable("KISZEDESLISTA", context.kontenerList)
                 context.igenyKiszedesFragment.arguments = bundle
                 context.supportFragmentManager.beginTransaction()
                     .replace(R.id.frame_container, context.igenyKiszedesFragment, "KISZEDES")
                     .addToBackStack(null).commit()
                 CoroutineScope(Dispatchers.Main).launch {
                     context.menuFragment.setMenuProgressOff()
                 }
             }
         } catch (e: Exception) {
             Log.d(TAG, "loadIgenyKiszedes: $e")
             CoroutineScope(Dispatchers.Main).launch {
                 context.menuFragment.setMenuProgressOff()
                 context.setAlert("Probléma van :\n $e")
             }
         }
     }

     fun checkIfContainerIsOpen(kontener: String, context: MainActivity) {
         val connection: Connection
         Class.forName("net.sourceforge.jtds.jdbc.Driver")
         try {
             CoroutineScope(Dispatchers.Main).launch {
                 context.igenyKiszedesFragment.setProgressBarOn()
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
                     context.igenyKiszedesFragment.setProgressBarOff()
                 }
             } else {
                 val statement2 =
                     connection.prepareStatement(res.getString(R.string.atvevoBeiras))
                 statement2.setString(1, context.dolgKod)
                 statement2.setString(2, kontener)
                 statement2.executeUpdate()
                 Log.d(TAG, "checkIfContainerIsOpen: Sikeres update")
                 val statment3 =
                     connection.prepareStatement(res.getString(R.string.igenyKontenerLezarasCikkLezaras))
                 statment3.setInt(1, kontener.toInt())
                 statment3.setString(2, context.dolgKod)
                 val resultSet1 = statment3.executeQuery()
                 if (!resultSet1.next()) {
                     CoroutineScope(Dispatchers.Main).launch {
                         //setAlert("A konténer üres")
                         context.igenyKiszedesFragment.setProgressBarOff()
                     }
                     context.supportFragmentManager.beginTransaction()
                         .replace(R.id.frame_container, context.ellenorzoKodFragment, "ELLENOR")
                         .commit()
                 } else {
                     val fragment = IgenyKontnerKiszedesCikk()
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
                                 kontenerId
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
                     CoroutineScope(Dispatchers.Main).launch {
                         context.igenyKiszedesFragment.setProgressBarOff()
                     }
                 }
             }
         } catch (e: Exception) {
             CoroutineScope(Dispatchers.Main).launch {
                 context.setAlert("Hiba \n $e")
                 Log.d(TAG, "checkIfContainerIsOpen: $e")
                 context.igenyKiszedesFragment.setProgressBarOff()
             }
         }
     }

     fun loadKiszedesreVaro(context: MainActivity) {
         val connection: Connection
         Class.forName("net.sourceforge.jtds.jdbc.Driver")
         try {
             CoroutineScope(Dispatchers.Main).launch {
                 context.menuFragment.setMenuProgressOn()
             }
             connection = DriverManager.getConnection(MainActivity.url)
             val statement =
                 connection.prepareStatement(res.getString(R.string.igenyKontenerKiszedese))
             val resultSet = statement.executeQuery()
             if (!resultSet.next()) {
                 CoroutineScope(Dispatchers.Main).launch {
                     context.menuFragment.setMenuProgressOff()
                 }
                 context.supportFragmentManager.beginTransaction()
                     .replace(R.id.frame_container, context.kiszedesreVaroIgenyFragment, "VARAS")
                     .addToBackStack(null).commit()
             } else {
                 context.myList.clear()
                 do {
                     val kontener: String? = resultSet.getString("kontener")
                     val polc: String? = resultSet.getString("polc")
                     val datum: String? = resultSet.getString("igenyelve")
                     val tetelszam = resultSet.getInt("tetelszam")
                     val id: String = resultSet.getString("id")
                     val status: Int = resultSet.getInt("statusz")
                     context.myList.add(KontenerItem(kontener, polc, datum, tetelszam, id, status))
                 } while (resultSet.next())
                 val bundle = Bundle()
                 bundle.putSerializable("VAROLISTA", context.myList)
                 context.kiszedesreVaroIgenyFragment.arguments = bundle
                 context.supportFragmentManager.beginTransaction()
                     .replace(R.id.frame_container, context.kiszedesreVaroIgenyFragment, "VARAS")
                     .addToBackStack(null).commit()
                 CoroutineScope(Dispatchers.Main).launch {
                     context.menuFragment.setMenuProgressOff()
                 }
             }
         } catch (e: Exception) {
             Log.d(TAG, "loadIgenyKiszedes: $e")
             CoroutineScope(Dispatchers.Main).launch {
                 context.menuFragment.setMenuProgressOff()
                 context.setAlert("Probléma van :\n $e")
             }
         }
     }

     fun loadKontenerCikkekHatos(kontener_id: String, context: MainActivity) {
         val connection: Connection
         Class.forName("net.sourceforge.jtds.jdbc.Driver")
         try {
             CoroutineScope(Dispatchers.Main).launch {
                 context.kiszedesreVaroIgenyFragment.setProgressBarOn()
             }
             connection = DriverManager.getConnection(MainActivity.url)
             val statement =
                 connection.prepareStatement(res.getString(R.string.igenyKontenerLezarasCikkLezarasNezegetos))
             statement.setInt(1, kontener_id.toInt())
             //statement.setInt(2,1)
             val resultSet = statement.executeQuery()
             if (!resultSet.next()) {
                 Log.d(TAG, "loadKontenerCikkek: HIBA VAN")
                 CoroutineScope(Dispatchers.Main).launch {
                     context.setAlert("A konténerben nincs 1 státuszú cikk")
                     context.kiszedesreVaroIgenyFragment.setProgressBarOff()
                 }
             } else {
                 val igenyKiszedesCikkLezaras = IgenyKontenerLezarasCikkLezaras()
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
                             kontenerId
                         )
                     )
                 } while (resultSet.next())
                 val bundle = Bundle()
                 bundle.putSerializable("CIKKLEZAR", kontenerCikkLezar)
                 bundle.putString("KONTENER_ID", kontener_id)
                 bundle.putBoolean("LEZARBUTN", false)
                 igenyKiszedesCikkLezaras.arguments = bundle
                 context.supportFragmentManager.beginTransaction()
                     .replace(
                         R.id.data_frame3,
                         igenyKiszedesCikkLezaras,
                         "CIKKLEZARASFRAGMENTHATOS"
                     )
                     .addToBackStack(null).commit()
                 CoroutineScope(Dispatchers.Main).launch {
                     context.kiszedesreVaroIgenyFragment.setProgressBarOff()
                 }
             }
         } catch (e: Exception) {
             Log.d(TAG, "loadKontenerCikkek: $e")
             CoroutineScope(Dispatchers.Main).launch {
                 context.kiszedesreVaroIgenyFragment.setProgressBarOff()
             }
         }
     }

     fun cikkAdataokSql(
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
             statement.setString(2, context.dolgKod)
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
                 statement1.setString(2, context.dolgKod)
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
                     val resultSet2 = statement2.executeQuery()
                     if (!resultSet2.next()) {
                         CoroutineScope(Dispatchers.Main).launch {
                             context.setAlert("Nincs készleten")
                             progress.visibility = View.GONE
                         }
                     } else {
                         val myList: ArrayList<PolcLocation> = ArrayList()
                         do {
                             val polc = resultSet2.getString("BinNumber")
                             val mennyiseg = resultSet2.getString("BalanceQty")
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
                         context.igenyKontenerKiszedesCikkKiszedes.arguments = bundle
                         context.supportFragmentManager.beginTransaction().replace(
                             R.id.frame_container,
                             context.igenyKontenerKiszedesCikkKiszedes,
                             "KISZEDESCIKK"
                         ).commit()
                     }
                     CoroutineScope(Dispatchers.Main).launch {
                         progress.visibility = View.GONE
                     }
                 } else {
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
                     val resultSet2 = statement2.executeQuery()
                     if (!resultSet2.next()) {
                         CoroutineScope(Dispatchers.Main).launch {
                             context.setAlert("Nincs készleten")
                             progress.visibility = View.GONE
                         }
                     } else {
                         val myList: ArrayList<PolcLocation> = ArrayList()
                         do {
                             val polc = resultSet2.getString("BinNumber")
                             val mennyiseg = resultSet2.getString("BalanceQty")
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
                         context.igenyKontenerKiszedesCikkKiszedes.arguments = bundle
                         context.supportFragmentManager.beginTransaction().replace(
                             R.id.frame_container,
                             context.igenyKontenerKiszedesCikkKiszedes,
                             "KISZEDESCIKK"
                         ).commit()
                     }
                 }
                 CoroutineScope(Dispatchers.Main).launch {
                     progress.visibility = View.GONE
                 }
             }
         } catch (e: Exception) {
             CoroutineScope(Dispatchers.Main).launch {
                 context.setAlert("Csekk\n $e")
             }
         }
         Log.d(TAG, "cikkAdatok: ")
     }

     fun cikkCodeSql(code: Int, context: MainActivity) {
         val connection: Connection
         Class.forName("net.sourceforge.jtds.jdbc.Driver")
         try {
             connection = DriverManager.getConnection(MainActivity.url)
             val statement = connection.prepareStatement(res.getString(R.string.getAtvevo))
             statement.setInt(1, code)
             val resultSet = statement.executeQuery()
             if (!resultSet.next()) {
                 CoroutineScope(Dispatchers.Main).launch {
                     context.setAlert("Nincs neki átvevője")
                 }
             } else {
                 val atvevo = resultSet.getString("atvevo")
                 val statement1 = connection.prepareStatement(res.getString(R.string.nev))
                 statement1.setString(1, atvevo)
                 val resultSet1 = statement1.executeQuery()
                 if (!resultSet1.next()) {
                     CoroutineScope(Dispatchers.Main).launch {
                         context.setAlert("Nem fogja senki")
                     }
                 } else {
                     val nev = resultSet1.getString("TextDescription")
                     CoroutineScope(Dispatchers.Main).launch {
                         context.setAlert(nev + " fogja a cikket")
                     }
                 }
             }
         } catch (e: Exception) {
             CoroutineScope(Dispatchers.Main).launch {
                 context.setAlert("Probléma a nevekkel $e")
             }
         }
     }

     fun checkEllenorzoKodSql(code: String, context: MainActivity) {
         val connection: Connection
         Class.forName("net.sourceforge.jtds.jdbc.Driver")
         try {
             CoroutineScope(Dispatchers.Main).launch {
                 context.ellenorzoKodFragment.setProgressBarOn()
             }
             connection = DriverManager.getConnection(connectionString)
             val statement =
                 connection.prepareStatement(res.getString(R.string.kontenerBinDesciption))
             statement.setString(1, context.selectedContainer)
             val resultSet = statement.executeQuery()
             if (!resultSet.next()) {
                 CoroutineScope(Dispatchers.Main).launch {
                     context.setAlert("Gáz van")
                     context.ellenorzoKodFragment.setProgressBarOff()
                 }
             } else {
                 val ellKod = resultSet.getString("BinDescript2")
                 if (code.trim().equals(ellKod)) {
                     CoroutineScope(Dispatchers.Main).launch {
                         context.setAlert("ITT kell lezárni a konténert")
                         context.ellenorzoKodFragment.setProgressBarOff()
                     }
                 } else {
                     CoroutineScope(Dispatchers.Main).launch {
                         context.setAlert("Nem egyezik a kód a szállító járművel")
                         context.ellenorzoKodFragment.setProgressBarOff()
                     }
                 }
             }
         } catch (e: Exception) {
             CoroutineScope(Dispatchers.Main).launch {
                 context.setAlert("Ellenorzo\n $e")
                 context.ellenorzoKodFragment.setProgressBarOff()
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
                 context.igenyKontenerKiszedesCikkKiszedes.setProgressBarOn()
             }
             connection = DriverManager.getConnection(connectionString)
             val statement1 =
                 connection.prepareStatement(res.getString(R.string.getMozgatottMennyiseg))
             statement1.setString(1, itemId)
             val resultSet1 = statement1.executeQuery()
             if (!resultSet1.next()) {
                 Log.d(TAG, "checkIfContainerIsDone: nincs mozgatott mennyiség (hazugság)")
                 CoroutineScope(Dispatchers.Main).launch {
                     context.igenyKontenerKiszedesCikkKiszedes.setProgressBarOff()
                 }
             } else {
                 mozgatott = resultSet1.getDouble("mozgatott_mennyiseg")
                 val statement2 =
                     connection.prepareStatement(res.getString(R.string.getSzallitoJarmu))
                 statement2.setString(1, container)
                 val resultSet2 = statement2.executeQuery()
                 if (!resultSet2.next()) {
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
                 }
                 CoroutineScope(Dispatchers.Main).launch {
                     context.igenyKontenerKiszedesCikkKiszedes.setProgressBarOff()
                 }
             }
         } catch (e: Exception) {
             CoroutineScope(Dispatchers.Main).launch {
                 context.setAlert("Probléma a konténer ellenőrzésével $e")
                 context.igenyKontenerKiszedesCikkKiszedes.setProgressBarOff()
             }
         }
     }
     fun updateItemAtvevoSql(itemId: String, context: MainActivity){
         val connection: Connection
         Class.forName("net.sourceforge.jtds.jdbc.Driver")
         try {
             CoroutineScope(Dispatchers.Main).launch {
                 context.igenyKontenerKiszedesCikkKiszedes.setProgressBarOn()
             }
             connection = DriverManager.getConnection(connectionString)
             val statement =
                 connection.prepareStatement(res.getString(R.string.updateCikkAtvevo))
             statement.setString(1, context.dolgKod)
             statement.setString(2, itemId)
             statement.executeUpdate()
             CoroutineScope(Dispatchers.Main).launch {
                 context.igenyKontenerKiszedesCikkKiszedes.setProgressBarOff()
             }
         } catch (e: Exception) {
             CoroutineScope(Dispatchers.Main).launch {
                 context.setAlert("Nem tudom az átvevőt kinullázni $e")
                 context.igenyKontenerKiszedesCikkKiszedes.setProgressBarOff()
             }
         }
     }
     fun updtaeItemStatusSql(itemId: String, context: MainActivity){
         val connection: Connection
         Class.forName("net.sourceforge.jtds.jdbc.Driver")
         try {
             CoroutineScope(Dispatchers.Main).launch {
                 context.igenyKontenerKiszedesCikkKiszedes.setProgressBarOn()
             }
             connection = DriverManager.getConnection(connectionString)
             val statement =
                 connection.prepareStatement(res.getString(R.string.updateKontenerTeletStatusz))
             statement.setInt(1, 3)
             statement.setString(2, itemId)
             statement.executeUpdate()
             context.igenyKontenerKiszedesCikkKiszedes.isUpdated = true
             CoroutineScope(Dispatchers.Main).launch {
                 context.igenyKontenerKiszedesCikkKiszedes.setProgressBarOff()
             }
         } catch (e: Exception) {
             CoroutineScope(Dispatchers.Main).launch {
                 context.setAlert("Probléma a tétel 3-ra írásával $e")
                 context.igenyKontenerKiszedesCikkKiszedes.setProgressBarOff()
             }
         }
     }
     fun insertDataToRaktarTetelSql(cikk: String, mennyiseg: Double, raktarKod: String, polc: String, context: MainActivity){
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
             context.igenyKontenerKiszedesCikkKiszedes.isSaved = true
         } catch (e: Exception) {
             CoroutineScope(Dispatchers.Main).launch {
                 context.setAlert("Probléma a ratar_tetel feltöltésnél $e")
             }
         }
     }
     fun cikkUpdateSql(cikk: Int, context: MainActivity){
         val connection: Connection
         Class.forName("net.sourceforge.jtds.jdbc.Driver")
         try {
             CoroutineScope(Dispatchers.Main).launch {
                 context.igenyKontenerKiszedesCikkKiszedes.setProgressBarOn()
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
                 context.igenyKontenerKiszedesCikkKiszedes.setProgressBarOff()
             }
         } catch (e: Exception) {
             CoroutineScope(Dispatchers.Main).launch {
                 context.setAlert("CikkUpdateHiba $e")
                 context.igenyKontenerKiszedesCikkKiszedes.setProgressBarOff()
             }
         }
     }
     fun updateCikkSql(kontener_id: String, context: MainActivity){
         val connection: Connection
         Class.forName("net.sourceforge.jtds.jdbc.Driver")
         try {
             connection = DriverManager.getConnection(connectionString)
             val statement =
                 connection.prepareStatement(res.getString(R.string.updateItemStatus))
             statement.setString(1, kontener_id)
             statement.executeUpdate()
             Log.d(TAG, "updateCikkAndKontener: Cikkek lezárva")
         } catch (e: Exception) {
             Log.d(TAG, "updateCikkAndKontener: $e")
             context.setAlert("Probléma van\n $e")
         }
     }
     fun updateKontenerSql(kontener_id: String, context: MainActivity){
         val connection: Connection
         Class.forName("net.sourceforge.jtds.jdbc.Driver")
         try {
             connection = DriverManager.getConnection(connectionString)
             val statment =
                 connection.prepareStatement(res.getString(R.string.updateContainerStatus))
             statment.setInt(1, 1)
             statment.setString(2, "NULL")//ide kell majd valami
             statment.setString(3, context.dolgKod)//ide kell a bejelentkezős kód
             statment.setString(4, kontener_id)
             statment.executeUpdate()
             Log.d(TAG, "updateCikkAndKontener: Konténer lezárva")
             context.lezarandoKontener = ""
         } catch (e: Exception) {
             Log.d(TAG, "updateKontener: $e")
             context.setAlert("Probléma van a konténer 1-re átírásánál\n $e")
         }
     }
     fun chekcPolcAndSetBinSql(code: String, context: MainActivity){
         val connection: Connection
         Class.forName("net.sourceforge.jtds.jdbc.Driver")
         try {
             CoroutineScope(Dispatchers.Main).launch {
                 context.igenyKontenerKiszedesCikkKiszedes.setProgressBarOn()
             }
             connection = DriverManager.getConnection(MainActivity.url)
             val statement = connection.prepareStatement(res.getString(R.string.isPolc))
             statement.setString(1, code)
             val resultSet = statement.executeQuery()
             if (!resultSet.next()) {
                 CoroutineScope(Dispatchers.Main).launch {
                     context.setAlert("Nincs ilyen polc")
                     context.igenyKontenerKiszedesCikkKiszedes.setProgressBarOff()
                 }
             } else {
                 CoroutineScope(Dispatchers.Main).launch {
                     context.igenyKontenerKiszedesCikkKiszedes.setBin(code)
                     context.igenyKontenerKiszedesCikkKiszedes.setProgressBarOff()
                 }
             }
         } catch (e: Exception) {
             CoroutineScope(Dispatchers.Main).launch {
                 context.setAlert("Probléma $e")
                 context.igenyKontenerKiszedesCikkKiszedes.setProgressBarOff()
             }
         }
     }

 }