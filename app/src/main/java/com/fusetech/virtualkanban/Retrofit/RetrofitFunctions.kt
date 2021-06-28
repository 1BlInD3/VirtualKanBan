package com.fusetech.virtualkanban.Retrofit

import android.util.Log
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import com.fusetech.virtualkanban.Fragments.IgenyKontenerKiszedesCikkKiszedes.Companion.isSent
import com.fusetech.virtualkanban.Fragments.PolcraHelyezesFragment.Companion.isSentTranzit


class RetrofitFunctions{

    fun retrofitGet(file: File,path: String) {
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
            RequestBody.create(MediaType.parse("multipart/form-data"),path),
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
}