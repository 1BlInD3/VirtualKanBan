package com.fusetech.virtualkanban.activities

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.fusetech.virtualkanban.R
import com.fusetech.virtualkanban.retrofit.RetrofitFunctions
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_splash_screen.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

private const val TAG = "SplashScreen"

class SplashScreen : AppCompatActivity(), RetrofitFunctions.Trigger {

    private var further = true
    private lateinit var progress: ProgressBar
    var fusetech = ""

    companion object {
       // var mainUrl = "http://10.0.2.149:8030/"
        var mainUrl = "http://10.0.1.69:8030/"
        var backupURL = "http://10.0.1.199:8030/"
        var endPoint = """"""
        var logPath = ""
        var timeOut = 1L
        var ipAddress: HashMap<String, String> = HashMap()
        var itAccess: ArrayList<String> = ArrayList()
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
                    getLocation()
                    delay(1000L)
                    intent.putExtra("main", mainUrl)
                    intent.putExtra("backup", backupURL)
                    intent.putExtra("endpoint", endPoint)
                    intent.putExtra("logPath", logPath)
                    intent.putExtra("timeOut", timeOut)
                    intent.putExtra("szallitoMap", ipAddress)
                    intent.putExtra("fusetech", fusetech)
                    intent.putExtra("it", itAccess)
                    //intent.put("it", itAccess)
                    startActivity(intent)
                    finish()
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

    private fun getLocalIpAddress(): String? {
        try {
            val en: Enumeration<NetworkInterface> = NetworkInterface.getNetworkInterfaces()
            while (en.hasMoreElements()) {
                val intf: NetworkInterface = en.nextElement()
                val enumIpAddr: Enumeration<InetAddress> = intf.inetAddresses
                while (enumIpAddr.hasMoreElements()) {
                    val inetAddress: InetAddress = enumIpAddr.nextElement()
                    if (!inetAddress.isLoopbackAddress && inetAddress is Inet4Address) {
                        return inetAddress.getHostAddress()
                    }
                }
            }
        } catch (ex: SocketException) {
            ex.printStackTrace()
        }
        return null
    }

    private fun getLocation() {
        val myIpAddress = getLocalIpAddress()
        if (myIpAddress!!.isNotEmpty()) {
            val index = myIpAddress.substring(3, 4)
            for (i in 0 until ipAddress.size) {
                val a = (i + 1).toString()
                if (ipAddress[a]!!.substring(3, 4) == index) {
                    fusetech = a
                }
            }
        }
        Log.d(TAG, "getLocation: $fusetech")
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, splashContainer).let { controller ->
            //controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.hide(WindowInsetsCompat.Type.navigationBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}