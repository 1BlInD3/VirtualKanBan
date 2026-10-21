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
import com.fusetech.virtualkanban.databinding.ActivitySplashScreenBinding
import com.fusetech.virtualkanban.retrofit.RetrofitFunctions
import dagger.hilt.android.AndroidEntryPoint
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

@AndroidEntryPoint
class SplashScreen : AppCompatActivity(), RetrofitFunctions.Trigger {

    private lateinit var binding: ActivitySplashScreenBinding
    private lateinit var progress: ProgressBar
    var fusetech = ""

    companion object {
        //var mainUrl = "http://10.0.2.149:8030/"
        //var mainUrl = "http://192.168.1.9:8030/"
        var mainUrl = ""
        var backupURL = "http://10.0.1.69:8030/"
        var endPoint = """"""
        var logPath = ""
        var timeOut = 1L
        var ipAddress: HashMap<String, String> = HashMap()
        var itAccess: ArrayList<String> = ArrayList()
        var url =
            ""
        var connectionString =
            ""
        var further = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        further = true
        val retro = RetrofitFunctions()
        progress = binding.progressBar3
        try {
            val intent = Intent(this, MainActivity::class.java)
            CoroutineScope(IO).launch {
                try {
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
                        intent.putExtra("url", url)
                        intent.putExtra("connectionString", connectionString)
                        intent.putExtra("szallitoMap", ipAddress)
                        intent.putExtra("fusetech", fusetech)
                        intent.putExtra("it", itAccess)
                        startActivity(intent)
                        finish()
                    } else {
                        CoroutineScope(Main).launch {
                            setSplashAlert()
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error during splash init: ", e)
                    CoroutineScope(Main).launch {
                        setSplashAlert()
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
            Log.d("IOTHREAD", "onResponse: ${Thread.currentThread().name + "tr2"}")
            progress.visibility = View.GONE
        }
    }

    private fun getLocalIpAddress(): String? {
        try {
            val en: Enumeration<NetworkInterface> = NetworkInterface.getNetworkInterfaces() ?: return null
            while (en.hasMoreElements()) {
                val intf: NetworkInterface = en.nextElement()
                val enumIpAddr: Enumeration<InetAddress> = intf.inetAddresses ?: continue
                while (enumIpAddr.hasMoreElements()) {
                    val inetAddress: InetAddress = enumIpAddr.nextElement()
                    if (!inetAddress.isLoopbackAddress && inetAddress is Inet4Address) {
                        return inetAddress.hostAddress
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
        if (!myIpAddress.isNullOrEmpty() && myIpAddress.length >= 6) {
            val index = myIpAddress.substring(5, 6)
            /*for (i in 0 until ipAddress.size) {
                val a = (i + 1).toString()
                val ip = ipAddress[a]
                if (ip != null && ip.length >= 6 && ip.substring(5, 6) == index) {
                    fusetech = a
                }
            }*/
            if (index.equals("6")) {
                fusetech = "1"
            } else if (index.equals("8")) {
                fusetech = "2"
            } else {
                fusetech = ""
            }
        }
        Log.d(TAG, "getLocation: $fusetech")
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, binding.splashContainer).let { controller ->
            controller.hide(WindowInsetsCompat.Type.navigationBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}
