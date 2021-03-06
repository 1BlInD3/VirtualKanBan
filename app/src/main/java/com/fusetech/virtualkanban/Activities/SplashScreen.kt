package com.fusetech.virtualkanban.activities

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
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


class SplashScreen : AppCompatActivity(), RetrofitFunctions.Trigger {

    var further = true
    private lateinit var progress: ProgressBar

    companion object {
        var mainUrl = "http://10.0.2.149:8030/"
        var backupURL = "http://10.0.1.199:8030/"
        var endPoint = """"""
        var logPath = ""
        var timeOut = 1L
        //var szallitoJarmu: ArrayList<String> = ArrayList()
       // var ellenorzoKod: ArrayList<String> = ArrayList()
        var szallito: HashMap<String,String> = HashMap()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        supportActionBar?.hide()
        val retro = RetrofitFunctions(this)
        progress = progressBar3
        this.window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                // or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

        try {
            val intent = Intent(this, MainActivity::class.java)
            CoroutineScope(IO).launch {
                retro.getConfigDetails()
                Log.d("IOTHREAD", "onResponse: ${Thread.currentThread().name + " splash"}")
                if(further) {
                    delay(1000L)
                    intent.putExtra("main", mainUrl)
                    intent.putExtra("backup", backupURL)
                    intent.putExtra("endpoint", endPoint)
                    intent.putExtra("logPath", logPath)
                    intent.putExtra("timeOut", timeOut)
                    intent.putExtra("szallitoMap", szallito)
                    //intent.putExtra("szallitoJarmu", szallitoJarmu)
                   // intent.putExtra("ellenorzokod", ellenorzoKod)
                    startActivity(intent)
                    finish()
                }
            }
        } catch (e: Exception) {
            setSplashAlert()
            /*val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("main", "mainUrl")
            intent.putExtra("backup", "backupURL")
            intent.putExtra("endpoint", "endPoint")
            intent.putExtra("logPath", "logPath")
            intent.putExtra("timeOut", 1.0)
            intent.putExtra("szallitoJarmu", "szallitoJarmu")
            intent.putExtra("ellenorzokod", "ellenorzoKod")
            startActivity(intent)
            finish()*/

        }
    }

    fun setSplashAlert() {
        val builder = AlertDialog.Builder(this@SplashScreen)
        builder.setTitle("Figyelem")
            .setMessage("A szerver nem el??rhet??!")
            .setPositiveButton("OK"){_,_ ->
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
            Log.d("IOTHREAD", "onResponse: ${Thread.currentThread().name +"tr2"}")
            progress.visibility = View.GONE
        }
    }
}