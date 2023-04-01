package com.net.instantmessagek.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.net.instantmessagek.common.Com
import com.net.instantmessagek.common.LogDmy
import com.net.instantmessagek.connection.Connection
import com.net.instantmessagek.pojo.User
import kotlinx.coroutines.runBlocking

class FriendViewModel:ViewModel() {
    val friends: MutableLiveData<List<User>> by lazy { MutableLiveData() }

    fun getFriends(userName: String) {
        val j = JsonObject()
        j.addProperty(Com.ACTION, Com.QUERY)
        j.addProperty(Com.DATA, Com.ALL)
        j.addProperty(Com.NAME,userName)
        j.addProperty(Com.TYPE, Com.USER)
        runBlocking{
            val backString = Connection().httpPost(Com.servletURL + "UserServlet",j.toString())
            val gsonDate = GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create()
            val type = object : TypeToken<List<User>>() {}.type
            friends?.value  = gsonDate.fromJson<List<User>>(backString, type)
        }
    }
}