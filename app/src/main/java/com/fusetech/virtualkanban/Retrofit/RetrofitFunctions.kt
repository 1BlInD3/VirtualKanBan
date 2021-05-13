package com.fusetech.virtualkanban.Retrofit

import android.util.Log
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import com.fusetech.virtualkanban.Fragments.IgenyKontenerKiszedesCikkKiszedes.Companion.isSent

class RetrofitFunctions(val retro: RetrofitMessage) {

    interface RetrofitMessage {
        fun retrofitAlert(message: String)
    }

    fun retrofitGet(file: File) {
      //  CoroutineScope(IO).launch {
            val response = SendAPI().getTest().execute()
            val res: String = response.body()!!.message.trim()
            if(res == "OK"){
                Log.d("IOTHREAD", "onResponse: ${Thread.currentThread().name + res}")
                uploadXml(file)
            }
            /*.enqueue(object : Callback<UploadResponse> {
                override fun onFailure(call: Call<UploadResponse>, t: Throwable) {
                    retro.retrofitAlert("$t")
                }

                override fun onResponse(
                    call: Call<UploadResponse>,
                    response: Response<UploadResponse>
                ) {
                    errorCode = response.body()?.message.toString()
                    if (errorCode == "OK") {
                        uploadXml(file)
                        Log.d("IOTHREAD", "onResponse: ${Thread.currentThread().name}")
                    } else {
                        retro.retrofitAlert("Nincs endpoint")
                    }
                }
            })*/
        //}
    }

    fun uploadXml(file: File) {
        val body = UploadRequestBody(file, "file")
        val xmlResponse = SendAPI().uploadXml(MultipartBody.Part.createFormData("file", file.name, body),RequestBody.create(MediaType.parse("multipart/form-data"), "xml a kutyurol")).execute()
        val xmlRes = xmlResponse.body()!!.message.trim()
        Log.d("IOTHREAD", "onResponse: ${Thread.currentThread().name + xmlRes}")
        if(xmlRes == "success"){
            isSent = true
            if (file.exists()) {
                file.delete()
                Log.d("MainActivity", "onResponse: delete successful")
            }
        }
    /*.enqueue(object : Callback<UploadResponse> {
            override fun onFailure(call: Call<UploadResponse>, t: Throwable) {
                Log.d("MainActivity", "onFailure: $t")
            }
            override fun onResponse(
                call: Call<UploadResponse>,
                response: Response<UploadResponse>
            ) {
                Log.d("MainActivity", "onResponse: ${response.body()?.message.toString()}")
                if (response.body()?.message.toString() == "success") {
                    if (file.exists()) {
                        file.delete()
                        Log.d("MainActivity", "onResponse: delete successful")
                    }
                }
            }
        })*/
    }
}