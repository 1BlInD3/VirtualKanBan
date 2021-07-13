package com.fusetech.virtualkanban.utils

import android.annotation.SuppressLint
import android.util.Log
import java.io.File
import java.io.FileOutputStream

private const val TAG = "SaveFile"

class SaveFile {
    @SuppressLint("SimpleDateFormat")
    fun saveFile(file: File,data : String){
        try {
            val stream = FileOutputStream(file)
            stream.write(data.toByteArray())
            stream.close()
        }catch (e: Exception){
            Log.d(TAG, "saveXml: ")
        }
    }
    fun prepareFile(path: String, name: String) : File{
        return prepareFile(path,name)
    }

}