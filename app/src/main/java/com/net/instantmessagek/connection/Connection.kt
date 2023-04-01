package com.net.instantmessagek.connection

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.YuvImage
import android.util.Log
import android.widget.ImageView
import com.net.instantmessagek.common.LogDmy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class Connection {
    private val TAG = "TAG ${javaClass.simpleName}"

    suspend fun httpImage(url: String, outString: String, ): ByteArray? {
        var byteArray: ByteArray? = null
        withContext(Dispatchers.IO) {
            (URL(url).openConnection() as HttpURLConnection)?.apply {
                LogDmy().d(TAG,"url",url)
                LogDmy().d(TAG,"httpImage outString",outString)
                doInput = true
                doOutput = true
                setChunkedStreamingMode(0)
                requestMethod = "POST"
                useCaches = false
                setRequestProperty("charset","UTF-8")
                outputStream.bufferedWriter().use {
                    it.write(outString)
                }
                if(responseCode == 200) {
                    byteArray = inputStream.readBytes()
                }
            }
        }
        if(byteArray == null) {
            LogDmy().d(TAG,"null")
        }
        return byteArray
    }
    suspend fun httpPost(url:String, outString: String):String{
        var inString = ""
        var stringBuffer = StringBuffer()
        withContext(Dispatchers.IO) {
            ( URL(url).openConnection() as? HttpURLConnection )?.apply {
                LogDmy().d(TAG,"url",url)
                LogDmy().d(TAG,"httpPost outString",outString)
                doInput = true
                doOutput = true
                setChunkedStreamingMode(0)
                requestMethod = "POST"
                useCaches = false
                setRequestProperty("charset","UTF-8")
                outputStream.bufferedWriter().use {
                    it.write(outString)
                }
                if(responseCode == 200) {
                    // 另一寫法
//                    inString = inputStream.bufferedReader().readLine()
//                    inputStream.bufferedReader().useLines { lines ->
//                        inString = lines.fold("") { text,line -> "$text$line" }
//                        LogDmy().d(TAG,"inString",inString)
//                    }
                    inputStream.bufferedReader().useLines{
                        it.iterator().forEach { line ->
                            stringBuffer.append(line)
                        }
                    }
                    inString = stringBuffer.toString()
                }
            }
        }
        LogDmy().d(TAG,"inString return",inString)

        return inString
    }
}