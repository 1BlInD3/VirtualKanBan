package com.fusetech.virtualkanban.Retrofit

import android.util.Log
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import com.fusetech.virtualkanban.Fragments.IgenyKontenerKiszedesCikkKiszedes.Companion.isSent

class RetrofitFunctions{

    fun retrofitGet(file: File) {
        val response = SendAPI().getTest().execute()
        val res: String = response.body()!!.message.trim()
        if (res == "OK") {
            Log.d("IOTHREAD", "onResponse: ${Thread.currentThread().name + res}")
            uploadXml(file)
        }
    }

    private fun uploadXml(file: File) {
        val body = UploadRequestBody(file, "file")
        val xmlResponse = SendAPI().uploadXml(
            MultipartBody.Part.createFormData("file", file.name, body),
            RequestBody.create(MediaType.parse("multipart/form-data"), "xml a kutyurol")
        ).execute()
        val xmlRes = xmlResponse.body()!!.message.trim()
        Log.d("IOTHREAD", "onResponse: ${Thread.currentThread().name + xmlRes}")
        if (xmlRes == "success") {
            isSent = true
            if (file.exists()) {
                file.delete()
                Log.d("MainActivity", "onResponse: delete successful")
            }
        }
    }
}