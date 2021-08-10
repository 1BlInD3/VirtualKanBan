package com.fusetech.virtualkanban.retrofit

import android.util.Log
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import com.fusetech.virtualkanban.fragments.IgenyKontenerKiszedesCikkKiszedes.Companion.isSent
import com.fusetech.virtualkanban.fragments.PolcraHelyezesFragment.Companion.isSentTranzit
import com.fusetech.virtualkanban.activities.SplashScreen.Companion.mainUrl
import com.fusetech.virtualkanban.activities.SplashScreen.Companion.backupURL
import com.fusetech.virtualkanban.activities.SplashScreen.Companion.endPoint
import com.fusetech.virtualkanban.activities.SplashScreen.Companion.logPath
import com.fusetech.virtualkanban.activities.SplashScreen.Companion.timeOut
import com.fusetech.virtualkanban.activities.SplashScreen.Companion.szallito
import kotlin.Exception

private const val TAG = "RetrofitFunctions"

class RetrofitFunctions(val trigger: Trigger) {

    interface Trigger{
        fun triggerError()
    }

    fun retrofitGet(file: File, path: String) {
        val response = SendAPI().getTest().execute()
        val res: String = response.body()!!.message.trim()
        if (res == "OK") {
            Log.d("IOTHREAD", "onResponse: ${Thread.currentThread().name + res}")
            uploadXml(file, path)
        }
    }

    private fun uploadXml(file: File, path: String) {
        val body = UploadRequestBody(file, "file")
        val xmlResponse = SendAPI().uploadXml(
            RequestBody.create(MediaType.parse("multipart/form-data"), path),
            MultipartBody.Part.createFormData("file", file.name, body),
            RequestBody.create(MediaType.parse("multipart/form-data"), "xml a kutyurol")
        ).execute()
        val xmlRes = xmlResponse.body()!!.message.trim()
        Log.d("IOTHREAD", "onResponse: ${Thread.currentThread().name + xmlRes}")
        if (xmlRes == "success") {
            isSentTranzit = true
            isSent = true
            if (file.exists()) {
                file.delete()
                Log.d("MainActivity", "onResponse: delete successful")
            }
        }
    }

    fun getConfigDetails() {
        try {
            val response = SendAPI().loadConfig().execute()
            mainUrl = response.body()!!.mainServer.trim()
            Log.d(TAG, "getConfigDetails: $mainUrl")
            backupURL = response.body()!!.backupServer
            Log.d(TAG, "getConfigDetails: $backupURL")
            endPoint = response.body()!!.endPoint
            Log.d(TAG, "getConfigDetails: $endPoint")
            logPath = response.body()!!.logPath.trim()
            Log.d(TAG, "getConfigDetails: $logPath")
            timeOut = response.body()!!.timeOut.toLong()
            Log.d(TAG, "getConfigDetails: $timeOut")
            /*szallitoJarmu = response.body()!!.szallitoJarmu
            Log.d(TAG, "getConfigDetails: $szallitoJarmu")
            ellenorzoKod = response.body()!!.ellenorzoKod
            Log.d(TAG, "getConfigDetails: $ellenorzoKod")*/
            szallito = response.body()!!.szallito
            Log.d(TAG, "getConfigDetails: $szallito")
        } catch (e: Exception) {
            Log.d(TAG, "getConfigDetails: CATCH ÁG")
            Log.d("IOTHREAD", "onResponse: ${Thread.currentThread().name + "getConfig"}")
            trigger.triggerError()
        }
    }
}