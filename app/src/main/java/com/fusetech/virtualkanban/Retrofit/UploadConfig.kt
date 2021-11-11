package com.fusetech.virtualkanban.retrofit

class UploadConfig(
    val mainServer: String,
    val backupServer: String,
    val endPoint: String,
    val logPath: String,
    val timeOut: Int,
    val ipAddress: HashMap<String,String>
) {

}