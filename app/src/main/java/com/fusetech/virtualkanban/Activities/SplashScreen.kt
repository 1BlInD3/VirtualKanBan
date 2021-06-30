package com.fusetech.virtualkanban.Activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.fusetech.virtualkanban.R
import com.fusetech.virtualkanban.Retrofit.RetrofitFunctions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlin.concurrent.thread


class SplashScreen : AppCompatActivity() {
    companion object{
        var mainUrl = "http://10.0.2.149:8030/"
        var backupURL = "http://10.0.1.199:8030/"
        var endPoint = """"""
        var logPath = ""
        var timeOut = 0L
        var szallitoJarmu: ArrayList<String> = ArrayList()
        var ellenorzoKod: ArrayList<String> = ArrayList()
    }
   // private var handler : Handler = Handler()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        supportActionBar?.hide()
        val retro = RetrofitFunctions()
        val intent = Intent(this,MainActivity::class.java)
        CoroutineScope(IO).launch {
            try {
                retro.getConfigDetails()
                intent.putExtra("main", mainUrl)
                intent.putExtra("backup", backupURL)
                intent.putExtra("endpoint", endPoint)
                intent.putExtra("logPath", logPath)
                intent.putExtra("timeOut", timeOut)
                intent.putExtra("szallitoJarmu", szallitoJarmu)
                intent.putExtra("ellenorzokod", ellenorzoKod)
                startActivity(intent)
                finish()
            }catch (e: Exception){

            }
        }

        /*handler.postDelayed(Runnable {
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
            finish()
        },3000)*/
    }
}