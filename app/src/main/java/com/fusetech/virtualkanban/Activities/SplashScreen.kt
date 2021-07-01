package com.fusetech.virtualkanban.Activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.fusetech.virtualkanban.R
import com.fusetech.virtualkanban.Retrofit.RetrofitFunctions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class SplashScreen : AppCompatActivity(), RetrofitFunctions.Trigger {

    var further = true

    companion object {
        var mainUrl = "http://10.0.2.149:8030/"
        var backupURL = "http://10.0.1.199:8030/"
        var endPoint = """"""
        var logPath = ""
        var timeOut = 0L
        var szallitoJarmu: ArrayList<String> = ArrayList()
        var ellenorzoKod: ArrayList<String> = ArrayList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        supportActionBar?.hide()
        val retro = RetrofitFunctions(this)

        try {
            val intent = Intent(this, MainActivity::class.java)
            CoroutineScope(IO).launch {
                retro.getConfigDetails()
                Log.d("IOTHREAD", "onResponse: ${Thread.currentThread().name + " splash"}")
                if(further) {
                    delay(2000L)
                    intent.putExtra("main", mainUrl)
                    intent.putExtra("backup", backupURL)
                    intent.putExtra("endpoint", endPoint)
                    intent.putExtra("logPath", logPath)
                    intent.putExtra("timeOut", timeOut)
                    intent.putExtra("szallitoJarmu", szallitoJarmu)
                    intent.putExtra("ellenorzokod", ellenorzoKod)
                    startActivity(intent)
                    finish()
                }
            }
        } catch (e: Exception) {
            setSplashAlert()
        }
    }

    fun setSplashAlert() {
        val builder = AlertDialog.Builder(this@SplashScreen)
        builder.setTitle("Figyelem")
            .setMessage("Nem tudott a szerverhez csatlakozni!")
            .setPositiveButton("OK"){dialog, which ->

            }
        builder.create()
        builder.show()
    }

    override fun triggerError() {
        Log.d("IOTHREAD", "onResponse: ${Thread.currentThread().name + " triggererror"}")
        further = false
        CoroutineScope(Main).launch {
            setSplashAlert()
            delay(5000L)
            Log.d("IOTHREAD", "onResponse: ${Thread.currentThread().name + " main"}")
        }
    }
}