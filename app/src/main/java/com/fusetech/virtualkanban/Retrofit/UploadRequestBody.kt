package com.fusetech.virtualkanban.Retrofit

import okhttp3.MediaType
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.File
import java.io.FileInputStream

class UploadRequestBody(
    private val file: File,
    private val contentType: String
): RequestBody() {
    override fun contentType() = MediaType.parse("$contentType/*")

    override fun contentLength() = file.length()

    override fun writeTo(sink: BufferedSink) {
        val buffer = ByteArray(DEFALT_BUFFER_SIZE)
        val fileInputStream = FileInputStream(file)
        fileInputStream.use {inputStream ->
            var read: Int
            while(inputStream.read(buffer).also{ read = it}!= -1){
                sink.write(buffer,0,read)
            }
        }
    }

    companion object{
        private const val DEFALT_BUFFER_SIZE = 1048
    }
}