package com.fusetech.virtualkanban.viewmodels

import android.annotation.SuppressLint
import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.fusetech.virtualkanban.activities.MainActivity
import com.fusetech.virtualkanban.dataItems.PolcItems
import com.fusetech.virtualkanban.interfaces.MozgasListener
import com.fusetech.virtualkanban.retrofit.RetrofitFunctions
import com.fusetech.virtualkanban.utils.SaveFile
import com.fusetech.virtualkanban.utils.SqlLogic
import com.fusetech.virtualkanban.utils.XML
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

@HiltViewModel
class RaktarMozgasViewModel
@Inject
constructor(
    private val sql: SqlLogic,
    private val save: SaveFile,
    private val xml: XML,
    private val retro: RetrofitFunctions
) : ViewModel() {
    var mozgasListener: MozgasListener? = null
    var celRaktar = ""
    var kiinduloRakhely = ""
    var yesClicked = false
    private var adatok = MutableLiveData<ArrayList<PolcItems>>()
    val valasztasLista: ArrayList<PolcItems> = ArrayList()
    var cikkTomb = "03011001"
    var mennyisegTomb = "500"
    var unit = ""

    fun getItems(): LiveData<ArrayList<PolcItems>> {
        return adatok
    }

    fun loadItems(code: String) {
        adatok = sql.polcResultQuery(code)
    }

    @SuppressLint("SimpleDateFormat")
    fun sendToScala(
        file: File,
        cikk: String,
        mennyiseg: Double,
        kiinduloPolc: String,
        celPolc: String,
        rbol: String,
        rba: String
    ) {
        val currentDate = SimpleDateFormat("yyyy-MM-dd").format(Date())
        save.saveFile(
            file,
            xml.createXml(
                currentDate,
                mennyiseg,
                cikk,
                rbol,
                kiinduloPolc,
                rba,
                celPolc,
                MainActivity.dolgKod
            ) // ez lesz másik xml ha kész lesz
        )
        Log.d("IOTHREAD", "sendXmlData: ${Thread.currentThread().name}")
        try {
            retro.retrofitGet(file, MainActivity.endPoint)
            mozgasListener?.highlightText()
        } catch (e: Exception) {
            try {
                val a = MainActivity.mainUrl
                MainActivity.mainUrl = MainActivity.backupURL
                retro.retrofitGet(file, MainActivity.endPoint)
                MainActivity.mainUrl = a
                mozgasListener?.setPolcText("")
            } catch (e: Exception) {
                CoroutineScope(Main).launch {
                    //writeLog(e.stackTraceToString(), "arg1 $cikkszam arg2 $polchely arg3 $mennyisege arg4 $rbol arg5 $rba arg6 $polchelyre")
                    if (file.exists()) {
                        file.delete()
                    }
                }
            }
        }
    }

    fun checkPolc(code: String) {
        CoroutineScope(IO).launch {
            CoroutineScope(Main).launch {
                mozgasListener?.setProgressOn()
            }
            if (sql.isPolc02(code)) {
                mozgasListener?.setPolcText(code)
                if (yesClicked) {
                    mozgasListener?.setSend()
                    yesClicked = false
                }else{
                    mozgasListener?.sendOneByOne()
                }
                CoroutineScope(Main).launch {
                    mozgasListener?.setProgressOff()
                }
            }else{
                CoroutineScope(Main).launch {
                    mozgasListener?.message("Nem raktári polcot olvastál le")
                    mozgasListener?.setProgressOff()
                }
            }
        }
    }

    fun arrayAddOrDelete(position: Int){
        when {
            valasztasLista.size == 0 -> {
                valasztasLista.add(
                    PolcItems(
                        getItems().value!![position].mMennyiseg,
                        getItems().value!![position].mEgyseg,
                        "",
                        "",
                        "",
                        "Szabad",
                        getItems().value!![position].mCikk.trim()
                    )
                )
            }
            contains(getItems().value!![position].mCikk) -> {
                Log.d("MOZGAS", "tartalmaz")
                for(i in 0 until valasztasLista.size){
                    try{
                        if(valasztasLista[i].mCikk == getItems().value!![position].mCikk){
                            valasztasLista.remove(valasztasLista[i])
                            Log.d("MOZGAS", "onCurrentClick: Törölve")
                        }
                    }catch (e: Exception){
                        Log.d("DELETETAG", "arrayAddOrDelete: $e")
                    }
                }
            }
            else -> {
                valasztasLista.add(
                    PolcItems(
                        getItems().value!![position].mMennyiseg,
                        getItems().value!![position].mEgyseg,
                        "",
                        "",
                        "",
                        "Szabad",
                        getItems().value!![position].mCikk.trim()
                    )
                )
            }
        }
        Log.d("MOZGAS", "onCurrentClick sima: $valasztasLista")
    }
    private fun contains(cikk: String): Boolean{
        for(i in 0 until valasztasLista.size){
            if(valasztasLista[i].mCikk == cikk){
                return true
            }
        }
        return false
    }
    fun befejezesClick(view: View){
        mozgasListener?.whenButtonIsClicked()
    }
    fun clearPolc(view: View){
        mozgasListener?.setPolcText("")
    }
}
