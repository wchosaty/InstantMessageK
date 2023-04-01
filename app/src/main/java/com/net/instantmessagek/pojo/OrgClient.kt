package com.net.instantmessagek.pojo

import android.app.Activity
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.net.instantmessagek.MessageAdapter
import com.net.instantmessagek.common.Com
import com.net.instantmessagek.common.LogDmy
import com.net.instantmessagek.databinding.ActivityMessageBinding
import kotlinx.coroutines.runBlocking
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.lang.Exception
import java.net.URI
import java.nio.ByteBuffer

class OrgClient(uri: URI?,val activity: Activity,val binding: ActivityMessageBinding): WebSocketClient(uri) {
    private val TAG = "TAG ${javaClass.simpleName}"


    override fun onOpen(handshakedata: ServerHandshake?) {
        LogDmy().d(TAG,"onOpen","Open")
    }

    override fun onMessage(message: String?) {
        LogDmy().d(TAG,"onMessage","String")
        if(!message.isNullOrEmpty()) {
            var j : JsonObject = Gson().fromJson(message,JsonObject::class.java)

            if(j.get(Com.ACTION).asString == Com.MESSAGE ) {
                var m = Gson().fromJson(j.get(Com.MESSAGE).asString, MessageHistory::class.java)
                activity.runOnUiThread {
                    with(binding){
                        var a: MessageAdapter = binding.recyclerAC.adapter as MessageAdapter
                        if(a != null){
                            a.addItem(m)
                            if(a.itemCount > 0) {
                                recyclerAC.scrollToPosition(a.itemCount - 1)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onMessage(bytes: ByteBuffer?) {
        var image : ByteArray? = bytes?.array()
        runBlocking {
            activity.runOnUiThread {
                // 測試 Kotlin WebSocket傳圖
//                binding.ivTest.setImageBitmap(BitmapFactory.decodeByteArray(image,0, image?.size ?: 0))
                var m = MessageHistory(0L,0L,"","")
                m.type = Com.IMAGE
                m.b = image
                val a = binding.recyclerAC.adapter as MessageAdapter
                if(a != null ){
                    a.addItem(m)
                    if (a.itemCount>0){
                        binding.recyclerAC.scrollToPosition(a.itemCount-1)
                    }
                }
            }
        }
    }

    override fun onClosing(code: Int, reason: String?, remote: Boolean) {
        super.onClosing(code, reason, remote)
        LogDmy().d(TAG,"onClosing"," code :"+code+"/reason :"+reason)
    }
    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        LogDmy().d(TAG,"onClose"," code :"+code+"/reason :"+reason)
    }

    override fun onError(ex: Exception?) {
        LogDmy().d(TAG,"onError", ex?.message.toString())
    }
}