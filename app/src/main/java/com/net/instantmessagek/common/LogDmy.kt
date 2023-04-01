package com.net.instantmessagek.common

import android.util.Log


class LogDmy {
    private val flag = true
    fun d(tag:String,title: String,message:String){
       if (flag) Log.d(tag,title +" : "+ message)
    }
    fun d(tag:String,message:String){
        if (flag) Log.d(tag,message)
    }
}