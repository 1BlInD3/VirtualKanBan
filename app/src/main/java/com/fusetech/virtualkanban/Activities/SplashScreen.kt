package com.fusetech.virtualkanban.activities

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.fusetech.virtualkanban.R
import com.fusetech.virtualkanban.retrofit.RetrofitFunctions
import kotlinx.android.synthetic.main.activity_splash_screen.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.HashMap

private const val TAG = "SplashScreen"

class SplashScreen : AppCompatActivity(), RetrofitFunctions.Trigger {

    private var further = true
    private lateinit var progress: ProgressBar
    private var myMacAddress = ""
    var fusetech = ""
    companion object {
        var mainUrl = "http://10.0.2.149:8030/"
        //var mainUrl = "http://10.0.1.69:8030/"
        var backupURL = "http://10.0.1.199:8030/"
        var endPoint = """"""
        var logPath = ""
        var timeOut = 1L
        var macAddress: HashMap<String, List<String>> = HashMap()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        supportActionBar?.hide()
        val retro = RetrofitFunctions()
        progress = progressBar3
        try {
            val intent = Intent(this, MainActivity::class.java)
            CoroutineScope(IO).launch {
                retro.getConfigDetails()
                Log.d("IOTHREAD", "onResponse: ${Thread.currentThread().name + " splash"}")
                if (further) {
                 myMacAddress = getAccessPointMac()
                    if(myMacAddress != "02:00:00:00:00:00" && myMacAddress != "00:00:00:00:00:00"){
                        Log.d(TAG, "onCreate: Success $myMacAddress")
                        Log.d(TAG, "onCreate: ${getKey(macAddress,myMacAddress)}")
                        containsList()
                        delay(1000L)
                        intent.putExtra("main", mainUrl)
                        intent.putExtra("backup", backupURL)
                        intent.putExtra("endpoint", endPoint)
                        intent.putExtra("logPath", logPath)
                        intent.putExtra("timeOut", timeOut)
                        intent.putExtra("szallitoMap", macAddress)
                        intent.putExtra("fusetech", fusetech)
                        startActivity(intent)
                        finish()
                    }
                    else{
                        fusetech = "666"
                    }
                }
            }
        } catch (e: Exception) {
            setSplashAlert()
        }
    }

    private fun setSplashAlert() {
        val builder = AlertDialog.Builder(this@SplashScreen)
        builder.setTitle("Figyelem")
            .setMessage("A szerver nem elérhető!")
            .setPositiveButton("OK") { _, _ ->
                finishAndRemoveTask()
            }
        builder.create()
        builder.show().getButton(DialogInterface.BUTTON_POSITIVE).requestFocus()
    }

    override fun triggerError() {
        Log.d("IOTHREAD", "onResponse: ${Thread.currentThread().name + " triggererror"}")
        further = false
        CoroutineScope(Main).launch {
            setSplashAlert()
            //delay(5000L)
            Log.d("IOTHREAD", "onResponse: ${Thread.currentThread().name + "tr2"}")
            progress.visibility = View.GONE
        }
    }

    private fun getAccessPointMac(): String {
        val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        val wifiInfo: WifiInfo = wifiManager.connectionInfo
        Log.d(TAG, "getAccessPointMac: $wifiInfo.bssid")
        //val ip: String = Formatter.formatIpAddress(wifiManager.connectionInfo.ipAddress)
        return wifiInfo.bssid
    }

    private fun <K, V> getKey(map: Map<K, V>, value: V): K? {
        for ((key, value1) in map) {
            if (value1 == value) {
                return key
            }
        }
        return null
    }
    private fun containsList(){
        for(i in 0 until macAddress.size){
            val a = (i+1).toString()
            val list = macAddress[a]
            for(j in 0 until list?.size!!){
                if(myMacAddress == list[j]){
                    Log.d(TAG, "containsList: Megegyezik ${list[j]} + $a")
                    fusetech = a
                    break
                }
            }
        }
        Log.d(TAG, "containsList: $fusetech")
    }
}