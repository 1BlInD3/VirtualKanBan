package com.fusetech.virtualkanban.Utils

import android.annotation.SuppressLint
import android.util.Log
import java.io.File
import java.io.FileOutputStream

private const val TAG = "SaveFile"

class SaveFile {
    @SuppressLint("SimpleDateFormat")
    fun saveXml(file: File,data : String){
        try {
            val stream = FileOutputStream(file)
            stream.write(data.toByteArray())
            stream.close()
        }catch (e: Exception){
            Log.d(TAG, "saveXml: ")
        }
    }
}